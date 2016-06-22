package org.taskdistribution;

import lombok.extern.slf4j.Slf4j;
import org.jgroups.*;
import org.jgroups.util.Util;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Predicate;

/**
 * @author stliu at apache.org
 * @since 6/17/16
 */
@Slf4j
public class ClusterNode extends ReceiverAdapter implements Closeable {
    private View view;
    private int clusterSize;
    private int rank;

    //    private final Server server;
    private Channel channel;
    private Address localAddress;
    private final PartitionMatchPredicate predicate = new PartitionMatchPredicate();
    private final ChannelMessageSender channelMessageSender;
    public final ExecutorService threadPool = Executors.newCachedThreadPool();

    public ClusterNode(Channel channel) {
        this.channel = channel;
        this.channelMessageSender = new ChannelMessageSender(channel);
        this.localAddress = channel.getAddress();

    }

    public int getRank() {
        return rank;
    }

    public ConcurrentMap<ClusterID, TaskEntry> getTasks() {
        return tasks;
    }


    public View getView() {
        return view;
    }

    /**
     * Maps task IDs to Tasks
     */
    private final ConcurrentMap<ClusterID, TaskEntry> tasks = new ConcurrentHashMap<>();

    @Override
    public void viewAccepted(View newView) {
        log.info("newView accepted: {}", newView);
        this.localAddress = channel.getAddress();
        List<Address> leftMembers = View.leftMembers(view, newView);
        this.view = newView;
        clusterSize = newView.size();
        List<Address> mbrs = newView.getMembers();
        int oldRank = rank;
        log.info("addresses {}", mbrs);
        for (int i = 0; i < mbrs.size(); i++) {
            Address tmp = mbrs.get(i);
            if (tmp.equals(localAddress)) {
                rank = i;
                break;
            } else {
                log.info("local address is {} and current address is {}", localAddress, tmp);
            }
        }
        if (oldRank == -1 || oldRank != rank) {
            log.info("my old rank is {} and new rank is {} ", oldRank, rank);
        }

        /**
         * Take over the tasks previously assigned to this member *if* the ID matches my (new rank)
         */

        // process tasks by left members
        if (leftMembers != null && !leftMembers.isEmpty()) {
            log.info("member {} has left", leftMembers);
            for (Map.Entry<ClusterID, TaskEntry> entry : tasks.entrySet()) {
                handleExecute(entry.getKey(), entry.getValue());
            }
        }
    }


    private void handleExecute(ClusterID clusterID, TaskEntry taskEntry) {
        Address sender = taskEntry.getSubmitter();
        if (predicate.test(clusterID)) {
            log.info("handle execute request id: {} from sender {}", clusterID, sender);
            CompletableFuture.supplyAsync(new Handler(clusterID, sender, taskEntry.getTask()), threadPool)
                    .thenAccept(channelMessageSender);
        }
    }


    /**
     * All we receive is Requests
     */
    @Override
    public void receive(Message msg) {
        Address senderAddress = msg.getSrc();
        try {
            log.info("received message from {} to {}", senderAddress, msg.dest());
            Request req = (Request) Util.streamableFromByteBuffer(Request.class, msg.getRawBuffer(), msg.getOffset(), msg.getLength());
            log.info("received {}", req);
            handler(senderAddress, req);
        } catch (Exception e) {
            log.error("exception receiving message from " + senderAddress, e);
        }
    }

    private void handler(Address senderAddress, Request req) {
        ClusterID senderClusterId = req.getId();
        switch (req.getRequestType()) {
            case EXECUTE:
                /**
                 * 所有的节点都会收到
                 */
                TaskEntry taskEntry = new TaskEntry(req.getTask(), senderAddress);
                log.info("put {} into tasks", senderClusterId);
                tasks.putIfAbsent(senderClusterId, taskEntry);
                handleExecute(senderClusterId, taskEntry);
                break;
            case RESULT:
                log.info("got result, trying to get task entry from tasks with key {}", senderClusterId);
                TaskEntry entry = tasks.get(senderClusterId);
                if (entry == null) {
                    log.error("found no entry for request " + senderClusterId);
                } else {
                    Object result = req.getResult();
                    entry.getPromise().setResult(result);
                }
                Request removeReq = new Request(RequestType.REMOVE, null, senderClusterId, null);
                sendRequest(removeReq);
                break;
            case REMOVE:
                tasks.remove(senderClusterId);
                break;
            default:
                throw new IllegalArgumentException("type " + req.getRequestType() + " is not recognized");
        }
    }

    public Object submit(Task task, long timeout) throws Exception {
        ClusterID id = ClusterID.create(localAddress);


        TaskEntry entry = new TaskEntry(task, localAddress);
        tasks.putIfAbsent(id, entry);


        Request req = new Request(RequestType.EXECUTE, task, id, null);
        log.info("==> submitting task {} from node {}", task, id);
        sendRequest(req);
        // wait on entry for result

        Object result = entry.getPromise().getResultWithTimeout(timeout);
        log.info("<== got task {} result [{}] ", task, result);
        return result;
    }

    private void sendRequest(Request req) {
        channelMessageSender.accept(new ChannelRequestMessageMapper().apply(req));
    }

    @Override
    public void close() throws IOException {
        threadPool.shutdown();
    }


    /**
     * 判断一个task是否需要在本节点上执行, 可以后续按照strategy模式, 把这个挪出去
     */
    private class PartitionMatchPredicate implements Predicate<ClusterID> {
        @Override
        public boolean test(ClusterID clusterID) {
            int index = clusterID.getId() % clusterSize;
            if (index != rank) {
                log.info("current cluster size is {}, my index is {}, target index is {}", clusterSize, rank, index);
                return false;
            }
            return true;
        }
    }

}
