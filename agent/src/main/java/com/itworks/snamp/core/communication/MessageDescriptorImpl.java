package com.itworks.snamp.core.communication;

/**
 * Represents default implementation of the {@link MessageDescriptor} class.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class MessageDescriptorImpl<REQ, RES> implements MessageDescriptor<REQ, RES> {
    private final Class<REQ> requestType;
    private final Class<RES> responseType;

    /**
     * Initializes a new message descriptor.
     * @param reqClass Type of the input message. Cannot be {@literal null}.
     * @param resClass Type of the output message. Cannot be {@literal null}.
     * @throws IllegalArgumentException reqClass or resClass is {@literal null}.
     */
    public MessageDescriptorImpl(final Class<REQ> reqClass, final Class<RES> resClass){
        if(reqClass == null) throw new IllegalArgumentException("reqClass is null.");
        else if(resClass == null) throw new IllegalArgumentException("resClass is null.");
        else {
            requestType = reqClass;
            responseType = resClass;
        }
    }

    /**
     * Gets type of the request payload.
     *
     * @return The type of the request payload.
     */
    @Override
    public final Class<REQ> getInputMessagePayloadType() {
        return requestType;
    }

    /**
     * Gets type of the response payload.
     *
     * @return The type of the response payload.
     */
    @Override
    public final Class<RES> getOutputMessagePayloadType() {
        return responseType;
    }

    /**
     * Determines whether the input message is one-way.
     * <p>
     *     In the default implementation this method returns true if
     *     {@link #getOutputMessagePayloadType()} is {@link Void}.
     * </p>
     * @return {@literal true}, if the input message is one-way; otherwise, {@literal false}.
     */
    @Override
    public boolean isOneWay() {
        return responseType.equals(Void.class);
    }
}
