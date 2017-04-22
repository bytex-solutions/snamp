package com.bytex.snamp.core;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.management.openmbean.InvalidKeyException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * Represents a set of distributed services.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class DistributedServices {
    @Immutable
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
                        return new InMemoryCounter(key.serviceName);
                    else if(key.represents(ClusterMember.COMMUNICATOR))
                        return new InMemoryCommunicator(key.serviceName);
                    else if(key.represents(ClusterMember.SHARED_BOX))
                        return new InMemoryBox(key.serviceName);
                    else if(key.represents(ClusterMember.KV_STORAGE))
                        return new InMemoryKeyValueStorage(key.serviceName);
                    else if(key.represents(ClusterMember.PERSISTENT_KV_STORAGE))
                        return new FileBasedKeyValueStorage(key.serviceName);
                    else throw new InvalidKeyException(String.format("Service %s is not supported", key));
                }
            });

    private DistributedServices(){
        throw new InstantiationError();
    }

    public static <S extends SharedObject> S getProcessLocalObject(final String serviceName, final SharedObjectDefinition<S> serviceType) {
        final LocalServiceKey<S> key = new LocalServiceKey<>(serviceName, serviceType);
        try {
            return serviceType.cast(LOCAL_SERVICES.get(key));
        } catch (final ExecutionException e) {
            return null;
        }
    }

    private static <S> Optional<S> processClusterNode(final BundleContext context,
                                            final Function<? super ClusterMember, S> processor) {
        return Optional.ofNullable(context).flatMap(ctx -> ServiceHolder.tryCreate(ctx, ClusterMember.class).map(holder -> {
            try {
                return processor.apply(holder.get());
            } finally {
                holder.release(ctx);
            }
        }));
    }

    public static <S extends SharedObject> S getDistributedObject(final BundleContext context,
                                                                  final String serviceName,
                                                                  final SharedObjectDefinition<S> serviceType) {
        return processClusterNode(context, node -> node.getService(serviceName, serviceType))
                .orElseGet(() -> getProcessLocalObject(serviceName, serviceType));
    }

    /**
     * Determines whether the caller code hosted in active cluster node.
     * @param context Context of the caller bundle.
     * @return {@literal true}, the caller code hosted in active cluster node; otherwise, {@literal false}.
     */
    public static boolean isActiveNode(final BundleContext context) {
        return processClusterNode(context, ClusterMember::isActive).orElseGet(LocalMember.INSTANCE::isActive);
    }

    /**
     * Determines whether the called code executed in SNAMP cluster.
     * @param context Context of the caller bundle.
     * @return {@literal true}, if this method is called in clustered environment; otherwise, {@literal false}.
     */
    public static boolean isInCluster(final BundleContext context) {
        return processClusterNode(context, Objects::nonNull).orElse(false);
    }

    /**
     * Gets name of the current node in the cluster.
     * @param context Context of the caller bundle.
     * @return Name of the cluster node.
     */
    public static String getLocalMemberName(final BundleContext context){
        return processClusterNode(context, ClusterMember::getName).orElseGet(LocalMember.INSTANCE::getName);
    }
}
