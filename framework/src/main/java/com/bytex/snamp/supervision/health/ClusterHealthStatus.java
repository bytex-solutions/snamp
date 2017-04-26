package com.bytex.snamp.supervision.health;

import com.bytex.snamp.connector.health.HealthStatus;

/**
 * Represents health status of the resource group acting as a cluster.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class ClusterHealthStatus extends ResourceGroupHealthStatus {
    private static final long serialVersionUID = 5045918984341655519L;
    private ClusterMalfunctionStatus clusterMalfunction;

    public void setClusterMalfunction(final ClusterMalfunctionStatus status){
        clusterMalfunction = status;
    }

    /**
     * Gets accumulated health status of entire group of resources.
     *
     * @return Accumulated health status.
     */
    @Override
    public HealthStatus getStatus() {
        return clusterMalfunction == null ? super.getStatus() : clusterMalfunction;
    }
}
