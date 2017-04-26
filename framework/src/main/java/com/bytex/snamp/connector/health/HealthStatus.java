package com.bytex.snamp.connector.health;

import com.bytex.snamp.Localizable;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents health check status.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class HealthStatus implements Serializable, Localizable {
    private static final long serialVersionUID = -8700097915541124870L;
    private final Instant timeStamp;

    HealthStatus(@Nonnull final Instant timeStamp) {
        this.timeStamp = Objects.requireNonNull(timeStamp);
    }

    /**
     * Gets time stamp of this health status.
     * @return Time stamp of this health status.
     */
    public final Instant getTimeStamp(){
        return timeStamp;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(final Object obj);

    /**
     * Selects the worst health status between two statuses.
     * @param other Other health status.
     * @return The worst health status.
     */
    public abstract HealthStatus worst(@Nonnull final HealthStatus other);

    @Override
    public String toString() {
        return toString(Locale.getDefault());
    }
}
