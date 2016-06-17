package org.taskdistribution;

import lombok.extern.slf4j.Slf4j;
import org.jgroups.Address;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author stliu at apache.org
 * @since 6/17/16
 */
@Slf4j
public class TaskManager implements Closeable{
    public final ExecutorService threadPool = Executors.newCachedThreadPool();


    public void execute(ClusterID id, Address sender, Task task) {
//        log.info("handle execute request id: {} from sender {}", id, sender);
//        CompletableFuture.supplyAsync(new Handler(id, sender, task), threadPool)
//                .thenAccept(server.getChannelMessageSender());
    }

    @Override
    public void close() throws IOException {
        threadPool.shutdown();
    }
}
