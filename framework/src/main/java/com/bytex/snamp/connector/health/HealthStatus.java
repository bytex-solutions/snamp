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
 * @version 2.1
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

    /**
     * Determines whether this health status is similar to the specified status.
     * @param status Health status.
     * @return {@literal true}, if this health status is similar to the specified status.
     * @implSpec This method has weaker semantics than {@link #equals(Object)}.
     *              Similarity means that only significant data in health status used are equal.
     *              Volatile data such as {@link #getTimeStamp()} should be ignored.
     */
    public abstract boolean like(final HealthStatus status);

    /**
     * Selects the worst health status between two statuses.
     * @param other Other health status.
     * @return The worst health status.
     */
    public abstract HealthStatus worst(@Nonnull final HealthStatus other);

    /**
     * Determines whether the current status worse than specified.
     * @param other Health status to compare.
     * @return {@literal true} if this health status is worse than specified; otherwise, {@literal false}.
     */
    public final boolean worseThan(@Nonnull final HealthStatus other) {
        return worst(other) == this;
    }

    @Override
    public String toString() {
        return toString(Locale.getDefault());
    }
}
