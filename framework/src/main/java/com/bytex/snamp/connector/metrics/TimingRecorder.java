package com.bytex.snamp.connector.metrics;


import com.bytex.snamp.math.DoubleReservoir;
import com.bytex.snamp.math.ExponentialMovingAverage;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents implementation of {@link Timing}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class TimingRecorder extends GaugeImpl<Duration> implements Timing {
    private final MetricsIntervalMap<ExponentialMovingAverage> meanValues;
    private final DoubleReservoir reservoir;
    private final AtomicReference<Duration> summary;

    public TimingRecorder(final String name, final int samplingSize) {
        super(name, Duration.ZERO);
        meanValues = new MetricsIntervalMap<>(MetricsInterval::createEMA);
        reservoir = new DoubleReservoir(samplingSize);
        summary = new AtomicReference<>(Duration.ZERO);
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        super.reset();
        meanValues.values().forEach(ExponentialMovingAverage::reset);
        reservoir.reset();
        summary.set(Duration.ZERO);
    }

    private static long toMicros(final Duration value){
        return value.toNanos() / 1000;
    }

    private static Duration fromMicros(final long value){
        return Duration.ofNanos(value * 1000);
    }

    public TimingRecorder(final String name){
        this(name, AbstractNumericGauge.DEFAULT_SAMPLING_SIZE);
    }

    public Duration getMeanValue(final MetricsInterval interval) {
        return meanValues.get(interval, avg -> fromMicros(avg.getAsLong()));
    }

    @Override
    public void update(final Duration value) {
        super.update(value);
        meanValues.forEachAcceptLong(toMicros(value), ExponentialMovingAverage::accept);
        reservoir.accept(toMicros(value));
        summary.accumulateAndGet(value, Duration::plus);
    }

    /**
     * Gets duration at the specified quantile.
     *
     * @param quantile The quantile value.
     * @return Duration at the specified quantile.
     */
    @Override
    public Duration getQuantile(final double quantile) {
        return fromMicros(Math.round(reservoir.getQuantile(quantile)));
    }

    /**
     * Gets standard deviation of all durations.
     *
     * @return The standard deviation of all durations.
     */
    @Override
    public Duration getDeviation() {
        return fromMicros(Math.round(reservoir.getDeviation()));
    }

    /**
     * Gets summary duration of all events.
     *
     * @return The summary duration of all events.
     */
    @Override
    public Duration getSummaryValue() {
        return summary.get();
    }
}
