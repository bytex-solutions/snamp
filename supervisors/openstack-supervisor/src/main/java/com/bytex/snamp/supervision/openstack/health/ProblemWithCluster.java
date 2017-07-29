package com.bytex.snamp.supervision.openstack.health;

import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.supervision.health.ClusterMalfunctionStatus;
import org.openstack4j.model.senlin.Cluster;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Locale;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Represents some problem with cluster.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class ProblemWithCluster extends ClusterMalfunctionStatus {
    private static final long serialVersionUID = -8376473095942011064L;
    private final Level level;
    private final String details;

    private ProblemWithCluster( final String details,
                       final Level level){
        super(Instant.now());
        this.level = level;
        this.details = nullToEmpty(details);
    }

    static ProblemWithCluster critical(final Cluster cluster){
        return new ProblemWithCluster(cluster.getStatusReason(), Level.CRITICAL);
    }

    static ProblemWithCluster warning(final Cluster cluster){
        return new ProblemWithCluster(cluster.getStatusReason(), Level.SUBSTANTIAL);
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
     * Gets malfunction level.
     *
     * @return Malfunction level.
     */
    @Nonnull
    @Override
    public Level getLevel() {
        return level;
    }

    private boolean like(final ProblemWithCluster status){
        return super.like(status) && status.details.equals(details);
    }

    @Override
    public boolean like(final HealthStatus status) {
        return status instanceof ProblemWithCluster && like((ProblemWithCluster) status);
    }
}
