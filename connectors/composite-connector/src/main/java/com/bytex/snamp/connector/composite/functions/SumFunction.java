package com.bytex.snamp.connector.composite.functions;

import com.bytex.snamp.math.StatefulDoubleUnaryFunction;
import com.bytex.snamp.math.UnaryFunctions;

import java.util.concurrent.TimeUnit;

/**
 * Computes sum of values in the specified interval of time.
 */
final class SumFunction extends NumericFunction {
    private final long intervalNanos;
    private long checkpointNanos;
    private final StatefulDoubleUnaryFunction sum;

    SumFunction(final long interval, final TimeUnit unit){
        intervalNanos = unit.toNanos(interval);
        checkpointNanos = System.nanoTime();
        sum = UnaryFunctions.sum();
    }

    @Override
    synchronized double compute(final Number input, final OperandResolver resolver) throws Exception {
        if(System.nanoTime() - checkpointNanos > intervalNanos){
            checkpointNanos = System.nanoTime();
            sum.reset();
        }
        return sum.applyAsDouble(input.doubleValue());
    }
}
