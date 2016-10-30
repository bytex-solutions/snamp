package com.bytex.snamp.connector.metrics;

import java.time.Duration;
import java.time.Instant;

/**
 * Represents implementation of {@link RatedTimer}.
 * @since 2.0
 * @version 2.0
 */
public class RatedTimeRecorder extends TimeRecorder implements RatedTimer {
    private static final long serialVersionUID = -1501369457653393355L;
    private final RateRecorder rate;

    public RatedTimeRecorder(final String name, final int samplingSize) {
        super(name, samplingSize);
        rate = new RateRecorder(name);
    }

    RatedTimeRecorder(final String name, final int samplingSize, final double scaleFactor){
        super(name, samplingSize, scaleFactor);
        rate = new RateRecorder(name);
    }

    public RatedTimeRecorder(final String name) {
        super(name);
        rate = new RateRecorder(name);
    }

    protected RatedTimeRecorder(final RatedTimeRecorder source){
        super(source);
        rate = source.rate.clone();
    }

    public final void setStartTime(final Instant startTime){
        rate.setStartTime(startTime);
    }

    public final Instant getStartTime(){
        return rate.getStartTime();
    }

    @Override
    public RatedTimeRecorder clone() {
        return new RatedTimeRecorder(this);
    }

    @Override
    public void reset() {
        super.reset();
        rate.reset();
    }

    @Override
    protected void writeValue(final Duration value) {
        rate.mark();
        super.writeValue(value);
    }

    @Override
    public final long getTotalRate() {
        return rate.getTotalRate();
    }

    @Override
    public final long getLastRate(final MetricsInterval interval) {
        return rate.getLastRate(interval);
    }

    @Override
    public final double getLastMeanRate(final MetricsInterval interval) {
        return rate.getLastMeanRate(interval);
    }

    @Override
    public final double getMeanRate(final MetricsInterval scale) {
        return rate.getMeanRate(scale);
    }

    @Override
    public final long getMaxRate(final MetricsInterval interval) {
        return rate.getMaxRate(interval);
    }

    @Override
    public final long getLastMaxRatePerSecond(final MetricsInterval interval) {
        return rate.getLastMaxRatePerSecond(interval);
    }

    @Override
    public final long getLastMaxRatePerMinute(MetricsInterval interval) {
        return rate.getLastMaxRatePerMinute(interval);
    }
}
