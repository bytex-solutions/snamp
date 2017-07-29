package com.bytex.snamp.core;

import com.bytex.snamp.Convert;
import com.bytex.snamp.concurrent.LazyReference;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import javax.management.openmbean.InvalidKeyException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Represents information about local cluster member.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class LocalMember implements ClusterMember {
    private static final class LocalServiceKey<S extends SharedObject> extends SharedObjectType<S> {
        private final String serviceName;

        private LocalServiceKey(final String serviceName, final SharedObjectType<S> definition) {
            super(definition);
            this.serviceName = serviceName;
        }

        private boolean represents(final SharedObjectType<?> definition){
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
    private static final LazyReference<LocalMember> INSTANCE = LazyReference.soft();
    private final LoadingCache<LocalServiceKey<?>, SharedObject> localServices;

    private LocalMember(){
        localServices = createServiceCache();
    }

    private static LoadingCache<LocalServiceKey<?>, SharedObject> createServiceCache() {
        return CacheBuilder.newBuilder()
                .build(new CacheLoader<LocalServiceKey<?>, SharedObject>() {

                    @Override
                    public SharedObject load(@Nonnull final LocalServiceKey<?> key) throws InvalidKeyException {
                        if (key.represents(SharedObjectType.COUNTER))
                            return new InMemoryCounter(key.serviceName);
                        else if (key.represents(SharedObjectType.COMMUNICATOR))
                            return new InMemoryCommunicator(key.serviceName);
                        else if (key.represents(SharedObjectType.BOX))
                            return new InMemoryBox(key.serviceName);
                        else if (key.represents(SharedObjectType.KV_STORAGE))
                            return new InMemoryKeyValueStorage(key.serviceName);
                        else if (key.represents(SharedObjectType.PERSISTENT_KV_STORAGE))
                            return new FileBasedKeyValueStorage(key.serviceName);
                        else throw new InvalidKeyException(String.format("Service %s is not supported", key));
                    }
                });
    }

    static LocalMember getInstance(){
        return INSTANCE.lazyGet(LocalMember::new);
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public String getName() {
        return ManagementFactory.getRuntimeMXBean().getName();
    }

    @Override
    public InetSocketAddress getAddress() {
        return null;
    }

    @Override
    public Map<String, ?> getAttributes() {
        return ImmutableMap.of();
    }

    /**
     * Marks this node as passive and execute leader election.
     */
    @Override
    public void resign() {

    }

    /**
     * Gets distributed service.
     *
     * @param serviceName Service name.
     * @param serviceType Service type.
     * @return Distributed service.
     * @see SharedObjectType#COUNTER
     * @see SharedObjectType#KV_STORAGE
     * @see SharedObjectType#COMMUNICATOR
     * @see SharedObjectType#BOX
     */
    @Override
    public <S extends SharedObject> Optional<S> getService(final String serviceName, final SharedObjectType<S> serviceType) {
        final LocalServiceKey<S> key = new LocalServiceKey<>(serviceName, serviceType);
        SharedObject obj;
        try {
            obj = localServices.get(key);
        } catch (final ExecutionException e) {
            obj = null;
        }
        return serviceType.cast(obj);
    }

    /**
     * Destroys the specified service
     *
     * @param serviceName Name of the service to release.
     * @param serviceType Type of the service to release.
     */
    @Override
    public void releaseService(final String serviceName, final SharedObjectType<?> serviceType) {
        localServices.invalidate(new LocalServiceKey<>(serviceName, serviceType));
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the requested object.
     * @return An instance of the aggregated object.
     */
    @Override
    public <T> Optional<T> queryObject(@Nonnull final Class<T> objectType) {
        return Convert.toType(this, objectType);
    }
}
