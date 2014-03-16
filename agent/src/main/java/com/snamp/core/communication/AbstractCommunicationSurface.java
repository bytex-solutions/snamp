package com.snamp.core.communication;

import com.snamp.TimeSpan;
import com.snamp.core.AbstractPlatformService;

import java.lang.ref.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import static com.snamp.core.communication.CommunicableObject.ReceiverSelector;

/**
 * Represents an abstract class for building communication surfaces.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractCommunicationSurface extends AbstractPlatformService implements CommunicationSurface {

    /**
     * Represents message delivery channel.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static interface DeliveryChannel{
        /**
         * Sends asynchronous message without blocking of the caller thread.
         * @param <REQ> Type of the request payload.
         * @param sender The message sender. Cannot be {@literal null}.
         * @param descriptor The message descriptor. Cannot be {@literal null}.
         * @param request The message to send. Cannot be {@literal null}.
         * @param receiver The message receiver. May be {@literal null} for broadcasting.
         * @throws IllegalArgumentException receiver not found.
         * @throws IllegalStateException The sender is removed from surface.
         */
        <REQ> void sendSignal(final Object sender, final MessageDescriptor<REQ, Void> descriptor, final REQ request, final ReceiverSelector receiver);

        /**
         * Sends message to the specified receiver.
         * @param <REQ> Type of the request payload.
         * @param sender The message sender. Cannot be {@literal null}.
         * @param descriptor The message descriptor. Cannot be {@literal null}.
         * @param request The message to send. Cannot be {@literal null}.
         * @param correlID Correlation identifier used for dialogs.
         * @param receiver The address of the receiver. Cannot be {@literal null}.
         * @throws IllegalArgumentException Receiver not found; or sender or descriptor or request or receiver is {@literal null}.
         * @throws IllegalStateException The sender is removed from surface.
         * @return The response payload.
         */
        <REQ, RES> Future<RES> sendMessage(final Object sender,
                                   final MessageDescriptor<REQ, RES> descriptor,
                                   final REQ request,
                                   final long correlID,
                                   final ReceiverSelector receiver);

        /**
         * Generates a new correlation ID.
         * @return A new unique correlation ID.
         */
        long newCorrelationID();
    }

    /**
     * Represents closeable messenger.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static interface CloseableMessenger extends CommunicableObject.Messenger, AutoCloseable{
        /**
         * Releases all resources associated with this messenger.
         */
        @Override
        void close();
    }

    /**
     * Represents closeable messenger. This class cannot be inherited.
     * @version 1.0
     * @since 1.0
     * @author Roman Sakno
     */
    private static final class DefaultMessenger implements CloseableMessenger{
        private final Reference<DeliveryChannel> channel;
        private final Reference<CommunicableObject> owner;

        /**
         * Initializes a new instance of the messenger.
         * @param owner
         * @param channel
         */
        public DefaultMessenger(final CommunicableObject owner, final DeliveryChannel channel){
            if(owner == null) throw new IllegalArgumentException("owner is null.");
            else if(channel == null) throw new IllegalArgumentException("channel is null.");
            else {
                this.channel = new WeakReference<>(channel);
                this.owner = new WeakReference<>(owner);
            }
        }

        /**
         * Sends asynchronous message without blocking of the caller thread.
         *
         * @param <REQ>      Type of the request payload.
         * @param descriptor The message descriptor. Cannot be {@literal null}.
         * @param request    The message to send. Cannot be {@literal null}.
         * @param receiver   The message receiver. May be {@literal null} for broadcasting.
         * @throws IllegalArgumentException descriptor or message or receiver is {@literal null}.
         * @throws IllegalStateException    The messenger is disconnected from the surface.
         */
        @Override
        public final  <REQ> void sendSignal(final MessageDescriptor<REQ, Void> descriptor, final REQ request, final CommunicableObject.ReceiverSelector receiver) {
            final DeliveryChannel sender = channel.get();
            final CommunicableObject obj = owner.get();
            if(sender == null || obj == null) throw new IllegalStateException("This messenger is disconnected.");
            sender.sendSignal(obj, descriptor, request, receiver);
        }

        /**
         * Sends message to the specified receiver.
         *
         * @param <REQ>      Type of the request payload.
         * @param descriptor The message descriptor. Cannot be {@literal null}.
         * @param request    The message to send. Cannot be {@literal null}.
         * @param receiver   The address of the receiver. May be {@literal null} for system requests.
         * @param timeout    Response waiting timeout. May be {@literal null} for infinite timeout.
         * @return The response payload.
         * @throws IllegalArgumentException Receiver not found.
         * @throws IllegalStateException    The messenger is disconnected from the surface.
         * @throws InterruptedException     The current thread is interrupted.
         * @throws java.util.concurrent.TimeoutException
         *                                  Timeout reached and response is not accepted.
         */
        @Override
        public final <REQ, RES> RES sendMessage(final MessageDescriptor<REQ, RES> descriptor, final REQ request, final long correlID, final ReceiverSelector receiver, final TimeSpan timeout) throws TimeoutException, InterruptedException, ExecutionException {
            final Future<RES> response = sendMessage(descriptor, request, correlID, receiver);
            return timeout == TimeSpan.INFINITE ?
                    response.get():
                    response.get(timeout.duration, timeout.unit);
        }

        /**
         * Sends message to the specified receiver.
         *
         * @param <REQ>      Type of the request payload.
         * @param descriptor The message descriptor. Cannot be {@literal null}.
         * @param request    The message to send. Cannot be {@literal null}.
         * @param receiver   The address of the receiver. May be {@literal null} for system requests.
         * @return An object that controls asynchronous state of the response.
         * @throws IllegalArgumentException Receiver not found.
         * @throws IllegalStateException    The messenger is disconnected from the surface.
         */
        @Override
        public final <REQ, RES> Future<RES> sendMessage(final MessageDescriptor<REQ, RES> descriptor, final REQ request, final long correlID, final ReceiverSelector receiver) {
            final DeliveryChannel sender = channel.get();
            final CommunicableObject obj = owner.get();
            if(sender == null || obj == null) throw new IllegalStateException("This messenger is disconnected.");
            return sender.sendMessage(obj, descriptor, request, correlID, receiver);
        }

        /**
         * Creates a new unique correlation ID.
         *
         * @return A new unqiue correlation ID.
         * @throws IllegalStateException The messenger is disconnected from the surface.
         */
        @Override
        public final long newCorrelationID() {
            final DeliveryChannel sender = channel.get();
            if(sender == null) throw new IllegalStateException("This messenger is disconnected.");
            return sender.newCorrelationID();
        }

        /**
         * Closes all resources associated with this messenger.
         */
        @Override
        public final void close(){
            channel.clear();
            owner.clear();
        }
    }

    /**
     * Represents delivery channel.
     */
    private final DeliveryChannel channel;

    /**
     * Initializes a new instance of the platform service.
     *
     * @param loggerInstance A logger associated with this instance of the platform service.
     * @param channel Message delivery channel. Cannot be {@literal null}.
     * @throws IllegalArgumentException channel is {@literal null}.
     */
    protected AbstractCommunicationSurface(final Logger loggerInstance, final DeliveryChannel channel) {
        super(loggerInstance);
        if(channel == null) throw new IllegalArgumentException("channel is null.");
        else this.channel = channel;
    }

    /**
     * Gets delivery channel casted to the specified type.
     * @param destinationType The type that implements {@link DeliveryChannel}.
     * @param <T> The type that implements {@link DeliveryChannel}.
     * @throws ClassCastException Underlying channel is not an instance of T.
     * @return Type-safe representation of the delivery channel.
     */
    protected final <T extends DeliveryChannel> T getDeliveryChannel(final Class<T> destinationType){
        return destinationType.cast(channel);
    }

    /**
     * Initializes a new instance of the platform service.
     *
     * @param loggerName The name of the logger to be associated with this instance of the platform service.
     * @param channel Message delivery channel. Cannot be {@literal null}.
     * @throws IllegalArgumentException channel is {@literal null}.
     */
    protected AbstractCommunicationSurface(final String loggerName, final DeliveryChannel channel) {
        super(loggerName);
        if(channel == null) throw new IllegalArgumentException("channel is null.");
        else this.channel = channel;
    }

    /**
     * Creates a new messenger for the specified object.
     * @param owner The messenger owner.
     * @return A new instance of the messenger.
     */
    protected final CloseableMessenger createMessenger(final CommunicableObject owner){
        return new DefaultMessenger(owner, channel);
    }

    /**
     * Connects the specified object to this surface and adds it to the registry.
     * @param obj An object to connect.
     * @param messenger A messenger for the specified object.
     * @return {@literal false}, if the specified object already connected; otherwise, {@literal true}.
     */
    protected boolean connect(final CommunicableObject obj, final CloseableMessenger messenger){
        return obj.connect(messenger);
    }

    /**
     * Determines whether the specified object is already connected.
     * @param obj An object to check.
     * @return {@literal true}, if the specified object already connected to this surface;
     *  otherwise, {@literal false}.
     */
    protected abstract boolean isRegistered(final CommunicableObject obj);

    /**
     * Adds the specified object to be available for communication with other objects
     * in this surface.
     *
     * @param obj An object to register.
     * @return {@literal true}, if the specified object successfully registered;
     *         otherwise, {@literal false}.
     */
    @Override
    public final boolean registerObject(final CommunicableObject obj) {
        if(isRegistered(obj)) return false;
        final CloseableMessenger messenger = createMessenger(obj);
        if(connect(obj, messenger)) return true;
        else {
            messenger.close();
            return false;
        }
    }

    /**
     * Disconnects the object from this surface and removes it from the registry.
     * @param obj An object to disconnect.
     */
    protected void disconnect(final CommunicableObject obj){
        obj.disconnect();
    }

    /**
     * Removes registration the specified object from this surface.
     *
     * @param obj An object to remove.
     * @return {@literal true}, if the specified object is unregistered successfully; otherwise, {@literal false}.
     */
    @Override
    public final boolean removeObject(final CommunicableObject obj) {
        if(isRegistered(obj)){
            disconnect(obj);
            return true;
        }
        else return false;
    }

    /**
     * Sends asynchronous system message to the specified receiver.
     *
     * @param descriptor The descriptor of the message to send. Cannot be {@literal null}.
     * @param request    The request to send. Cannot be {@literal null}.
     * @param receiver   The receiver of the message. May be {@literal null} for broadcasting.
     * @param <REQ>      Type of the system request.
     * @throws IllegalArgumentException Receiver not found.
     */
    @Override
    public final <REQ> void sendSystemMessage(final MessageDescriptor<REQ, Void> descriptor, final REQ request, final ReceiverSelector receiver) {
        channel.sendSignal(this, descriptor, request, receiver);
    }
}
