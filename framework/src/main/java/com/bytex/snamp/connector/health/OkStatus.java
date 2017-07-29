package com.bytex.snamp.connector.health;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Locale;

/**
 * Indicates that everything is OK.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class OkStatus extends HealthStatus {
    private static final long serialVersionUID = 5391122005596632004L;

    public OkStatus(final Instant timeStamp) {
        super(timeStamp);
    }

    @Override
    public boolean like(final HealthStatus status) {
        return status instanceof OkStatus;
    }

    public OkStatus(){
        this(Instant.now());
    }

    public static boolean notOk(final HealthStatus status){
        return !(status instanceof OkStatus);
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
}
