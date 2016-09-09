package com.bytex.snamp.connector.composite.functions;

import java.util.concurrent.TimeUnit;

/**
 * Represents function that computes average value in time interval.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class AverageFunction extends NumericFunction {
    private final long intervalNanos;
    private long checkpointNanos;
    private final StatefulDoubleUnaryFunction avg;

    AverageFunction(final long interval, final TimeUnit unit){
        intervalNanos = unit.toNanos(interval);
        checkpointNanos = System.nanoTime();
        avg = UnaryFunctions.average();
    }

    @Override
    double compute(final Number input, final OperandResolver resolver) {
        if(System.nanoTime() - checkpointNanos > intervalNanos){
            checkpointNanos = System.nanoTime();
            avg.reset();
        }
        return avg.applyAsDouble(input.doubleValue());
    }
}
