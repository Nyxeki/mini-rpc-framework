package io.github.nyxeki.minirpcframework.consumer;

import io.github.nyxeki.minirpcframework.api.HelloService;
import io.github.nyxeki.minirpcframework.api.RpcRequest;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RpcClient {

    public static void main(String[] args) {

        RpcClientProxy rpcClientProxy = new RpcClientProxy();

        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);

        String result = helloService.sayHello("world");

        System.out.println("Response from server: " + result);
    }
}
