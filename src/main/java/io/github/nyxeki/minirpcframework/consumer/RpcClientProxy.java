package io.github.nyxeki.minirpcframework.consumer;

import io.github.nyxeki.minirpcframework.api.RpcRequest;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;

@SuppressWarnings("unchecked")
public class RpcClientProxy {

    public <T> T getProxy(final Class<T> serviceInterface) {
        return (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class<?>[]{serviceInterface},
                (proxy, method, args) -> {
                    RpcRequest request = new RpcRequest();
                    request.setInterfaceName(serviceInterface.getName());
                    request.setMethodName(method.getName());
                    request.setParameters(args);
                    request.setParameterTypes(method.getParameterTypes());
                    try (Socket socket = new Socket("localhost", 9000)) {
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                        objectOutputStream.writeObject(request);

                        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                        return objectInputStream.readObject();
                    }
                }
        );

    }
}
