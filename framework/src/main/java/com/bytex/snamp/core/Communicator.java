package com.bytex.snamp.core;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.ComputationPipeline;

import java.io.Serializable;
import java.time.Duration;
import java.util.EventListener;
import java.util.concurrent.TimeoutException;

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
        void receive(final ClusterMemberInfo sender, final Serializable message, final long messageID);
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

    Serializable receiveMessage(final MessageFilter filter, final Duration timeout) throws InterruptedException, TimeoutException;

    ComputationPipeline<? extends Serializable> receiveMessage(final MessageFilter filter);

    SafeCloseable addMessageListener(final MessageListener listener, final MessageFilter filter);

    static Serializable sendMessage(final Communicator communicator, final Serializable message, final Duration timeout) throws InterruptedException, TimeoutException {
        final long messageID = communicator.postMessage(message);
        return communicator.receiveMessage((msg, id) -> messageID == id, timeout);
    }

    static ComputationPipeline<? extends Serializable> sendMessage(final Communicator communicator, final Serializable message) throws InterruptedException {
        final long messageID = communicator.postMessage(message);
        return communicator.receiveMessage((msg, id) -> messageID == id);
    }
}
