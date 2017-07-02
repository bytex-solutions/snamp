package com.bytex.snamp.core;

import com.bytex.snamp.SafeCloseable;

import java.io.Serializable;
import java.time.Duration;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents message bus used to communicate with other SNAMP nodes in the cluster.
 * @since 2.0
 * @version 2.0
 */
public interface Communicator extends SharedObject {
    Predicate<? super MessageEvent> ANY_MESSAGE = msg -> true;
    Predicate<MessageEvent> REMOTE_MESSAGE = MessageEvent::isRemote;

    /**
     * Represents message type.
     */
    enum MessageType implements Predicate<MessageEvent>, Serializable {
        /**
         * Represents request message.
         */
        REQUEST,
        /**
         * Represents response message.
         */
        RESPONSE,
        /**
         * Represents one-way message without response
         */
        SIGNAL;

        @Override
        public final boolean test(final MessageEvent message) {
            return equals(message.getType());
        }
    }

    /**
     * Represents incoming message.
     */
    abstract class MessageEvent extends EventObject {
        private static final long serialVersionUID = 6709318850279093072L;

        protected MessageEvent(final ClusterMemberInfo sender) {
            super(sender);
        }

        /**
         * Gets sender of the message.
         * @return Sender of the message; or {@literal null} when communicator is not in cluster.
         */
        @Override
        public ClusterMemberInfo getSource() {
            return (ClusterMemberInfo) super.getSource();
        }

        /**
         * Gets payload of the message.
         * @return Payload of the message.
         */
        public abstract Serializable getPayload();

        /**
         * Gets message identifier.
         * @return Message identifier.
         */
        public abstract long getMessageID();

        /**
         * Gets publication time of this message in Unix time format.
         * @return Publication time of this message in Unix time format.
         */
        public abstract long getTimeStamp();

        /**
         * Gets type of this message.
         * @return Type of this message.
         */
        public abstract MessageType getType();

        /**
         * Determines whether this message was sent by remote publisher.
         * @return {@literal true}, if this message was sent by remote peer; {@literal false}, if this message was sent from the current process.
         */
        public abstract boolean isRemote();
    }

    @FunctionalInterface
    interface MessageListener extends EventListener, Consumer<MessageEvent>{
        /**
         * Receives message.
         * @param message A message to receive.
         */
        @Override
        void accept(final MessageEvent message);
    }

    /**
     * Represents input message box.
     * @param <V> Type of processed messages.
     */
    interface MessageBox<V> extends Queue<V>, SafeCloseable{

    }

    /**
     * Generates a new unique message identifier.
     * @return Unique message identifier.
     */
    long newMessageID();

    /**
     * Sends a one-way message.
     * @param signal A message to send.
     * @see MessageType#SIGNAL
     */
    void sendSignal(final Serializable signal);

    /**
     * Sends a message of the specified type.
     * @param payload Payload of the message to send. Cannot be {@literal null}.
     * @param type Type of the message.
     */
    void sendMessage(final Serializable payload, final MessageType type);

    /**
     * Sends a message of the specified type.
     * @param payload A message to send. Cannot be {@literal null}.
     * @param type Type of the message.
     * @param messageID User-defined message ID.
     */
    void sendMessage(final Serializable payload, final MessageType type, final long messageID);

    <V> V receiveMessage(final Predicate<? super MessageEvent> filter, final Function<? super MessageEvent, ? extends V> messageParser, final Duration timeout) throws InterruptedException, TimeoutException;

    <V> CompletableFuture<V> receiveMessage(final Predicate<? super MessageEvent> filter, final Function<? super MessageEvent, ? extends V> messageParser);

    SafeCloseable addMessageListener(final MessageListener listener, final Predicate<? super MessageEvent> filter);

    <V> MessageBox<V> createMessageBox(final int capacity, final Predicate<? super MessageEvent> filter, final Function<? super MessageEvent, ? extends V> messageParser);

    <V> MessageBox<V> createMessageBox(final Predicate<? super MessageEvent> filter, final Function<? super MessageEvent, ? extends V> messageParser);

    <V> V sendRequest(final Serializable request, final Function<? super MessageEvent, ? extends V> messageParser, final Duration timeout) throws InterruptedException, TimeoutException;

    <V> CompletableFuture<V> sendRequest(final Serializable request, final Function<? super MessageEvent, ? extends V> messageParser) throws InterruptedException;

    static Predicate<MessageEvent> responseWithMessageID(final long messageID){
        return MessageType.RESPONSE.and(msg -> msg.getMessageID() == messageID);
    }

    static String getPayloadAsString(final MessageEvent message){
        return Objects.toString(message.getPayload());
    }

    static Predicate<MessageEvent> responseWithPayload(final Serializable expected){
        return MessageType.RESPONSE.and(msg -> Objects.equals(msg.getPayload(), expected));
    }
}
