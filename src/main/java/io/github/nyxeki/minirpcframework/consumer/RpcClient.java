package io.github.nyxeki.minirpcframework.consumer;

import io.github.nyxeki.minirpcframework.api.HelloService;
import io.github.nyxeki.minirpcframework.api.RpcRequest;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RpcClient {

    public static void main(String[] args) {
        System.out.println("RpcClient starting...");
        try (Socket socket = new Socket("127.0.0.1", 9000)) {
            System.out.println("Connected...");

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            RpcRequest rpcRequest = new RpcRequest();
            rpcRequest.setInterfaceName(HelloService.class.getName());
            rpcRequest.setMethodName("sayHello");
            rpcRequest.setParameters(new Object[]{"world"});
            rpcRequest.setParameterTypes(new Class<?>[]{String.class});

            oos.writeObject(rpcRequest);
            oos.flush();
            System.out.println("Sending request: " + rpcRequest.getMethodName());

            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Object response = ois.readObject();
            System.out.println("Received response: " + response);

        } catch (Exception e) {
            System.err.println("Client failed: " + e.getMessage());
        }
        System.out.println("RpcClient finished");
    }
}
