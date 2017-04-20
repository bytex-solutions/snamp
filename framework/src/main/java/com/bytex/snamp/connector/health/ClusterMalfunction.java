package com.bytex.snamp.connector.health;

/**
 * Indicates that the cluster with resources is in malfunction state.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class ClusterMalfunction extends MalfunctionStatus {
    private static final long serialVersionUID = -4762866679904079839L;
    private final String clusterName;

    protected ClusterMalfunction(final String clusterName,
                                 final int statusCode,
                                 final boolean critical) {
        super("", statusCode, critical);
        this.clusterName = clusterName;
    }

    /**
     * Gets name of the cluster.
     * @return Name of the cluster.
     */
    public final String getClusterName(){
        return clusterName;
    }
}
