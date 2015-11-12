package com.bytex.snamp.core.cluster;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.core.ClusterNode;
import com.hazelcast.core.HazelcastInstance;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ClusteredNodeImpl extends AbstractAggregator implements ClusterNode {
    private static final String NODE_STATUS_PROPERTY = "snampNodeStatus";
    private final HazelcastInstance hazelcast;
    private final Logger logger = Logger.getLogger("com.bytex.snamp.core.cluster");

    public ClusteredNodeImpl(final HazelcastInstance hazelcastInstance){
        hazelcast = Objects.requireNonNull(hazelcastInstance);
        hazelcast.getCluster().getLocalMember().setBooleanAttribute(NODE_STATUS_PROPERTY, true);
    }

    /**
     * Determines whether this node is active.
     * <p/>
     * Passive SNAMP node ignores any notifications received by resource connectors.
     * As a result, all resource adapters will not route notifications to the connected
     * monitoring tools. But you can still read any attributes.
     *
     * @return {@literal true}, if this node is active; otherwise, {@literal false}.
     */
    @Override
    public boolean isActive() {
        return hazelcast.getCluster().getLocalMember().getBooleanAttribute(NODE_STATUS_PROPERTY);
    }

    /**
     * Marks this node as active or passive.
     *
     * @param value {@literal true} to activate the node in the cluster; otherwise, {@literal false}.
     */
    @Override
    public void setActive(final boolean value) {
        hazelcast.getCluster().getLocalMember().setBooleanAttribute(NODE_STATUS_PROPERTY, value);
    }

    /**
     * Gets unique name of this node.
     *
     * @return Name of the cluster node.
     */
    @Override
    public String getName() {
        return hazelcast.getName();
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return logger;
    }
}
