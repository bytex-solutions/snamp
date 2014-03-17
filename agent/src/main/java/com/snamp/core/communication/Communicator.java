package com.snamp.core.communication;

import com.snamp.TimeSpan;
import org.apache.commons.collections4.Predicate;

import java.util.concurrent.*;

/**
 * Represents a messenger that is used to communicate with other objects.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface Communicator{
    /**
     * Sends asynchronous message without blocking of the caller thread.
     * @param <REQ> Type of the request payload.
     * @param sender The message sender. Cannot be {@literal null}.
     * @param descriptor The message descriptor. Cannot be {@literal null}.
     * @param request The message to send. Cannot be {@literal null}.
     * @param receiver The message receiver. May be {@literal null} for broadcasting.
     * @throws IllegalArgumentException One of the required arguments is {@literal null}; or receiver not found.
     * @throws IllegalStateException The messenger is disconnected from the surface.
     */
    <REQ> void sendSignal(final CommunicableObject sender, final MessageDescriptor<REQ, Void> descriptor, final REQ request, final Predicate<CommunicableObject> receiver);

    /**
     * Sends message to the specified receiver.
     * @param <REQ> Type of the request payload.
     * @param sender The message sender. Cannot be {@literal null}.
     * @param descriptor The message descriptor. Cannot be {@literal null}.
     * @param request The message to send. Cannot be {@literal null}.
     * @param receiver The address of the receiver. Cannot be {@literal null}.
     * @param timeout Response waiting timeout. May be {@literal null} for infinite timeout.
     * @throws IllegalArgumentException Receiver not found; or one of the required arguments is {@literal null}.
     * @throws InterruptedException The current thread is interrupted.
     * @throws java.util.concurrent.TimeoutException Timeout reached and response is not accepted.
     * @throws ExecutionException An error occurred during message processing by receiver.
     * @return The response payload.
     * @see MessageNotSupportedException
     */
    <REQ, RES> RES sendMessage(final CommunicableObject sender,
                               final MessageDescriptor<REQ, RES> descriptor,
                               final REQ request,
                               final long correlID,
                               final Predicate<CommunicableObject> receiver,
                               final TimeSpan timeout) throws TimeoutException, InterruptedException, ExecutionException;

    /**
     * Sends message to the specified receiver.
     * @param <REQ> Type of the request payload.
     * @param descriptor The message descriptor. Cannot be {@literal null}.
     * @param request The message to send. Cannot be {@literal null}.
     * @param receiver The address of the receiver. Cannot be {@literal null}.
     * @throws IllegalArgumentException Receiver not found; or one of the required arguments is {@literal null}.
     * @return An object that controls asynchronous state of the response.
     */
    <REQ, RES> Future<RES> sendMessage(final CommunicableObject sender,
                                       final MessageDescriptor<REQ, RES> descriptor,
                                       final REQ request,
                                       final long correlID,
                                       final Predicate<CommunicableObject> receiver);

    /**
     * Creates a new unique correlation ID.
     * @return A new unqiue correlation ID.
     */
    long newCorrelationID();
}
