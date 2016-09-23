package com.bytex.snamp.connector.composite.functions;

import com.bytex.snamp.concurrent.TimeLimitedDouble;

import java.util.concurrent.TimeUnit;

/**
 * Computes sum of values in the specified interval of time.
 */
final class SumFunction extends NumericFunction {
    private final TimeLimitedDouble sum;

    SumFunction(final long interval, final TimeUnit unit){
        sum = TimeLimitedDouble.adder(0D, unit.toMillis(interval));
    }

    @Override
    double getFallbackValue() {
        return sum.getAsDouble();
    }

    @Override
    double invoke(final NameResolver resolver, final Number input) {
        return sum.update(input.doubleValue());
    }
}
