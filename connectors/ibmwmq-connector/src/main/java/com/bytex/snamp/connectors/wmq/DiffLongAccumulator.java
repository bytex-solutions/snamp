package com.bytex.snamp.connectors.wmq;

import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.concurrent.LongAccumulator;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class DiffLongAccumulator extends LongAccumulator {
    private static final long serialVersionUID = -3241751805411986796L;

    /**
     * Initializes a new accumulator of {@literal long} values.
     *
     * @param ttl          Time-to-live of the value in this accumulator. Cannot be {@literal null}.
     */
    DiffLongAccumulator(final TimeSpan ttl){
        super(0L, ttl);
    }

    @Override
    protected long combine(final long current, final long newValue) {
        return newValue - current;
    }
}
