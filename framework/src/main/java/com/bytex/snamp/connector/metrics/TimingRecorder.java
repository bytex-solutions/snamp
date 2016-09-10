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
    private final double timeScaleFactor;

    TimingRecorder(final String name, final int samplingSize, final double scaleFactor){
        super(name, Duration.ZERO);
        meanValues = new MetricsIntervalMap<>(MetricsInterval::createEMA);
        reservoir = new DoubleReservoir(samplingSize);
        summary = new AtomicReference<>(Duration.ZERO);
        timeScaleFactor = scaleFactor;
    }

    public TimingRecorder(final String name, final int samplingSize) {
        this(name, samplingSize, 1000D);    //store duration in reservoir in microseconds
    }

    public TimingRecorder(final String name){
        this(name, AbstractNumericGauge.DEFAULT_SAMPLING_SIZE);
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

    private double toDouble(final Duration value){
        return value.toNanos() / timeScaleFactor;
    }

    private Duration fromDouble(final double value) {
        return Duration.ofNanos(Math.round(value * timeScaleFactor));
    }

    public Duration getMeanValue(final MetricsInterval interval) {
        return meanValues.get(interval, avg -> fromDouble(avg.getAsDouble()));
    }

    @Override
    public void accept(final Duration value) {
        super.accept(value);
        meanValues.forEachAcceptDouble(toDouble(value), ExponentialMovingAverage::accept);
        reservoir.accept(toDouble(value));
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
        return fromDouble(Math.round(reservoir.getQuantile(quantile)));
    }

    /**
     * Gets standard deviation of all durations.
     *
     * @return The standard deviation of all durations.
     */
    @Override
    public Duration getDeviation() {
        return fromDouble(Math.round(reservoir.getDeviation()));
    }

    /**
     * Computes a percent of durations that are greater than or equal to the specified duration.
     *
     * @param value A value to compute.
     * @return A percent of durations that are greater that or equal to the specified duration.
     */
    @Override
    public double lessThanOrEqualDuration(final Duration value) {
        return reservoir.lessThanOrEqualValues(toDouble(value));
    }

    /**
     * Computes a percent of durations that are less than or equal to the specified duration.
     *
     * @param value A value to compute.
     * @return A percent of durations that are greater that or less to the specified duration.
     */
    @Override
    public double greaterThanOrEqualDuration(final Duration value) {
        return reservoir.greaterThanOrEqualValues(toDouble(value));
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
