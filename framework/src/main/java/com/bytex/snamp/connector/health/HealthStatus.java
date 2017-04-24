package com.bytex.snamp.connector.health;

import com.bytex.snamp.Localizable;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.time.Instant;
import java.util.Locale;

/**
 * Represents health check status.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class HealthStatus implements Serializable, Comparable<HealthStatus>, Localizable {
    private static final long serialVersionUID = -8700097915541124870L;
    private final int severity;
    private final Instant timeStamp;

    HealthStatus(final int severity, @Nonnull final Instant timeStamp) {
        this.severity = severity;
        this.timeStamp = timeStamp;
    }

    /**
     * Gets time stamp of this health status.
     * @return Time stamp of this health status.
     */
    public final Instant getTimeStamp(){
        return timeStamp;
    }

    @Override
    public final int compareTo(@Nonnull final HealthStatus other) {
        return severity == other.severity ?
                Boolean.compare(isCritical(), other.isCritical()) :
                Integer.compare(severity, other.severity);
    }

    /**
     * Indicates that resource is in critical state (potentially unavailable).
     * @return {@literal true}, if managed resource is in critical state; otherwise, {@literal false}.
     */
    public abstract boolean isCritical();

    public final HealthStatus worst(@Nonnull final HealthStatus other){
        return compareTo(other) >= 0 ? this : other;
    }

    @Override
    public String toString() {
        return toString(Locale.getDefault());
    }
}
