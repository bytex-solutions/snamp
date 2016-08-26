package com.bytex.snamp.connector.composite.functions;

import com.bytex.snamp.math.StatefulDoubleUnaryFunction;
import com.bytex.snamp.math.UnaryFunctions;

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
    private final StatefulDoubleUnaryFunction avg;

    AverageFunction(final long interval, final TimeUnit unit){
        intervalNanos = unit.toNanos(interval);
        checkpointNanos = System.nanoTime();
        avg = UnaryFunctions.average();
    }

    @Override
    synchronized double compute(final double input) {
        if(System.nanoTime() - checkpointNanos > intervalNanos){
            checkpointNanos = System.nanoTime();
            avg.reset();
        }
        return avg.applyAsDouble(input);
    }
}
