package com.itworks.snamp.concurrent;

/**
 * Computes peak value at the specified time interval.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class PeakLongAccumulator extends AbstractLongAccumulator {
    private static final long serialVersionUID = -8443558435352145841L;

    /**
     * Initializes a new accumulator of {@literal long} values.
     *
     * @param initialValue The initial value of this accumulator.
     * @param ttl          Time-to-live of the value in this accumulator, in millis.
     */
    public PeakLongAccumulator(final long initialValue, final long ttl) {
        super(initialValue, ttl);
    }

    /**
     * Combines the current value of the accumulator with a new value.
     *
     * @param current  The current value stored in this accumulator.
     * @param newValue The new value supplied by the caller code.
     * @return A new value.
     */
    @Override
    protected long combine(final long current, final long newValue) {
        return Math.max(current, newValue);
    }
}
