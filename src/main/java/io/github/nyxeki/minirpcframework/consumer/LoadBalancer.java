package io.github.nyxeki.minirpcframework.consumer;

import java.util.List;

/**
 * Common interface for load balancers.
 */
public interface LoadBalancer {
    /**
     * Selects one service address from a list of available instances.
     *
     * @param instances the list of available service addresses.
     * @return the selected service address.
     */
    String select(List<String> instances);
}
