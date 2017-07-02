package com.bytex.snamp.connector.composite.functions;

import com.bytex.snamp.moa.DoubleReservoir;
import com.bytex.snamp.moa.Reservoir;

/**
 * Computes percentile.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class PercentileFunction extends NumericFunction {
    private final Reservoir reservoir;
    private final float quantile;

    PercentileFunction(final byte percentile){
        reservoir = new DoubleReservoir(4096);
        quantile = percentile / 100F;
    }

    @Override
    double getFallbackValue() {
        return reservoir.getQuantile(quantile);
    }

    @Override
    double invoke(final EvaluationContext resolver, final Number input) {
        reservoir.add(input);
        return getFallbackValue();
    }
}
