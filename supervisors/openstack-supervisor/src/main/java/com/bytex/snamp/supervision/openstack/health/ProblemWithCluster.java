package com.bytex.snamp.supervision.openstack.health;

import com.bytex.snamp.supervision.health.ClusterMalfunctionStatus;
import org.openstack4j.model.senlin.Cluster;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Represents some problem with cluster.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ProblemWithCluster extends ClusterMalfunctionStatus {
    private static final long serialVersionUID = -8376473095942011064L;
    private final Level level;
    private final String details;

    private ProblemWithCluster(final String clusterName,
                       final String details,
                       final Level level){
        super(clusterName, Instant.now());
        this.level = level;
        this.details = nullToEmpty(details);
    }

    static ProblemWithCluster critical(final Cluster cluster){
        return new ProblemWithCluster(cluster.getName(), cluster.getStatusReason(), Level.CRITICAL);
    }

    static ProblemWithCluster warning(final Cluster cluster){
        return new ProblemWithCluster(cluster.getName(), cluster.getStatusReason(), Level.SUBSTANTIAL);
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

    @Override
    public int hashCode() {
        return Objects.hash(getClusterName(), getTimeStamp(), level, details);
    }

    private boolean equals(final ProblemWithCluster other){
        return other.getClusterName().equals(getClusterName()) &&
                other.getTimeStamp().equals(getTimeStamp()) &&
                other.level.equals(level) &&
                other.details.equals(details);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ProblemWithCluster && equals((ProblemWithCluster) other);
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
}
