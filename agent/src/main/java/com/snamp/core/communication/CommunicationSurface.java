package com.snamp.core.communication;

import com.snamp.core.PlatformService;

import java.net.URI;

/**
 * Represents communication bus that is used to communicate between SNAMP plugins
 * in loosely-coupled manner.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface CommunicationSurface extends PlatformService, AutoCloseable {
    /**
     * Adds the specified object to be available for communication with other objects
     * in this surface.
     * @param obj An object to register.
     * @return {@literal true}, if the specified object successfully registered;
     * otherwise, {@literal false}.
     */
    public boolean registerObject(final CommunicableObject obj);

    /**
     * Unregisters the specified object from this surface.
     * @param obj An object to be unregister.
     * @return {@literal true}, if the specified object is unregistered successfully; otherwise, {@literal false}.
     */
    public boolean removeObject(final CommunicableObject obj);

    /**
     * Removes all objects in this surface.
     */
    void removeAll();

    /**
     * Sends asynchronous system message to the specified receiver.
     * @param descriptor The descriptor of the message to send. Cannot be {@literal null}.
     * @param request The request to send. Cannot be {@literal null}.
     * @param receiver The receiver of the message. May be {@literal null} for broadcasting.
     * @param <REQ> Type of the system request.
     * @throws IllegalArgumentException Receiver not found.
     */
    <REQ> void sendSystemMessage(final MessageDescriptor<REQ, Void> descriptor, final REQ request, final CommunicableObject.ReceiverSelector receiver);
}
