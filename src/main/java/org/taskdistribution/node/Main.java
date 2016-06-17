package org.taskdistribution.node;

import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jgroups.*;

import java.util.List;
import java.util.Random;

/**
 * @author stliu at apache.org
 * @since 6/17/16
 */
@Slf4j
public class Main {
    public static void main(String[] args) throws Exception {
        JChannel channel = new JChannel("udp.xml").name("node-" + Math.abs(new Random().nextInt()));
        channel.setReceiver(new ClusterViewReceiver(channel));
        channel.connect("dzone-demo");
    }

    public static class ClusterViewReceiver extends ReceiverAdapter {
        private final Channel channel;
        private NodeInfo nodeInfo;

        public ClusterViewReceiver(Channel channel) {
            this.channel = channel;
        }

        @Override
        public void viewAccepted(View newView) {
            log.info("newView accepted: {}", newView);
            Address address = channel.getAddress();
            NodeInfo newNodeInfo = NodeInfo.builder()
                    .address(address)
                    .clusterSize(newView.size())
                    .view(newView)
                    .index(getIndexFromView(newView, address))
                    .build();
            if (nodeInfo != null) {
                //说明这不是刚刚启动的时候, 而是有节点的增加或减少
                List<Address> leftMembers = View.leftMembers(nodeInfo.getView(), newView);
                log.info("members left: {}", leftMembers);

                if (nodeInfo.getIndex() != newNodeInfo.getIndex()) {
                    log.info("my index changed from {} to {}", nodeInfo.getIndex(), newNodeInfo.getIndex());
                }
            }
            this.nodeInfo = newNodeInfo;
            log.info("node is {}", nodeInfo);
        }

        private static int getIndexFromView(View view, Address localAddress) {
            List<Address> mbrs = view.getMembers();
            for (int i = 0; i < mbrs.size(); i++) {
                Address tmp = mbrs.get(i);
                if (tmp.equals(localAddress)) {
                    return i;
                } else {
                    log.info("local address is {} and current address is {}", localAddress, tmp);
                }
            }
            throw new IllegalStateException("could not found local address " + localAddress + "from view " + view);
        }
    }

    @Value
    @Builder
    public static class NodeInfo {
        private Address address;
        private int clusterSize;
        private View view;
        private int index;
    }
}
