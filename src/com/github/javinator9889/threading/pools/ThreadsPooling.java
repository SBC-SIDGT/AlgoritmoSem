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

package com.github.javinator9889.threading.pools;

import com.github.javinator9889.threading.pools.rejectedhandlers.DefaultRejectedExecutionHandler;
import com.github.javinator9889.threading.pools.rejectedhandlers.ImmediatelyRunOnRejectedExecutionHandler;
import com.github.javinator9889.threading.pools.rejectedhandlers.NoRejectedExecutionHandler;
import com.github.javinator9889.threading.pools.rejectedhandlers.RunWhenTasksFinishedOnRejectedHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * {@code ThreadsPooling} provides a <b>fast, easy</b> access to a pool of threads that concurrently
 * must be running, with upper limits.
 * <p>
 * It handles {@linkplain ThreadPoolExecutor} and {@linkplain BlockingQueue} of {@code Runnable} s,
 * manipulating and offering access to {@linkplain ThreadPoolExecutor} methods without
 * <b>high user interaction</b>: by using the custom {@link Builder} (invoked via
 * {@link ThreadsPooling#builder()}), a developer can <b>easily</b> setup this class with the
 * requirements needed for an optimum execution.
 * <p>
 * In addition, this class <b>provides some useful</b> handlers when a new thread <b>is
 * rejected</b>: for example, if th {@code queue} is full, a {@link RejectedExecutionHandler}
 * subclass will be called, with the {@code Runnable} with the just rejected thread and {@code
 * ThreadPoolExecutor} with the executor that rejected the thread. There are <b>several
 * predefined</b> handlers with some "default" options that can satisfy daily developers work:
 * <ul>
 * <li>
 * {@linkplain #DEFAULT_REJECTED_EXECUTION_HANDLER}
 * </li>
 * <li>
 * {@linkplain #NO_ACTION_ON_REJECTED_HANDLER}
 * </li>
 * <li>
 * {@linkplain #IMMEDIATELY_RUN_ON_REJECTED_HANDLER}
 * </li>
 * <li>
 * {@linkplain #WAIT_SHUTDOWN_RUN_TASK_ON_REJECTED_HANDLER}
 * </li>
 * </ul>
 * <p>
 * All those handlers can be access instantly from within the class and used {@linkplain
 * Builder#withRejectedExecutionHandler(RejectedExecutionHandler) inside the builder} for using the
 * one you prefer.
 */
public class ThreadsPooling {
    /**
     * The default number of threads that should be running concurrently - if needed, it can be
     * automatically increased for filling {@linkplain #DEFAULT_MAX_THREADS maximum threads}.
     */
    public static final int DEFAULT_CORE_THREADS = 4;

    /**
     * The default maximum number of threads that can be concurrently running - this amount is only
     * reached when there is a heavy load of pending threads.
     */
    public static final int DEFAULT_MAX_THREADS = 8;
    /**
     * The default keep alive time - when the number of threads is higher than the {@linkplain
     * #DEFAULT_CORE_THREADS core threads}, this is the time that idle threads will wait for new
     * tasks before terminating.
     */
    public static final long DEFAULT_KEEP_ALIVE = 100;

    /**
     * The default time unit for the {@linkplain #DEFAULT_KEEP_ALIVE keep alive time}.
     */
    public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;

    /**
     * The default starting queue capacity - if you know how much process you are going to run
     * concurrently, it is better to define the {@linkplain Builder#withQueueCapacity(int) queue
     * capacity}.
     */
    public static final int DEFAULT_QUEUE_CAPACITY = 100;

    /**
     * The default rejected execution handler - it throws an exception ({@link
     * NoRejectedExecutionHandler}) with all useful information obtained from the thread and the
     * executor.
     * <p>
     * Refer to {@link DefaultRejectedExecutionHandler} for more information.
     */
    public static final RejectedExecutionHandler DEFAULT_REJECTED_EXECUTION_HANDLER =
            new DefaultRejectedExecutionHandler();

    /**
     * A predefined rejected execution handler that does nothing when a thread is rejected.
     * <p>
     * Refer to {@link NoRejectedExecutionHandler} for more information.
     */
    public static final RejectedExecutionHandler NO_ACTION_ON_REJECTED_HANDLER =
            new NoRejectedExecutionHandler();

    /**
     * A predefined rejected execution handler that runs the rejected thread immediately.
     * <p>
     * Refer to {@link ImmediatelyRunOnRejectedExecutionHandler} for more information.
     */
    public static final RejectedExecutionHandler IMMEDIATELY_RUN_ON_REJECTED_HANDLER =
            new ImmediatelyRunOnRejectedExecutionHandler();

    /**
     * A predefined rejected execution handler that waits until all process inside the {@link
     * ThreadPoolExecutor} finish and then, runs the rejected thread.
     * <p>
     * Refer to {@link RunWhenTasksFinishedOnRejectedHandler} for more information.
     */
    public static final RejectedExecutionHandler WAIT_SHUTDOWN_RUN_TASK_ON_REJECTED_HANDLER =
            new RunWhenTasksFinishedOnRejectedHandler(10000, TimeUnit.MILLISECONDS);

    /**
     * The {@link ThreadPoolExecutor} that manages the process running.
     */
    private ThreadPoolExecutor mPoolExecutor;

    /**
     * The {@link BlockingQueue} synchronized with {@linkplain #mPoolExecutor the pool executor} for
     * managing incoming threads.
     */
    private BlockingQueue<Runnable> mWorkingThreadsQueue;

    /**
     * Private constructor used by {@link Builder} - cannot be accessed from outside.
     * <p>
     * It has the same behaviour as calling {@link #ThreadsPooling(int, int, long, TimeUnit,
     * BlockingQueue, RejectedExecutionHandler)} with params {@code ThreadsPooling(coreThreads,
     * maximumPoolSize, keepAliveTime, timeUnit, workingThreadsQueue, DEFAULT_REJECTED_EXECUTION_HANDLER}).
     *
     * @param coreThreads         the threads that must be running concurrently.
     * @param maximumPoolSize     the maximum threads that can be running concurrently.
     * @param keepAliveTime       the keep alive time for idle threads to wait for running ones.
     * @param timeUnit            the time unit that sets up the {@code keepAliveTime}.
     * @param workingThreadsQueue the queue of the threads that will be executed.
     *
     * @see #ThreadsPooling(int, int, long, TimeUnit, BlockingQueue, RejectedExecutionHandler)
     */
    private ThreadsPooling(int coreThreads, int maximumPoolSize, long keepAliveTime,
                           TimeUnit timeUnit, BlockingQueue<Runnable> workingThreadsQueue) {
        this(coreThreads,
                maximumPoolSize,
                keepAliveTime,
                timeUnit,
                workingThreadsQueue,
                DEFAULT_REJECTED_EXECUTION_HANDLER);
    }

    /**
     * Private constructor used by {@link Builder} - cannot be accessed from outside.
     * <p>
     * It has the same behaviour as calling {@link #ThreadsPooling(int, int, long, TimeUnit,
     * BlockingQueue, ThreadFactory, RejectedExecutionHandler)} with params {@code ThreadsPooling
     * (coreThreads, maximumPoolSize, keepAliveTime, timeUnit, workingThreadsQueue, factory,
     * DEFAULT_REJECTED_EXECUTION_HANDLER}).
     *
     * @param coreThreads         the threads that must be running concurrently.
     * @param maximumPoolSize     the maximum threads that can be running concurrently.
     * @param keepAliveTime       the keep alive time for idle threads to wait for running ones.
     * @param timeUnit            the time unit that sets up the {@code keepAliveTime}.
     * @param workingThreadsQueue the queue of the threads that will be executed.
     * @param factory             the factory used for creating new threads.
     *
     * @see #ThreadsPooling(int, int, long, TimeUnit, BlockingQueue, ThreadFactory,
     * RejectedExecutionHandler)
     */
    private ThreadsPooling(int coreThreads, int maximumPoolSize, long keepAliveTime,
                           TimeUnit timeUnit, BlockingQueue<Runnable> workingThreadsQueue,
                           ThreadFactory factory) {
        this(coreThreads,
                maximumPoolSize,
                keepAliveTime,
                timeUnit,
                workingThreadsQueue,
                factory,
                DEFAULT_REJECTED_EXECUTION_HANDLER);
    }

    /**
     * Private constructor used by {@link Builder} - cannot be accessed from outside.
     * <p>
     *
     * @param coreThreads              the threads that must be running concurrently.
     * @param maximumPoolSize          the maximum threads that can be running concurrently.
     * @param keepAliveTime            the keep alive time for idle threads to wait for running
     *                                 ones.
     * @param timeUnit                 the time unit that sets up the {@code keepAliveTime}.
     * @param workingThreadsQueue      the queue of the threads that will be executed.
     * @param rejectedExecutionHandler the handler used when a new thread is rejected.
     *
     * @see #ThreadsPooling(int, int, long, TimeUnit, BlockingQueue, RejectedExecutionHandler)
     * @see ThreadPoolExecutor#ThreadPoolExecutor(int, int, long, TimeUnit, BlockingQueue,
     * RejectedExecutionHandler)
     */
    private ThreadsPooling(int coreThreads, int maximumPoolSize, long keepAliveTime,
                           TimeUnit timeUnit, BlockingQueue<Runnable> workingThreadsQueue,
                           RejectedExecutionHandler rejectedExecutionHandler) {
        mWorkingThreadsQueue = workingThreadsQueue;
        mPoolExecutor = new ThreadPoolExecutor(coreThreads,
                maximumPoolSize,
                keepAliveTime,
                timeUnit,
                workingThreadsQueue,
                rejectedExecutionHandler);
    }

    /**
     * Private constructor used by {@link Builder} - cannot be accessed from outside.
     * <p>
     *
     * @param coreThreads              the threads that must be running concurrently.
     * @param maximumPoolSize          the maximum threads that can be running concurrently.
     * @param keepAliveTime            the keep alive time for idle threads to wait for running
     *                                 ones.
     * @param timeUnit                 the time unit that sets up the {@code keepAliveTime}.
     * @param workingThreadsQueue      the queue of the threads that will be executed.
     * @param factory                  the factory used for creating new threads.
     * @param rejectedExecutionHandler the handler used when a new thread is rejected.
     *
     * @see #ThreadsPooling(int, int, long, TimeUnit, BlockingQueue, RejectedExecutionHandler)
     * @see ThreadPoolExecutor#ThreadPoolExecutor(int, int, long, TimeUnit, BlockingQueue,
     * ThreadFactory, RejectedExecutionHandler)
     */
    private ThreadsPooling(int coreThreads, int maximumPoolSize, long keepAliveTime,
                           TimeUnit timeUnit, BlockingQueue<Runnable> workingThreadsQueue,
                           ThreadFactory factory,
                           RejectedExecutionHandler rejectedExecutionHandler) {
        mWorkingThreadsQueue = workingThreadsQueue;
        mPoolExecutor = new ThreadPoolExecutor(coreThreads,
                maximumPoolSize,
                keepAliveTime,
                timeUnit,
                workingThreadsQueue,
                factory,
                rejectedExecutionHandler);
    }

    /**
     * Access the {@link Builder} class via {@link ThreadsPooling} for generating a new instance of
     * that class.
     *
     * @return {@code Builder} object.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Adds a new {@code Runnable} to the queue of threads. If there is any error/exception ({@link
     * IllegalStateException}, {@link ClassCastException}, {@link NullPointerException}, {@link
     * IllegalArgumentException}), then {@link RejectedExecutionHandler#rejectedExecution(Runnable,
     * ThreadPoolExecutor)} is called.
     *
     * @param thread the new thread to include for execution.
     */
    public void add(@NotNull Runnable thread) {
        try {
            mWorkingThreadsQueue.add(thread);
        } catch (IllegalStateException | ClassCastException | NullPointerException |
                IllegalArgumentException ignored) {
            getRejectedExecutionHandler().rejectedExecution(thread, mPoolExecutor);
        }
    }

    /**
     * Adds new {@code Runnable}s to the queue of threads. If there is any error/exception ({@link
     * IllegalStateException}, {@link ClassCastException}, {@link NullPointerException}, {@link
     * IllegalArgumentException}), then {@link RejectedExecutionHandler#rejectedExecution(Runnable,
     * ThreadPoolExecutor)} is called, with the correspondent thread that cannot be included inside
     * the {@code array} ot threads.
     *
     * @param threads the new threads to include for execution.
     */
    public void add(@NotNull Runnable... threads) {
        int sizeBeforeAddingTheElements = mWorkingThreadsQueue.size();
        int remainingCapacity = mWorkingThreadsQueue.remainingCapacity();
        try {
            mWorkingThreadsQueue.addAll(Arrays.asList(threads));
        } catch (IllegalStateException | ClassCastException | NullPointerException |
                IllegalArgumentException | UnsupportedOperationException ignored) {
            int threadNotAdded = remainingCapacity - sizeBeforeAddingTheElements;
            if (threadNotAdded >= threads.length)
                threadNotAdded = 0;
            getRejectedExecutionHandler().rejectedExecution(threads[threadNotAdded], mPoolExecutor);
        }
    }

    /**
     * Starts running the threads included inside the {@linkplain #mWorkingThreadsQueue queue}, by
     * calling {@link ThreadPoolExecutor#prestartAllCoreThreads()}.
     * <p>
     * By default, it starts only the specified {@code core threads}.
     *
     * @return {@code int} with the amount of threads started.
     */
    public int start() {
        return mPoolExecutor.prestartAllCoreThreads();
    }

    /**
     * Tries to shutdown the {@link ThreadPoolExecutor} waiting until all the threads have finished,
     * waiting {@code 100 ms} until stopping abruptly.
     *
     * @return {@code true} if all threads finished normally, {@code false} if they were interrupted
     * by this method.
     *
     * @throws InterruptedException if interrupted while waiting.
     */
    public boolean shutdownWaitTermination() throws InterruptedException {
        return shutdownWaitTermination(DEFAULT_KEEP_ALIVE, DEFAULT_TIME_UNIT);
    }

    /**
     * Tries to shutdown the {@link ThreadPoolExecutor} waiting until all the threads have finished,
     * waiting {@code timeout waitingUnit} until stopping abruptly.
     * <p>
     * If {@code timeout} is set to zero, then it will be automatically changed by {@code 100 ms}.
     *
     * @param timeout     waiting time until a thread is interrupted - must be zero or higher.
     * @param waitingUnit the time unit for the timeout waiting.
     *
     * @return {@code true} if all threads finished normally, {@code false} if they were interrupted
     * by this method.
     *
     * @throws InterruptedException if interrupted while waiting.
     */
    public boolean shutdownWaitTermination(long timeout, @NotNull TimeUnit waitingUnit)
            throws InterruptedException {
        if (timeout < 0)
            throw new IllegalArgumentException("Timeout must be zero or higher, not " + timeout);
        if (timeout == 0) {
            timeout = DEFAULT_KEEP_ALIVE;
            waitingUnit = DEFAULT_TIME_UNIT;
        }
        mPoolExecutor.shutdown();
        return mPoolExecutor.awaitTermination(timeout, waitingUnit);
    }

    /**
     * Tries to shutdown the {@link ThreadPoolExecutor}, executing the submitted process but without
     * waiting them to finish.
     * <p>
     * For immediately finish, try using {@link #shutdownImmediately()} instead.
     *
     * @throws SecurityException if a security manager exists and shutting down this ExecutorService
     *                           may manipulate threads that the caller is not permitted to modify
     *                           because it does not hold {@link RuntimePermission
     *                           RuntimePermission("modifyThread")}, or the security manager's
     *                           {@code checkAccess} method denies access.
     */
    public void shutdownNotWaiting() {
        mPoolExecutor.shutdown();
    }

    /**
     * Attempts to stop all actively executing tasks, halts the processing of waiting tasks, and
     * returns a <b>list of the tasks that were awaiting execution</b>. These tasks are drained
     * (removed) from the task queue upon return from this method.
     * <p>
     * This method does not wait for actively executing tasks to terminate. Use awaitTermination to
     * do that.
     * <p>
     * There are no guarantees beyond best-effort attempts to stop processing actively executing
     * tasks. This implementation interrupts tasks via {@link Thread#interrupt() Thread.interrupt};
     * any task that fails to respond to interrupts may never terminate.
     *
     * @return list of tasks that never commenced execution.
     *
     * @throws SecurityException if a security manager exists and shutting down this ExecutorService
     *                           may manipulate threads that the caller is not permitted to modify
     *                           because it does not hold {@link RuntimePermission
     *                           RuntimePermission("modifyThread")}, or the security manager's
     *                           {@code checkAccess} method denies access.
     */
    public List<Runnable> shutdownImmediately() {
        return mPoolExecutor.shutdownNow();
    }

    /**
     * It sets the new amount of threads that can be concurrently running at the same time.
     * <p>
     * If the new value is <b>smaller</b> than the current one, some threads will stop executing
     * when they become idle.
     * <p>
     * If bigger, any pending task will be added for execution with the new limit.
     * <p>
     * If the {@code param} is lower than zero, it returns immediately without doing nothing. If
     * zero, the new amount of concurrent threads will be {@linkplain #DEFAULT_CORE_THREADS the
     * default value}.
     *
     * @param newAmountOfThreadsRunning the normally amount of threads executing at the same time.
     */
    public void updateConcurrentThreadsRunning(int newAmountOfThreadsRunning) {
        if (newAmountOfThreadsRunning < 0)
            return;
        else if (newAmountOfThreadsRunning == 0)
            newAmountOfThreadsRunning = DEFAULT_CORE_THREADS;
        if (newAmountOfThreadsRunning != mPoolExecutor.getCorePoolSize())
            mPoolExecutor.setCorePoolSize(newAmountOfThreadsRunning);
    }

    /**
     * It sets the maximum new amount of threads that can be concurrently running at the same time.
     * <p>
     * If the new value is <b>smaller</b> than the current one, excess existing threads will stop
     * executing when they become idle.
     * <p>
     * If the {@code param} is lower than zero, it returns immediately without doing nothing. If
     * zero, the new amount of concurrent threads will be {@linkplain #DEFAULT_MAX_THREADS the
     * default value}.
     *
     * @param newMaximumActiveThreads the maximum amount of threads executing at the same time.
     */
    public void updateMaximumActiveThreads(int newMaximumActiveThreads) {
        if (newMaximumActiveThreads < 0)
            return;
        else if (newMaximumActiveThreads == 0)
            newMaximumActiveThreads = DEFAULT_MAX_THREADS;
        if (newMaximumActiveThreads != mPoolExecutor.getMaximumPoolSize())
            mPoolExecutor.setMaximumPoolSize(newMaximumActiveThreads);
    }

    /**
     * It updates the default keep alive time by the provided one, using by default {@code
     * milliseconds}.
     * <p>
     * If you want to use a different {@code TimeUnit}, use {@link #updateKeepAliveTime(long,
     * TimeUnit)} instead.
     * <p>
     * If value is lower than zero, returns immediately without doing nothing. If zero, it will be
     * the {@linkplain #DEFAULT_KEEP_ALIVE default value}.
     *
     * @param newKeepAliveTime the new keep alive time.
     */
    public void updateKeepAliveTime(long newKeepAliveTime) {
        updateKeepAliveTime(newKeepAliveTime, DEFAULT_TIME_UNIT);
    }

    /**
     * It updates the default keep alive time by the provided one, using the also included time
     * unit.
     * <p>
     * If value is lower than zero, returns immediately without doing nothing. If zero, it will be
     * the {@linkplain #DEFAULT_KEEP_ALIVE default value}.
     *
     * @param newKeepAliveTime the new keep alive time.
     * @param newTimeUnit      the new time unit for the keep alive time.
     */
    public void updateKeepAliveTime(long newKeepAliveTime, @NotNull TimeUnit newTimeUnit) {
        if (newKeepAliveTime < 0)
            return;
        else if (newKeepAliveTime == 0) {
            newKeepAliveTime = DEFAULT_KEEP_ALIVE;
            newTimeUnit = DEFAULT_TIME_UNIT;
        }
        mPoolExecutor.setKeepAliveTime(newKeepAliveTime, newTimeUnit);
    }

    /**
     * Updates the rejected execution handler. If it is {@code null}, it uses the {@linkplain
     * #DEFAULT_REJECTED_EXECUTION_HANDLER default rejected execution handler}.
     *
     * @param newHandler the new rejected execution handler - use {@code null} for the default
     *                   value.
     */
    public void updateRejectedExecutionHandler(@Nullable RejectedExecutionHandler newHandler) {
        if (newHandler == null)
            mPoolExecutor.setRejectedExecutionHandler(DEFAULT_REJECTED_EXECUTION_HANDLER);
        else
            mPoolExecutor.setRejectedExecutionHandler(newHandler);
    }

    /**
     * Updates the thread factory used when creating threads. Use {@linkplain ThreadFactory default
     * value} if you do not have any {@code ThreadFactory}.
     *
     * @param newThreadFactory the new thread factory.
     */
    public void updateThreadFactory(@NotNull ThreadFactory newThreadFactory) {
        mPoolExecutor.setThreadFactory(newThreadFactory);
    }

    /**
     * Gets the amount of threads that can be running concurrently.
     *
     * @return {@code int} with the amount of threads that can be running concurrently.
     */
    public int getConcurrentThreadsRunning() {
        return mPoolExecutor.getCorePoolSize();
    }

    /**
     * Gets the maximum amount of threads that can be running concurrently.
     *
     * @return {@code int} with the maximum amount of threads that can be running concurrently.
     */
    public int getMaximumThreadsRunning() {
        return mPoolExecutor.getMaximumPoolSize();
    }

    /**
     * Gets approximately the number of threads currently running.
     *
     * @return {@code int} with the approximately number of threads currently running.
     */
    public int getActiveThreadsCount() {
        return mPoolExecutor.getActiveCount();
    }

    /**
     * Gets the keep alive time being used by the {@link ThreadPoolExecutor} in {@code
     * milliseconds}.
     * <p>
     * For a different time unit, use {@link #getKeepAliveTimeWithUnit(TimeUnit)} instead.
     *
     * @return {@code long} with the keep alive time in milliseconds.
     */
    public long getKeepAliveTime() {
        return getKeepAliveTimeWithUnit(DEFAULT_TIME_UNIT);
    }

    /**
     * Gets the keep alive time being used by the {@link ThreadPoolExecutor} in the specified time
     * unit.
     *
     * @param unit the time unit for wrapping keep alive time.
     * @return {@code long} with the keep alive time.
     */
    public long getKeepAliveTimeWithUnit(@NotNull TimeUnit unit) {
        return mPoolExecutor.getKeepAliveTime(unit);
    }

    /**
     * Gets the rejected execution handler used by {@link ThreadPoolExecutor}.
     *
     * @return {@code RejectedExecutionHandler} with the handler.
     */
    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return mPoolExecutor.getRejectedExecutionHandler();
    }

    /**
     * Gets the thread factory used by {@link ThreadPoolExecutor}.
     *
     * @return {@code ThreadFactory} with the factory.
     */
    public ThreadFactory getThreadFactory() {
        return mPoolExecutor.getThreadFactory();
    }

    /**
     * Gets the queue used by {@link ThreadPoolExecutor}.
     *
     * @return {@code BlockingQueue} with the queue.
     */
    public BlockingQueue<Runnable> getWorkingThreadsQueue() {
        return mWorkingThreadsQueue;
    }

    /**
     * Gets the approximately amount of threads that completed its execution.
     *
     * @return {@code long} with the approximately amount of threads that completed its execution.
     */
    public long getCompletedThreadCount() {
        return mPoolExecutor.getCompletedTaskCount();
    }

    /**
     * Gets the maximum amount of threads that have been running concurrently.
     *
     * @return {@code long} with the maximum amount of threads that have been running concurrently.
     */
    public int getLargestActiveThreadsRunning() {
        return mPoolExecutor.getLargestPoolSize();
    }

    /**
     * Returns the approximate total number of tasks that have ever been scheduled for execution.
     *
     * @return {@code long} with the approximate total number of tasks that have ever been scheduled
     * for execution.
     */
    public long getThreadCount() {
        return mPoolExecutor.getTaskCount();
    }

    /**
     * Returns the current number of threads in the pool.
     *
     * @return {@code int} with the current number of threads in the pool.
     */
    public int getPoolSize() {
        return mPoolExecutor.getPoolSize();
    }

    /**
     * Human readable representation of this class.
     *
     * @return {@code String} with the representation.
     */
    @Override
    public String toString() {
        return super.toString() + '{' +
                "ThreadPoolExecutor: " + mPoolExecutor.toString() + ",\n" +
                "BlockingQueue: " + mWorkingThreadsQueue.toString() + ",\n" +
                "RejectedExecutionHandler: " + getRejectedExecutionHandler().toString() + '}';
    }

    /**
     * Static class for generating a new instance of {@link ThreadsPooling}.
     * <p>
     * This class provides multiple methods for defining a new {@link ThreadsPooling} instance
     * completely customizable.
     * <p>
     * In addition, for developer fast production, it uses some default values defined so it is
     * possible to just create a new instance of this class just by running:
     * <pre>
     *     {@code
     *     ThreadsPooling pooling = ThreadsPooling.builder().build();
     *     }
     * </pre>
     * The default values are:
     * <ul>
     * <li>
     * <b>Core threads</b>: {@linkplain #DEFAULT_CORE_THREADS default core threads}.
     * </li>
     * <li>
     * <b>Maximum pool size</b>: {@linkplain #DEFAULT_MAX_THREADS default maximum pool
     * size}.
     * </li>
     * <li>
     * <b>Keep alive time</b>: {@linkplain #DEFAULT_KEEP_ALIVE default keep alive time}.
     * </li>
     * <li>
     * <b>Time unit</b>: {@linkplain #DEFAULT_TIME_UNIT default time unit}.
     * </li>
     * <li>
     * <b>Queue capacity</b>: {@linkplain #DEFAULT_QUEUE_CAPACITY default queue capacity}.
     * </li>
     * <li>
     * <b>Rejected execution handler</b>:
     * {@linkplain #DEFAULT_REJECTED_EXECUTION_HANDLER default rejected execiton handler}.
     * </li>
     * </ul>
     * <p>
     * All the methods here allows the developer the ability to totally configure {@link
     * ThreadPoolExecutor} very easily.
     */
    public static final class Builder {
        private int mCoreThreads;
        private int mMaximumPoolSize;
        private long mKeepAliveTime;
        private TimeUnit mTimeUnit;
        private int mQueueCapacity;
        private BlockingQueue<Runnable> mWorkingThreadsQueue;
        private ThreadFactory mThreadFactory;
        private RejectedExecutionHandler mRejectedExecutionHandler;

        /**
         * Default constructor used at {@link #builder()} - sets all values to its default one.
         */
        private Builder() {
            mCoreThreads = DEFAULT_CORE_THREADS;
            mMaximumPoolSize = DEFAULT_MAX_THREADS;
            mKeepAliveTime = DEFAULT_KEEP_ALIVE;
            mTimeUnit = DEFAULT_TIME_UNIT;
            mQueueCapacity = DEFAULT_QUEUE_CAPACITY;
            mWorkingThreadsQueue = null;
            mThreadFactory = null;
            mRejectedExecutionHandler = null;
        }

        /**
         * Sets the normally amount of concurrent running threads - default is {@linkplain
         * #DEFAULT_CORE_THREADS '4'}.
         *
         * @param concurrentThreadsRunning amount of concurrent running threads.
         *
         * @return {@code Builder} itself.
         *
         * @throws IllegalArgumentException when the {@code param} is lower than zero.
         */
        public Builder withConcurrentThreadsRunning(int concurrentThreadsRunning) {
            if (isParamHigherThanZero(concurrentThreadsRunning)) {
                mCoreThreads = (concurrentThreadsRunning == 0) ?
                        DEFAULT_CORE_THREADS :
                        concurrentThreadsRunning;
                return this;
            } else
                throw illegalArgumentException("Concurrent threads", concurrentThreadsRunning);
        }

        /**
         * Sets the maximum amount of concurrent running threads - default is {@linkplain
         * #DEFAULT_MAX_THREADS '8'}.
         *
         * @param maximumPoolSize amount of maximum concurrent running threads.
         *
         * @return {@code Builder} itself.
         *
         * @throws IllegalArgumentException when the {@code param} is lower than zero.
         */
        public Builder withMaximumPoolSize(int maximumPoolSize) {
            if (isParamHigherThanZero(maximumPoolSize)) {
                mMaximumPoolSize = (maximumPoolSize == 0) ?
                        DEFAULT_MAX_THREADS :
                        maximumPoolSize;
                return this;
            } else
                throw illegalArgumentException("Maximum threads", maximumPoolSize);
        }

        /**
         * Sets the keep alive time, which is the amount of time that threads may remain idle before
         * being terminated.
         *
         * @param keepAliveTime time in seconds.
         *
         * @return {@code Builder} itself.
         *
         * @throws IllegalArgumentException when the {@code param} is lower than zero.
         */
        public Builder withKeepAliveInSeconds(long keepAliveTime) {
            if (isParamHigherThanZero(keepAliveTime)) {
                setKeepAliveTime(keepAliveTime);
                mTimeUnit = TimeUnit.SECONDS;
                return this;
            } else
                throw illegalArgumentException("Keep Alive time", keepAliveTime);
        }

        /**
         * Sets the keep alive time, which is the amount of time that threads may remain idle before
         * being terminated.
         *
         * @param keepAliveTime time in milliseconds.
         *
         * @return {@code Builder} itself.
         *
         * @throws IllegalArgumentException when the {@code param} is lower than zero.
         */
        public Builder withKeepAliveInMilliseconds(long keepAliveTime) {
            if (isParamHigherThanZero(keepAliveTime)) {
                setKeepAliveTime(keepAliveTime);
                mTimeUnit = TimeUnit.MILLISECONDS;
                return this;
            } else
                throw illegalArgumentException("Keep Alive time", keepAliveTime);
        }

        /**
         * Sets the keep alive time, which is the amount of time that threads may remain idle before
         * being terminated.
         *
         * @param keepAliveTime time in microseconds.
         *
         * @return {@code Builder} itself.
         *
         * @throws IllegalArgumentException when the {@code param} is lower than zero.
         */
        public Builder withKeepAliveInMicroseconds(long keepAliveTime) {
            if (isParamHigherThanZero(keepAliveTime)) {
                setKeepAliveTime(keepAliveTime);
                mTimeUnit = TimeUnit.MICROSECONDS;
                return this;
            } else
                throw illegalArgumentException("Keep Alive time", keepAliveTime);
        }

        /**
         * Sets the keep alive time, which is the amount of time that threads may remain idle before
         * being terminated.
         *
         * @param keepAliveTime time in nanoseconds.
         *
         * @return {@code Builder} itself.
         *
         * @throws IllegalArgumentException when the {@code param} is lower than zero.
         */
        public Builder withKeepAliveInNanoseconds(long keepAliveTime) {
            if (isParamHigherThanZero(keepAliveTime)) {
                setKeepAliveTime(keepAliveTime);
                mTimeUnit = TimeUnit.MICROSECONDS;
                return this;
            } else
                throw illegalArgumentException("Keep Alive time", keepAliveTime);
        }

        /**
         * Sets the keep alive time, which is the amount of time that threads may remain idle before
         * being terminated.
         *
         * @param keepAliveTime time in minutes.
         *
         * @return {@code Builder} itself.
         *
         * @throws IllegalArgumentException when the {@code param} is lower than zero.
         */
        public Builder withKeepAliveInMinutes(long keepAliveTime) {
            if (isParamHigherThanZero(keepAliveTime)) {
                setKeepAliveTime(keepAliveTime);
                mTimeUnit = TimeUnit.MINUTES;
                return this;
            } else
                throw illegalArgumentException("Keep Alive time", keepAliveTime);
        }

        /**
         * Sets the keep alive time, which is the amount of time that threads may remain idle before
         * being terminated.
         *
         * @param keepAliveTime time in hours.
         *
         * @return {@code Builder} itself.
         *
         * @throws IllegalArgumentException when the {@code param} is lower than zero.
         */
        public Builder withKeepAliveInHours(long keepAliveTime) {
            if (isParamHigherThanZero(keepAliveTime)) {
                setKeepAliveTime(keepAliveTime);
                mTimeUnit = TimeUnit.HOURS;
                return this;
            } else
                throw illegalArgumentException("Keep Alive time", keepAliveTime);
        }

        /**
         * Sets the keep alive time, which is the amount of time that threads may remain idle before
         * being terminated.
         *
         * @param keepAliveTime time in days.
         *
         * @return {@code Builder} itself.
         *
         * @throws IllegalArgumentException when the {@code param} is lower than zero.
         */
        public Builder withKeepAliveInDays(long keepAliveTime) {
            if (isParamHigherThanZero(keepAliveTime)) {
                setKeepAliveTime(keepAliveTime);
                mTimeUnit = TimeUnit.DAYS;
                return this;
            } else
                throw illegalArgumentException("Keep Alive time", keepAliveTime);
        }

        /**
         * Sets the keep alive time unit - default is {@linkplain #DEFAULT_TIME_UNIT milliseconds}.
         *
         * @param timeUnit keep alive time unit.
         *
         * @return {@code Builder} itself.
         */
        public Builder withTimeUnit(@Nullable TimeUnit timeUnit) {
            mTimeUnit = (timeUnit == null) ?
                    DEFAULT_TIME_UNIT :
                    timeUnit;
            return this;
        }

        /**
         * Sets the queue capacity - default is {@linkplain #DEFAULT_QUEUE_CAPACITY '100'}.
         *
         * @param queueCapacity new queue capacity.
         *
         * @return {@code Builder} itself.
         *
         * @throws IllegalArgumentException when the {@code param} is lower than zero.
         */
        public Builder withQueueCapacity(int queueCapacity) {
            if (isParamHigherThanZero(queueCapacity)) {
                mQueueCapacity = (queueCapacity == 0) ?
                        DEFAULT_QUEUE_CAPACITY :
                        queueCapacity;
                return this;
            } else
                throw illegalArgumentException("Queue capacity", queueCapacity);
        }

        /**
         * Adds a new thread that will be executed by calling {@link ThreadsPooling#start()}
         * method.
         *
         * @param thread thread that will be executed.
         *
         * @return {@code Builder} itself.
         *
         * @throws IllegalStateException if the element cannot be added at this time due to capacity
         *                               restrictions.
         */
        public Builder withThread(@NotNull Runnable thread) {
            if (mWorkingThreadsQueue == null)
                mWorkingThreadsQueue = new LinkedBlockingQueue<>(mQueueCapacity);
            mWorkingThreadsQueue.add(thread);
            return this;
        }

        /**
         * Adds new threads that will be executed by calling {@link ThreadsPooling#start()} method.
         *
         * @param threads thread that will be executed.
         *
         * @return {@code Builder} itself.
         *
         * @throws IllegalStateException if the element cannot be added at this time due to capacity
         *                               restrictions.
         */
        public Builder withThreads(@NotNull Runnable... threads) {
            if (mWorkingThreadsQueue == null)
                mWorkingThreadsQueue = new LinkedBlockingQueue<>(mQueueCapacity);
            mWorkingThreadsQueue.addAll(Arrays.asList(threads));
            return this;
        }

        /**
         * Sets the thread factory that will be used for creating threads - can be {@code null}.
         *
         * @param threadFactory thread factory that will be used.
         *
         * @return {@code Builder} itself.
         */
        public Builder withThreadFactory(@Nullable ThreadFactory threadFactory) {
            mThreadFactory = threadFactory;
            return this;
        }

        /**
         * Sets the rejected execution handler that will be used when a new thread cannot be
         * included inside the threads queue (due to capacity restrictions) - can be {@code null} ,
         * which will be {@linkplain #DEFAULT_REJECTED_EXECUTION_HANDLER the default rejected
         * handler}.
         *
         * @param handler rejected execution handler.
         *
         * @return {@code Builder} itself.
         */
        public Builder withRejectedExecutionHandler(@Nullable RejectedExecutionHandler handler) {
            mRejectedExecutionHandler = handler;
            return this;
        }

        /**
         * Sets the rejected execution handler that will be used when a new thread cannot be
         * included inside the threads queue (due to capacity restrictions) that does nothing.
         *
         * @return {@code Builder} itself.
         */
        public Builder withNoActionOnTaskRejected() {
            mRejectedExecutionHandler = NO_ACTION_ON_REJECTED_HANDLER;
            return this;
        }

        /**
         * Sets the rejected execution handler that will be used when a new thread cannot be
         * included inside the threads queue (due to capacity restrictions) that immediately runs
         * the rejected thread.
         *
         * @return {@code Builder} itself.
         */
        public Builder withImmediatelyRunRejectedTask() {
            mRejectedExecutionHandler = IMMEDIATELY_RUN_ON_REJECTED_HANDLER;
            return this;
        }

        /**
         * Sets the rejected execution handler that will be used when a new thread cannot be
         * included inside the threads queue (due to capacity restrictions) that runs the rejected
         * thread after requesting the shutdown of the executor.
         *
         * @return {@code Builder} itself.
         */
        public Builder withRunningTaskWhenAllCompleted() {
            mRejectedExecutionHandler = WAIT_SHUTDOWN_RUN_TASK_ON_REJECTED_HANDLER;
            return this;
        }

        /**
         * Generates a new {@link ThreadsPooling} instance by using the provided arguments with the
         * available constructors at that class.
         *
         * @return {@code ThreadsPooling} new instance.
         */
        public ThreadsPooling build() {
            if (mWorkingThreadsQueue == null)
                mWorkingThreadsQueue = new LinkedBlockingQueue<>(mQueueCapacity);
            if (mThreadFactory == null && mRejectedExecutionHandler == null)
                return new ThreadsPooling(mCoreThreads,
                        mMaximumPoolSize,
                        mKeepAliveTime,
                        mTimeUnit,
                        mWorkingThreadsQueue);
            else if (mThreadFactory != null && mRejectedExecutionHandler == null)
                return new ThreadsPooling(mCoreThreads,
                        mMaximumPoolSize,
                        mKeepAliveTime,
                        mTimeUnit,
                        mWorkingThreadsQueue,
                        mThreadFactory);
            else if (mThreadFactory == null && mRejectedExecutionHandler != null)
                return new ThreadsPooling(mCoreThreads,
                        mMaximumPoolSize,
                        mKeepAliveTime,
                        mTimeUnit,
                        mWorkingThreadsQueue,
                        mRejectedExecutionHandler);
            else
                return new ThreadsPooling(mCoreThreads,
                        mMaximumPoolSize,
                        mKeepAliveTime,
                        mTimeUnit,
                        mWorkingThreadsQueue,
                        mThreadFactory,
                        mRejectedExecutionHandler);
        }

        /**
         * Checks whether the param is higher or equal than zero.
         *
         * @param paramToCheck value to check.
         *
         * @return {@code true} if zero or higher, else {@code false}.
         */
        private boolean isParamHigherThanZero(long paramToCheck) {
            return paramToCheck >= 0;
        }

        /**
         * Sets the keep alive time to the default value or the given one if zero or different.
         *
         * @param keepAliveTime possible keep alive time to set-up.
         */
        private void setKeepAliveTime(long keepAliveTime) {
            mKeepAliveTime = (keepAliveTime == 0) ?
                    DEFAULT_KEEP_ALIVE :
                    keepAliveTime;
        }

        /**
         * Generates an {@link IllegalArgumentException} by replacing the spaces by the given
         * values.
         *
         * @param valueName    name of the value that is not correct.
         * @param valueContent value that is not correct.
         *
         * @return {@code IllegalArgumentException} configured.
         */
        private IllegalArgumentException illegalArgumentException(String valueName,
                                                                  long valueContent) {
            return new IllegalArgumentException(String.format("%s must be zero or" +
                    " higher, not '%d'", valueName, valueContent));
        }
    }
}
