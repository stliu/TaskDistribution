package org.taskdistribution.tasks;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * @author stliu at apache.org
 * @since 6/17/16
 */
public interface TaskExecutor {
    <T> CompletableFuture<T> submit(Callable<T> callable);
}
