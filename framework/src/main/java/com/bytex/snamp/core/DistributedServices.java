package com.bytex.snamp.core;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.TypeTokens;
import com.bytex.snamp.concurrent.ComputationPipeline;
import com.bytex.snamp.concurrent.LockManager;
import com.bytex.snamp.concurrent.ThreadSafeObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.reflect.TypeToken;
import org.osgi.framework.BundleContext;

import javax.management.openmbean.InvalidKeyException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a set of distributed services.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class DistributedServices {
    private static final class MessageFuture extends CompletableFuture<Serializable> implements ComputationPipeline<Serializable>, Communicator.MessageListener{
        @Override
        public void receive(final ClusterMemberInfo sender, final Serializable message, final long messageID) {
            complete(message);
        }
    }

    private static final class MessageListenerNode implements Communicator.MessageListener, Communicator.MessageFilter, SafeCloseable{
        private final Communicator.MessageListener listener;
        private final Communicator.MessageFilter filter;
        private final LockManager writeLock;
        private MessageListenerNode previous;
        private MessageListenerNode next;

        private MessageListenerNode(final LockManager writeLock){
            this(writeLock, MessageListenerNode::emptyHandler, MessageListenerNode::ignoreMessages);
        }

        private static void emptyHandler(final ClusterMemberInfo memberInfo, final Serializable message, final long messageID){

        }

        private static boolean ignoreMessages(final Serializable message, final long messageID){
            return false;
        }

        private MessageListenerNode(final LockManager writeLock, final Communicator.MessageListener listener, final Communicator.MessageFilter filter){
            this.listener = Objects.requireNonNull(listener);
            this.filter = Objects.requireNonNull(filter);
            this.writeLock = Objects.requireNonNull(writeLock);
        }

        @Override
        public void receive(final ClusterMemberInfo sender, final Serializable message, final long messageID) {
            listener.receive(sender, message, messageID);
        }

        @Override
        public boolean test(final Serializable message, final long messageID) {
            return filter.test(message, messageID);
        }

        private void setPrevious(final MessageListenerNode listener){
            previous = listener;
        }

        private void setNext(final MessageListenerNode listener){
            next = listener;
        }

        private MessageListenerNode getPrevious(){
            return previous;
        }

        @Override
        public void close() {
            try (final SafeCloseable ignored = writeLock.acquireLock(LocalCommunicator.RESOURCE_GROUP)) {
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

    private static final class LocalCommunicator extends ThreadSafeObject implements Communicator{
        private static Enum<?> RESOURCE_GROUP = SingleResourceGroup.INSTANCE;
        private final LongCounter idGenerator;
        private MessageListenerNode lastNode;

        private LocalCommunicator() {
            super(SingleResourceGroup.class);
            idGenerator = new LocalLongCounter();
            lastNode = new MessageListenerNode(writeLock);   //empty node
        }

        @Override
        public long postMessage(final Serializable message) {
            final long messageID = idGenerator.getAsLong();
            postMessage(message, messageID);
            return messageID;
        }

        private void postMessageImpl(final Serializable message, final long messageID){
            for (MessageListenerNode node = lastNode; node != null; node = node.getPrevious())
                if (node.test(message, messageID))
                    node.receive(null, message, messageID);
        }

        @Override
        public void postMessage(final Serializable message, final long messageID) {
            readLock.acceptLong(RESOURCE_GROUP, message, messageID, this::postMessageImpl);
        }

        @Override
        public Serializable receiveMessage(final MessageFilter filter, final Duration timeout) throws InterruptedException, TimeoutException {
            final MessageFuture future = new MessageFuture();
            try(final SafeCloseable ignored = addMessageListener(future, filter)){
                return future.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
            } catch (final ExecutionException e) {
                throw new AssertionError("Unexpected execution exception", e);    //should never be happened
            }
        }

        @Override
        public ComputationPipeline<? extends Serializable> receiveMessage(final MessageFilter filter) {
            final MessageFuture future = new MessageFuture();
            final SafeCloseable subscription = addMessageListener(future, filter);
            future.thenAccept(msg -> subscription.close());
            return future;
        }

        private SafeCloseable addMessageListenerImpl(final MessageListener listener, final MessageFilter filter){
            final MessageListenerNode node = new MessageListenerNode(writeLock, listener, filter);
            node.setPrevious(lastNode);
            lastNode.setNext(node);
            return lastNode = node;
        }

        @Override
        public SafeCloseable addMessageListener(final MessageListener listener, final MessageFilter filter) {
            return writeLock.apply(RESOURCE_GROUP, listener, filter, this::addMessageListenerImpl);
        }
    }

    private static final class LocalStorage extends ConcurrentHashMap<String, Object>{
        private static final long serialVersionUID = 2412615001344706359L;
    }

    private static final class LocalLongCounter extends AtomicLong implements LongCounter {
        private static final long serialVersionUID = 498408165929062468L;

        LocalLongCounter(){
            super(0L);
        }

        @Override
        public long getAsLong() {
            return getAndIncrement();
        }
    }

    private static final class InMemoryServiceCacheKey<S>{
        private final TypeToken<S> serviceType;
        private final String serviceName;

        private InMemoryServiceCacheKey(final String serviceName, final TypeToken<S> serviceType){
            this.serviceType = Objects.requireNonNull(serviceType);
            this.serviceName = Objects.requireNonNull(serviceName);
        }

        private boolean equals(final InMemoryServiceCacheKey other){
            return serviceName.equals(other.serviceName) && serviceType.equals(other.serviceType);
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof InMemoryServiceCacheKey && equals((InMemoryServiceCacheKey)other);
        }

        @Override
        public int hashCode() {
            return Objects.hash(serviceName, serviceType);
        }
    }

    //in-memory services should be stored as soft-reference. This strategy helps to avoid memory
    //leaks in long-running scenarios
    private static LoadingCache<InMemoryServiceCacheKey, Object> IN_MEMORY_SERVICES = CacheBuilder.newBuilder()
            .softValues()
            .build(new CacheLoader<InMemoryServiceCacheKey, Object>() {

                @Override
                public Object load(final InMemoryServiceCacheKey key) throws InvalidKeyException {
                    if(ClusterMember.IDGEN_SERVICE.equals(key.serviceType))
                        return new LocalLongCounter();
                    else if(ClusterMember.STORAGE_SERVICE.equals(key.serviceType))
                        return new LocalStorage();
                    else throw new InvalidKeyException(String.format("Service type %s is not supported", key.serviceType));
                }
            });

    private DistributedServices(){
        throw new InstantiationError();
    }

    private static <S> S getProcessLocalService(final String serviceName, final TypeToken<S> serviceType) {
        final InMemoryServiceCacheKey<S> key = new InMemoryServiceCacheKey<>(serviceName, serviceType);
        try {
            return TypeTokens.cast(IN_MEMORY_SERVICES.get(key), serviceType);
        } catch (final ExecutionException e) {
            return null;
        }
    }

    /**
     * Gets local ID generator that doesn't share counter across cluster.
     * @param generatorName The name of generator.
     * @return ID generator instance.
     */
    public static LongCounter getProcessLocalCounterGenerator(final String generatorName){
        return getProcessLocalService(generatorName, ClusterMember.IDGEN_SERVICE);
    }

    public static ConcurrentMap<String, Object> getProcessLocalStorage(final String collectionName){
        return getProcessLocalService(collectionName, ClusterMember.STORAGE_SERVICE);
    }

    private static <S> S processClusterNode(final BundleContext context,
                                            final Function<? super ClusterMember, S> processor,
                                            final Supplier<S> def) {
        final ServiceHolder<ClusterMember> holder = ServiceHolder.tryCreate(context, ClusterMember.class);
        if (holder != null)
            try {
                return processor.apply(holder.getService());
            } finally {
                holder.release(context);
            }
        else return def.get();
    }

    private static <S> S getService(final BundleContext context,
                                    final String serviceName,
                                    final TypeToken<S> serviceType) {
        return processClusterNode(context, node -> node.getService(serviceName, serviceType), () -> getProcessLocalService(serviceName, serviceType));
    }

    /**
     * Gets distributed {@link java.util.concurrent.ConcurrentMap}.
     * @param context Context of the caller OSGi bundle.
     * @param collectionName Name of the distributed collection.
     * @return Distributed or process-local storage.
     */
    public static ConcurrentMap<String, Object> getDistributedStorage(final BundleContext context,
                                                                            final String collectionName){
        return getService(context, collectionName, ClusterMember.STORAGE_SERVICE);
    }

    /**
     * Gets distributed {@link LongCounter}.
     * @param context Context of the caller OSGi bundle.
     * @param generatorName Name of the generator to obtain.
     * @return Distributed or process-local generator.
     */
    public static LongCounter getDistributedCounter(final BundleContext context,
                                                    final String generatorName){
        return getService(context, generatorName, ClusterMember.IDGEN_SERVICE);
    }

    /**
     * Determines whether the caller code hosted in active cluster node.
     * @param context Context of the caller bundle.
     * @return {@literal true}, the caller code hosted in active cluster node; otherwise, {@literal false}.
     */
    public static boolean isActiveNode(final BundleContext context) {
        return processClusterNode(context, ClusterMember::isActive, () -> true);
    }

    /**
     * Determines whether the called code executed in SNAMP cluster.
     * @param context Context of the caller bundle.
     * @return {@literal true}, if this method is called in clustered environment; otherwise, {@literal false}.
     */
    public static boolean isInCluster(final BundleContext context) {
        return processClusterNode(context, Objects::nonNull, () -> false);
    }

    /**
     * Gets name of the current node in the cluster.
     * @param context Context of the caller bundle.
     * @return Name of the cluster node.
     */
    public static String getLocalMemberName(final BundleContext context){
        return processClusterNode(context, ClusterMember::getName, () -> ManagementFactory.getRuntimeMXBean().getName());
    }
}
