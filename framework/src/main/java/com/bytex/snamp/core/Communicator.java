package com.bytex.snamp.core;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.ComputationPipeline;

import java.io.Serializable;
import java.time.Duration;
import java.util.EventListener;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.ObjLongConsumer;

/**
 * Represents message bus used to communicate with other SNAMP nodes in the cluster.
 * @since 2.0
 * @version 2.0
 */
public interface Communicator {
    /**
     * Represents message listener.
     */
    @FunctionalInterface
    interface MessageListener extends EventListener{
        void receive(final ClusterMemberInfo memberInfo, final Serializable message, final long messageID);
    }

    /**
     * Represents message filter.
     */
    @FunctionalInterface
    interface MessageFilter{
        boolean test(final Serializable message, final long messageID);

        default MessageFilter and(final MessageFilter filter) {
            return (message, id) -> test(message, id) && filter.test(message, id);
        }
    }

    /**
     * Represents message producer.
     */
    interface MessageProducer{
        /**
         * Sends a message.
         * @param message A message to send. Cannot be {@literal null}.
         * @return Message ID.
         */
        long postMessage(final Serializable message);

        /**
         * Sends a message.
         * @param message A message to send. Cannot be {@literal null}.
         * @param messageID User-defined message ID.
         */
        void postMessage(final Serializable message, final long messageID);
    }

    /**
     * Represents message consumer.
     */
    interface MessageConsumer{
        Serializable receiveMessage(final MessageFilter filter, final Duration timeout) throws InterruptedException, TimeoutException;

        ComputationPipeline<? extends Serializable> receiveMessage(final MessageFilter filter);

        SafeCloseable addMessageListener(final MessageListener listener, final MessageFilter filter);
    }

    MessageProducer getProducer();

    /**
     * Gets buffered consumer of input messages.
     * @param bufferSize Max size of buffer.
     * @return A new consumer.
     */
    MessageConsumer getConsumer(final int bufferSize);

    /**
     * Gets consumer without buffer.
     * @return A new consumer.
     */
    MessageConsumer getConsumer();

    static Serializable sendMessage(final Communicator communicator, final Serializable message, final Duration timeout) throws InterruptedException, TimeoutException {
        final MessageProducer producer = communicator.getProducer();
        final MessageConsumer consumer = communicator.getConsumer();
        final long messageID = producer.postMessage(message);
        return consumer.receiveMessage((msg, id) -> messageID == id, timeout);
    }

    static ComputationPipeline<? extends Serializable> sendMessage(final Communicator communicator, final Serializable message) throws InterruptedException {
        final MessageProducer producer = communicator.getProducer();
        final MessageConsumer consumer = communicator.getConsumer();
        final long messageID = producer.postMessage(message);
        return consumer.receiveMessage((msg, id) -> messageID == id);
    }
}
