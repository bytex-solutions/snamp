package com.bytex.snamp.core;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.ComputationPipeline;

import java.io.Serializable;
import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Represents message bus used to communicate with other SNAMP nodes in the cluster.
 * @since 2.0
 * @version 2.0
 */
public interface Communicator {
    Predicate<? super IncomingMessage> ANY_MESSAGE = msg -> true;

    /**
     * Represents message type.
     */
    enum MessageType implements Predicate<IncomingMessage>, Serializable{
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
        public final boolean test(final IncomingMessage message) {
            return equals(message.getType());
        }
    }

    /**
     * Represents incoming message.
     */
    interface IncomingMessage {
        /**
         * Gets payload of the message.
         * @return Payload of the message.
         */
        Serializable getPayload();

        /**
         * Gets sender of the message.
         * @return Sender of the message.
         */
        ClusterMemberInfo getSender();

        /**
         * Gets message identifier.
         * @return Message identifier.
         */
        long getMessageID();

        /**
         * Gets publication time of this message in Unix time format.
         * @return Publication time of this message in Unix time format.
         */
        long getTimeStamp();

        /**
         * Gets type of this message.
         * @return Type of this message.
         */
        MessageType getType();
    }
    /**
     * Represents input message box.
     */
    interface MessageBox extends Queue<IncomingMessage>, SafeCloseable{

    }

    /**
     * Generates a new unique message identifier.
     * @return Unique message identifier.
     */
    long newMessageID();

    /**
     * Sends a one-way message.
     * @param signal A message to send.
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

    IncomingMessage receiveMessage(final Predicate<? super IncomingMessage> filter, final Duration timeout) throws InterruptedException, TimeoutException;

    ComputationPipeline<? extends IncomingMessage> receiveMessage(final Predicate<? super IncomingMessage> filter);

    SafeCloseable addMessageListener(final Consumer<? super IncomingMessage> listener, final Predicate<? super IncomingMessage> filter);

    MessageBox createMessageBox(final int capacity, final Predicate<? super IncomingMessage> filter);

    MessageBox createMessageBox(final Predicate<? super IncomingMessage> filter);

    IncomingMessage sendRequest(final Serializable request, final Duration timeout) throws InterruptedException, TimeoutException;

    ComputationPipeline<? extends IncomingMessage> sendRequest(final Serializable request) throws InterruptedException;

    static Predicate<? super IncomingMessage> responseWithMessageID(final long messageID){
        return MessageType.RESPONSE.and(msg -> msg.getMessageID() == messageID);
    }
}
