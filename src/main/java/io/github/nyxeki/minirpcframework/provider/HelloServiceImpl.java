package io.github.nyxeki.minirpcframework.provider;

import io.github.nyxeki.minirpcframework.api.HelloService;

public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
