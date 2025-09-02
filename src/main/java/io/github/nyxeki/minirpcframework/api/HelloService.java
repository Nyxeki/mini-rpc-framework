package io.github.nyxeki.minirpcframework.api;

public interface HelloService {

    /**
     * Return a greeting according to the incoming name.
     * @param name
     * @return greeting
     */
    String sayHello(String name);
}
