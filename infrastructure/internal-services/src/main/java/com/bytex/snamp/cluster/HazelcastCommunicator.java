package com.bytex.snamp.cluster;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.core.Communicator;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class HazelcastCommunicator extends HazelcastSharedObject<ITopic<TransferObject>> implements Communicator {
    private static final class TransferObjectListener implements com.hazelcast.core.MessageListener<TransferObject>, SafeCloseable{
        private final String localMemberID;
        private final Predicate<? super HazelcastIncomingMessage> filter;
        private final Consumer<? super HazelcastIncomingMessage> listener;
        private final ITopic<TransferObject> topic;
        private final String subscription;

        private TransferObjectListener(final ITopic<TransferObject> topic, final String localMember, final Predicate<? super HazelcastIncomingMessage> filter, final MessageListener listener){
            this.localMemberID = localMember;
            this.filter = Objects.requireNonNull(filter);
            this.listener = Objects.requireNonNull(listener);
            this.topic = Objects.requireNonNull(topic);
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

    private static class MessageReceiver<V> extends CompletableFuture<V> implements com.hazelcast.core.MessageListener<TransferObject>, SafeCloseable{
        private final Predicate<? super HazelcastIncomingMessage> filter;
        private final String subscription;
        private final ITopic<TransferObject> topic;
        private final String localMemberID;
        private final Function<? super MessageEvent, ? extends V> messageParser;

        private MessageReceiver(final ITopic<TransferObject> topic,
                                final String localMember,
                                final Predicate<? super HazelcastIncomingMessage> filter,
                                final Function<? super MessageEvent, ? extends V> messageParser){
            this.filter = Objects.requireNonNull(filter);
            this.topic = Objects.requireNonNull(topic);
            this.localMemberID = localMember;
            this.subscription = topic.addMessageListener(this);
            this.messageParser = Objects.requireNonNull(messageParser);
        }

        private void onMessage(final HazelcastIncomingMessage inputMessage){
            if(filter.test(inputMessage))
                complete(messageParser.apply(inputMessage));
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

    private static final class LinkedMessageBox<V> extends LinkedBlockingQueue<V> implements MessageBox<V>, com.hazelcast.core.MessageListener<TransferObject>{
        private static final long serialVersionUID = 5833889571236077744L;
        private final Predicate<? super MessageEvent> filter;
        private final String subscription;
        private final ITopic<TransferObject> topic;
        private final String localMemberID;
        private final Function<? super MessageEvent, ? extends V> messageParser;

        private LinkedMessageBox(final ITopic<TransferObject> topic,
                                 final String localMember,
                                 final Predicate<? super MessageEvent> filter,
                                 final Function<? super MessageEvent, ? extends V> messageParser) {
            this.filter = Objects.requireNonNull(filter);
            this.topic = Objects.requireNonNull(topic);
            this.subscription = topic.addMessageListener(this);
            this.localMemberID = localMember;
            this.messageParser = Objects.requireNonNull(messageParser);
        }

        private void onMessage(final HazelcastIncomingMessage message){
            if(filter.test(message))
                add(messageParser.apply(message));
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

    private static final class FixedSizeMessageBox<V> extends ArrayBlockingQueue<V> implements MessageBox<V>, com.hazelcast.core.MessageListener<TransferObject>, SafeCloseable{
        private static final long serialVersionUID = 2173687138535015363L;
        private final Predicate<? super MessageEvent> filter;
        private final String subscription;
        private final transient ITopic<TransferObject> topic;
        private final String localMemberID;
        private final Function<? super MessageEvent, ? extends V> messageParser;

        private FixedSizeMessageBox(final int capacity,
                                    final ITopic<TransferObject> topic,
                                    final String localMember,
                                    final Predicate<? super MessageEvent> filter,
                                    final Function<? super MessageEvent, ? extends V> messageParser){
            super(capacity);
            this.filter = Objects.requireNonNull(filter);
            this.topic = Objects.requireNonNull(topic);
            this.subscription = topic.addMessageListener(this);
            this.localMemberID = localMember;
            this.messageParser = Objects.requireNonNull(messageParser);
        }

        private void onMessage(final HazelcastIncomingMessage message){
            if(filter.test(message))
                add(messageParser.apply(message));
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
    private final String localMember;

    HazelcastCommunicator(final HazelcastInstance hazelcast,
                          final String communicatorName){
        super(hazelcast, communicatorName, HazelcastInstance::getTopic);
        this.hazelcast = Objects.requireNonNull(hazelcast);
        localMember = hazelcast.getCluster().getLocalMember().getUuid();
    }

    @Override
    public long newMessageID() {
        return hazelcast.getAtomicLong("MSGID-".concat(getName())).getAndIncrement();
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
        getDistributedObject().publish(new TransferObject(new HazelcastNodeInfo(hazelcast), payload, type, messageID));
    }

    @Override
    public <V> V receiveMessage(final Predicate<? super MessageEvent> filter, final Function<? super MessageEvent, ? extends V> messageParser, final Duration timeout) throws InterruptedException, TimeoutException {
        try(final MessageReceiver<V> receiver = receiveMessage(filter, messageParser)) {
            final Callable<V> callable = timeout == null ? receiver::get : () -> receiver.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
            return callable.call();
        } catch (final InterruptedException | TimeoutException e){
            throw e;
        } catch (final Exception e){
            throw new UncheckedExecutionException(e);
        }
    }

    @Override
    public <V> MessageReceiver<V> receiveMessage(final Predicate<? super MessageEvent> filter, final Function<? super MessageEvent, ? extends V> messageParser) {
        return new MessageReceiver<>(getDistributedObject(), localMember, filter, messageParser);
    }

    @Override
    public TransferObjectListener addMessageListener(final MessageListener listener, final Predicate<? super MessageEvent> filter) {
        return new TransferObjectListener(getDistributedObject(), localMember, filter, listener);
    }

    @Override
    public <V> FixedSizeMessageBox<V> createMessageBox(final int capacity, final Predicate<? super MessageEvent> filter, final Function<? super MessageEvent, ? extends V> messageParser) {
        return new FixedSizeMessageBox<>(capacity, getDistributedObject(), localMember, filter, messageParser);
    }

    @Override
    public <V> LinkedMessageBox<V> createMessageBox(final Predicate<? super MessageEvent> filter, final Function<? super MessageEvent, ? extends V> messageParser) {
        return new LinkedMessageBox<>(getDistributedObject(), localMember, filter, messageParser);
    }

    @Override
    public <V> V sendRequest(final Serializable request, final Function<? super MessageEvent, ? extends V> messageParser, final Duration timeout) throws InterruptedException, TimeoutException {
        final long messageID = newMessageID();
        try (final MessageReceiver<V> receiver = receiveMessage(Communicator.responseWithMessageID(messageID), messageParser)) {
            sendMessage(request, MessageType.REQUEST, messageID);
            final Callable<V> callable = timeout == null ? receiver::get : () -> receiver.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
            return callable.call();
        } catch (final InterruptedException | TimeoutException e){
            throw e;
        } catch (final Exception e){
            throw new UncheckedExecutionException(e);
        }
    }

    @Override
    public <V> MessageReceiver<V> sendRequest(final Serializable request, final Function<? super MessageEvent, ? extends V> messageParser) {
        final long messageID = newMessageID();
        final MessageReceiver<V> receiver = receiveMessage(Communicator.responseWithMessageID(messageID), messageParser);
        sendMessage(request, MessageType.REQUEST, messageID);
        return receiver;
    }
}
