package com.bytex.snamp.supervision.health;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hash(getTimeStamp(), getClusterName());
    }

    private boolean equals(final ClusterRecoveryStatus other){
        return equalsHelper(other);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ClusterRecoveryStatus && equals((ClusterRecoveryStatus) other);
    }
}
