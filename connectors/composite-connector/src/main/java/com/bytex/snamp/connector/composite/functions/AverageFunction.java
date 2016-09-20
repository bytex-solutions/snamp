package com.bytex.snamp.connector.composite.functions;

import com.bytex.snamp.math.ExponentialMovingAverage;

import java.util.concurrent.TimeUnit;
import java.util.function.DoubleUnaryOperator;

/**
 * Represents function that computes average value in time interval.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class AverageFunction extends NumericFunction {
    private final DoubleUnaryOperator avg;

    AverageFunction(final long interval, final TimeUnit unit){
        avg = new ExponentialMovingAverage(interval, unit);
    }

    @Override
    double invoke(final NameResolver resolver, final Number input) {
        return avg.applyAsDouble(input.doubleValue());
    }
}
