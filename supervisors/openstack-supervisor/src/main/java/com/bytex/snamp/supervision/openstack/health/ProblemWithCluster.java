package com.bytex.snamp.supervision.openstack.health;

import com.bytex.snamp.connector.health.ClusterMalfunctionStatus;

import java.util.Locale;

/**
 * Represents some problem with cluster.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ProblemWithCluster extends ClusterMalfunctionStatus {
    private static final long serialVersionUID = -8376473095942011064L;
    private final boolean critical;

    public ProblemWithCluster(final String clusterName,
                              final boolean critical){
        super(clusterName);
        this.critical = critical;
    }

    /**
     * Returns the localized description of this object.
     *
     * @param locale The locale of the description. If it is {@literal null} then returns description
     *               in the default locale.
     * @return The localized description of this object.
     */
    @Override
    public String toString(final Locale locale) {
        return critical ?
                "Cluster crashed" :
                "Cluster nodes partially unavailable";
    }

    /**
     * Indicates that resource is in critical state (potentially unavailable).
     *
     * @return {@literal true}, if managed resource is in critical state; otherwise, {@literal false}.
     */
    @Override
    public boolean isCritical() {
        return critical;
    }
}
