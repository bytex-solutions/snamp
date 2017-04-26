package com.bytex.snamp.supervision.health;

import com.bytex.snamp.connector.health.HealthStatus;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Locale;

/**
 * Indicates that the cluster is in recovery state and may be unavailable.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ClusterRecoveryStatus extends ClusterMalfunctionStatus {
    private static final long serialVersionUID = 3186397258235973954L;

    public ClusterRecoveryStatus(final String clusterName, final Instant timeStamp) {
        super(clusterName, timeStamp);
    }

    public ClusterRecoveryStatus(final String clusterName){
        this(clusterName, Instant.now());
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
        return "Cluster is in recovery state and may be unavailable";
    }

    /**
     * Gets malfunction level.
     *
     * @return Malfunction level.
     */
    @Nonnull
    @Override
    public Level getLevel() {
        return Level.MODERATE;
    }

    /**
     * Determines whether this health status is similar to the specified status.
     *
     * @param status Health status.
     * @return {@literal true}, if this health status is similar to the specified status.
     * @implSpec This method has weaker semantics than {@link #equals(Object)}.
     * Similarity means that only significant data in health status used are equal.
     * Volatile data such as {@link #getTimeStamp()} should be ignored.
     */
    @Override
    public boolean like(final HealthStatus status) {
        return status instanceof ClusterRecoveryStatus && super.like((ClusterRecoveryStatus) status);
    }
}
