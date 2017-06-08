package com.bytex.snamp.connector.composite.functions;

import com.bytex.snamp.moa.DoubleEMA;

import java.util.concurrent.TimeUnit;

/**
 * Represents function that computes average value in time interval.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class AverageFunction extends NumericFunction {
    private final DoubleEMA avg;

    AverageFunction(final long interval, final TimeUnit unit){
        avg = new DoubleEMA(interval, unit);
    }

    @Override
    double getFallbackValue() {
        return avg.doubleValue();
    }

    @Override
    double invoke(final EvaluationContext resolver, final Number input) {
        return avg.applyAsDouble(input.doubleValue());
    }
}
