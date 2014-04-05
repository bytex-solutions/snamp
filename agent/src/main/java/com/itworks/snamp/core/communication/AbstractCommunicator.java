package com.itworks.snamp.core.communication;

import com.itworks.snamp.TimeSpan;
import org.apache.commons.collections4.Predicate;

import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents abstract implementation of the communicator.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractCommunicator implements Communicator {
    private final AtomicLong correlIdCounter;
    private final AtomicLong messageIdCounter;

    /**
     * Initializes a new communicator.
     */
    protected AbstractCommunicator(){
        correlIdCounter = new AtomicLong(0L);
        messageIdCounter = new AtomicLong(0L);
    }

    private static <REQ, RES> InputMessage<REQ, RES> createInputMessage(final MessageDescriptor<REQ, RES> descriptor, final REQ request, final long messageID, final long correlID){
        if(descriptor == null) throw new IllegalArgumentException("descriptor is null.");
        else if(request == null) throw new IllegalArgumentException("request is null.");
        else return new InputMessage<REQ, RES>() {
            private final Date timestamp = new Date();

            @Override
            public final MessageDescriptor<REQ, RES> getDescriptor() {
                return descriptor;
            }

            @Override
            public final Date getTimestamp() {
                return timestamp;
            }

            @Override
            public final long getMessageID() {
                return messageID;
            }

            @Override
            public final long getCorrelationID() {
                return correlID;
            }

            @Override
            public final REQ getPayload() {
                return request;
            }
        };
    }

    /**
     * Creates a new input message.
     * @param descriptor The message descriptor. Cannot be {@literal null}.
     * @param request The request to process. Cannot be {@literal null}.
     * @param correlID Correlation ID used to organize dialogs.
     * @param <REQ> Type of the request object..
     * @param <RES> Type of the response object.
     * @return A new instance of the input message.
     */
    protected <REQ, RES> InputMessage<REQ, RES> createInputMessage(final MessageDescriptor<REQ, RES> descriptor, final REQ request, final long correlID){
        return createInputMessage(descriptor, request, newMessageID(), correlID);
    }

    /**
     * Sends one-way message to the specified receiver.
     * @param sender The message sender.
     * @param message The message to be sent.
     * @param receiver The message receiver.
     * @param <REQ> Type of the request.
     */
    protected abstract <REQ> void sendSignal(final CommunicableObject sender, final InputMessage<REQ, Void> message, final CommunicableObject receiver);

    /**
     * Sends asynchronous message without blocking of the caller thread.
     *
     * @param <REQ>      Type of the request payload.
     * @param sender     The message sender. Cannot be {@literal null}.
     * @param descriptor The message descriptor. Cannot be {@literal null}.
     * @param request    The message to send. Cannot be {@literal null}.
     * @param receiver   The message receiver. May be {@literal null} for broadcasting.
     * @throws IllegalArgumentException One of the required arguments is {@literal null}; or receiver not found.
     * @throws IllegalStateException    The messenger is disconnected from the surface.
     */
    @Override
    public final  <REQ> void sendSignal(final CommunicableObject sender, final MessageDescriptor<REQ, Void> descriptor, final REQ request, final Predicate<CommunicableObject> receiver) {
        if(descriptor == null) throw new IllegalArgumentException("descriptor is null.");
        else if(!descriptor.isOneWay()) throw new IllegalArgumentException("Signal cannot have response. Use sendMessage instead.");
        if(receiver == null) //force broadcasting
            for(final CommunicableObject candidate: getReceivers())
                sendSignal(sender, createInputMessage(descriptor, request, -1L), candidate);
        else{
            final CommunicableObject exactReceiver = selectReceiver(receiver);
            if(exactReceiver == null) throw new IllegalArgumentException("Receiver not found.");
            sendSignal(sender, createInputMessage(descriptor, request, -1L), exactReceiver);
        }
    }

    /**
     * Sends message to the specified receiver.
     *
     * @param <REQ>      Type of the request payload.
     * @param sender     The message sender. Cannot be {@literal null}.
     * @param descriptor The message descriptor. Cannot be {@literal null}.
     * @param request    The message to send. Cannot be {@literal null}.
     * @param receiver   The address of the receiver. Cannot be {@literal null}.
     * @param timeout    Response waiting timeout. May be {@literal null} for infinite timeout.
     * @return The response payload.
     * @throws IllegalArgumentException Receiver not found; or one of the required arguments is {@literal null}.
     * @throws InterruptedException     The current thread is interrupted.
     * @throws java.util.concurrent.TimeoutException
     *                                  Timeout reached and response is not accepted.
     * @throws java.util.concurrent.ExecutionException
     *                                  An error occurred during message processing by receiver.
     * @see MessageNotSupportedException
     */
    @Override
    public final <REQ, RES> RES sendMessage(final CommunicableObject sender, final MessageDescriptor<REQ, RES> descriptor, final REQ request, final long correlID, final Predicate<CommunicableObject> receiver, final TimeSpan timeout) throws TimeoutException, InterruptedException, ExecutionException {
        final Future<RES> response = sendMessage(sender, descriptor, request, correlID, receiver);
        return timeout != null ? response.get(timeout.duration, timeout.unit) : response.get();
    }

    /**
     * Sends message to the specified receiver.
     *
     * @param <REQ>      Type of the request payload.
     * @param descriptor The message descriptor. Cannot be {@literal null}.
     * @param request    The message to send. Cannot be {@literal null}.
     * @param receiver   The address of the receiver. Cannot be {@literal null}.
     * @return An object that controls asynchronous state of the response.
     * @throws IllegalArgumentException Receiver not found; or one of the required arguments is {@literal null}.
     */
    @Override
    public final <REQ, RES> Future<RES> sendMessage(final CommunicableObject sender, final MessageDescriptor<REQ, RES> descriptor, final REQ request, final long correlID, final Predicate<CommunicableObject> receiver) {
        if(receiver == null) throw new IllegalArgumentException("receiver is null.");
        else if(descriptor == null) throw new IllegalArgumentException("descriptor is null.");
        else if(descriptor.isOneWay()) throw new IllegalArgumentException("One-way message cannot have response. Use sendSignal method instead.");
        final CommunicableObject exactReceiver = selectReceiver(receiver);
        if(exactReceiver == null) throw new IllegalArgumentException("Receiver not found.");
        else return sendMessage(sender, createInputMessage(descriptor, request, correlID), exactReceiver);
    }

    /**
     * Sends message to the specified receiver.
     * @param sender The message sender.
     * @param inputMessage The message to be sent.
     * @param receiver The message receiver.
     * @param <RES> Type of the response.
     * @param <REQ> Type of the request.
     * @return An object that controls asynchronous state of the response.
     */
    protected abstract <REQ, RES> Future<RES> sendMessage(final CommunicableObject sender, final InputMessage<REQ, RES> inputMessage, final CommunicableObject receiver);

    /**
     * Gets a collection of connected objects.
     * @return A collection of connected objects.
     */
    protected abstract Iterable<CommunicableObject> getReceivers();

    /**
     * Finds the message receiver.
     * @param selector The receiver selector.
     * @return
     */
    private CommunicableObject selectReceiver(final Predicate<CommunicableObject> selector){
        for(final CommunicableObject candidate: getReceivers())
            if(selector.evaluate(candidate)) return candidate;
        return null;
    }

    /**
     * Creates a new unique correlation ID.
     * @return A new unqiue correlation ID.
     */
    @Override
    public final long newCorrelationID() {
        return correlIdCounter.getAndIncrement();
    }

    /**
     * Creates a new unique message ID.
     * @return A new unique message ID.
     */
    protected final long newMessageID(){
        return messageIdCounter.getAndIncrement();
    }
}
