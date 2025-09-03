package io.github.nyxeki.minirpcframework.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
    private String errorMessage;

    @JsonIgnore
    public boolean isSuccess() {
        return errorMessage == null;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}