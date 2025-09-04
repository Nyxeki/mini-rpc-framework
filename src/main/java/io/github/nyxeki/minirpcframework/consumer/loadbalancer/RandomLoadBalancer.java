package io.github.nyxeki.minirpcframework.consumer.loadbalancer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomLoadBalancer implements LoadBalancer {

    @Override
    public String select(List<String> instances) {
        if (instances == null || instances.isEmpty()) {
            return null;
        }
        // Generate a random index within the bounds of the list size
        int randomIndex = ThreadLocalRandom.current().nextInt(instances.size());
        return instances.get(randomIndex);
    }
}
