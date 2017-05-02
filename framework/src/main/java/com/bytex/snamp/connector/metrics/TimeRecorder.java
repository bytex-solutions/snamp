package com.bytex.snamp.connector.metrics;


import com.bytex.snamp.io.SerializedState;
import com.bytex.snamp.moa.DoubleReservoir;
import com.bytex.snamp.moa.ExponentialMovingAverage;

import javax.annotation.concurrent.ThreadSafe;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Represents implementation of {@link Timer}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@ThreadSafe
public class TimeRecorder extends GaugeImpl<Duration> implements Timer {
    private static final long serialVersionUID = 7250210436685797077L;
    private final MetricsIntervalMap<ExponentialMovingAverage> meanValues;
    private final AtomicLong count;
    private final DoubleReservoir reservoir;
    private final AtomicReference<Duration> summary;
    private final double timeScaleFactor;

    TimeRecorder(final String name, final int samplingSize, final double scaleFactor){
        super(name, Duration.ZERO);
        meanValues = new MetricsIntervalMap<>(MetricsInterval::createEMA);
        reservoir = new DoubleReservoir(samplingSize);
        summary = new AtomicReference<>(Duration.ZERO);
        timeScaleFactor = scaleFactor;
        count = new AtomicLong(0L);
    }

    public TimeRecorder(final String name, final int samplingSize) {
        this(name, samplingSize, 1000D);    //store duration in reservoir in microseconds
    }

    public TimeRecorder(final String name){
        this(name, AbstractNumericGauge.DEFAULT_SAMPLING_SIZE);
    }

    protected TimeRecorder(final TimeRecorder source){
        super(source);
        meanValues = new MetricsIntervalMap<>(source.meanValues, ExponentialMovingAverage::clone);
        count = new AtomicLong(source.count.get());
        reservoir = ((SerializedState<DoubleReservoir>)source.reservoir.takeSnapshot()).get();
        summary = new AtomicReference<>(source.summary.get());
        timeScaleFactor = source.timeScaleFactor;
    }

    @Override
    public TimeRecorder clone() {
        return new TimeRecorder(this);
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
    public final Duration getQuantile(final float quantile) {
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
    public final double getMeanNumberOfCompletedTasks(final MetricsInterval scale) {
        return getNumberOfCompletedTasks(scale, this::getMeanValue);
    }

    @Override
    public final double getMaxNumberOfCompletedTasks(final MetricsInterval scale){
        return getNumberOfCompletedTasks(scale, this::getMinValue);
    }

    @Override
    public final double getMinNumberOfCompletedTasks(final MetricsInterval scale){
        return getNumberOfCompletedTasks(scale, this::getMaxValue);
    }
}
