package io.github.nyxeki.minirpcframework.provider;

import io.github.nyxeki.minirpcframework.api.RpcRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class RpcServer {

    public static void main(String[] args) {
        RpcServer server = new RpcServer();
        server.start(9000);
    }

    public void start(int port) {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("RPC server started on port " + port);

            while (true) {
                Socket client = server.accept();
                System.out.println("Accepted connection from " + client.getInetAddress());
                new Thread(() -> {
                    try (
                        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                        ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream())
                    ) {
                        System.out.println("Processing request in thread: " + Thread.currentThread().getName());
                        RpcRequest rpcRequest = (RpcRequest) ois.readObject();
                        System.out.println("Received request from client: " + rpcRequest);

                        // TODO: Call the real service method according to the content of the request.

                    } catch (IOException | ClassNotFoundException e) {
                        System.err.println("Error processing request: " + e.getMessage());
                    } finally {
                        try {
                            client.close();
                        } catch (IOException e) {
                            System.err.println("Error closing client: " + e.getMessage());
                        }
                    }
                }).start();
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
