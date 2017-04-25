package com.bytex.snamp.supervision.openstack.health;

import com.bytex.snamp.connector.health.ClusterMalfunctionStatus;
import org.openstack4j.model.senlin.Cluster;

import java.time.Instant;
import java.util.Locale;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents some problem with cluster.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ProblemWithCluster extends ClusterMalfunctionStatus {
    private static final long serialVersionUID = -8376473095942011064L;
    private final boolean critical;
    private final String details;

    private ProblemWithCluster(final String clusterName,
                       final String details,
                       final boolean critical){
        super(clusterName, Instant.now());
        this.critical = critical;
        if(isNullOrEmpty(details))
            this.details = critical ? "Cluster crashed" : "Cluster nodes partially unavailable";
        else
            this.details = details;
    }

    static ProblemWithCluster critical(final Cluster cluster){
        return new ProblemWithCluster(cluster.getName(), cluster.getStatusReason(), true);
    }

    static ProblemWithCluster warning(final Cluster cluster){
        return new ProblemWithCluster(cluster.getName(), cluster.getStatusReason(), false);
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
        return details;
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
