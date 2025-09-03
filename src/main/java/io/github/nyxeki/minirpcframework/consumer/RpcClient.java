package io.github.nyxeki.minirpcframework.consumer;

import io.github.nyxeki.minirpcframework.api.HelloService;
import io.github.nyxeki.minirpcframework.provider.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    public static void main(String[] args) {

        RpcClientProxy rpcClientProxy = new RpcClientProxy();

        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);

        String result = helloService.sayHello("world");

        logger.info("Response from server: {}", result);
    }
}
