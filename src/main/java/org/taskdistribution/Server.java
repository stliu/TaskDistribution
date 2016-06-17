package org.taskdistribution;

import lombok.extern.slf4j.Slf4j;
import org.jgroups.Channel;
import org.jgroups.JChannel;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Bela Ban
 *         todo: make submit() non-blocking, e.g. with a CompletableFuture (JDK 8)
 */
@Slf4j
public class Server {
    private String props = "udp.xml";
    private Channel channel;
    /**
     * Used to handle received tasks
     */
    private ClusterNode clusterNode;


    public Server(String props) {
        this.props = props;
    }

    public void start(String name) throws Exception {
        log.info("starting server {}", name);
        channel = new JChannel(props).name(name);
        clusterNode = new ClusterNode(channel);
        channel.setReceiver(clusterNode);
        channel.connect("dzone-demo");
        log.info("server {} started", name);
    }

    public void stop() throws IOException {
        clusterNode.close();
        channel.close();
    }

    public String info() {
        StringBuilder sb = new StringBuilder();
        sb.append("local_addr=" + channel.getAddress() + "\nview=" + clusterNode.getView()).append("\n");
        sb.append("rank=" + clusterNode.getRank() + "\n");
        sb.append("(" + clusterNode.getTasks().size() + " entries in tasks cache)");
        return sb.toString();
    }

    public Object submit(Task task, long timeout) throws Exception {
        return clusterNode.submit(task, timeout);
    }


}
