package com.bytex.snamp.core;

import com.bytex.snamp.MethodStub;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.*;
import org.osgi.framework.BundleContext;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Represents cluster-local communicator.
 * @since 2.0
 * @version 2.0
 */
final class LocalCommunicator extends ThreadSafeObject implements Communicator {
    private static final class LocalIncomingMessage implements IncomingMessage{
        private final long messageID;
        private final Serializable payload;
        private final long timeStamp;
        private final MessageType messageType;

        private LocalIncomingMessage(final Serializable payload, final long messageID, final MessageType type){
            this.messageID = messageID;
            this.payload = Objects.requireNonNull(payload);
            this.timeStamp = System.currentTimeMillis();
            this.messageType = Objects.requireNonNull(type);
        }

        @Override
        public Serializable getPayload() {
            return payload;
        }

        @Override
        public ClusterMemberInfo getSender() {
            return null;
        }

        @Override
        public long getMessageID() {
            return messageID;
        }

        @Override
        public long getTimeStamp() {
            return timeStamp;
        }

        @Override
        public MessageType getType() {
            return messageType;
        }

        @Override
        public boolean isRemote() {
            return false;
        }
    }

    interface MessageListenerNode extends Consumer<IncomingMessage>, Predicate<IncomingMessage>, SafeCloseable{
        MessageListenerNode getPrevious();
        MessageListenerNode getNext();
        void setNext(final MessageListenerNode value);
        void setPrevious(final MessageListenerNode value);
    }

    /**
     * Represents position of the listener node in the listener chain.
     */
    private static class NodePosition extends AtomicReference<LockManager> implements Predicate<IncomingMessage>{
        private final Predicate<? super IncomingMessage> filter;
        private MessageListenerNode previous;
        private MessageListenerNode next;

        private NodePosition(final LockManager writeLock, final Predicate<? super IncomingMessage> filter){
            super(Objects.requireNonNull(writeLock));
            this.filter = Objects.requireNonNull(filter);
        }

        public final void setNext(final MessageListenerNode value){
            next = value;
        }

        public final void setPrevious(final MessageListenerNode value){
            previous = value;
        }

        public final MessageListenerNode getPrevious(){
            return previous;
        }

        public final MessageListenerNode getNext(){
            return next;
        }

        @Override
        public final boolean test(final IncomingMessage message) {
            return filter.test(message);
        }

        final void removeNode() {
            final LockManager writeLock = getAndSet(null);
            if (writeLock != null)
                try (final SafeCloseable ignored = writeLock.acquireLock(SingleResourceGroup.INSTANCE)) {
                    //remove this node from the chain
                    if (next != null)
                        next.setPrevious(previous);
                    if (previous != null)
                        previous.setNext(next);
                } finally {
                    previous = next = null;
                }
        }
    }

    private static final class FixedSizeMessageBox<V> extends ArrayBlockingQueue<V> implements MessageBox<V>, MessageListenerNode{
        private final NodePosition position;
        private final Function<? super IncomingMessage, ? extends V> messageParser;

        private FixedSizeMessageBox(final int capacity,
                                    final LockManager writeLock,
                                    final Predicate<? super IncomingMessage> filter,
                                    final Function<? super IncomingMessage, ? extends V> messageParser) {
            super(capacity);
            this.position = new NodePosition(writeLock, filter);
            this.messageParser = Objects.requireNonNull(messageParser);
        }

        @Override
        public void close() {
            position.removeNode();
        }

        @Override
        public void accept(final IncomingMessage message) {
            add(messageParser.apply(message));
        }

        @Override
        public boolean test(final IncomingMessage message) {
            return position.test(message);
        }

        @Override
        public MessageListenerNode getPrevious() {
            return position.getPrevious();
        }

        @Override
        public void setNext(final MessageListenerNode value) {
            position.setNext(value);
        }

        @Override
        public MessageListenerNode getNext() {
            return position.getNext();
        }

        @Override
        public void setPrevious(final MessageListenerNode value) {
            position.setPrevious(value);
        }
    }

    private static final class LinkedMessageBox<V> extends LinkedBlockingQueue<V> implements MessageBox<V>, MessageListenerNode{
        private final NodePosition position;
        private final Function<? super IncomingMessage, ? extends V> messageParser;

        private LinkedMessageBox(final LockManager writeLock,
                                 final Predicate<? super IncomingMessage> filter,
                                 final Function<? super IncomingMessage, ? extends V> messageParser){
            position = new NodePosition(writeLock, filter);
            this.messageParser = Objects.requireNonNull(messageParser);
        }

        @Override
        public void close() {
            position.removeNode();
        }

        @Override
        public void accept(final IncomingMessage message) {
            add(messageParser.apply(message));
        }

        @Override
        public boolean test(final IncomingMessage message) {
            return position.test(message);
        }

        @Override
        public MessageListenerNode getPrevious() {
            return position.getPrevious();
        }

        @Override
        public void setNext(final MessageListenerNode value) {
            position.setNext(value);
        }

        @Override
        public MessageListenerNode getNext() {
            return position.getNext();
        }

        @Override
        public void setPrevious(final MessageListenerNode value) {
            position.setPrevious(value);
        }
    }

    private static final class MessageFuture<V> extends CompletableFuture<V> implements ComputationPipeline<V>, MessageListenerNode{
        private final NodePosition position;
        private final Function<? super IncomingMessage, ? extends V> messageParser;

        private MessageFuture(final LockManager writeLock,
                              final Predicate<? super IncomingMessage> filter,
                              final Function<? super IncomingMessage, ? extends V> messageParser){
            position = new NodePosition(writeLock, filter);
            this.messageParser = Objects.requireNonNull(messageParser);
        }

        @Override
        public void accept(final IncomingMessage message) {
            complete(messageParser.apply(message));
        }

        @Override
        public void close() {
            position.removeNode();
        }

        @Override
        public boolean test(final IncomingMessage message) {
            return position.test(message);
        }

        @Override
        public MessageListenerNode getPrevious() {
            return position.getPrevious();
        }

        @Override
        public void setNext(final MessageListenerNode value) {
            position.setNext(value);
        }

        @Override
        public MessageListenerNode getNext() {
            return position.getNext();
        }

        @Override
        public void setPrevious(final MessageListenerNode value) {
            position.setPrevious(value);
        }
    }

    private static abstract class TerminalListenerNode implements MessageListenerNode{
        @Override
        @MethodStub
        public final void close() {

        }

        @Override
        @MethodStub
        public final void accept(final IncomingMessage message) {

        }

        @Override
        @MethodStub
        public final boolean test(final IncomingMessage message) {
            return false;
        }
    }

    private static final class HeadMessageListenerNode extends TerminalListenerNode{
        private volatile MessageListenerNode next;

        private HeadMessageListenerNode() {
        }

        @Override
        public MessageListenerNode getPrevious() {
            return null;
        }

        @Override
        public MessageListenerNode getNext() {
            return next;
        }

        @Override
        public void setNext(final MessageListenerNode value) {
            next = value;
        }

        @Override
        @MethodStub
        public void setPrevious(final MessageListenerNode value) {

        }
    }

    private static final class TailMessageListenerNode extends TerminalListenerNode{
        private volatile MessageListenerNode previous;

        @Override
        public MessageListenerNode getPrevious() {
            return previous;
        }

        @Override
        public MessageListenerNode getNext() {
            return null;
        }

        @Override
        @MethodStub
        public void setNext(final MessageListenerNode value) {

        }

        @Override
        public void setPrevious(final MessageListenerNode value) {
            previous = value;
        }
    }

    private static final class MessageListenerHolder extends NodePosition implements MessageListenerNode{
        private final Consumer<? super IncomingMessage> listener;

        private MessageListenerHolder(final LockManager writeLock, final Consumer<? super IncomingMessage> listener, final Predicate<? super IncomingMessage> filter){
            super(writeLock, filter);
            this.listener = Objects.requireNonNull(listener);
        }

        @Override
        public void accept(final IncomingMessage message) {
            listener.accept(message);
        }

        @Override
        public String toString() {
            return listener.toString();
        }

        @Override
        public void close() {
            removeNode();
        }
    }

    private static final LazyValue<ExecutorService> LOCAL_MESSAGE_SENDER = LazyValueFactory.THREAD_SAFE_SOFT_REFERENCED.of(() -> newSingleThreadExecutor(new GroupedThreadFactory("LocalCommunicator")));
    private final LongCounter idGenerator;
    private final HeadMessageListenerNode firstNode;
    private final TailMessageListenerNode lastNode;

    LocalCommunicator() {
        super(SingleResourceGroup.class);
        idGenerator = new LocalLongCounter();
        firstNode = new HeadMessageListenerNode();
        lastNode = new TailMessageListenerNode();   //tail empty node
        firstNode.setNext(lastNode);
        lastNode.setPrevious(firstNode);
    }

    private ExecutorService getExecutorService() {
        final BundleContext context = getBundleContextOfObject(this);
        ExecutorService result = context == null ? LOCAL_MESSAGE_SENDER.get() : ThreadPoolRepository.getDefaultThreadPool(context);
        if (result == null)
            result = LOCAL_MESSAGE_SENDER.get();
        return result;
    }

    @Override
    public long newMessageID() {
        return idGenerator.getAsLong();
    }

    @Override
    public void sendSignal(final Serializable signal) {
        sendMessage(signal, MessageType.SIGNAL);
    }

    @Override
    public void sendMessage(final Serializable payload, final MessageType type) {
        sendMessage(payload, type, newMessageID());
    }

    private void sendMessageImpl(final Serializable payload, final long messageID, final MessageType type) {
        final IncomingMessage message = new LocalIncomingMessage(payload, messageID, type);
        for (MessageListenerNode node = firstNode.getNext(); !(node instanceof TailMessageListenerNode); node = node.getNext())
            if (node.test(message)) {
                final Consumer<? super IncomingMessage> listener = node;
                getExecutorService().execute(() -> listener.accept(message));
            }
    }

    @Override
    public void sendMessage(final Serializable payload, final MessageType type, final long messageID) {
        readLock.accept(SingleResourceGroup.INSTANCE, this, communicator -> communicator.sendMessageImpl(payload, messageID, type));
    }

    @Override
    public <V> V receiveMessage(final Predicate<? super IncomingMessage> filter, final Function<? super IncomingMessage, ? extends V> messageParser, final Duration timeout) throws InterruptedException, TimeoutException {
        try (final MessageFuture<V> future = receiveMessage(filter, messageParser, false)) {
            return timeout == null ? future.get() : future.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (final ExecutionException e) {
            throw new AssertionError("Unexpected execution exception", e);    //should never be happened
        }
    }

    private <V> MessageFuture<V> receiveMessage(final Predicate<? super IncomingMessage> filter, final Function<? super IncomingMessage, ? extends V> messageParser, final boolean closeWhenComplete) {
        final MessageFuture<V> result = addMessageListener(lock -> new MessageFuture<V>(lock, filter, messageParser));
        if(closeWhenComplete)
            result.thenRun(result::close);
        return result;
    }

    @Override
    public <V> MessageFuture<V> receiveMessage(final Predicate<? super IncomingMessage> filter, final Function<? super IncomingMessage, ? extends V> messageParser) {
        return receiveMessage(filter, messageParser, true);
    }

    private <N extends MessageListenerNode> N addMessageListenerImpl(final Function<? super LockManager, ? extends N> nodeFactory){
        final N node = nodeFactory.apply(writeLock);
        final MessageListenerNode oldPrevious = lastNode.getPrevious();
        //link last node
        node.setNext(lastNode);
        lastNode.setPrevious(node);
        //link old previous node
        node.setPrevious(oldPrevious);
        oldPrevious.setNext(node);
        return node;
    }

    private <N extends MessageListenerNode> N addMessageListener(final Function<? super LockManager, ? extends N> nodeFactory) {
        return writeLock.apply(SingleResourceGroup.INSTANCE, this, nodeFactory, LocalCommunicator::addMessageListenerImpl);
    }

    @Override
    public MessageListenerHolder addMessageListener(final Consumer<? super IncomingMessage> listener, final Predicate<? super IncomingMessage> filter) {
        return addMessageListener(lock -> new MessageListenerHolder(lock, listener, filter));
    }

    @Override
    public <V> FixedSizeMessageBox<V> createMessageBox(final int capacity, final Predicate<? super IncomingMessage> filter, final Function<? super IncomingMessage, ? extends V> messageParser) {
        return addMessageListener(lock -> new FixedSizeMessageBox<V>(capacity, lock, filter, messageParser));
    }

    @Override
    public <V> LinkedMessageBox<V> createMessageBox(final Predicate<? super IncomingMessage> filter, final Function<? super IncomingMessage, ? extends V> messageParser) {
        return addMessageListener(lock -> new LinkedMessageBox<V>(lock, filter, messageParser));
    }

    @Override
    public <V> V sendRequest(final Serializable message, final Function<? super IncomingMessage, ? extends V> messageParser, final Duration timeout) throws InterruptedException, TimeoutException {
        final long messageID = newMessageID();
        try (final MessageFuture<V> receiver = receiveMessage(Communicator.responseWithMessageID(messageID), messageParser, false)) {
            sendMessage(message, MessageType.REQUEST, messageID);
            return timeout == null ? receiver.get() : receiver.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (final ExecutionException e) {
            throw new AssertionError("Unexpected exception", e);
        }
    }

    @Override
    public <V> MessageFuture<V> sendRequest(final Serializable message, final Function<? super IncomingMessage, ? extends V> messageParser) throws InterruptedException {
        final long messageID = newMessageID();
        final MessageFuture<V> receiver = receiveMessage(Communicator.responseWithMessageID(messageID), messageParser, true);
        sendMessage(message, MessageType.REQUEST, messageID);
        return receiver;
    }

    boolean hasNoSubscribers(){
        return firstNode.getNext() == lastNode;
    }
}
