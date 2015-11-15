package com.bytex.snamp.core;

import com.google.common.reflect.TypeToken;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents SNAMP service that represents single SNAMP node in the cluster.
 * You can discover this service via OSGi service registry.
 * <p>
 *     You can query the following cluster-wide services:
 *     <ul>
 *         <li>{@link SequenceNumberGenerator} for generating unique identifiers</li>
 *         <li>{@link ConcurrentMap}&lt;{@link String}, {@link Object}&gt; for accessing data collections</li>
 *     </ul>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @see SequenceNumberGenerator
 */
public interface ClusterNode extends FrameworkService {
    /**
     * Represents cluster-wide generator of unique identifiers.
     */
    TypeToken<SequenceNumberGenerator> IDGEN_SERVICE = TypeToken.of(SequenceNumberGenerator.class);

    /**
     * Represents distributed map.
     */
    TypeToken<ConcurrentMap<String, Object>> STORAGE_SERVICE = new TypeToken<ConcurrentMap<String, Object>>() {};

    /**
     * Determines whether this node is active.
     * <p>
     *   Passive SNAMP node ignores any notifications received by resource connectors.
     *   As a result, all resource adapters will not route notifications to the connected
     *   monitoring tools. But you can still read any attributes.
     * @return {@literal true}, if this node is active; otherwise, {@literal false}.
     */
    boolean isActive();

    /**
     * Marks this node as passive and execute leader election.
     */
    void resign();

    /**
     * Gets unique name of this node.
     * @return Name of the cluster node.
     */
    String getName();

    /**
     * Gets address of this node.
     * @return Address of this node.
     */
    InetSocketAddress getAddress();

    /**
     * Gets distributed service.
     * @param serviceName Service name.
     * @param serviceType Service type.
     * @param <S> Type of the service contract.
     * @return Distributed service; or {@literal null}, if service is not supported.
     */
    <S> S getService(final String serviceName, final TypeToken<S> serviceType);

    /**
     * Destroys the specified service
     * @param serviceName Name of the service to release.
     * @param serviceType Type of the service to release.
     */
    void releaseService(final String serviceName, final TypeToken<?> serviceType);
}
