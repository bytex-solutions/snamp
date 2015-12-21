package com.bytex.snamp.core;

import com.bytex.snamp.TypeTokens;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
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

/**
 * Represents a set of distributed services.
 * @author Roman Sakno
 * @version 1.0
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
                                            final Supplier<S> def){
        ServiceHolder<ClusterMember> holder = null;
        try{
            holder = new ServiceHolder<>(context, ClusterMember.class);
            return processor.apply(holder.getService());
        } catch (final IllegalArgumentException ignored){ //service not found
            return def.get();
        }finally {
            if(holder != null)
                holder.release(context);
        }
    }

    private static <S> S getService(final BundleContext context,
                                    final String serviceName,
                                    final TypeToken<S> serviceType) {
        return processClusterNode(context, new Function<ClusterMember, S>() {
            @Override
            public S apply(final ClusterMember node) {
                return node.getService(serviceName, serviceType);
            }
        }, new Supplier<S>() {
            @Override
            public S get() {
                return getProcessLocalService(serviceName, serviceType);
            }
        });
    }

    /**
     * Gets distributed {@link java.util.concurrent.ConcurrentMap}.
     * @param context Context of the caller OSGi bundle.
     * @param collectionName Name of the distributed collection.
     * @return Distributed or process-lAbstractocal storage.
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
        return processClusterNode(context, new Function<ClusterMember, Boolean>() {
            @Override
            public Boolean apply(final ClusterMember node) {
                return node.isActive();
            }
        }, Suppliers.ofInstance(Boolean.TRUE));
    }

    /**
     * Determines whether the called code executed in SNAMP cluster.
     * @param context Context of the caller bundle.
     * @return {@literal true}, if this method is called in clustered environment; otherwise, {@literal false}.
     */
    public static boolean isInCluster(final BundleContext context) {
        return processClusterNode(context, Functions.constant(Boolean.TRUE), Suppliers.ofInstance(Boolean.FALSE));
    }

    /**
     * Gets name of the current node in the cluster.
     * @param context Context of the caller bundle.
     * @return Name of the cluster node.
     */
    public static String getLocalMemberName(final BundleContext context){
        return processClusterNode(context, new Function<ClusterMember, String>() {
            @Override
            public String apply(final ClusterMember node) {
                return node.getName();
            }
        }, new Supplier<String>() {
            @Override
            public String get() {
                return ManagementFactory.getRuntimeMXBean().getName();
            }
        });
    }
}
