package io.github.nyxeki.minirpcframework.api;

/**
 * Common interface for serializers
 */
public interface Serializer {

    /**
     * Serializes object into byte[]
     * @param object the object to serialize
     * @return the resulting byte array
     */
    byte[] serialize(Object object);

    /**
     * Deserializes a byte[] back into an object of the given class.
     *
     * @param bytes the byte array to deserialize.
     * @param clazz the class of the target object.
     * @param <T>   the type of the target object.
     * @return the deserialized object.
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
