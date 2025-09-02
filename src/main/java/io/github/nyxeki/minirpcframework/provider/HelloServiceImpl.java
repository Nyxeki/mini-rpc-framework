package io.github.nyxeki.minirpcframework.provider;

import io.github.nyxeki.minirpcframework.api.HelloService;

public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        System.out.println("服务端接收到客户端的请求 +" + name);
        return "Hello from rpc service" + name;
    }
}
