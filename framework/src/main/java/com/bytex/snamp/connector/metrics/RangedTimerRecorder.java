package com.bytex.snamp.connector.metrics;

import javax.annotation.concurrent.ThreadSafe;
import java.time.Duration;
import java.util.function.Consumer;

/**
 * Represents timing record with normative.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@ThreadSafe
public class RangedTimerRecorder extends AbstractRangedRecorder implements RangedTimer, Consumer<Duration> {
    private static final long serialVersionUID = -6020447472626416864L;
    private final TimeRecorder timer;
    private final Duration rangeStart;
    private final Duration rangeEnd;

    protected RangedTimerRecorder(final RangedTimerRecorder source) {
        super(source);
        timer = source.timer.clone();
        rangeStart = source.rangeStart;
        rangeEnd = source.rangeEnd;
    }

    public RangedTimerRecorder(final String name, final Duration from, final Duration to) {
        super(name);
        timer = new TimeRecorder(name);
        if(from.compareTo(to) > 0)
            throw new IllegalArgumentException("Illegal range definition");
        rangeStart = from;
        rangeEnd = to;
    }

    @Override
    public RangedTimerRecorder clone() {
        return new RangedTimerRecorder(this);
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    @Override
    public void accept(final Duration value) {
        updateValue(HitResult.compute(rangeStart, rangeEnd, value));
        timer.accept(value);
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        super.reset();
        timer.reset();
    }

    /**
     * Gets maximum value ever presented.
     *
     * @return Maximum value ever presented.
     */
    @Override
    public final Duration getMaxValue() {
        return timer.getMaxValue();
    }

    @Override
    public final Duration getSummaryValue(final MetricsInterval interval) {
        return timer.getSummaryValue(interval);
    }

    /**
     * Gets maximum value for the last period.
     *
     * @param interval Period.
     * @return Maximum value of the last period.
     */
    @Override
    public final Duration getLastMaxValue(final MetricsInterval interval) {
        return timer.getLastMaxValue(interval);
    }

    /**
     * The minimum value ever presented.
     *
     * @return The minimum value ever presented.
     */
    @Override
    public final Duration getMinValue() {
        return timer.getMinValue();
    }

    /**
     * Gets minimum value for the last period.
     *
     * @param interval Period.
     * @return Minimum value for the last period.
     */
    @Override
    public final Duration getLastMinValue(final MetricsInterval interval) {
        return timer.getLastMinValue(interval);
    }

    /**
     * The last presented value.
     *
     * @return The last presented value.
     */
    @Override
    public final Duration getLastValue() {
        return timer.getLastValue();
    }

    /**
     * Gets duration at the specified quantile.
     *
     * @param quantile The quantile value.
     * @return Duration at the specified quantile.
     */
    @Override
    public final Duration getQuantile(final float quantile) {
        return timer.getQuantile(quantile);
    }

    /**
     * Gets standard deviation of all durations.
     *
     * @return The standard deviation of all durations.
     */
    @Override
    public final Duration getDeviation() {
        return timer.getDeviation();
    }

    @Override
    public final double getMeanNumberOfCompletedTasks(final MetricsInterval scale) {
        return timer.getMaxNumberOfCompletedTasks(scale);
    }

    @Override
    public final double getMaxNumberOfCompletedTasks(final MetricsInterval scale) {
        return timer.getMaxNumberOfCompletedTasks(scale);
    }

    @Override
    public final double getMinNumberOfCompletedTasks(final MetricsInterval scale) {
        return timer.getMinNumberOfCompletedTasks(scale);
    }

    /**
     * Gets summary duration of all events.
     *
     * @return The summary duration of all events.
     */
    @Override
    public final Duration getSummaryValue() {
        return timer.getSummaryValue();
    }

    @Override
    public final Duration getMeanValue(final MetricsInterval interval) {
        return timer.getMeanValue(interval);
    }

    @Override
    public final Duration getMeanValue() {
        return timer.getMeanValue();
    }
}
