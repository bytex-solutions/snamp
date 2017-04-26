package com.bytex.snamp.connector.health;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Locale;

/**
 * Indicates that everything is OK.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class OkStatus extends HealthStatus {
    private static final long serialVersionUID = 5391122005596632004L;

    public OkStatus(final Instant timeStamp) {
        super(timeStamp);
    }

    public OkStatus(){
        this(Instant.now());
    }

    /**
     * Selects the worst health status between two statuses.
     *
     * @param other Other health status.
     * @return The worst health status.
     */
    @Override
    public HealthStatus worst(@Nonnull final HealthStatus other) {
        return other;
    }

    @Override
    public String toString(final Locale locale) {
        return "Everything is fine";
    }

    @Override
    public int hashCode() {
        return getTimeStamp().hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof OkStatus;
    }
}
