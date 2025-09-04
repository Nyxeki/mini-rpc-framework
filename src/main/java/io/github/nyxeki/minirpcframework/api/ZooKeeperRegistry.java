package io.github.nyxeki.minirpcframework.api;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ZooKeeperRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperRegistry.class);

    private static final String ZK_CONNECTION_STRING = "localhost:2181";

    private static final String ZK_ROOT_PATH = "/rpc";

    private final CuratorFramework zkClient;

    public ZooKeeperRegistry() {
        this.zkClient = CuratorFrameworkFactory.builder()
                .connectString(ZK_CONNECTION_STRING)
                .sessionTimeoutMs(25000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        this.zkClient.start();
        logger.info("ZooKeeper client started.");
    }

    public void registerService(String serviceName, String serviceAddress) {
        try {
            if (zkClient.checkExists().forPath(ZK_ROOT_PATH) == null) {
                zkClient.create().creatingParentsIfNeeded().forPath(ZK_ROOT_PATH);
            }

            String servicePath = ZK_ROOT_PATH + "/" + serviceName;
            if (zkClient.checkExists().forPath(servicePath) == null) {
                zkClient.create().forPath(servicePath);
            }

            String serviceInstancePath = servicePath + "/" + serviceAddress;
            zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(serviceInstancePath);
            logger.info("Successfully registered service {} at {}", serviceName, serviceInstancePath);

        } catch (Exception e) {
            logger.error("Failed to register service", e);
        }
    }

    public List<String> discoverService(String serviceName) {
        try {
            String servicePath = ZK_ROOT_PATH + "/" + serviceName;
            List<String> instances = zkClient.getChildren().forPath(servicePath);
            if (instances == null || instances.isEmpty()) {
                throw new RuntimeException("No available instances for service: " + serviceName);
            }
            logger.info("Discovered service {} at {}", serviceName, instances);
            return instances;
        } catch (Exception e) {
            logger.error("Failed to discover service", e);
            throw new RuntimeException(e);
        }
    }
}
