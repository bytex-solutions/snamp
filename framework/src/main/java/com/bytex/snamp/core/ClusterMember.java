package com.bytex.snamp.core;

import java.util.Optional;

/**
 * Represents SNAMP service that represents single SNAMP member in the cluster.
 * You can discover this service via OSGi service registry.
 * <p>
 *     You can query the following cluster-wide services:
 *     <ul>
 *         <li>{@link SharedCounter} for generating unique identifiers</li>
 *         <li>{@link KeyValueStorage} for accessing data collections</li>
 *         <li>{@link SharedObject} for accessing single object</li>
 *     </ul>
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 * @see SharedCounter
 */
public interface ClusterMember extends ClusterMemberInfo, SupportService {


    /**
     * Marks this node as passive and execute leader election.
     */
    void resign();

    /**
     * Gets distributed service.
     *
     * @param serviceName Service name.
     * @param serviceType Service type.
     * @param <S>         Type of the service contract.
     * @return Distributed service.
     *
     * @see SharedObjectType#COUNTER
     * @see SharedObjectType#KV_STORAGE
     * @see SharedObjectType#COMMUNICATOR
     * @see SharedObjectType#BOX
     */
    <S extends SharedObject> Optional<S> getService(final String serviceName, final SharedObjectType<S> serviceType);

    /**
     * Destroys the specified service
     *
     * @param serviceName Name of the service to release.
     * @param serviceType Type of the service to release.
     */
    void releaseService(final String serviceName, final SharedObjectType<?> serviceType);
}
