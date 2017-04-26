package com.bytex.snamp.supervision.health;

import com.bytex.snamp.connector.health.MalfunctionStatus;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Objects;

/**
 * Indicates that the cluster with resources is in malfunction state.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class ClusterMalfunctionStatus extends MalfunctionStatus {
    private static final long serialVersionUID = -4762866679904079839L;
    private final String clusterName;

    protected ClusterMalfunctionStatus(@Nonnull final String clusterName, @Nonnull final Instant timeStamp) {
        super(timeStamp);
        this.clusterName = Objects.requireNonNull(clusterName);
    }

    /**
     * Gets name of the cluster.
     * @return Name of the cluster.
     */
    public final String getClusterName(){
        return clusterName;
    }

    protected final boolean like(final ClusterMalfunctionStatus status){
        return status.clusterName.equals(clusterName) && status.getLevel().equals(getLevel());
    }
}
