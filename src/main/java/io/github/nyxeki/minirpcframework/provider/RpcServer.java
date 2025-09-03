package io.github.nyxeki.minirpcframework.provider;

import io.github.nyxeki.minirpcframework.api.RpcRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RpcServer {

    private final ExecutorService threadPool;

    // A map to store registered services. The key is the interface name, and the value is the implementation object.
    private final Map<String, Object> serviceRegistry = new HashMap<>();

    public static void main(String[] args) {
        RpcServer server = new RpcServer();

        // Register the HelloService implementation before starting the server.
        server.register(new HelloServiceImpl());
        
        server.start(9000);
    }

    /**
     * Initialize the thread pool.
     */
    public RpcServer() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        threadPool = Executors.newFixedThreadPool(corePoolSize);
        System.out.println("ThreadPool started with " + corePoolSize + " cores.");
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
                threadPool.submit(() -> {
                    try (
                            ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                            ObjectInputStream ois = new ObjectInputStream(client.getInputStream())) {
                        RpcRequest request = (RpcRequest) ois.readObject();
                        System.out.println("Processing request in thread: " + Thread.currentThread().getName());


                        Object service = serviceRegistry.get(request.getInterfaceName());
                        if (service == null) {
                            throw new ClassNotFoundException(request.getInterfaceName() + " not found.");
                        }
                        java.lang.reflect.Method method = service.getClass().getMethod(request.getMethodName(), request.getParameterTypes());
                        Object result = method.invoke(service, request.getParameters());

                        oos.writeObject(result);
                        oos.flush();
                    } catch (Exception e) {
                        System.err.println("Error processing request: " + e.getMessage());
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
