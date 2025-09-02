package io.github.nyxeki.minirpcframework.provider;

import io.github.nyxeki.minirpcframework.api.RpcRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class RpcServer {

    // A map to store registered services. The key is the interface name, and the value is the implementation object.
    private final Map<String, Object> serviceRegistry = new HashMap<>();

    public static void main(String[] args) {
        RpcServer server = new RpcServer();

        // Register the HelloService implementation before starting the server.
        server.register(new HelloServiceImpl());
        
        server.start(9000);
    }

    /**
     * Registers a service implementation object.
     * It uses the first implemented interface as the service name.
     * @param service the service implementation object.
     */
    public void register(Object service) {
        // For simplicity, we use the first implemented interface as the service name.
        String serviceName = service.getClass().getInterfaces()[0].getName();
        serviceRegistry.put(serviceName, service);
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

                        // Find the serviceImpl from the register
                        Object service = serviceRegistry.get(rpcRequest.getInterfaceName());
                        if (service == null) {
                            throw new ClassNotFoundException(rpcRequest.getInterfaceName() + " not found.");
                        }

                        java.lang.reflect.Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());

                        Object result = method.invoke(service, rpcRequest.getParameters());

                        oos.writeObject(result);
                        oos.flush();
                        System.out.println("Returned response: " + result);


                        // TODO: Call the real service method according to the content of the request.

                    } catch (Exception e) {
                        System.err.println("Error processing request: " + e.getMessage());
                    }
                }).start();
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
