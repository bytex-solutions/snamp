package com.bytex.snamp.core.cluster;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.*;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Represents default implementation of {@link ClusterNode} in non-clustered environment.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class NonClusteredNode extends AbstractAggregator implements ClusterNode {
    private final AtomicBoolean activeNode = new AtomicBoolean(false);
    private final Logger logger = Logger.getLogger("com.bytex.snamp.core.cluster");

    @Aggregation
    @SpecialUse
    public ObjectStorage getStorage(){
        return ClusterServices.getProcessLocalObjectStorage();
    }

    @Aggregation
    @SpecialUse
    public IDGenerator getIDGeneratorService(){
        return ClusterServices.getProcessLocalIDGenerator();
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
        return activeNode.get();
    }

    /**
     * Marks this node as active or passive.
     *
     * @param value {@literal true} to activate the node in the cluster; otherwise, {@literal false}.
     */
    @Override
    public void setActive(final boolean value) {
        activeNode.set(value);
    }

    /**
     * Gets unique name of this node.
     *
     * @return Name of the cluster node.
     */
    @Override
    public String getName() {
        return ManagementFactory.getRuntimeMXBean().getName();
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    @Aggregation
    public Logger getLogger() {
        return logger;
    }
}
