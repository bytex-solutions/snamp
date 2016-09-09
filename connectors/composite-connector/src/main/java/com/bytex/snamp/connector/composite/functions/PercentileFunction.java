package com.bytex.snamp.connector.composite.functions;

import java.util.concurrent.TimeUnit;

/**
 * Computes percentile.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class PercentileFunction extends NumericFunction {
    private final long intervalNanos;
    private long checkpointNanos;
    private final StatefulDoubleUnaryFunction percentile;

    PercentileFunction(final long percentile, final long interval, final TimeUnit unit){
        intervalNanos = unit.toNanos(interval);
        checkpointNanos = System.nanoTime();
        this.percentile = UnaryFunctions.percentile(100, percentile / 100F);
    }

    @Override
    synchronized double compute(final Number input, final OperandResolver resolver) {
        if (System.nanoTime() - checkpointNanos > intervalNanos) {
            percentile.reset();
            checkpointNanos = System.nanoTime();
        }
        return percentile.applyAsDouble(input.doubleValue());
    }
}
