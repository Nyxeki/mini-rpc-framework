package io.github.nyxeki.minirpcframework.consumer;

import io.github.nyxeki.minirpcframework.api.RpcRequest;
import io.github.nyxeki.minirpcframework.api.Serializer;
import io.github.nyxeki.minirpcframework.provider.JsonSerializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
                        Serializer serializer = new JsonSerializer();
                        byte[] requestBytes = serializer.serialize(request);

                        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        dataOutputStream.writeInt(requestBytes.length);
                        dataOutputStream.write(requestBytes);
                        dataOutputStream.flush();

                        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                        int responseLength = dataInputStream.readInt();
                        byte[] responseBytes = new byte[responseLength];
                        dataInputStream.readFully(responseBytes);

                        return serializer.deserialize(responseBytes, String.class);

                    }
                }
        );

    }
}
