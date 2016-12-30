package com.bytex.snamp.core;

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
     * Represents definition of non-persistent cluster-wide generator of unique identifiers.
     */
    SharedObjectDefinition<SharedCounter> SHARED_COUNTER = new SharedObjectDefinition<>(SharedCounter.class, false);

    /**
     * Represents definition of non-durable communication service.
     */
    SharedObjectDefinition<Communicator> COMMUNICATOR = new SharedObjectDefinition<>(Communicator.class, false);

    /**
     * Represents definition of distributed non-persistent scalar storage.
     */
    SharedObjectDefinition<SharedBox> SHARED_BOX = new SharedObjectDefinition<>(SharedBox.class, false);

    /**
     * Represents definition of distributed non-persistent key/value storage.
     */
    SharedObjectDefinition<KeyValueStorage> KV_STORAGE = new SharedObjectDefinition<>(KeyValueStorage.class, false);

    /**
     * Represents definition of distributed persistent key/value storage.
     */
    SharedObjectDefinition<KeyValueStorage> PERSISTENT_KV_STORAGE = new SharedObjectDefinition<>(KeyValueStorage.class, true);


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
     * @return Distributed service; or {@literal null}, if service is not supported.
     * @see #SHARED_COUNTER
     * @see #KV_STORAGE
     * @see #COMMUNICATOR
     * @see #SHARED_BOX
     */
    <S extends SharedObject> S getService(final String serviceName, final SharedObjectDefinition<S> serviceType);

    /**
     * Destroys the specified service
     *
     * @param serviceName Name of the service to release.
     * @param serviceType Type of the service to release.
     */
    void releaseService(final String serviceName, final SharedObjectDefinition<?> serviceType);
}
