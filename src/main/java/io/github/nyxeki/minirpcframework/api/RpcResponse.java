package io.github.nyxeki.minirpcframework.api;

/**
 * A standard response object for all RPC calls.
 */
public class RpcResponse {

    /**
     * The result data of a successful method invocation.
     */
    private Object data;

    /**
     * The exception thrown if the method invocation failed.
     */
    private Exception exception;

    public boolean isSuccess() {
        return exception == null;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}