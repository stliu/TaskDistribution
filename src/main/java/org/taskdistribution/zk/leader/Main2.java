package org.taskdistribution.zk.leader;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.AfterConnectionEstablished;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author stliu at apache.org
 * @since 6/22/16
 */
public class Main2 {
    private static final int CLIENT_QTY = 10;

    private static final String PATH = "/gateway/node_";

    public static void main(String[] args) throws Exception {
        String zookeeperConnectionString = "127.0.0.1:2181";

        int clientSize = 10;
        CountDownLatch countDownLatch = new CountDownLatch(clientSize);
        ExecutorService executorService = Executors.newFixedThreadPool(clientSize);
        for (int i = 0; i < clientSize; i++) {
            executorService.submit(() -> {
                RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
                CuratorFramework client = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy);
                try {

                    client.start();

                    client.blockUntilConnected();
                    String path = client.create().creatingParentsIfNeeded().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(PATH);

                    String subPath = path.substring(PATH.length());
                    System.out.println(Thread.currentThread().getName() + " created path " + path+"---"+subPath);
                    Thread.sleep(10000);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    client.close();
                    countDownLatch.countDown();
                }
            });
        }


        countDownLatch.await();
        executorService.shutdown();


    }





}
