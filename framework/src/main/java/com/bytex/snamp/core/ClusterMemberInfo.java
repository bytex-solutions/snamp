package com.bytex.snamp.core;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * Represents information about SNAMP cluster member.
 * @since 2.0
 * @version 2.0
 */
public interface ClusterMemberInfo {
    /**
     * Determines whether this node is active.
     * <p>
     *   Passive SNAMP node ignores any notifications received by resource connector.
     *   As a result, all gateways will not route notifications to the connected
     *   monitoring tools. But you can still read any attributes.
     * @return {@literal true}, if this node is active; otherwise, {@literal false}.
     */
    boolean isActive();

    /**
     * Gets unique name of this member.
     * @return Name of the cluster node.
     */
    String getName();

    /**
     * Gets address of this node.
     * @return Address of this node.
     */
    InetSocketAddress getAddress();

    /**
     * Gets attributes associated with the cluster member.
     * @return Attributes of the cluster member.
     */
    Map<String, ?> getAttributes();
}
