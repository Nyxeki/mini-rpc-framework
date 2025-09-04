package io.github.nyxeki.minirpcframework.provider;

import io.github.nyxeki.minirpcframework.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RpcServer {

    Serializer serializer = new JsonSerializer();

    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    private final ExecutorService threadPool;

    // A map to store registered services. The key is the interface name, and the value is the implementation object.
    private final Map<String, Object> serviceRegistry = new HashMap<>();

    public static void main(String[] args) {
        int port = 9000; // Default port
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        final String serviceAddress = "localhost:" + port;

        RpcServer server = new RpcServer();
        HelloService helloService = new HelloServiceImpl();
        server.register(helloService);

        ZooKeeperRegistry registry = new ZooKeeperRegistry();
        try {
            registry.registerService(HelloService.class.getName(), serviceAddress);
        } catch (Exception e) {
            logger.error("Failed to register service. Shutting down.", e);
            System.exit(1);
        }

        server.start(port);
    }

    /**
     * Initialize the thread pool.
     */
    public RpcServer() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        threadPool = Executors.newFixedThreadPool(corePoolSize);
        logger.info("ThreadPool started with {} cores.", corePoolSize);
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
            logger.info("RPC Server started on port: {}", port);

            while (true) {
                Socket client = server.accept();
                logger.info("Accepted connection from {}", client.getInetAddress());
                threadPool.submit(() -> {
                    RpcResponse response = new RpcResponse();
                    try (
                            DataInputStream dataInputStream = new DataInputStream(client.getInputStream());
                            DataOutputStream dataOutputStream = new DataOutputStream(client.getOutputStream())
                    ) {
                        int requestLength = dataInputStream.readInt();
                        byte[] requestBytes = new byte[requestLength];
                        dataInputStream.readFully(requestBytes);
                        RpcRequest request = serializer.deserialize(requestBytes, RpcRequest.class);
                        logger.info("Processing request for interface: {}", request.getInterfaceName());

                        try {
                            Object service = serviceRegistry.get(request.getInterfaceName());
                            if (service == null) {
                                throw new ClassNotFoundException(request.getInterfaceName() + " not found.");
                            }
                            java.lang.reflect.Method method = service.getClass().getMethod(request.getMethodName(), request.getParameterTypes());
                            Object result = method.invoke(service, request.getParameters());
                            response.setData(result);
                        } catch (Exception e) {
                            logger.error("Method invocation failed for request: {}", request.getInterfaceName(), e);
                            Throwable cause = e.getCause();
                            String errorMessage = (cause != null) ? cause.getMessage() : e.getMessage();

                            response.setErrorMessage(errorMessage);
                        }

                        byte[] responseBytes = serializer.serialize(response);

                        dataOutputStream.writeInt(responseBytes.length);
                        dataOutputStream.write(responseBytes);
                        dataOutputStream.flush();
                    } catch (Exception e) {
                        logger.error("Error processing request: {}", e.getMessage(), e);
                    }
                });
            }
        } catch (IOException e) {
            logger.error("Server exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
