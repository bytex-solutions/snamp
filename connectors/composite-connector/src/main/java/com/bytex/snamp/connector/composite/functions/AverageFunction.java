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
    private long checkpointNanos;
    private long denominator;
    private double sum;

    AverageFunction(final long interval, final TimeUnit unit){
        intervalNanos = unit.toNanos(interval);
        checkpointNanos = System.nanoTime();
    }

    @Override
    synchronized double compute(final double input) {
        if(System.nanoTime() - checkpointNanos > intervalNanos){
            checkpointNanos = System.nanoTime();
            denominator = 0L;
            sum = 0F;
        }
        sum += input;
        denominator += 1L;
        return sum / denominator;
    }
}
