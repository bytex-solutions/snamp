package com.snamp.core.communication;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.concurrent.*;

/**
 * Represents communication surface that is used to deliver messages between objects
 * in the same memory space.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class InMemoryCommunicationSurface extends AbstractCommunicationSurface {
    private static final class InMemoryDeliveryChannel extends Vector<CommunicableObject> implements DeliveryChannel, AutoCloseable{
        private final ExecutorService executor;
        private final AtomicLong correlIdCounter = new AtomicLong(0L);
        private final AtomicLong messageIdCounter = new AtomicLong(0L);

        public InMemoryDeliveryChannel(final ExecutorService executor){
            super(15);
            if(executor == null) throw new IllegalArgumentException("executor");
            else this.executor = executor;
        }

        private final <REQ, RES> InputMessage<REQ, RES> createMessage(final MessageDescriptor<REQ, RES> descriptor, final REQ request, final long correlationID) {
            if(descriptor == null) throw new IllegalArgumentException("descriptor is null.");
            else if(request == null) throw new IllegalArgumentException("request is null.");
            final long messageID = messageIdCounter.incrementAndGet();
            return new InputMessage<REQ, RES>() {
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
                    return correlationID;
                }

                @Override
                public final REQ getPayload() {
                    return request;
                }
            };
        }

        private boolean isValidSender(final Object sender){
            return sender instanceof CommunicationSurface || contains(sender);
        }

        private static <REQ, RES> Future<RES> sendMessage(final ExecutorService executor, final Object sender, final InputMessage<REQ, RES> request, final CommunicableObject receiver){
            return executor.submit(new Callable<RES>() {
                @Override
                public final RES call() throws Exception {
                    return receiver.processMessage(sender, request);
                }
            });
        }

        /**
         * Sends asynchronous message without blocking of the caller thread.
         *
         * @param <REQ>      Type of the request payload.
         * @param sender     The message sender. Cannot be {@literal null}.
         * @param descriptor The message descriptor. Cannot be {@literal null}.
         * @param request    The message to send. Cannot be {@literal null}.
         * @param receiver   The message receiver. May be {@literal null} for broadcasting.
         * @throws IllegalArgumentException Receiver not found.
         * @throws IllegalStateException The sender is removed from surface.
         */
        @Override
        public final  <REQ> void sendSignal(final Object sender, final MessageDescriptor<REQ, Void> descriptor, final REQ request, final CommunicableObject.ReceiverSelector receiver) {
            if(isValidSender(sender)){
                if(receiver == null) //broadcasting
                    for(final CommunicableObject obj: this)
                        sendMessage(executor, sender, createMessage(descriptor, request, -1L), obj);
                else { //unicasting
                    for(final CommunicableObject obj: this)
                        if(receiver.match(obj)){
                            sendMessage(executor, sender, createMessage(descriptor, request, -1L), obj);
                            return;
                        }
                    throw new IllegalArgumentException("Receiver not found");
                }
            }
            else throw new IllegalStateException(String.format("Sender %s is disconnected from communication surface", sender));
        }

        /**
         * Sends message to the specified receiver.
         *
         * @param <REQ>      Type of the request payload.
         * @param sender     The message sender. Cannot be {@literal null}.
         * @param descriptor The message descriptor. Cannot be {@literal null}.
         * @param request    The message to send. Cannot be {@literal null}.
         * @param correlID   Correlation identifier used for dialogs.
         * @param receiver   The address of the receiver. May be {@literal null} for system requests.
         * @return The response payload.
         * @throws IllegalArgumentException Receiver not found.
         */
        @Override
        public final <REQ, RES> Future<RES> sendMessage(final Object sender, final MessageDescriptor<REQ, RES> descriptor, final REQ request, final long correlID, final CommunicableObject.ReceiverSelector receiver) {
            if(receiver == null) throw new IllegalArgumentException("receiver is null.");
            else if(isValidSender(sender)){
                for(final CommunicableObject obj: this)
                    if(receiver.match(obj))
                        return sendMessage(executor, sender, createMessage(descriptor, request, -1L), obj);
                throw new IllegalArgumentException("Receiver not found");
            }
            else throw new IllegalStateException(String.format("Sender %s is disconnected from communication surface", sender));
        }

        /**
         * Generates a new correlation ID.
         *
         * @return A new unique correlation ID.
         */
        @Override
        public final long newCorrelationID() {
            return correlIdCounter.incrementAndGet();
        }

        /**
         * Releases all resources associated with this channel.
         */
        @Override
        public void close() {
            executor.shutdown();
        }
    }

    /**
     * Initializes a new instance of the platform service.
     *
     * @param loggerInstance A logger associated with this instance of the platform service.
     * @param messageExecutor An executor that coordinates message flows. Cannot be {@literal null}.
     * @throws IllegalArgumentException messageExecutor is {@literal null}.
     */
    protected InMemoryCommunicationSurface(final Logger loggerInstance, final ExecutorService messageExecutor) {
        super(loggerInstance, new InMemoryDeliveryChannel(messageExecutor));
    }

    /**
     * Initializes a new instance of the platform service.
     *
     * @param loggerName The name of the logger to be associated with this instance of the platform service.
     * @param messageExecutor An executor that coordinates message flows. Cannot be {@literal null}.
     * @throws IllegalArgumentException messageExecutor is {@literal null}.
     */
    protected InMemoryCommunicationSurface(final String loggerName, final ExecutorService messageExecutor) {
        super(loggerName, new InMemoryDeliveryChannel(messageExecutor));
    }

    /**
     * Initializes a new instance of the platform service.
     *
     * @param loggerInstance A logger associated with this instance of the platform service.
     * @param nThreads Max count of threads in the thread pool used to process messages.
     */
    protected InMemoryCommunicationSurface(final Logger loggerInstance, final int nThreads) {
        this(loggerInstance, Executors.newFixedThreadPool(nThreads));
    }

    /**
     * Initializes a new instance of the platform service.
     *
     * @param loggerName The name of the logger to be associated with this instance of the platform service.
     * @param nThreads Max count of threads in the thread pool used to process messages.
     */
    protected InMemoryCommunicationSurface(final String loggerName, final int nThreads) {
        this(loggerName, Executors.newFixedThreadPool(nThreads));
    }

    /**
     * Initializes a new instance of the in-memory communication surface.
     * @param nThreads Max count of threads in the thread pool used to process messages.
     */
    public InMemoryCommunicationSurface(final int nThreads){
        this(InMemoryCommunicationSurface.class.getCanonicalName(), nThreads);
    }

    private InMemoryDeliveryChannel getDeliveryChannel(){
        return (InMemoryDeliveryChannel)channel;
    }

    /**
     * Determines whether the specified object is already connected.
     *
     * @param obj An object to check.
     * @return {@literal true}, if the specified object already connected to this surface;
     *         otherwise, {@literal false}.
     */
    @Override
    protected final boolean isRegistered(final CommunicableObject obj) {
        return getDeliveryChannel().contains(obj);
    }

    /**
     * Connects the specified object to this surface and adds it to the registry.
     *
     * @param obj       An object to connect.
     * @param messenger A messenger for the specified object.
     * @return {@literal false}, if the specified object already connected; otherwise, {@literal true}.
     */
    @Override
    protected final boolean connect(final CommunicableObject obj, final CloseableMessenger messenger) {
        if(super.connect(obj, messenger))
            return getDeliveryChannel().add(obj);
        else return false;
    }

    /**
     * Disconnects the object from this surface and removes it from the registry.
     *
     * @param obj An object to disconnect.
     */
    @Override
    protected final void disconnect(final CommunicableObject obj) {
        super.disconnect(obj);
        getDeliveryChannel().remove(obj);
    }

    /**
     * Removes all objects in this surface.
     */
    @Override
    public final void removeAll() {
        final Collection<CommunicableObject> objects = getDeliveryChannel();
        for(final CommunicableObject obj: objects)
            obj.disconnect();
        objects.clear();
    }

    /**
     * Releases all resources associated with this surface.
     */
    @Override
    public void close() {
        removeAll();
        getDeliveryChannel().close();
    }
}
