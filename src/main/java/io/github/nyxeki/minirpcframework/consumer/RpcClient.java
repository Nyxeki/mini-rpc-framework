package io.github.nyxeki.minirpcframework.consumer;

import io.github.nyxeki.minirpcframework.api.HelloService;
import io.github.nyxeki.minirpcframework.provider.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    public static void main(String[] args) {

        RpcClientProxy rpcClientProxy = new RpcClientProxy();

        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Press Enter to call the helloService, or type 'q' to quit.");
            String input = scanner.nextLine();

            // If the user types 'q', exit the loop.
            if ("q".equalsIgnoreCase(input)) {
                break;
            }

            // Call the method on the proxy object.
            String result = helloService.sayHello(input);

            // Print the result returned from the server.
            logger.info("Response from server: {}", result);
            System.out.println("------------------------------------------");
        }

        logger.info("Client shutting down");
    }
}
