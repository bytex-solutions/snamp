package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.moa.DoubleReservoir;
import com.bytex.snamp.moa.EWMA;

import java.util.function.Supplier;

/**
 * Abstract class for numeric gauges.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class AbstractNumericGauge extends AbstractMetric implements NumericGauge {
    static final int DEFAULT_SAMPLING_SIZE = 4096;
    private static final long serialVersionUID = 6307047277703768318L;
    private final DoubleReservoir reservoir;
    private final MetricsIntervalMap<EWMA> meanValues;

    AbstractNumericGauge(final AbstractNumericGauge source) {
        super(source);
        reservoir = ((Supplier<DoubleReservoir>) source.reservoir.takeSnapshot()).get();
        meanValues = new MetricsIntervalMap<>(source.meanValues, EWMA::clone);
    }

    AbstractNumericGauge(final String name, final int samplingSize) {
        super(name);
        reservoir = new DoubleReservoir(samplingSize);
        meanValues = new MetricsIntervalMap<>(MetricsInterval::createEMA);
    }

    @Override
    public abstract AbstractNumericGauge clone();

    final void updateReservoir(final double value){
        reservoir.add(value);
        meanValues.forEachAcceptDouble(value, EWMA::accept);
    }

    @Override
    public final double getLastMeanValue(final MetricsInterval interval) {
        return meanValues.getAsDouble(interval, EWMA::doubleValue);
    }

    @Override
    public final double getQuantile(final float quantile) {
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
        meanValues.values().forEach(EWMA::reset);
    }
}
