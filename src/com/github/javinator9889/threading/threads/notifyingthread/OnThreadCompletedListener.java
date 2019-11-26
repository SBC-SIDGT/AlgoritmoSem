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
 * Created by Javinator9889 on 17/11/2018 - ThreadingTools.
 */

package com.github.javinator9889.threading.threads.notifyingthread;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface that classes that want to get notified by {@link NotifyingThread} must implement.
 * <p>
 * {@code OnThreadCompletedListener} provides an <b>easy access method</b> for classes to get
 * notified when a {@link NotifyingThread} finishes.
 * <p>
 * This <b>listener</b> is useful for both:
 * <ul>
 * <li>
 * <b>Handling finished threads</b>, by using the {@code Runnable} param included
 * <b>always</b> with this method call. That {@code Runnable} will have the just finished
 * thread, and cannot be {@code null} (refer to {@link NotifyingThread#run()} for more
 * information).
 * </li>
 * <li>
 * <b>Managing exceptions</b>: by default, {@link NotifyingThread} manages the
 * <b>unhandled exceptions</b> by itself, when creating a new instance, assigning the
 * {@linkplain NotifyingThread#setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler) method
 * call} to itself, for managing and including the exception when <b>it happens while executing</b>
 * the thread. Refer to {@link NotifyingThread#uncaughtException(Thread, Throwable)} for more
 * information.
 * </li>
 * </ul>
 * <p>
 * At any time you can <b>override</b> the {@linkplain NotifyingThread#setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler)
 * uncaught exception handler} for setting up a custom behaviour, knowing that, when an exception
 * occurs, {@link #onThreadCompletedListener(Thread, Throwable)} will not be called.
 */
public interface OnThreadCompletedListener {
    /**
     * When a thread finish its execution, if using a {@link NotifyingThread} and the class is
     * subscribed, this method is called, with the {@code Runnable} which corresponds the just
     * finished thread, and the {@code Throwable} containing the exception (if any exception has
     * benn thrown).
     * <p>
     * Refer to {@link NotifyingThread#addOnThreadCompletedListener(OnThreadCompletedListener)} for
     * getting more information about subscribing classes.
     *  @param thread    the thread that has just finished its execution.
     * @param exception the exception if happened, else {@code null}.
     */
    void onThreadCompletedListener(@NotNull final Thread thread, @Nullable Throwable exception);
}
