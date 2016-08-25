package com.bytex.snamp.connector.composite.functions;

import java.util.concurrent.TimeUnit;

/**
 * Represents function that computes average value in time interval.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class AverageFunction extends ToDoubleFunction {
    private final long intervalNanos;

    AverageFunction(final long interval, final TimeUnit unit){
        intervalNanos = unit.toNanos(interval);
    }

    @Override
    double compute(final double input) {
        return 0;
    }
}
