package com.itworks.snamp.core.communication;

/**
 * Represents message descriptor.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface MessageDescriptor<REQ, RES> {
    /**
     * Gets type of the request payload.
     * @return The type of the request payload.
     */
    Class<REQ> getInputMessagePayloadType();

    /**
     * Gets type of the response payload.
     * @return The type of the response payload.
     */
    Class<RES> getOutputMessagePayloadType();

    /**
     * Determines whether the input message is one-way.
     * @return {@literal true}, if the input message is one-way; otherwise, {@literal false}.
     */
    boolean isOneWay();
}
