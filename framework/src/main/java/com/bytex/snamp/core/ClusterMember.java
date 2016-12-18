package com.bytex.snamp.core;

import com.bytex.snamp.Box;
import com.google.common.reflect.TypeToken;

import java.util.concurrent.ConcurrentMap;

/**
 * Represents SNAMP service that represents single SNAMP member in the cluster.
 * You can discover this service via OSGi service registry.
 * <p>
 *     You can query the following cluster-wide services:
 *     <ul>
 *         <li>{@link LongCounter} for generating unique identifiers</li>
 *         <li>{@link ConcurrentMap}&lt;{@link String}, {@link Object}&gt; for accessing data collections</li>
 *     </ul>
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 * @see LongCounter
 */
public interface ClusterMember extends ClusterMemberInfo, SupportService {
    /**
     * Represents cluster-wide generator of unique identifiers.
     */
    TypeToken<LongCounter> IDGEN_SERVICE = TypeToken.of(LongCounter.class);

    /**
     * Represents distributed map.
     */
    TypeToken<ConcurrentMap<String, Object>> MAP_SERVICE = new TypeToken<ConcurrentMap<String, Object>>() {};

    /**
     * Represents communication service.
     */
    TypeToken<Communicator> COMMUNICATION_SERVICE = TypeToken.of(Communicator.class);

    /**
     * Represents distributed box.
     */
    TypeToken<Box<Object>> BOX = new TypeToken<Box<Object>>() {};

    TypeToken<KeyValueStorage> KV_STORAGE_SERVICE = TypeToken.of(KeyValueStorage.class);

    /**
     * Marks this node as passive and execute leader election.
     */
    void resign();

    /**
     * Gets distributed service.
     * @param serviceName Service name.
     * @param serviceType Service type.
     * @param <S> Type of the service contract.
     * @return Distributed service; or {@literal null}, if service is not supported.
     * @see #IDGEN_SERVICE
     * @see #MAP_SERVICE
     */
    <S> S getService(final String serviceName, final TypeToken<S> serviceType);

    /**
     * Destroys the specified service
     * @param serviceName Name of the service to release.
     * @param serviceType Type of the service to release.
     */
    void releaseService(final String serviceName, final TypeToken<?> serviceType);
}
