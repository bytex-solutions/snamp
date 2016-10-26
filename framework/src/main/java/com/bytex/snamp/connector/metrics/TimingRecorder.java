package com.bytex.snamp.connector.metrics;


import com.bytex.snamp.math.DoubleReservoir;
import com.bytex.snamp.math.ExponentialMovingAverage;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.DoubleFunction;
import java.util.function.Supplier;

/**
 * Represents implementation of {@link Timing}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class TimingRecorder extends GaugeImpl<Duration> implements Timing {
    private static final long serialVersionUID = 7250210436685797077L;
    private final MetricsIntervalMap<ExponentialMovingAverage> meanValues;
    private final AtomicLong count;
    private final DoubleReservoir reservoir;
    private final AtomicReference<Duration> summary;
    private final double timeScaleFactor;

    TimingRecorder(final String name, final int samplingSize, final double scaleFactor){
        super(name, Duration.ZERO);
        meanValues = new MetricsIntervalMap<>(MetricsInterval::createEMA);
        reservoir = new DoubleReservoir(samplingSize);
        summary = new AtomicReference<>(Duration.ZERO);
        timeScaleFactor = scaleFactor;
        count = new AtomicLong(0L);
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
        count.set(0L);
    }

    private double toDouble(final Duration value){
        return value.toNanos() / timeScaleFactor;
    }

    private Duration fromDouble(final double value) {
        return Duration.ofNanos(Math.round(value * timeScaleFactor));
    }

    @Override
    public final Duration getMeanValue() {
        return fromDouble(toDouble(getSummaryValue()) / count.get());
    }

    @Override
    public final Duration getLastMeanValue(final MetricsInterval interval) {
        return meanValues.get(interval, avg -> fromDouble(avg.getAsDouble()));
    }

    @Override
    protected void writeValue(final Duration value) {
        super.writeValue(value);
        meanValues.forEachAcceptDouble(toDouble(value), ExponentialMovingAverage::accept);
        reservoir.accept(toDouble(value));
        summary.accumulateAndGet(value, Duration::plus);
        count.incrementAndGet();
    }

    /**
     * Gets duration at the specified quantile.
     *
     * @param quantile The quantile value.
     * @return Duration at the specified quantile.
     */
    @Override
    public final Duration getQuantile(final double quantile) {
        return fromDouble(reservoir.getQuantile(quantile));
    }

    /**
     * Gets standard deviation of all durations.
     *
     * @return The standard deviation of all durations.
     */
    @Override
    public final Duration getDeviation() {
        return fromDouble(Math.round(reservoir.getDeviation()));
    }

    /**
     * Computes a percent of durations that are greater than or equal to the specified duration.
     *
     * @param value A value to compute.
     * @return A percent of durations that are greater that or equal to the specified duration.
     */
    @Override
    public final double lessThanOrEqualDuration(final Duration value) {
        return reservoir.lessThanOrEqualValues(toDouble(value));
    }

    /**
     * Computes a percent of durations that are less than or equal to the specified duration.
     *
     * @param value A value to compute.
     * @return A percent of durations that are greater that or less to the specified duration.
     */
    @Override
    public final double greaterThanOrEqualDuration(final Duration value) {
        return reservoir.greaterThanOrEqualValues(toDouble(value));
    }

    /**
     * Gets summary duration of all events.
     *
     * @return The summary duration of all events.
     */
    @Override
    public final Duration getSummaryValue() {
        return summary.get();
    }

    private double getNumberOfCompletedTasks(final MetricsInterval interval, final Supplier<Duration> durationProvider){
        return 1D / interval.divideFP(durationProvider.get());
    }

    @Override
    public final double getMeanNumberOfCompletedTasks(final MetricsInterval interval) {
        return getNumberOfCompletedTasks(interval, this::getMeanValue);
    }

    @Override
    public final double getMaxNumberOfCompletedTasks(final MetricsInterval interval){
        return getNumberOfCompletedTasks(interval, this::getMinValue);
    }

    @Override
    public final double getMinNumberOfCompletedTasks(final MetricsInterval interval){
        return getNumberOfCompletedTasks(interval, this::getMaxValue);
    }
}
