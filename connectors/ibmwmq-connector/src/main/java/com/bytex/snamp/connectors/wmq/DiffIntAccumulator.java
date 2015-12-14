package com.bytex.snamp.connectors.wmq;

import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.concurrent.IntAccumulator;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class DiffIntAccumulator extends IntAccumulator {
    private static final long serialVersionUID = -7636894125809244868L;

    /**
     * Initializes a new accumulator of {@literal long} values.
     *
     * @param ttl          Time-to-live of the value in this accumulator. Cannot be {@literal null}.
     */
    DiffIntAccumulator(final TimeSpan ttl){
        super(0, ttl);
    }

    @Override
    protected int combine(final int current, final int newValue) {
        return newValue - current;
    }
}
