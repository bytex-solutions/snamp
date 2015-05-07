package com.itworks.snamp.concurrent;

/**
 * Accumulates sum between ints.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SumIntAccumulator extends AbstractIntAccumulator {
    private static final long serialVersionUID = 117073431654121295L;

    /**
     * Initializes a new accumulator of {@literal int} values.
     *
     * @param initialValue The initial value of this accumulator.
     * @param ttl          Time-to-live of the value in this accumulator, in millis.
     */
    public SumIntAccumulator(final int initialValue, final long ttl) {
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
    protected int combine(final int current, final int newValue) {
        return current + newValue;
    }
}
