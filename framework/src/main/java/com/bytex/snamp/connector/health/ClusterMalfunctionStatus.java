package com.bytex.snamp.connector.health;

import java.time.Instant;

/**
 * Indicates that the cluster with resources is in malfunction state.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class ClusterMalfunctionStatus extends MalfunctionStatus {
    private static final long serialVersionUID = -4762866679904079839L;
    private final String clusterName;
    static final int SEVERITY = Integer.MAX_VALUE / 2;

    protected ClusterMalfunctionStatus(final String clusterName, final Instant timeStamp) {
        this(SEVERITY, clusterName, timeStamp);
    }

    ClusterMalfunctionStatus(final int severity, final String clusterName, final Instant timeStamp) {
        super(severity, timeStamp);
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
