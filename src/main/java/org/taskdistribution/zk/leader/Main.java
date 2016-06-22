package org.taskdistribution.zk.leader;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.CountDownLatch;

/**
 * @author stliu at apache.org
 * @since 6/22/16
 */
public class Main {
    public static void main(String[] args) throws Exception {
        String zookeeperConnectionString = "127.0.0.1:2181";
        String gatewayLeaderPath = "/no";

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy);
        client.start();


        final CountDownLatch countDownLatch = new CountDownLatch(1);

//        Executors.newSingleThreadExecutor().submit(() -> {
        LeaderSelectorListener listener = new LeaderSelectorListenerAdapter() {
            public void takeLeadership(CuratorFramework client) throws Exception {
                // this callback will get called when you are the leader
                // do whatever leader work you need to and only exit
                // this method when you want to relinquish leadership

                System.out.println("===========" );
//                countDownLatch.countDown();
            }
        };

        LeaderSelector selector = new LeaderSelector(client, gatewayLeaderPath, listener);
//        selector.autoRequeue();  // not required, but this is behavior that you will probably expect
        selector.start();
//        });
        countDownLatch.await();
    }
}
