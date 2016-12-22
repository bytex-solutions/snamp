package com.bytex.snamp.core;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.management.openmbean.InvalidKeyException;
import java.util.Objects;
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
    private static final class LocalServiceKey<S extends SharedObject> extends SharedObjectDefinition<S> {
        private final String serviceName;

        private LocalServiceKey(final String serviceName, final SharedObjectDefinition<S> definition) {
            super(definition);
            this.serviceName = serviceName;
        }

        private boolean represents(final SharedObjectDefinition<?> definition){
            return Objects.equals(getType(), definition.getType()) && isPersistent() == definition.isPersistent();
        }

        private boolean equals(final LocalServiceKey<?> other){
            return serviceName.equals(other.serviceName) && Objects.equals(getType(), other.getType()) && isPersistent() == other.isPersistent();
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof LocalServiceKey<?> && equals((LocalServiceKey<?>) other);
        }

        @Override
        public int hashCode() {
            return Objects.hash(serviceName, getType(), isPersistent());
        }

        @Override
        public String toString() {
            return "LocalServiceKey{" +
                    "persistent=" + isPersistent() +
                    ", objectType=" + getType() +
                    ", serviceName=" + serviceName +
                    '}';
        }
    }

    //in-memory services should be stored as soft-reference. This strategy helps to avoid memory
    //leaks in long-running scenarios
    private static LoadingCache<LocalServiceKey<?>, SharedObject> LOCAL_SERVICES = CacheBuilder.newBuilder()
            .softValues()
            .build(new CacheLoader<LocalServiceKey<?>, SharedObject>() {

                @Override
                public SharedObject load(@Nonnull final LocalServiceKey<?> key) throws InvalidKeyException {
                    if(key.represents(ClusterMember.SHARED_COUNTER))
                        return new LocalCounter(key.serviceName);
                    else if(key.represents(ClusterMember.COMMUNICATOR))
                        return new LocalCommunicator(key.serviceName);
                    else if(key.represents(ClusterMember.SHARED_BOX))
                        return new LocalBox(key.serviceName);
                    else if(key.represents(ClusterMember.KV_STORAGE))
                        return new LocalKeyValueStorage(key.serviceName);
                    else throw new InvalidKeyException(String.format("Service %s is not supported", key));
                }
            });

    private DistributedServices(){
        throw new InstantiationError();
    }

    private static <S extends SharedObject> S getProcessLocalService(final String serviceName, final SharedObjectDefinition<S> serviceType) {
        final LocalServiceKey<S> key = new LocalServiceKey<>(serviceName, serviceType);
        try {
            return serviceType.cast(LOCAL_SERVICES.get(key));
        } catch (final ExecutionException e) {
            return null;
        }
    }

    public static Communicator getProcessLocalCommunicator(final String channelName){
        return getProcessLocalService(channelName, ClusterMember.COMMUNICATOR);
    }

    public static SharedBox getProcessLocalBox(final String boxName){
        return getProcessLocalService(boxName, ClusterMember.SHARED_BOX);
    }

    /**
     * Gets local ID generator that doesn't share counter across cluster.
     * @param generatorName The name of generator.
     * @return ID generator instance.
     */
    public static SharedCounter getProcessLocalCounter(final String generatorName){
        return getProcessLocalService(generatorName, ClusterMember.SHARED_COUNTER);
    }

    public static KeyValueStorage getProcessLocalStorage(final String storageName){
        return getProcessLocalService(storageName, ClusterMember.KV_STORAGE);
    }

    private static <S> S processClusterNode(final BundleContext context,
                                            final Function<? super ClusterMember, S> processor,
                                            final Supplier<S> def) {
        if(context == null) return def.get();
        final ServiceHolder<ClusterMember> holder = ServiceHolder.tryCreate(context, ClusterMember.class);
        if (holder != null)
            try {
                return processor.apply(holder.getService());
            } finally {
                holder.release(context);
            }
        else return def.get();
    }

    private static <S extends SharedObject> S getService(final BundleContext context,
                                    final String serviceName,
                                    final SharedObjectDefinition<S> serviceType) {
        return processClusterNode(context, node -> node.getService(serviceName, serviceType), () -> getProcessLocalService(serviceName, serviceType));
    }

    /**
     * Gets distributed {@link Communicator}.
     * @param context Context of the caller OSGi bundle.
     * @param channelName Name of the communicator.
     * @return Distributed or process-local communicator.
     */
    public static Communicator getDistributedCommunicator(final BundleContext context, final String channelName){
        return getService(context, channelName, ClusterMember.COMMUNICATOR);
    }

    /**
     * Gets distributed {@link KeyValueStorage}.
     * @param context Context of the caller OSGi bundle.
     * @param collectionName Name of the distributed collection.
     * @return Distributed or process-local storage.
     */
    public static KeyValueStorage getDistributedStorage(final BundleContext context,
                                                                            final String collectionName, final boolean persistent){
        return getService(context, collectionName, persistent ? ClusterMember.PERSISTENT_KV_STORAGE : ClusterMember.KV_STORAGE);
    }

    /**
     * Gets distributed {@link SharedCounter}.
     * @param context Context of the caller OSGi bundle.
     * @param generatorName Name of the generator to obtain.
     * @return Distributed or process-local generator.
     */
    public static SharedCounter getDistributedCounter(final BundleContext context,
                                                      final String generatorName){
        return getService(context, generatorName, ClusterMember.SHARED_COUNTER);
    }

    /**
     * Gets distributed {@link SharedBox}.
     * @param context Context of the caller OSGi bundle.
     * @param boxName Name of the generator to obtain.
     * @return Distributed or process-local generator.
     */
    public static SharedBox getDistributedBox(final BundleContext context, final String boxName){
        return getService(context, boxName, ClusterMember.SHARED_BOX);
    }

    /**
     * Determines whether the caller code hosted in active cluster node.
     * @param context Context of the caller bundle.
     * @return {@literal true}, the caller code hosted in active cluster node; otherwise, {@literal false}.
     */
    public static boolean isActiveNode(final BundleContext context) {
        return processClusterNode(context, ClusterMember::isActive, LocalMember.INSTANCE::isActive);
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
        return processClusterNode(context, ClusterMember::getName, LocalMember.INSTANCE::getName);
    }
}
