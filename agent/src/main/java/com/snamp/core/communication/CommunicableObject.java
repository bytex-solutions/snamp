package com.snamp.core.communication;

import com.snamp.TimeSpan;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * Represents an object that can communicate with other objects in loosely-coupled manner.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface CommunicableObject {

    /**
     * Represents receiver selector.
     * @author Roman Sakno
     * @version 1.0
     * @since 1.0
     */
    public static interface ReceiverSelector{
        /**
         * Determines whether the specified candidate is a valid receiver for the message.
         * @param candidate The message receiving candidate.
         * @return {@literal true} to send the message to the specified receiver; otherwise, {@literal false}.
         */
        boolean match(final Object candidate);
    }

    /**
     * Represents a messenger that is used to communicate with other
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static interface Messenger{
        /**
         * Sends asynchronous message without blocking of the caller thread.
         * @param <REQ> Type of the request payload.
         * @param descriptor The message descriptor. Cannot be {@literal null}.
         * @param request The message to send. Cannot be {@literal null}.
         * @param receiver The message receiver. May be {@literal null} for broadcasting.
         * @throws IllegalArgumentException descriptor or message or receiver is {@literal null}; or receiver not found.
         * @throws IllegalStateException The messenger is disconnected from the surface.
         */
        <REQ> void sendSignal(final MessageDescriptor<REQ, Void> descriptor, final REQ request, final ReceiverSelector receiver);

        /**
         * Sends message to the specified receiver.
         * @param <REQ> Type of the request payload.
         * @param descriptor The message descriptor. Cannot be {@literal null}.
         * @param request The message to send. Cannot be {@literal null}.
         * @param receiver The address of the receiver. May be {@literal null} for system requests.
         * @param timeout Response waiting timeout. May be {@literal null} for infinite timeout.
         * @throws IllegalArgumentException Receiver not found.
         * @throws IllegalStateException The messenger is disconnected from the surface.
         * @throws InterruptedException The current thread is interrupted.
         * @throws TimeoutException Timeout reached and response is not accepted.
         * @return The response payload.
         */
        <REQ, RES> RES sendMessage(final MessageDescriptor<REQ, RES> descriptor, final REQ request,
                                   final long correlID,
                                   final ReceiverSelector receiver,
                                   final TimeSpan timeout) throws TimeoutException, InterruptedException, ExecutionException;

        /**
         * Sends message to the specified receiver.
         * @param <REQ> Type of the request payload.
         * @param descriptor The message descriptor. Cannot be {@literal null}.
         * @param request The message to send. Cannot be {@literal null}.
         * @param receiver The address of the receiver. May be {@literal null} for system requests.
         * @throws IllegalArgumentException Receiver not found.
         * @throws IllegalStateException The messenger is disconnected from the surface.
         * @return An object that controls asynchronous state of the response.
         */
        <REQ, RES> Future<RES> sendMessage(final MessageDescriptor<REQ, RES> descriptor, final REQ request,
                                           final long correlID,
                                           final ReceiverSelector receiver);

        /**
         * Creates a new unique correlation ID.
         * @return A new unqiue correlation ID.
         * @throws IllegalStateException The messenger is disconnected from the surface.
         */
        long newCorrelationID();
    }

    /**
     * Connects this object to the communication surface.
     * @param messenger An object that is used to communicate with other objects. Cannot be {@literal null}.
     * @return {@literal true}, if this object successfully connected to the surface;
     *          {@literal false}, if this object already connected.
     * @throws IllegalArgumentException address or messenger is {@literal null}.
     */
    boolean connect(final Messenger messenger);

    /**
     * Informs this object that it is disconnected from the surface.
     */
    void disconnect();

    /**
     * Processes incoming message
     * @param sender The message sender. It may be {@link URI}, if
     *               sender is not available by reference. May be {@literal null} for system messages.
     * @param message The message to process. Cannot be {@literal null}.
     * @return Response message.
     * @throws IllegalArgumentException message is {@literal null}.
     */
    <REQ, RES> RES processMessage(final Object sender, final InputMessage<REQ, RES> message) throws Exception;
}
