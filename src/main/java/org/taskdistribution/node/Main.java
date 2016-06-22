package org.taskdistribution.node;

import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jgroups.*;
import org.jgroups.blocks.AsyncRequestHandler;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.Response;
import org.jgroups.util.Util;
import org.taskdistribution.ClusterID;
import org.taskdistribution.Request;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author stliu at apache.org
 * @since 6/17/16
 */
@Slf4j
public class Main {
    public static void main(String[] args) throws Exception {
        JChannel channel = new JChannel("udp.xml").name("node-" + Math.abs(new Random().nextInt()));
        ClusterViewReceiver viewReceiver = new ClusterViewReceiver(channel);
//        channel.setReceiver(viewReceiver);
        channel.connect("dzone-demo");


        MessageDispatcher messageDispatcher = new MessageDispatcher(channel, null, viewReceiver, new StartRequestHandler());
//        messageDispatcher.castMessage()
//        for (int i = 0; i < 10000; i++) {
//            String m =System.currentTimeMillis() + "hello from ["+ channel.getAddressAsString()+"] ";
//
//            channel.send(new Message(null,m.getBytes()));
//            TimeUnit.SECONDS.sleep(1);
//        }
    }

    public static class StartRequestHandler implements RequestHandler{
        @Override
        public Object handle(Message msg) throws Exception {
            return null;
        }
    }

    public static class ClusterViewReceiver extends ReceiverAdapter {
        private final Channel channel;
        private NodeInfo nodeInfo;

        public ClusterViewReceiver(Channel channel) {
            this.channel = channel;
        }

        @Override
        public void receive(Message msg) {
            Address senderAddress = msg.getSrc();
            try {
                log.info("received message from {} to {} with message {}", senderAddress, msg.dest(), new String( msg.getBuffer()));

//                Request req = (Request) Util.streamableFromByteBuffer(Request.class, msg.getRawBuffer(), msg.getOffset(), msg.getLength());
//                log.info("received {}", req);
//                ClusterID senderClusterId = req.getId();
//                handler(senderAddress, req, senderClusterId);
            } catch (Exception e) {
                log.error("exception receiving message from " + senderAddress, e);
            }
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
                Address[][] diffMembers = View.diff(nodeInfo.getView(), newView);
                Address[] joinedMembers = diffMembers[0];
                Address[] leftMembers = diffMembers[1];
                log.info("members left: {}", leftMembers);
                log.info("members joined: {}", joinedMembers);

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
