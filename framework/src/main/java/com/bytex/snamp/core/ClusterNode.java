package com.bytex.snamp.core;

/**
 * Represents SNAMP service that represents single SNAMP node in the cluster.
 * You can discover this service via OSGi service registry.
 * <p>
 *     You can query the following cluster-wide services:
 *     <ul>
 *         <li>{@link IDGenerator} for generating unique identifiers</li>
 *         <li>{@link ObjectStorage} for accessing data collections</li>
 *     </ul>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @see IDGenerator
 */
public interface ClusterNode extends FrameworkService {

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
     * Marks this node as active or passive.
     * @param value {@literal true} to activate the node in the cluster; otherwise, {@literal false}.
     */
    void setActive(final boolean value);

    /**
     * Gets unique name of this node.
     * @return Name of the cluster node.
     */
    String getName();
}
