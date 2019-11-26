/*
 * Copyright © 2018 - present | ThreadingTools by Javinator9889
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 *
 * Created by Javinator9889 on 15/11/2018 - ThreadingTools.
 */

package com.github.javinator9889.threading.threads.notifyingthread;

import com.github.javinator9889.utils.ArgumentParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * {@code NotifyingThread} provides an specialised class on threading that adds more options to the
 * current available threading models:
 * <ul>
 * <li>
 * <b>Notifying</b>: sometimes we want to get notified when a {@link Thread} completes
 * its execution, but the most we can do is just {@linkplain Thread#join() wait} until its
 * completion for going to the next step in our execution. {@code NotifyingThread} provides a fast,
 * powerful class for getting notified when threads we want finish, just by subscribing to the
 * <b>listener classes</b> implementing the {@link OnThreadCompletedListener} interface.
 * </li>
 * <li>
 * <b>Fast development</b>: it is very common that we declare the following:
 * <pre>
 *             {@code
 *             // Class: ExampleClass
 *             public void functionWithHeavyLoad(final ArrayList<Double> params) {
 *                 final Integer value = new Random().nextInt(1337);
 *                 new Thread(new Runnable() {
 *                      public void run() {
 *                          double veryBigValue = params.get(0);
 *                          ExampleClass.this.mField = ExampleClass.this.doVeryBigMathCalc(value,
 *                          veryBigValue);
 *                      }
 *                 }).start();
 *             }
 *             }
 * </pre>
 * With {@code NotifyingThread} is as simple as declaring the method we are going to use, wrap
 * params using {@linkplain ArgumentParser} and returning the values if necessary. So, the latest
 * code will just be:
 * <pre>
 *             {@code
 *             // Class: ExampleClass
 *             public double doVeryBigMathCalc(ArgumentParser args) {
 *                 int firstValue = args.getInt("firstParam");
 *                 double secondValue = args.getDouble("secondParam");
 *                 // do calculations and store its result at: double result;
 *                 return result;
 *             }
 *
 *             public void functionWithHeavyLoad(final ArrayList<Double> params) {
 *                  final Integer value = new Random().nextInt(1337);
 *                  // Declare NotifyingThread, ArgumentParser and AtomicReference
 *                  NotifyingThread thread = new NotifyingThread();
 *                  ArgumentParser parser = new ArgumentParser(2);
 *                  AtomicReference<Double> result = new AtomicReference<>();
 *                  // Set params
 *                  parser.putParam("firstParam", value);
 *                  parser.putParam("secondParam", params.get(0));
 *                  // Set the executable by using lambdas
 *                  thread.setExecutable(this::doVeryBigMathCalc, parser, result);
 *                  // Start the thread
 *                  thread.start();
 *                  // Wait for the thread completion - we can define a listener for getting
 *                  notified.
 *                  System.out.println(result.get());
 *             }
 *             }
 *         </pre>
 * </li>
 * <li>
 * <b>Adaptive</b>: {@code NotifyingThread} provides adaptive methods for setting up the
 * executables, so you can use {@linkplain Consumer}, {@linkplain Function}, {@linkplain Runnable}
 * and {@linkplain Supplier}.
 * </li>
 * </ul>
 * By default, this class uses its own {@link java.lang.Thread.UncaughtExceptionHandler} for
 * notifying <b>subscribed classes</b> that the thread has finished with an exception. If you want
 * to use yours, just setup the {@code UncaughtExceptionHandler} with {@link
 * #setUncaughtExceptionHandler(UncaughtExceptionHandler)}.
 * <p>
 * In addition, this class supports <b>async calling</b> for subscribed classes when the threads
 * finishes. Refer to {@linkplain #setShouldCallSubscribedClassesAsynchronously(boolean) async
 * execution docs} for more information.
 */
@SuppressWarnings("unchecked")
public class NotifyingThread extends Thread implements Thread.UncaughtExceptionHandler {
    /**
     * Default capacity of subscribed classes - normally, we only want one class to get notified
     * when a thread completes.
     */
    public static final int DEFAULT_CAPACITY = 1;

    /**
     * Thread prefix used when invoking "toString".
     */
    public static final String THREAD_PREFIX = "NotifyingThread-";

    /**
     * Thread count number.
     */
    private static final AtomicInteger mThreadNumber = new AtomicInteger();

    /**
     * {@code List} of subscribed classes.
     *
     * @see OnThreadCompletedListener
     */
    private ArrayList<OnThreadCompletedListener> mSubscribedClasses;

    /**
     * Whether the calling of subscribed classes must be done asynchronously or not.
     *
     * @see #callSubscribedClasses(Thread, Throwable)
     */
    private AtomicBoolean mShouldCallSubscribedClassesAsynchronously = new AtomicBoolean(false);

    /**
     * Target that will be executed when {@link #start()} is called.
     */
    private Runnable mTarget;

    /**
     * Allocates a new {@code Thread} object. This constructor has the same effect as {@linkplain
     * #NotifyingThread(ThreadGroup, Runnable, String) NotifyingThread} {@code (null, null, gname)},
     * where {@code gname} is a newly generated name. Automatically generated names are of the form
     * {@code "NotifyingThread-"+}<i>n</i>, where <i>n</i> is an integer.
     */
    public NotifyingThread() {
        this(null, null, THREAD_PREFIX + nextThreadNumber());
    }

    /**
     * Allocates a new {@code Thread} object. This constructor has the same effect as {@linkplain
     * #NotifyingThread(ThreadGroup, Runnable, String, OnThreadCompletedListener...)
     * NotifyingThread} {@code (null, null, gname)}, where {@code gname} is a newly generated name.
     * Automatically generated names are of the form {@code "NotifyingThread-"+}<i>n</i>, where
     * <i>n</i> is an integer.
     *
     * @param listeners an {@code array} of {@linkplain OnThreadCompletedListener listeners} which
     *                  are classes that implements {@linkplain OnThreadCompletedListener}, so when
     *                  a thread finish its work, those classes are called at the {@link
     *                  OnThreadCompletedListener#onThreadCompletedListener(Thread, Throwable)}
     *                  method, giving both {@link Runnable} of the just finished thread and {@link
     *                  Throwable} with any exception that occurred during execution.
     */
    public NotifyingThread(@NotNull OnThreadCompletedListener... listeners) {
        this(null, null, THREAD_PREFIX + nextThreadNumber(), listeners);
    }

    /**
     * Allocates a new {@code Thread} object. This constructor has the same effect as {@linkplain
     * #NotifyingThread(ThreadGroup, Runnable, String) NotifyingThread} {@code (null, target,
     * gname)}, where {@code gname} is a newly generated name. Automatically generated names are of
     * the form {@code "NotifyingThread-"+}<i>n</i>, where <i>n</i> is an integer.
     *
     * @param target the object whose {@code run} method is invoked when this thread is started. If
     *               {@code null}, this classes {@code run} method does nothing.
     */
    public NotifyingThread(Runnable target) {
        this(null, target, THREAD_PREFIX + nextThreadNumber());
    }

    /**
     * Allocates a new {@code Thread} object. This constructor has the same effect as {@linkplain
     * #NotifyingThread(ThreadGroup, Runnable, String, OnThreadCompletedListener...) NotifyingThread
     * } {@code (null, target, gname, listeners)}, where {@code gname} is a newly generated name.
     * Automatically generated names are of the form {@code "NotifyingThread-"+}<i>n</i>, where
     * <i>n</i> is an integer.
     *
     * @param target    the object whose {@code run} method is invoked when this thread is started.
     *                  If {@code null}, this classes {@code run} method does nothing.
     * @param listeners an {@code array} of {@linkplain OnThreadCompletedListener listeners} which
     *                  are classes that implements {@linkplain OnThreadCompletedListener}, so when
     *                  a thread finish its work, those classes are called at the {@link
     *                  OnThreadCompletedListener#onThreadCompletedListener(Thread, Throwable)}
     *                  method, giving both {@link Runnable} of the just finished thread and {@link
     *                  Throwable} with any exception that occurred during execution.
     */
    public NotifyingThread(Runnable target,
                           @NotNull OnThreadCompletedListener... listeners) {
        this(null, target, THREAD_PREFIX + nextThreadNumber(), listeners);
    }

    /**
     * Allocates a new {@code Thread} object. This constructor has the same effect as {@linkplain
     * #NotifyingThread(ThreadGroup, Runnable, String) NotifyingThread} {@code (group, target,
     * gname)}, where {@code gname} is a newly generated name. Automatically generated names are of
     * the form {@code "NotifyingThread-"+}<i>n</i>, where <i>n</i> is an integer.
     *
     * @param group  the thread group. If {@code null} and there is a security manager, the group is
     *               determined by {@linkplain SecurityManager#getThreadGroup
     *               SecurityManager.getThreadGroup()}. If there is not a security manager or {@code
     *               SecurityManager.getThreadGroup()} returns {@code null}, the group is set to the
     *               current thread's thread group.
     * @param target the object whose {@code run} method is invoked when this thread is started. If
     *               {@code null}, this thread's run method is invoked.
     *
     * @throws SecurityException if the current thread cannot create a thread in the specified
     *                           thread group
     */
    public NotifyingThread(@Nullable ThreadGroup group,
                           Runnable target) {
        this(group, target, THREAD_PREFIX + nextThreadNumber());
    }

    /**
     * Allocates a new {@code Thread} object. This constructor has the same effect as {@linkplain
     * #NotifyingThread(ThreadGroup, Runnable, String, OnThreadCompletedListener...)
     * NotifyingThread} {@code (group, target, gname, listeners)} , where {@code gname} is a newly
     * generated name. Automatically generated names are of the form {@code
     * "NotifyingThread-"+}<i>n</i>, where <i>n</i> is an integer.
     *
     * @param group     the thread group. If {@code null} and there is a security manager, the group
     *                  is determined by {@linkplain SecurityManager#getThreadGroup
     *                  SecurityManager.getThreadGroup()}. If there is not a security manager or
     *                  {@code SecurityManager.getThreadGroup()} returns {@code null}, the group is
     *                  set to the current thread's thread group.
     * @param target    the object whose {@code run} method is invoked when this thread is started.
     *                  If {@code null}, this thread's run method is invoked.
     * @param listeners an {@code array} of {@linkplain OnThreadCompletedListener listeners} which
     *                  are classes that implements {@linkplain OnThreadCompletedListener}, so when
     *                  a thread finish its work, those classes are called at the {@link
     *                  OnThreadCompletedListener#onThreadCompletedListener(Thread, Throwable)}
     *                  method, giving both {@link Runnable} of the just finished thread and {@link
     *                  Throwable} with any exception that occurred during execution.
     *
     * @throws SecurityException if the current thread cannot create a thread in the specified
     *                           thread group
     */
    public NotifyingThread(@Nullable ThreadGroup group,
                           Runnable target,
                           @NotNull OnThreadCompletedListener... listeners) {
        this(group, target, THREAD_PREFIX + nextThreadNumber(), listeners);
    }

    /**
     * Allocates a new {@code Thread} object. This constructor has the same effect as {@linkplain
     * #NotifyingThread(ThreadGroup, Runnable, String, OnThreadCompletedListener...) NotifyingThread
     * } {@code (null, null, name, listeners)}.
     *
     * @param name      the name of the new thread.
     * @param listeners an {@code array} of {@linkplain OnThreadCompletedListener listeners} which
     *                  are classes that implements {@linkplain OnThreadCompletedListener}, so when
     *                  a thread finish its work, those classes are called at the {@link
     *                  OnThreadCompletedListener#onThreadCompletedListener(Thread, Throwable)}
     *                  method, giving both {@link Runnable} of the just finished thread and {@link
     *                  Throwable} with any exception that occurred during execution.
     */
    public NotifyingThread(@NotNull String name,
                           @NotNull OnThreadCompletedListener... listeners) {
        this(null, null, name, listeners);
    }

    /**
     * Allocates a new {@code Thread} object. This constructor has the same effect as {@linkplain
     * #NotifyingThread(ThreadGroup, Runnable, String, OnThreadCompletedListener...) NotifyingThread
     * } {@code (group, null, name, listeners)}.
     *
     * @param group     the thread group. If {@code null} and there is a security manager, the group
     *                  is determined by {@linkplain SecurityManager#getThreadGroup
     *                  SecurityManager.getThreadGroup()}. If there is not a security manager or
     *                  {@code SecurityManager.getThreadGroup()} returns {@code null}, the group is
     *                  set to the current thread's thread group.
     * @param name      the name of the new thread.
     * @param listeners an {@code array} of {@linkplain OnThreadCompletedListener listeners} which
     *                  are classes that implements {@linkplain OnThreadCompletedListener}, so when
     *                  a thread finish its work, those classes are called at the {@link
     *                  OnThreadCompletedListener#onThreadCompletedListener(Thread, Throwable)}
     *                  method, giving both {@link Runnable} of the just finished thread and {@link
     *                  Throwable} with any exception that occurred during execution.
     *
     * @throws SecurityException if the current thread cannot create a thread in the specified
     *                           thread group
     */
    public NotifyingThread(@Nullable ThreadGroup group,
                           @NotNull String name,
                           @NotNull OnThreadCompletedListener... listeners) {
        this(group, null, name, listeners);
    }

    /**
     * Allocates a new {@code Thread} object. This constructor has the same effect as {@linkplain
     * #NotifyingThread(ThreadGroup, Runnable, String) NotifyingThread} {@code (null, null, name)}.
     *
     * @param name the name of the new thread
     */
    public NotifyingThread(@NotNull String name) {
        this(null, null, name);
    }

    /**
     * Allocates a new {@code Thread} object. This constructor has the same effect as {@linkplain
     * #NotifyingThread(ThreadGroup, Runnable, String, OnThreadCompletedListener...)
     * NotifyingThread} {@code (null, target, name, listeners)}.
     *
     * @param target    the object whose {@code run} method is invoked when this thread is started.
     *                  If {@code null}, this thread's run method is invoked.
     * @param name      the name of the new thread.
     * @param listeners an {@code array} of {@linkplain OnThreadCompletedListener listeners} which
     *                  are classes that implements {@linkplain OnThreadCompletedListener}, so when
     *                  a thread finish its work, those classes are called at the {@link
     *                  OnThreadCompletedListener#onThreadCompletedListener(Thread, Throwable)}
     *                  method, giving both {@link Runnable} of the just finished thread and {@link
     *                  Throwable} with any exception that occurred during execution.
     */
    public NotifyingThread(Runnable target,
                           String name,
                           @NotNull OnThreadCompletedListener... listeners) {
        this(null, target, name, listeners);
    }

    /**
     * Allocates a new {@code Thread} object. This constructor has the same effect as {@linkplain
     * #NotifyingThread(ThreadGroup, Runnable, String) NotifyingThread} {@code (group, null,
     * name)}.
     *
     * @param group the thread group. If {@code null} and there is a security manager, the group is
     *              determined by {@linkplain SecurityManager#getThreadGroup
     *              SecurityManager.getThreadGroup()}. If there is not a security manager or {@code
     *              SecurityManager.getThreadGroup()} returns {@code null}, the group is set to the
     *              current thread's thread group.
     * @param name  the name of the new thread.
     *
     * @throws SecurityException if the current thread cannot create a thread in the specified
     *                           thread group
     */
    public NotifyingThread(@Nullable ThreadGroup group,
                           @NotNull String name) {
        this(group, null, name);
    }

    /**
     * Allocates a new {@code Thread} object so that it has {@code target} as its run object, has
     * the specified {@code name} as its name, and belongs to the thread group referred to by {@code
     * group}.
     *
     * <p>If there is a security manager, its
     * {@link SecurityManager#checkAccess(ThreadGroup) checkAccess} method is invoked with the
     * ThreadGroup as its argument.
     *
     * <p>In addition, its {@code checkPermission} method is invoked with
     * the {@code RuntimePermission("enableContextClassLoaderOverride")} permission when invoked
     * directly or indirectly by the constructor of a subclass which overrides the {@code
     * getContextClassLoader} or {@code setContextClassLoader} methods.
     *
     * <p>The priority of the newly created thread is set equal to the
     * priority of the thread creating it, that is, the currently running thread. The method
     * {@linkplain #setPriority setPriority} may be used to change the priority to a new value.
     *
     * <p>The newly created thread is initially marked as being a daemon
     * thread if and only if the thread creating it is currently marked as a daemon thread. The
     * method {@linkplain #setDaemon setDaemon} may be used to change whether or not a thread is a
     * daemon.
     *
     * @param group     the thread group. If {@code null} and there is a security manager, the group
     *                  is determined by {@linkplain SecurityManager#getThreadGroup
     *                  SecurityManager.getThreadGroup()}. If there is not a security manager or
     *                  {@code SecurityManager.getThreadGroup()} returns {@code null}, the group is
     *                  set to the current thread's thread group.
     * @param target    the object whose {@code run} method is invoked when this thread is started.
     *                  If {@code null}, this thread's run method is invoked.
     * @param name      the name of the new thread.
     * @param listeners an {@code array} of {@linkplain OnThreadCompletedListener listeners} which
     *                  are classes that implements {@linkplain OnThreadCompletedListener}, so when
     *                  a thread finish its work, those classes are called at the {@link
     *                  OnThreadCompletedListener#onThreadCompletedListener(Thread, Throwable)}
     *                  method, giving both {@link Runnable} of the just finished thread and {@link
     *                  Throwable} with any exception that occurred during execution.
     *
     * @throws SecurityException if the current thread cannot create a thread in the specified
     *                           thread group or cannot override the context class loader methods.
     */
    public NotifyingThread(@Nullable ThreadGroup group,
                           Runnable target,
                           @NotNull String name,
                           @NotNull OnThreadCompletedListener... listeners) {
        this(group, target, name, 0, listeners);
    }

    /**
     * Allocates a new {@code Thread} object. This constructor has the same effect as {@linkplain
     * #NotifyingThread(ThreadGroup, Runnable, String) NotifyingThread} {@code (null, target,
     * name)}.
     *
     * @param target the object whose {@code run} method is invoked when this thread is started. If
     *               {@code null}, this thread's run method is invoked.
     * @param name   the name of the new thread.
     */
    public NotifyingThread(Runnable target,
                           String name) {
        this(null, target, name);
    }

    /**
     * Allocates a new {@code Thread} object so that it has {@code target} as its run object, has
     * the specified {@code name} as its name, and belongs to the thread group referred to by {@code
     * group}.
     *
     * <p>If there is a security manager, its
     * {@link SecurityManager#checkAccess(ThreadGroup) checkAccess} method is invoked with the
     * ThreadGroup as its argument.
     *
     * <p>In addition, its {@code checkPermission} method is invoked with
     * the {@code RuntimePermission("enableContextClassLoaderOverride")} permission when invoked
     * directly or indirectly by the constructor of a subclass which overrides the {@code
     * getContextClassLoader} or {@code setContextClassLoader} methods.
     *
     * <p>The priority of the newly created thread is set equal to the
     * priority of the thread creating it, that is, the currently running thread. The method
     * {@linkplain #setPriority setPriority} may be used to change the priority to a new value.
     *
     * <p>The newly created thread is initially marked as being a daemon
     * thread if and only if the thread creating it is currently marked as a daemon thread. The
     * method {@linkplain #setDaemon setDaemon} may be used to change whether or not a thread is a
     * daemon.
     *
     * @param group  the thread group. If {@code null} and there is a security manager, the group is
     *               determined by {@linkplain SecurityManager#getThreadGroup
     *               SecurityManager.getThreadGroup()}. If there is not a security manager or {@code
     *               SecurityManager.getThreadGroup()} returns {@code null}, the group is set to the
     *               current thread's thread group.
     * @param target the object whose {@code run} method is invoked when this thread is started. If
     *               {@code null}, this thread's run method is invoked.
     * @param name   the name of the new thread.
     *
     * @throws SecurityException if the current thread cannot create a thread in the specified
     *                           thread group or cannot override the context class loader methods.
     */
    public NotifyingThread(@Nullable ThreadGroup group,
                           Runnable target,
                           @NotNull String name) {
        this(group, target, name, 0);
    }

    /**
     * Allocates a new {@code Thread} object so that it has {@code target} as its run object, has
     * the specified {@code name} as its name, and belongs to the thread group referred to by {@code
     * group}, and has the specified <i>stack size</i>.
     * <p>
     * This constructor is identical to {@link #NotifyingThread(ThreadGroup, Runnable, String)} with
     * the exception of the fact that it allows the thread stack size to be specified.  The stack
     * size is the approximate number of bytes of address space that the virtual machine is to
     * allocate for this thread's stack.  <b>The effect of the {@code stackSize} parameter, if any,
     * is highly platform dependent.</b>
     *
     * <p>On some platforms, specifying a higher value for the
     * {@code stackSize} parameter may allow a thread to achieve greater recursion depth before
     * throwing a {@link StackOverflowError}. Similarly, specifying a lower value may allow a
     * greater number of threads to exist concurrently without throwing an {@link OutOfMemoryError}
     * (or other internal error).  The details of the relationship between the value of the {@code
     * stackSize} parameter and the maximum recursion depth and concurrency level are
     * platform-dependent.  <b>On some platforms, the value of the {@code stackSize} parameter may
     * have no effect whatsoever.</b>
     *
     * <p>The virtual machine is free to treat the {@code stackSize}
     * parameter as a suggestion.  If the specified value is unreasonably low for the platform, the
     * virtual machine may instead use some platform-specific minimum value; if the specified value
     * is unreasonably high, the virtual machine may instead use some platform-specific maximum.
     * Likewise, the virtual machine is free to round the specified value up or down as it sees fit
     * (or to ignore it completely).
     *
     * <p>Specifying a value of zero for the {@code stackSize} parameter will
     * cause this constructor to behave exactly like the {@code Thread(ThreadGroup, Runnable,
     * String)} constructor.
     *
     * <p><i>Due to the platform-dependent nature of the behavior of this
     * constructor, extreme care should be exercised in its use. The thread stack size necessary to
     * perform a given computation will likely vary from one JRE implementation to another.  In
     * light of this variation, careful tuning of the stack size parameter may be required, and the
     * tuning may need to be repeated for each JRE implementation on which an application is to
     * run.</i>
     *
     * <p>Implementation note: Java platform implementers are encouraged to
     * document their implementation's behavior with respect to the {@code stackSize} parameter.
     *
     * @param group     the thread group. If {@code null} and there is a security manager, the group
     *                  is determined by {@linkplain SecurityManager#getThreadGroup
     *                  SecurityManager.getThreadGroup()}. If there is not a security manager or
     *                  {@code SecurityManager.getThreadGroup()} returns {@code null}, the group is
     *                  set to the current thread's thread group.
     * @param target    the object whose {@code run} method is invoked when this thread is started.
     *                  If {@code null}, this thread's run method is invoked.
     * @param name      the name of the new thread.
     * @param stackSize the desired stack size for the new thread, or zero to indicate that this
     *                  parameter is to be ignored.
     *
     * @throws SecurityException if the current thread cannot create a thread in the specified
     *                           thread group
     * @since 1.4
     */
    public NotifyingThread(@Nullable ThreadGroup group,
                           Runnable target,
                           @NotNull String name,
                           long stackSize) {
        super(group, target, name, stackSize);
        mSubscribedClasses = new ArrayList<>(DEFAULT_CAPACITY);
        mTarget = target;
        setUncaughtExceptionHandler(this);
    }

    /**
     * Allocates a new {@code Thread} object so that it has {@code target} as its run object, has
     * the specified {@code name} as its name, and belongs to the thread group referred to by {@code
     * group}, and has the specified <i>stack size</i>.
     * <p>
     * This constructor is identical to {@link #NotifyingThread(ThreadGroup, Runnable, String)} with
     * the exception of the fact that it allows the thread stack size to be specified.  The stack
     * size is the approximate number of bytes of address space that the virtual machine is to
     * allocate for this thread's stack.  <b>The effect of the {@code stackSize} parameter, if any,
     * is highly platform dependent.</b>
     *
     * <p>On some platforms, specifying a higher value for the
     * {@code stackSize} parameter may allow a thread to achieve greater recursion depth before
     * throwing a {@link StackOverflowError}. Similarly, specifying a lower value may allow a
     * greater number of threads to exist concurrently without throwing an {@link OutOfMemoryError}
     * (or other internal error).  The details of the relationship between the value of the {@code
     * stackSize} parameter and the maximum recursion depth and concurrency level are
     * platform-dependent.  <b>On some platforms, the value of the {@code stackSize} parameter may
     * have no effect whatsoever.</b>
     *
     * <p>The virtual machine is free to treat the {@code stackSize}
     * parameter as a suggestion.  If the specified value is unreasonably low for the platform, the
     * virtual machine may instead use some platform-specific minimum value; if the specified value
     * is unreasonably high, the virtual machine may instead use some platform-specific maximum.
     * Likewise, the virtual machine is free to round the specified value up or down as it sees fit
     * (or to ignore it completely).
     *
     * <p>Specifying a value of zero for the {@code stackSize} parameter will
     * cause this constructor to behave exactly like the {@code Thread(ThreadGroup, Runnable,
     * String)} constructor.
     *
     * <p><i>Due to the platform-dependent nature of the behavior of this
     * constructor, extreme care should be exercised in its use. The thread stack size necessary to
     * perform a given computation will likely vary from one JRE implementation to another.  In
     * light of this variation, careful tuning of the stack size parameter may be required, and the
     * tuning may need to be repeated for each JRE implementation on which an application is to
     * run.</i>
     *
     * <p>Implementation note: Java platform implementers are encouraged to
     * document their implementation's behavior with respect to the {@code stackSize} parameter.
     *
     * @param group     the thread group. If {@code null} and there is a security manager, the group
     *                  is determined by {@linkplain SecurityManager#getThreadGroup
     *                  SecurityManager.getThreadGroup()}. If there is not a security manager or
     *                  {@code SecurityManager.getThreadGroup()} returns {@code null}, the group is
     *                  set to the current thread's thread group.
     * @param target    the object whose {@code run} method is invoked when this thread is started.
     *                  If {@code null}, this thread's run method is invoked.
     * @param name      the name of the new thread.
     * @param stackSize the desired stack size for the new thread, or zero to indicate that this
     *                  parameter is to be ignored.
     * @param listeners an {@code array} of {@linkplain OnThreadCompletedListener listeners} which
     *                  are classes that implements {@linkplain OnThreadCompletedListener}, so when
     *                  a thread finish its work, those classes are called at the {@link
     *                  OnThreadCompletedListener#onThreadCompletedListener(Thread, Throwable)}
     *                  method, giving both {@link Runnable} of the just finished thread and {@link
     *                  Throwable} with any exception that occurred during execution.
     *
     * @throws SecurityException if the current thread cannot create a thread in the specified
     *                           thread group
     * @since 1.4
     */
    public NotifyingThread(@Nullable ThreadGroup group,
                           Runnable target,
                           @NotNull String name,
                           long stackSize,
                           @NotNull OnThreadCompletedListener... listeners) {
        super(group, target, name, stackSize);
        mSubscribedClasses = new ArrayList<>(Arrays.asList(listeners));
        mTarget = target;
        setUncaughtExceptionHandler(this);
    }

    /**
     * Returns the corresponding thread number for a newly created {@link NotifyingThread}.
     *
     * @return {@code int} with the thread number.
     */
    private static synchronized int nextThreadNumber() {
        return mThreadNumber.getAndIncrement();
    }

    /**
     * When a {@code thread} finishes, or an <b>exception is thrown</b>, this method is called by
     * {@link #run()} or {@link #uncaughtException(Thread, Throwable)}, the first one to occur.
     * <p>
     * This method has the following behaviour:
     * <ul>
     * <li>
     * If there is <b>no subscribed class</b>, it immediately finishes.
     * </li>
     * <li>
     * If {@linkplain #mShouldCallSubscribedClassesAsynchronously async mode} is set-up (it is
     * {@code true}), each {@linkplain #mSubscribedClasses subscribed class} is called on a new
     * thread for not blocking main thread doing the calls.
     * </li>
     * <li>
     * On the other hand, if {@linkplain #mShouldCallSubscribedClassesAsynchronously async mode} is
     * off, each {@linkplain #mSubscribedClasses subscribed class} is called on the main thread,
     * being possible not calling all classes if <b>any exception occurs</b>.
     * </li>
     * </ul>
     *
     * @param thread    thread that has just finished its execution (because of an exception or
     *                  finished normally).
     * @param exception exception thrown by the thread execution - it will be {@code null} if no
     *                  exception has been thrown.
     *
     * @see #setShouldCallSubscribedClassesAsynchronously(boolean)
     * @see #addOnThreadCompletedListener(OnThreadCompletedListener)
     * @see #addOnThreadCompleteListener(OnThreadCompletedListener...)
     */
    private void callSubscribedClasses(final @NotNull Thread thread,
                                       final @Nullable Throwable exception) {
        if (mSubscribedClasses.isEmpty())
            return;
        if (mShouldCallSubscribedClassesAsynchronously.get())
            for (final OnThreadCompletedListener subscribedClass : mSubscribedClasses)
                new Thread(() -> subscribedClass.onThreadCompletedListener(thread, exception))
                        .start();
        else
            for (OnThreadCompletedListener subscribedClass : mSubscribedClasses)
                subscribedClass.onThreadCompletedListener(thread, exception);
    }

    /**
     * When passing {@code true} to this method, when {@link #run()} finishes and {@linkplain
     * #mSubscribedClasses subscribed classes} are called, that call will be done on a new
     * asynchronous thread.
     * <p>
     * That has some advantages:
     * <ul>
     * <li>
     * There is <b>no blocking</b> during execution.
     * </li>
     * <li>
     * Because of what explained above, all <b>subscribed classes</b> are called, even if there is
     * any error and/or exception.
     * </li>
     * <li>
     * {@code Thread} is not blocked, so it can resume any execution or get immediately {@code idle}
     * in a {@link java.util.concurrent.ThreadPoolExecutor}.
     * </li>
     * </ul>
     * Also, it has some disadvantages:
     * <ul>
     * <li>
     * Both <b>starvation</b> and <b>race conditions</b> may occur during the subscribed classes
     * calling.
     * </li>
     * <li>
     * The exceptions occurred during the <i>new thread generation</i> are not handled.
     * </li>
     * </ul>
     * <p>
     * By default, that field is set to {@code false}, which is the normal execution of that type of
     * listeners.
     *
     * @param shouldCallAsynchronously whether the calling to subscribed classes must be async or
     *                                 not.
     */
    public void setShouldCallSubscribedClassesAsynchronously(boolean shouldCallAsynchronously) {
        mShouldCallSubscribedClassesAsynchronously.set(shouldCallAsynchronously);
    }

    /**
     * Adds a new {@linkplain OnThreadCompletedListener listener} to the list of subscribed classes.
     * For adding more than one thread at once, please refer to {@link
     * #addOnThreadCompleteListener(OnThreadCompletedListener...)}.
     *
     * @param listener new listener that will be called on this thread completion.
     */
    public void addOnThreadCompletedListener(@NotNull OnThreadCompletedListener listener) {
        mSubscribedClasses.add(listener);
    }

    /**
     * Adds new {@linkplain OnThreadCompletedListener listeners} to the list of subscribed classes
     * at once. For adding just one listener, please refer to
     * {@link #addOnThreadCompletedListener(OnThreadCompletedListener)}.
     *
     * @param listeners {@code array} of listeners that will be called on this thread completion.
     */
    public void addOnThreadCompleteListener(@NotNull OnThreadCompletedListener... listeners) {
        mSubscribedClasses.addAll(Arrays.asList(listeners));
    }

    /**
     * Tries to remove the given {@code listener} from the list of subscribed classes, if present.
     *
     * @param listener class to remove.
     *
     * @return {@code true} when the item was present, {@code false} if there was no listener
     * matching the one to delete.
     */
    public boolean removeOnThreadCompletedListener(@NotNull OnThreadCompletedListener listener) {
        return mSubscribedClasses.remove(listener);
    }

    /**
     * Un-subscribes all classes listening to this thread completion.
     */
    public void removeAllListeners() {
        mSubscribedClasses.clear();
    }

    /**
     * Once after the {@link NotifyingThread} is created, this method can be used for setting the
     * {@link Runnable} that will be executed when {@link #start()} is called.
     * <p>
     * This class uses a {@link Consumer}, which is a type of {@link Function} which accepts
     * <b>one parameter</b> and produces <b>no output</b>. They are {@code void} functions.
     * <p>
     * The function that will be <b>inside</b> this declaration must follow the following syntax:
     * <pre>{@code
     *         // Class name: ConsumerFunction
     *         public void functionName(ArgumentParser arguments) {
     *             // Obtain arguments here
     *             int firstArgument = arguments.getInt("firstArgumentName");
     *             // ...
     *             String nArgument = arguments.getString("nArgumentName");
     *             // Produce some result/attribute with the given params
     *             this.fieldToModify = nArgument + String.valueOf(firstArgument);
     *         }
     *         }</pre>
     * That function will apply a {@code Consumer} function, which can be used as follows:
     * <pre>
     *             {@code
     *             public static void main(String[] args) {
     *                 ConsumerFunction consumer = new ConsumerFunction(); // Here goes your class
     *                 which contains the consumer function.
     *                 // Declare both NotifyingThread and ArgumentParser
     *                 NotifyingThread thread = new NotifyingThread();
     *                 ArgumentParser parser = new ArgumentParser(2); // can be as much params as
     *                 you need.
     *                 // Set values contained at ArgumentParser
     *                 parser.putParam("firstArgumentName", 9889);
     *                 parser.putParam("nArgumentName", "Javinator");
     *                 // Add the new executable to the NotifyingThread
     *                 thread.setExecutable(consumer::functionName, parser);
     *                 // DO NOT FORGET TO EXECUTE THE THREAD
     *                 thread.start();
     *                 // -----------------------------------
     *                 // "fieldToModify" IN "ConsumerFunction" WILL BE:
     *                 // Javinator9889
     *             }
     *             }
     *         </pre>
     *
     * @param consumer function that follows the implementation described at the full
     *                 documentation.
     * @param args     arguments that receive that function.
     *
     * @see ArgumentParser
     */
    public void setExecutable(@NotNull Consumer<ArgumentParser> consumer,
                              @NotNull ArgumentParser args) {
        mTarget = () -> consumer.accept(args);
    }

    /**
     * Once after the {@link NotifyingThread} is created, this method can be used for setting the
     * {@link Runnable} that will be executed when {@link #start()} is called.
     * <p>
     * This class uses a {@link Runnable}, which is a type of {@link Function} which accepts
     * <b>no parameters</b> and produces <b>no output</b>. They are {@code void} functions.
     * <p>
     * The function that will be <b>inside</b> this declaration must follow the following syntax:
     * <pre>{@code
     *         // Class name: RunnableFunction
     *         public void functionName() {
     *             // Useful for working with class fields once an operation is completed.
     *             this.fieldToModify = "Javinator9889";
     *         }
     *         }</pre>
     * That function will apply a {@code Runnable} function, which can be used as follows:
     * <pre>
     *             {@code
     *             public static void main(String[] args) {
     *                 RunnableFunction runnable = new RunnableFunction(); // Here goes your class
     *                 which contains the runnable function.
     *                 // Declare a NotifyingThread
     *                 NotifyingThread thread = new NotifyingThread();
     *                 // Add the new executable to the NotifyingThread
     *                 thread.setExecutable(runnable::functionName);
     *                 // DO NOT FORGET TO EXECUTE THE THREAD
     *                 thread.start();
     *                 // -----------------------------------
     *                 // "fieldToModify" IN "RunnableFunction" WILL BE:
     *                 // Javinator9889
     *             }
     *             }
     *         </pre>
     *
     * @param runnable function that follows the implementation described at the full
     *                 documentation.
     */
    public void setExecutable(@NotNull Runnable runnable) {
        mTarget = runnable;
    }

    /**
     * Once after the {@link NotifyingThread} is created, this method can be used for setting the
     * {@link Runnable} that will be executed when {@link #start()} is called.
     * <p>
     * This class uses a {@link Supplier}, which is a type of {@link Function} which accepts
     * <b>no parameters</b> and produces <b>an output</b>. Also, this type of functions are very
     * similar to {@link java.util.concurrent.Future}.
     * <p>
     * The function that will be <b>inside</b> this declaration must follow the following syntax:
     * <pre>{@code
     *         // Class name: SupplierFunction
     *         public int functionName() {
     *             // Produce some result/attribute with the class fields'
     *             return this.fieldToProduce;
     *         }
     *         }</pre>
     * That function will apply a {@code Supplier} function, which can be used as follows:
     * <pre>
     *             {@code
     *             public static void main(String[] args) {
     *                 SupplierFunction supplier = new SupplierFunction(); // Here goes your class
     *                 which contains the supplier function.
     *                 // Declare both NotifyingThread and AtomicReference, which will contain
     *                 the result.
     *                 NotifyingThread thread = new NotifyingThread();
     *                 AtomicReference<Integer> result = new AtomicReference<>();
     *                 // Add the new executable to the NotifyingThread
     *                 thread.setExecutable(supplier::functionName, result);
     *                 // DO NOT FORGET TO EXECUTE THE THREAD
     *                 thread.start();
     *                 // ... wait until termination
     *                 System.out.println(result.get()); // The obtained result.
     *                 // -----------------------------------
     *             }
     *             }
     *         </pre>
     *
     * @param supplier function that follows the implementation described at the full
     *                 documentation.
     * @param result   {@code AtomicReference} that will contain the supplier result.
     *
     * @see AtomicReference
     */
    public void setExecutable(@NotNull Supplier supplier,
                              @NotNull final AtomicReference result) {
        mTarget = () -> result.set(supplier.get());
    }

    /**
     * Once after the {@link NotifyingThread} is created, this method can be used for setting the
     * {@link Runnable} that will be executed when {@link #start()} is called.
     * <p>
     * This class uses a {@link Function}, which is a type of {@link Function} which accepts
     * <b>one parameter</b> and produces <b>one output</b>. They are very similar to
     * {@link java.util.concurrent.Future}, as those functions receive zero or more args and produce
     * a result.
     * <p>
     * The function that will be <b>inside</b> this declaration must follow the following syntax:
     * <pre>{@code
     *         // Class name: FunctionFunction
     *         public String functionName(ArgumentParser arguments) {
     *             // Obtain arguments here
     *             int firstArgument = arguments.getInt("firstArgumentName");
     *             // ...
     *             String nArgument = arguments.getString("nArgumentName");
     *             // Produce some result/attribute with the given params
     *             this.fieldToModify = nArgument + String.valueOf(firstArgument);
     *             return this.fieldToModify;
     *         }
     *         }</pre>
     * That function will apply a {@code Function} function, which can be used as follows:
     * <pre>
     *             {@code
     *             public static void main(String[] args) {
     *                 FunctionFunction function = new FunctionFunction(); // Here goes your class
     *                 which contains the function function.
     *                 // Declare NotifyingThread, ArgumentParser and AtomicReference
     *                 NotifyingThread thread = new NotifyingThread();
     *                 ArgumentParser parser = new ArgumentParser(2); // can be as much params as
     *                 you need.
     *                 AtomicReference<String> result = new AtomicReference<>();
     *                 // Set values contained at ArgumentParser
     *                 parser.putParam("firstArgumentName", 9889);
     *                 parser.putParam("nArgumentName", "Javinator");
     *                 // Add the new executable to the NotifyingThread
     *                 thread.setExecutable(function::functionName, parser, result);
     *                 // DO NOT FORGET TO EXECUTE THE THREAD
     *                 thread.start();
     *                 // ... wait until termination
     *                 System.out.println(result.get());
     *                 // -----------------------------------
     *                 // "fieldToModify" IN "FunctionFunction" WILL BE:
     *                 // Javinator9889
     *                 // System.out.: Javinator9889
     *             }
     *             }
     *         </pre>
     *
     * @param function function that follows the implementation described at the full
     *                 documentation.
     * @param args     arguments that receive that function.
     * @param result   {@code AtomicReference} that will contain the function result.
     *
     * @see ArgumentParser
     * @see AtomicReference
     */
    public void setExecutable(@NotNull Function<ArgumentParser, ?> function,
                              @NotNull final ArgumentParser args,
                              @NotNull final AtomicReference result) {
        mTarget = () -> result.set(function.apply(args));
    }

    /**
     * If this thread was constructed using a separate {@code Runnable} run object, then that {@code
     * Runnable} object's {@code run} method is called; otherwise, this method does nothing and
     * returns. When finishes, it calls {@link #callSubscribedClasses(Thread, Throwable)
     * subscribed classes} notifying that it has just ended-up.
     * <p>
     * Subclasses of {@code Thread} should override this method.
     *
     * @see #start()
     * @see #stop()
     * @see #NotifyingThread(ThreadGroup, Runnable, String)
     */
    @Override
    public void run() {
        if (mTarget != null) {
            try {
                mTarget.run();
                callSubscribedClasses(this, null);
            } catch (Throwable ex) {
                UncaughtExceptionHandler handler = getUncaughtExceptionHandler();
                if (handler != null)
                    handler.uncaughtException(this, ex);
            }
        }
    }

    /**
     * Method invoked when the given thread terminates due to the given uncaught exception.
     * <p>Any exception thrown by this method will be ignored by the
     * Java Virtual Machine.
     *
     * @param thread    the thread
     * @param exception the exception
     */
    @Override
    public void uncaughtException(final @NotNull Thread thread,
                                  final @NotNull Throwable exception) {
        callSubscribedClasses(thread, exception);
    }

    /**
     * Returns a string representation of this thread, including the thread's name, priority, and
     * thread group.
     *
     * @return a string representation of this thread.
     */
    @Override
    public String toString() {
        ThreadGroup group = getThreadGroup();
        if (group != null) {
            return "NotifyingThread[" + getName() + ", " + getPriority() + ", " +
                    group.getName() + "]";
        } else {
            return "NotifyingThread[" + getName() + ", " + getPriority() + ", " +
                    "" + "]";
        }
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The {@code equals} method implements an equivalence relation on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     * {@code x}, {@code x.equals(x)} should return {@code true}.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     * {@code x} and {@code y}, {@code x.equals(y)} should return {@code true} if and only if {@code
     * y.equals(x)} returns {@code true}.
     * <li>It is <i>transitive</i>: for any non-null reference values
     * {@code x}, {@code y}, and {@code z}, if {@code x.equals(y)} returns {@code true} and {@code
     * y.equals(z)} returns {@code true}, then {@code x.equals(z)} should return {@code true}.
     * <li>It is <i>consistent</i>: for any non-null reference values
     * {@code x} and {@code y}, multiple invocations of {@code x.equals(y)} consistently return
     * {@code true} or consistently return {@code false}, provided no information used in {@code
     * equals} comparisons on the objects is modified.
     * <li>For any non-null reference value {@code x},
     * {@code x.equals(null)} should return {@code false}.
     * </ul>
     * <p>
     * The {@code equals} method for class {@code Object} implements the most discriminating
     * possible equivalence relation on objects; that is, for any non-null reference values {@code
     * x} and {@code y}, this method returns {@code true} if and only if {@code x} and {@code y}
     * refer to the same object ({@code x == y} has the value {@code true}).
     * <p>
     * Note that it is generally necessary to override the {@code hashCode} method whenever this
     * method is overridden, so as to maintain the general contract for the {@code hashCode} method,
     * which states that equal objects must have equal hash codes.
     *
     * @param obj the reference object with which to compare.
     *
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     *
     * @see #hashCode()
     * @see java.util.HashMap
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NotifyingThread that = (NotifyingThread) obj;
        return Objects.equals(mSubscribedClasses, that.mSubscribedClasses) &&
                Objects.equals(mShouldCallSubscribedClassesAsynchronously,
                        that.mShouldCallSubscribedClassesAsynchronously);
    }

    /**
     * Returns a hash code value for the object. This method is supported for the benefit of hash
     * tables such as those provided by {@link java.util.HashMap}.
     * <p>
     * The general contract of {@code hashCode} is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during
     * an execution of a Java application, the {@code hashCode} method must consistently return the
     * same integer, provided no information used in {@code equals} comparisons on the object is
     * modified. This integer need not remain consistent from one execution of an application to
     * another execution of the same application.
     * <li>If two objects are equal according to the {@code equals(Object)}
     * method, then calling the {@code hashCode} method on each of the two objects must produce the
     * same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal
     * according to the {@link java.lang.Object#equals(java.lang.Object)} method, then calling the
     * {@code hashCode} method on each of the two objects must produce distinct integer results.
     * However, the programmer should be aware that producing distinct integer results for unequal
     * objects may improve the performance of hash tables.
     * </ul>
     * <p>
     * As much as is reasonably practical, the hashCode method defined by class {@code Object} does
     * return distinct integers for distinct objects. (The hashCode may or may not be implemented as
     * some function of an object's memory address at some point in time.)
     *
     * @return a hash code value for this object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     * @see java.lang.System#identityHashCode
     */
    @Override
    public int hashCode() {
        return Objects.hash(mSubscribedClasses, mShouldCallSubscribedClassesAsynchronously);
    }

    /**
     * Throws CloneNotSupportedException as a Thread can not be meaningfully cloned. Construct a new
     * Thread instead.
     *
     * @throws CloneNotSupportedException always
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
