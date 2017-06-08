package com.bytex.snamp.connector.composite.functions;

import com.bytex.snamp.moa.DoubleEWMA;

import java.time.temporal.TemporalUnit;

/**
 * Represents function that computes average value in time interval.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class AverageFunction extends NumericFunction {
    private final DoubleEWMA avg;

    AverageFunction(final long interval, final TemporalUnit unit){
        avg = new DoubleEWMA(interval, unit);
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
