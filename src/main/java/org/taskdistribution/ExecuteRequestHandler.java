package org.taskdistribution;

import lombok.extern.slf4j.Slf4j;
import org.jgroups.Address;
import org.jgroups.blocks.*;

import java.util.concurrent.*;
import java.util.function.Predicate;

/**
 * @author stliu at apache.org
 * @since 6/22/16
 */
@Slf4j
public class ExecuteRequestHandler implements RequestHandler {
    private final Predicate<ClusterID> predicate ;

    private final ChannelMessageSender channelMessageSender;
    private final ConcurrentMap<ClusterID, TaskEntry> tasks = new ConcurrentHashMap<>();

    public ExecuteRequestHandler(ChannelMessageSender channelMessageSender, Predicate<ClusterID> predicate) {
        this.channelMessageSender = channelMessageSender;
        this.predicate = predicate;
    }

    public final ExecutorService threadPool = Executors.newCachedThreadPool();

    @Override
    public RequestType getSupportedRequestType() {
        return RequestType.EXECUTE;
    }

    @Override
    public void handle(Address senderAddress, Request req) {
        /**
         * 所有的节点都会收到
         */
        ClusterID senderClusterId = req.getId();
        TaskEntry taskEntry = new TaskEntry(req.getTask(), senderAddress);
        log.info("put {} into tasks", senderClusterId);
        tasks.putIfAbsent(senderClusterId, taskEntry);
        handleExecute(senderClusterId, taskEntry);


    }


    private void handleExecute(ClusterID clusterID, TaskEntry taskEntry) {
        Address sender = taskEntry.getSubmitter();
        if (predicate.test(clusterID)) {
            log.info("handle execute request id: {} from sender {}", clusterID, sender);
            CompletableFuture.supplyAsync(new Handler(clusterID, sender, taskEntry.getTask()), threadPool)
                    .thenAccept(channelMessageSender);
        }
    }

}
