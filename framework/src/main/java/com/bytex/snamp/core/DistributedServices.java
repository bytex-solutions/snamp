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
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a set of distributed services.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class DistributedServices {
    private static final class LocalServiceKey<S> {
        private final String serviceName;
        private final TypeToken<S> serviceType;

        private LocalServiceKey(final String serviceName, final TypeToken<S> serviceType){
            this.serviceName = Objects.requireNonNull(serviceName);
            this.serviceType = Objects.requireNonNull(serviceType);
        }

        private boolean equals(final LocalServiceKey other){
            return serviceName.equals(other.serviceName) && serviceType.equals(other.serviceType);
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof LocalServiceKey && equals((LocalServiceKey)other);
        }

        @Override
        public int hashCode() {
            return Objects.hash(serviceName, serviceType);
        }
    }

    //in-memory services should be stored as soft-reference. This strategy helps to avoid memory
    //leaks in long-running scenarios
    private static LoadingCache<LocalServiceKey, Object> LOCAL_SERVICES = CacheBuilder.newBuilder()
            .softValues()
            .build(new CacheLoader<LocalServiceKey, Object>() {

                @Override
                public Object load(final LocalServiceKey key) throws InvalidKeyException {
                    if(ClusterMember.IDGEN_SERVICE.equals(key.serviceType))
                        return new LocalLongCounter();
                    else if(ClusterMember.STORAGE_SERVICE.equals(key.serviceType))
                        return new LocalStorage();
                    else if(ClusterMember.COMMUNICATION_SERVICE.equals(key.serviceType))
                        return new LocalCommunicator(key.serviceName);
                    else throw new InvalidKeyException(String.format("Service type %s is not supported", key.serviceType));
                }
            });

    private DistributedServices(){
        throw new InstantiationError();
    }

    private static <S> S getProcessLocalService(final String serviceName, final TypeToken<S> serviceType) {
        final LocalServiceKey<S> key = new LocalServiceKey<>(serviceName, serviceType);
        try {
            return TypeTokens.cast(LOCAL_SERVICES.get(key), serviceType);
        } catch (final ExecutionException e) {
            return null;
        }
    }

    public static Communicator getProcessLocalCommunicator(final String channelName){
        return getProcessLocalService(channelName, ClusterMember.COMMUNICATION_SERVICE);
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
