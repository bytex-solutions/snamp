package com.bytex.snamp.connector;

/**
 * Represents connector of the managed resource acting as a member of some cluster.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface ClusteredResourceConnector extends ManagedResourceConnector {
    /**
     * Gets name of the clustered component.
     * @return Name of the clustered component.
     */
    default String getComponentName(){
        return getConfiguration().getGroupName();
    }

    /**
     * Gets name of the node in the cluster.
     * @return Name of the node in the cluster.
     */
    String getInstanceName();
}
