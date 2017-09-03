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
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Represents information about local cluster member.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class LocalMember implements ClusterMember {

    //in-memory services should be stored as soft-reference. This strategy helps to avoid memory
    //leaks in long-running scenarios
    private static final LazyReference<LocalMember> INSTANCE = LazyReference.soft();
    private final LoadingCache<SharedObject.ID<?>, SharedObject> localServices;

    private LocalMember(){
        localServices = createServiceCache();
    }

    private static LoadingCache<SharedObject.ID<?>, SharedObject> createServiceCache() {
        return CacheBuilder.newBuilder()
                .build(new CacheLoader<SharedObject.ID<?>, SharedObject>() {

                    @Override
                    public SharedObject load(@Nonnull final SharedObject.ID<?> key) throws InvalidKeyException {
                        return key.createDefaultImplementation();
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

    @SuppressWarnings("unchecked")
    @Override
    public <S extends SharedObject> Optional<S> getService(final SharedObject.ID<S> objectID) {
        try {
            final SharedObject so = localServices.get(objectID);
            return so == null ? Optional.empty() : Optional.of((S) so);
        } catch (final ExecutionException e) {
            return Optional.empty();
        }
    }

    /**
     * Destroys the specified service
     *
     * @param objectID Identifier of object to release.
     */
    @Override
    public void releaseService(final SharedObject.ID<?> objectID) {
        localServices.invalidate(objectID);
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
