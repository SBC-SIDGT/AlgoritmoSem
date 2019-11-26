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
 * Created by Javinator9889 on 16/11/2018 - ThreadingTools.
 */
package com.github.javinator9889.threading.pools.rejectedhandlers;

import com.github.javinator9889.threading.errors.NoRejectedHandlerError;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Default rejected execution handler that always throws {@link NoRejectedHandlerError}, with a
 * message containing useful information:
 * <ul>
 * <li>
 * <b>Queue size</b> for explaining why it has been rejected.
 * </li>
 * <li>
 * <b>Executor information</b>, a describer of {@link ThreadPoolExecutor} which calls
 * {@linkplain ThreadPoolExecutor#toString() the String method} of that class.
 * </li>
 * <li>
 * <b>Thread information</b>, a describer of {@link Runnable} which calls
 * {@linkplain Object#toString() the String method} of that class.
 * </li>
 * </ul>
 * <p>
 * Defined at {@link com.github.javinator9889.threading.pools.ThreadsPooling#DEFAULT_REJECTED_EXECUTION_HANDLER}.
 */
public class DefaultRejectedExecutionHandler implements RejectedExecutionHandler {
    public DefaultRejectedExecutionHandler() {
    }

    /**
     * Method that may be invoked by a {@link ThreadPoolExecutor} when {@link
     * ThreadPoolExecutor#execute execute} cannot accept a task.  This may occur when no more
     * threads or queue slots are available because their bounds would be exceeded, or upon shutdown
     * of the Executor.
     *
     * <p>In the absence of other alternatives, the method may throw
     * an unchecked {@link RejectedExecutionException}, which will be propagated to the caller of
     * {@code execute}.
     *
     * @param thread   the runnable task requested to be executed
     * @param executor the executor attempting to execute this task
     *
     * @throws RejectedExecutionException always
     * @throws NoRejectedHandlerError     always - custom exception that is being thrown which
     *                                    inherits from {@link RejectedExecutionException}.
     * @see NoRejectedHandlerError
     */
    @Override
    public void rejectedExecution(Runnable thread, ThreadPoolExecutor executor) {
        int queueSize = executor.getQueue().size();
        String executorDefinition = executor.toString();
        String rejectedThreadInformation = thread.toString();
        String exceptionMessage = String.format("There is no default RejectedExecutionHandler " +
                        "defined - the default RejectedExecutionHandler is being used " +
                        "(DefaultRejectedExecutionHandler)\n" +
                        "\n" +
                        "RejectedExecutionException at " +
                        "java.util.concurrent.RejectedExecutionException: attempting to add a " +
                        "new Runnable for executing when queue is completely filled\n" +
                        "\tQueue size: %d\n" +
                        "\tRunnable tried to execute: %s\n" +
                        "\tExecutor status: \n%s\n" +
                        "\n" +
                        "Please, define your own RejectedExecutionHandler by creating a class that " +
                        "implements java.util.concurrent.RejectedExecutionHandler",
                queueSize,
                rejectedThreadInformation,
                executorDefinition);
        throw new NoRejectedHandlerError(exceptionMessage);
    }
}
