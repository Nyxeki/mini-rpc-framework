package io.github.nyxeki.minirpcframework.consumer;

import java.net.Socket;

public class RpcClient {

    public static void main(String[] args) {
        System.out.println("RpcClient starting...");
        try (Socket socket = new Socket("127.0.0.1", 9000)) {
            System.out.println("Connected...");

        } catch (Exception e) {
            System.err.println("Client failed: " + e.getMessage());
        }
        System.out.println("RpcClient finished");
    }
}
