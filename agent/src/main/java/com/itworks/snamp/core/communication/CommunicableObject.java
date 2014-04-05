package com.itworks.snamp.core.communication;

import java.net.URI;

/**
 * Represents an object that can communicate with other objects in loosely-coupled manner.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface CommunicableObject {
    /**
     * Connects this object to the communication surface.
     * @param messenger An object that is used to communicate with other objects. Cannot be {@literal null}.
     * @return {@literal true}, if this object successfully connected to the surface;
     *          {@literal false}, if this object already connected.
     * @throws IllegalArgumentException address or messenger is {@literal null}.
     */
    boolean connect(final Communicator messenger);

    /**
     * Disconnects from the communicator.
     */
    void disconnect();

    /**
     * Processes incoming message
     * @param sender The message sender. It may be {@link URI}, if
     *               sender is not available by reference. May be {@literal null} for system messages.
     * @param message The message to process. Cannot be {@literal null}.
     * @return Response message.
     * @throws IllegalArgumentException message is {@literal null}.
     * @throws MessageNotSupportedException Input message is not supported.
     * @throws Exception Some error occurs during message processing.
     */
    <REQ, RES> RES processMessage(final CommunicableObject sender, final InputMessage<REQ, RES> message) throws Exception;
}
