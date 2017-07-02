package com.bytex.snamp.connector.composite.functions;

import com.bytex.snamp.concurrent.TimeLimitedDouble;

import java.time.Duration;
import java.time.temporal.TemporalUnit;

/**
 * Computes sum of values in the specified interval of time.
 */
final class SumFunction extends NumericFunction {
    private final TimeLimitedDouble sum;

    SumFunction(final long interval, final TemporalUnit unit){
        sum = TimeLimitedDouble.adder(0D, Duration.of(interval, unit));
    }

    @Override
    double getFallbackValue() {
        return sum.getAsDouble();
    }

    @Override
    double invoke(final EvaluationContext resolver, final Number input) {
        return sum.update(input.doubleValue());
    }
}
