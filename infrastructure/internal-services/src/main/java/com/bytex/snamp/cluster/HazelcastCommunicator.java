package com.bytex.snamp.cluster;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.ComputationPipeline;
import com.bytex.snamp.core.ClusterMemberInfo;
import com.bytex.snamp.core.Communicator;
import com.hazelcast.core.*;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 *
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class HazelcastCommunicator implements Communicator {
    private static class MessageReceiver extends CompletableFuture<HazelcastIncomingMessage> implements ComputationPipeline<HazelcastIncomingMessage>, MessageListener<TransferObject>{
        private final Predicate<? super HazelcastIncomingMessage> filter;

        private MessageReceiver(final Predicate<? super HazelcastIncomingMessage> filter){
            this.filter = Objects.requireNonNull(filter);
        }

        void onMessage(final HazelcastIncomingMessage inputMessage){
            if(filter.test(inputMessage))
                complete(inputMessage);
        }

        @Override
        public final void onMessage(final Message<TransferObject> hzMessage) {
            onMessage(new HazelcastIncomingMessage(hzMessage));
        }
    }

    private static final class LinkedMessageBox extends LinkedBlockingQueue<IncomingMessage> implements MessageBox, MessageListener<TransferObject>{
        private static final long serialVersionUID = 5833889571236077744L;
        private final Predicate<? super IncomingMessage> filter;
        private final String subscription;
        private final ITopic<TransferObject> topic;

        private LinkedMessageBox(final ITopic<TransferObject> topic, final Predicate<? super IncomingMessage> filter){
            this.filter = Objects.requireNonNull(filter);
            this.topic = Objects.requireNonNull(topic);
            this.subscription = topic.addMessageListener(this);
        }

        private void onMessage(final HazelcastIncomingMessage message){
            if(filter.test(message))
                add(message);
        }

        @Override
        public void onMessage(final Message<TransferObject> hzMessage) {
            onMessage(new HazelcastIncomingMessage(hzMessage));
        }

        @Override
        public void close() {
            topic.removeMessageListener(subscription);
        }
    }

    private static final class FixedSizeMessageBox extends ArrayBlockingQueue<IncomingMessage> implements MessageBox, MessageListener<TransferObject>{
        private static final long serialVersionUID = 2173687138535015363L;
        private final Predicate<? super IncomingMessage> filter;
        private final String subscription;
        private final ITopic<TransferObject> topic;

        private FixedSizeMessageBox(final ITopic<TransferObject> topic, final Predicate<? super IncomingMessage> filter, final int capacity){
            super(capacity);
            this.filter = Objects.requireNonNull(filter);
            this.topic = Objects.requireNonNull(topic);
            this.subscription = topic.addMessageListener(this);
        }

        private void onMessage(final HazelcastIncomingMessage message){
            if(filter.test(message))
                add(message);
        }

        @Override
        public void onMessage(final Message<TransferObject> message) {
            onMessage(new HazelcastIncomingMessage(message));
        }

        @Override
        public void close() {
            topic.removeMessageListener(subscription);
        }
    }

    private final ClusterMemberInfo senderInfo;
    private final IAtomicLong idGenerator;
    private final ITopic<TransferObject> topic;

    HazelcastCommunicator(final HazelcastInstance hazelcast,
                          final String communicatorName,
                          final ClusterMemberInfo sender){
        this.senderInfo = Objects.requireNonNull(sender);
        this.idGenerator = hazelcast.getAtomicLong("MSGID-".concat(communicatorName));
        this.topic = hazelcast.getTopic(communicatorName);
    }

    @Override
    public long newMessageID() {
        return idGenerator.getAndIncrement();
    }

    @Override
    public void sendSignal(final Serializable signal) {
        sendMessage(signal, MessageType.SIGNAL, newMessageID());
    }

    @Override
    public void sendMessage(final Serializable payload, final MessageType type) {
        sendMessage(payload, type, newMessageID());
    }

    @Override
    public void sendMessage(final Serializable payload, final MessageType type, final long messageID) {
        topic.publish(new TransferObject(senderInfo, payload, type, messageID));
    }

    @Override
    public IncomingMessage receiveMessage(final Predicate<? super IncomingMessage> filter, final Duration timeout) throws InterruptedException, TimeoutException {
        final MessageReceiver receiver = new MessageReceiver(filter);
        final String subscription = topic.addMessageListener(receiver);
        try {
            return receiver.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (final ExecutionException e) {
            throw new AssertionError("Unexpected exception", e);
        } finally {
            topic.removeMessageListener(subscription);
        }
    }

    @Override
    public MessageReceiver receiveMessage(final Predicate<? super IncomingMessage> filter) {
        final MessageReceiver receiver = new MessageReceiver(filter);
        final String subscription = topic.addMessageListener(receiver);
        receiver.thenRun(() -> topic.removeMessageListener(subscription));   //remove subscription after receiving message
        return receiver;
    }

    @Override
    public SafeCloseable addMessageListener(final Consumer<? super IncomingMessage> listener, final Predicate<? super IncomingMessage> filter) {
        final MessageListener<TransferObject> hzListener = hzMessage -> {
            final HazelcastIncomingMessage msg = new HazelcastIncomingMessage(hzMessage);
            if (filter.test(msg))
                listener.accept(msg);
        };
        final String subscription = topic.addMessageListener(hzListener);
        return () -> topic.removeMessageListener(subscription);
    }

    @Override
    public FixedSizeMessageBox createMessageBox(final int capacity, final Predicate<? super IncomingMessage> filter) {
        return new FixedSizeMessageBox(topic, filter, capacity);
    }

    @Override
    public LinkedMessageBox createMessageBox(final Predicate<? super IncomingMessage> filter) {
        return new LinkedMessageBox(topic, filter);
    }

    @Override
    public IncomingMessage sendRequest(final Serializable request, final Duration timeout) throws InterruptedException, TimeoutException {
        final long messageID = newMessageID();
        final MessageReceiver receiver = new MessageReceiver(Communicator.responseWithMessageID(messageID));
        final String subscription = topic.addMessageListener(receiver);
        try {
            sendMessage(request, MessageType.REQUEST, messageID);
            return receiver.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (final ExecutionException e) {
            throw new AssertionError("Unexpected exception", e);
        } finally {
            topic.removeMessageListener(subscription);
        }
    }

    @Override
    public MessageReceiver sendRequest(final Serializable request) throws InterruptedException {
        final long messageID = newMessageID();
        final MessageReceiver receiver = receiveMessage(Communicator.responseWithMessageID(messageID));
        sendMessage(request, MessageType.REQUEST, messageID);
        return receiver;
    }
}
