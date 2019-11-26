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

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Handler that does nothing when a thread is rejected - this handler is not recommended to be used
 * as you will lose information (threads that are not execute will disappear).
 * <p>
 * Defined at {@link com.github.javinator9889.threading.pools.ThreadsPooling#NO_ACTION_ON_REJECTED_HANDLER}.
 */
public class NoRejectedExecutionHandler implements RejectedExecutionHandler {
    public NoRejectedExecutionHandler() {
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
     * @param r        the runnable task requested to be executed
     * @param executor the executor attempting to execute this task
     */
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        return;
    }
}
