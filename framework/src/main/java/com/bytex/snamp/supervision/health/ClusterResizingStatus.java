package com.bytex.snamp.supervision.health;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/**
 * Indicates that the cluster is resizing.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ClusterResizingStatus extends ClusterMalfunctionStatus {
    private static final long serialVersionUID = 4665622610267812819L;

    public ClusterResizingStatus(final String clusterName, final Instant timeStamp) {
        super(clusterName, timeStamp);
    }

    public ClusterResizingStatus(final String clusterName){
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
        return "Cluster is resizing";
    }

    /**
     * Gets malfunction level.
     *
     * @return Malfunction level.
     */
    @Nonnull
    @Override
    public Level getLevel() {
        return Level.LOW;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClusterName(), getTimeStamp());
    }

    private boolean equals(final ClusterResizingStatus other){
        return equalsHelper(other);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ClusterResizingStatus && equals((ClusterResizingStatus) other);
    }
}
