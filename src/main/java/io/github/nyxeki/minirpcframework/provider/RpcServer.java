package io.github.nyxeki.minirpcframework.provider;

import io.github.nyxeki.minirpcframework.api.RpcRequest;
import io.github.nyxeki.minirpcframework.api.RpcResponse;
import io.github.nyxeki.minirpcframework.api.Serializer;
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
    RpcResponse response = new RpcResponse();

    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

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
                            response.setException(e);
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
