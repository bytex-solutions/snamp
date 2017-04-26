package com.bytex.snamp.connector.health;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Indicates some malfunction.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class MalfunctionStatus extends HealthStatus {
    /**
     * Represents malfunction level.
     */
    public enum Level{

        /**
         * Warning messages, not an error, but indication that an error will occur if action is not taken,
         * e.g. file system 85% full - each item must be resolved within a given time.
         */
        LOW,

        /**
         * Non-urgent failures, these should be relayed to developers or admins;
         * each item must be resolved within a given time.
         */
        MODERATE,

        /**
         * Should be corrected immediately, but indicates failure in a secondary system,
         * an example is a loss of a backup ISP connection.
         */
        SUBSTANTIAL,

        /**
         * Should be corrected immediately, therefore notify staff who can fix the problem.
         * An example would be the loss of a primary ISP connection.
         */
        SEVERE,

        /**
         * A "panic" condition usually affecting multiple apps/servers/sites.
         * At this level it would usually notify all tech staff on call.
         */
        CRITICAL
    }

    private static final long serialVersionUID = 1718771285813234068L;
    private final Map<String, Object> data;

    protected MalfunctionStatus(@Nonnull final Instant timeStamp) {
        super(timeStamp);
        data = new HashMap<>();
    }

    /**
     * Gets malfunction level.
     * @return Malfunction level.
     */
    @Nonnull
    public abstract Level getLevel();

    /**
     * Selects the worst health status between two statuses.
     *
     * @param other Other health status.
     * @return The worst health status.
     */
    public final MalfunctionStatus worst(final MalfunctionStatus other) {
        return getLevel().compareTo(other.getLevel()) >= 0 ? this : other;
    }

    /**
     * Selects the worst health status between two statuses.
     *
     * @param other Other health status.
     * @return The worst health status.
     */
    @Override
    public final HealthStatus worst(@Nonnull final HealthStatus other) {
        return other instanceof MalfunctionStatus ? worst((MalfunctionStatus) other) : other.worst(this);
    }

    /**
     * Gets additional data associated with this status.
     *
     * @return Map with additional data associated with this instance.
     */
    public final Map<String, Object> getData() {
        return firstNonNull(data, ImmutableMap.of());
    }
}
