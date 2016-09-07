package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.math.DoubleReservoir;

/**
 * Abstract class for numeric gauges.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class AbstractNumericGauge extends AbstractMetric implements NumericGauge {
    static final int DEFAULT_SAMPLING_SIZE = 1024;
    private final DoubleReservoir reservoir;

    AbstractNumericGauge(final String name, final int samplingSize) {
        super(name);
        reservoir = new DoubleReservoir(samplingSize);
    }

    final void updateReservoir(final double value){
        reservoir.add(value);
    }

    @Override
    public final double getQuantile(final double quantile) {
        return reservoir.getQuantile(quantile);
    }

    @Override
    public final double getDeviation() {
        return reservoir.getDeviation();
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        reservoir.reset();
    }
}
