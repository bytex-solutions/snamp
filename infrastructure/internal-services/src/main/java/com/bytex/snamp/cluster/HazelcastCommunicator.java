package com.bytex.snamp.cluster;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.ComputationPipeline;
import com.bytex.snamp.core.Communicator;
import com.hazelcast.core.*;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 *
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class HazelcastCommunicator implements Communicator {
    private static final class TransferObjectListener implements MessageListener<TransferObject>, SafeCloseable{
        private final String localMemberID;
        private final Predicate<? super HazelcastIncomingMessage> filter;
        private final Consumer<? super HazelcastIncomingMessage> listener;
        private final ITopic<TransferObject> topic;
        private final String subscription;

        private TransferObjectListener(final HazelcastInstance hazelcast, final String communicatorName, final Predicate<? super HazelcastIncomingMessage> filter, final Consumer<? super HazelcastIncomingMessage> listener){
            this.localMemberID = hazelcast.getCluster().getLocalMember().getUuid();
            this.filter = Objects.requireNonNull(filter);
            this.listener = Objects.requireNonNull(listener);
            this.topic = hazelcast.getTopic(communicatorName);
            this.subscription = topic.addMessageListener(this);
        }

        private void onMessage(final HazelcastIncomingMessage message){
            if(filter.test(message))
                listener.accept(message);
        }

        @Override
        public void onMessage(final Message<TransferObject> hzMessage) {
            final HazelcastIncomingMessage msg = new HazelcastIncomingMessage(hzMessage);
            msg.detectRemoteMessage(localMemberID);
            onMessage(msg);
        }

        @Override
        public void close() {
            topic.removeMessageListener(subscription);
        }
    }

    private static class MessageReceiver extends CompletableFuture<HazelcastIncomingMessage> implements ComputationPipeline<HazelcastIncomingMessage>, MessageListener<TransferObject>, SafeCloseable{
        private final Predicate<? super HazelcastIncomingMessage> filter;
        private final String subscription;
        private final ITopic<TransferObject> topic;
        private final String localMemberID;

        private MessageReceiver(final HazelcastInstance hazelcast, final String communicatorName, final Predicate<? super HazelcastIncomingMessage> filter){
            this.filter = Objects.requireNonNull(filter);
            this.topic = hazelcast.getTopic(communicatorName);
            this.localMemberID = hazelcast.getCluster().getLocalMember().getUuid();
            this.subscription = topic.addMessageListener(this);
        }

        private void onMessage(final HazelcastIncomingMessage inputMessage){
            if(filter.test(inputMessage))
                complete(inputMessage);
        }

        @Override
        public final void onMessage(final Message<TransferObject> hzMessage) {
            final HazelcastIncomingMessage msg = new HazelcastIncomingMessage(hzMessage);
            msg.detectRemoteMessage(localMemberID);
            onMessage(msg);
        }

        @Override
        public void close() {
            topic.removeMessageListener(subscription);
        }
    }

    private static final class LinkedMessageBox extends LinkedBlockingQueue<IncomingMessage> implements MessageBox, MessageListener<TransferObject>{
        private static final long serialVersionUID = 5833889571236077744L;
        private final Predicate<? super IncomingMessage> filter;
        private final String subscription;
        private final ITopic<TransferObject> topic;
        private final String localMemberID;

        private LinkedMessageBox(final HazelcastInstance hazelcast, final String communicatorName, final Predicate<? super IncomingMessage> filter){
            this.filter = Objects.requireNonNull(filter);
            this.topic = hazelcast.getTopic(communicatorName);
            this.subscription = topic.addMessageListener(this);
            this.localMemberID = hazelcast.getCluster().getLocalMember().getUuid();
        }

        private void onMessage(final HazelcastIncomingMessage message){
            if(filter.test(message))
                add(message);
        }

        @Override
        public void onMessage(final Message<TransferObject> hzMessage) {
            final HazelcastIncomingMessage msg = new HazelcastIncomingMessage(hzMessage);
            msg.detectRemoteMessage(localMemberID);
            onMessage(msg);
        }

        @Override
        public void close() {
            topic.removeMessageListener(subscription);
        }
    }

    private static final class FixedSizeMessageBox extends ArrayBlockingQueue<IncomingMessage> implements MessageBox, MessageListener<TransferObject>, SafeCloseable{
        private static final long serialVersionUID = 2173687138535015363L;
        private final Predicate<? super IncomingMessage> filter;
        private final String subscription;
        private final transient ITopic<TransferObject> topic;
        private final String localMemberID;

        private FixedSizeMessageBox(final HazelcastInstance hazelcast, final String communicatorName, final Predicate<? super IncomingMessage> filter, final int capacity){
            super(capacity);
            this.filter = Objects.requireNonNull(filter);
            this.topic = hazelcast.getTopic(communicatorName);
            this.subscription = topic.addMessageListener(this);
            this.localMemberID = hazelcast.getCluster().getLocalMember().getUuid();
        }

        private void onMessage(final HazelcastIncomingMessage message){
            if(filter.test(message))
                add(message);
        }

        @Override
        public void onMessage(final Message<TransferObject> message) {
            final HazelcastIncomingMessage msg = new HazelcastIncomingMessage(message);
            msg.detectRemoteMessage(localMemberID);
            onMessage(msg);
        }

        @Override
        public void close() {
            topic.removeMessageListener(subscription);
        }
    }

    private final HazelcastInstance hazelcast;
    private final String communicatorName;
    private final BooleanSupplier activeNodeDetector;

    HazelcastCommunicator(final HazelcastInstance hazelcast,
                          final BooleanSupplier activeNodeDetector,
                          final String communicatorName){
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.activeNodeDetector = Objects.requireNonNull(activeNodeDetector);
        this.communicatorName = communicatorName;
    }

    @Override
    public long newMessageID() {
        return hazelcast.getAtomicLong("MSGID-".concat(communicatorName)).getAndIncrement();
    }

    @Override
    public void sendSignal(final Serializable signal) {
        sendMessage(signal, MessageType.SIGNAL, newMessageID());
    }

    @Override
    public void sendMessage(final Serializable payload, final MessageType type) {
        sendMessage(payload, type, newMessageID());
    }

    private HazelcastNodeInfo createLocalNodeInfo(){
        return new HazelcastNodeInfo(hazelcast, activeNodeDetector.getAsBoolean());
    }

    @Override
    public void sendMessage(final Serializable payload, final MessageType type, final long messageID) {
        final ITopic<TransferObject> topic = hazelcast.getTopic(communicatorName);
        topic.publish(new TransferObject(createLocalNodeInfo(), payload, type, messageID));
    }

    @Override
    public IncomingMessage receiveMessage(final Predicate<? super IncomingMessage> filter, final Duration timeout) throws InterruptedException, TimeoutException {
        try(final MessageReceiver receiver = receiveMessage(filter)) {
            return timeout == null ? receiver.get() : receiver.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (final ExecutionException e) {
            throw new AssertionError("Unexpected exception", e);
        }
    }

    @Override
    public MessageReceiver receiveMessage(final Predicate<? super IncomingMessage> filter) {
        return new MessageReceiver(hazelcast, communicatorName, filter);
    }

    @Override
    public TransferObjectListener addMessageListener(final Consumer<? super IncomingMessage> listener, final Predicate<? super IncomingMessage> filter) {
        return new TransferObjectListener(hazelcast, communicatorName, filter, listener);
    }

    @Override
    public FixedSizeMessageBox createMessageBox(final int capacity, final Predicate<? super IncomingMessage> filter) {
        return new FixedSizeMessageBox(hazelcast, communicatorName, filter, capacity);
    }

    @Override
    public LinkedMessageBox createMessageBox(final Predicate<? super IncomingMessage> filter) {
        return new LinkedMessageBox(hazelcast, communicatorName, filter);
    }

    @Override
    public IncomingMessage sendRequest(final Serializable request, final Duration timeout) throws InterruptedException, TimeoutException {
        final long messageID = newMessageID();
        try (final MessageReceiver receiver = receiveMessage(Communicator.responseWithMessageID(messageID))) {
            sendMessage(request, MessageType.REQUEST, messageID);
            return timeout == null ? receiver.get() : receiver.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (final ExecutionException e) {
            throw new AssertionError("Unexpected exception", e);
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
