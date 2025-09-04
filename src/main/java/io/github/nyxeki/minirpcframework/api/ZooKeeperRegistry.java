package io.github.nyxeki.minirpcframework.api;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ZooKeeperRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperRegistry.class);

    private static final String ZK_CONNECTION_STRING = "localhost:2181";

    private static final String ZK_ROOT_PATH = "/rpc";

    private final CuratorFramework zkClient;

    // local cache for server address.
    private final Map<String, List<String>> serviceAddressCache = new ConcurrentHashMap<>();


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
            throw new RuntimeException("Failed to register service at " + serviceAddress, e);
        }
    }

    public List<String> discoverService(String serviceName) {

        // first, try to get the service list from the local cache.
        List<String> cachedInstances = serviceAddressCache.get(serviceName);
        if (cachedInstances != null && !cachedInstances.isEmpty()) {
            logger.info("Discovered {} instances for service {}. Caching and watching.", cachedInstances.size(), serviceName);
            return cachedInstances;
        }

        try {
            String servicePath = ZK_ROOT_PATH + "/" + serviceName;
            List<String> instances = zkClient.getChildren().forPath(servicePath);
            if (instances == null || instances.isEmpty()) {
                logger.warn("No available instances for service: {}", serviceName);
                return null;
            }
            logger.info("Discovered {} instances for service {}. Caching and watching.", instances.size(), serviceName);
            serviceAddressCache.put(serviceName, instances);
            registerWatcher(servicePath, serviceName);
            return instances;
        } catch (Exception e) {
            logger.error("Failed to discover service", e);
            throw new RuntimeException(e);
        }
    }

    private void registerWatcher(String servicePath, String serviceName) throws Exception {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);

        // Register a listener for the cache.
        pathChildrenCache.getListenable().addListener((client, event) -> {
            logger.info("Child node event received: {}", event.getType());

            if (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED ||
                    event.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED) {

                logger.info("Service {} has changed. Re-fetching service list...", serviceName);

                // Re-fetch the latest list of children (service addresses).
                List<String> latestInstances = client.getChildren().forPath(servicePath);

                // Update the local cache.
                serviceAddressCache.put(serviceName, latestInstances);
                logger.info("Service {} cache updated with instances: {}", serviceName, latestInstances);
            }
        });
        pathChildrenCache.start();
    }
}
