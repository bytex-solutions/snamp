package com.bytex.snamp.core;

import com.google.common.reflect.TypeToken;

import java.util.concurrent.ConcurrentMap;

/**
 * Represents SNAMP service that represents single SNAMP member in the cluster.
 * You can discover this service via OSGi service registry.
 * <p>
 *     You can query the following cluster-wide services:
 *     <ul>
 *         <li>{@link SharedCounter} for generating unique identifiers</li>
 *         <li>{@link ConcurrentMap}&lt;{@link String}, {@link Object}&gt; for accessing data collections</li>
 *     </ul>
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 * @see SharedCounter
 */
public interface ClusterMember extends ClusterMemberInfo, SupportService {
    /**
     * Represents cluster-wide generator of unique identifiers.
     */
    Class<SharedCounter> SHARED_COUNTER = SharedCounter.class;

    /**
     * Represents distributed map.
     */
    Class<SharedMap> SHARED_MAP = SharedMap.class;

    /**
     * Represents communication service.
     */
    Class<Communicator> COMMUNICATOR = Communicator.class;

    /**
     * Represents distributed box.
     */
    Class<SharedBox> SHARED_BOX = SharedBox.class;

    Class<KeyValueStorage> KV_STORAGE_SERVICE = KeyValueStorage.class;

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
     * @see #SHARED_COUNTER
     * @see #SHARED_MAP
     */
    <S extends SharedObject> S getService(final String serviceName, final Class<S> serviceType);

    /**
     * Destroys the specified service
     * @param serviceName Name of the service to release.
     * @param serviceType Type of the service to release.
     */
    void releaseService(final String serviceName, final Class<? extends SharedObject> serviceType);
}
