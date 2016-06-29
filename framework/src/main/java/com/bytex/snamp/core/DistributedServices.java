package com.bytex.snamp.core;

import com.bytex.snamp.TypeTokens;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.reflect.TypeToken;
import org.osgi.framework.BundleContext;

import javax.management.openmbean.InvalidKeyException;
import java.lang.management.ManagementFactory;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a set of distributed services.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class DistributedServices {
    private static final class InMemoryStorage extends ConcurrentHashMap<String, Object>{
        private static final long serialVersionUID = 2412615001344706359L;
    }

    private static final class InMemoryLongCounter extends AtomicLong implements LongCounter {
        private static final long serialVersionUID = 498408165929062468L;

        InMemoryLongCounter(){
            super(0L);
        }

        @Override
        public long increment() {
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
            return serviceType.hashCode() ^ serviceName.hashCode();
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
                        return new InMemoryLongCounter();
                    else if(ClusterMember.STORAGE_SERVICE.equals(key.serviceType))
                        return new InMemoryStorage();
                    else throw new InvalidKeyException(String.format("Service type %s is not supported", key.serviceType));
                }
            });

    private DistributedServices(){
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
        return processClusterNode(context, member -> true, () -> false);
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
