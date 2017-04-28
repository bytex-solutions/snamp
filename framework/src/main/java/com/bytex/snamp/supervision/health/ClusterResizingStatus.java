package com.bytex.snamp.supervision.health;

import com.bytex.snamp.connector.health.HealthStatus;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Locale;

/**
 * Indicates that the cluster is resizing.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ClusterResizingStatus extends ClusterMalfunctionStatus {
    private static final long serialVersionUID = 4665622610267812819L;

    public ClusterResizingStatus(final Instant timeStamp) {
        super(timeStamp);
    }

    public ClusterResizingStatus(){
        this(Instant.now());
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
    public boolean like(final HealthStatus status) {
        return status instanceof ClusterResizingStatus && super.like((ClusterResizingStatus) status);
    }
}
