package io.github.nyxeki.minirpcframework.provider;

import java.io.IOException;
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
                    try {
                        System.out.println("Processing request in thread: " + Thread.currentThread().getName());
                    } finally {
                        try {
                            client.close();
                        } catch (IOException e) {
                            System.err.println("Error closing client socket: " + e.getMessage());
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
