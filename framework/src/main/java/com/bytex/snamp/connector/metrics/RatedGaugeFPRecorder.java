package com.bytex.snamp.connector.metrics;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Represents implementation of {@link RatedGaugeFP}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@ThreadSafe
public class RatedGaugeFPRecorder extends GaugeFPRecorder implements RatedGaugeFP {
    private static final long serialVersionUID = 1843602850458733111L;
    private final RateRecorder rate;

    public RatedGaugeFPRecorder(final String name, final int samplingSize) {
        super(name, samplingSize);
        rate = new RateRecorder(name);
    }

    public RatedGaugeFPRecorder(final String name) {
        super(name);
        rate = new RateRecorder(name);
    }

    protected RatedGaugeFPRecorder(final RatedGaugeFPRecorder source){
        super(source);
        rate = source.rate.clone();
    }

    @Override
    public RatedGaugeFPRecorder clone() {
        return new RatedGaugeFPRecorder(this);
    }

    @Override
    protected void writeValue(final double value) {
        rate.mark();
        super.writeValue(value);
    }

    @Override
    public void reset() {
        rate.reset();
        super.reset();
    }

    /**
     * Gets the total rate.
     *
     * @return The total rate.
     */
    @Override
    public final long getTotalRate() {
        return rate.getTotalRate();
    }

    /**
     * Gets the last measured rate of actions.
     *
     * @param interval Measurement interval.
     * @return The last measured rate of actions.
     */
    @Override
    public final long getLastRate(final MetricsInterval interval) {
        return rate.getLastRate(interval);
    }

    /**
     * Gets the mean rate of actions per unit time from the historical perspective.
     *
     * @param scale Measurement interval.
     * @return Mean rate of actions per unit time from the historical perspective.
     */
    @Override
    public final double getMeanRate(final MetricsInterval scale) {
        return rate.getMeanRate(scale);
    }

    /**
     * Gets the max rate of actions received for the last time.
     *
     * @param interval Measurement interval.
     * @return The max rate of actions received for the last time.
     */
    @Override
    public final long getMaxRate(final MetricsInterval interval) {
        return rate.getMaxRate(interval);
    }

    /**
     * Gets the max rate of actions received per second for the last time.
     *
     * @param interval Measurement interval.
     * @return The max rate of actions received per second for the last time.
     */
    @Override
    public final long getLastMaxRatePerSecond(final MetricsInterval interval) {
        return rate.getLastMaxRatePerSecond(interval);
    }

    /**
     * Gets the max rate of actions received per second for the last time.
     *
     * @param interval Measurement interval. Cannot be less than {@link MetricsInterval#MINUTE}.
     * @return The max rate of actions received per second for the last time.
     */
    @Override
    public final long getLastMaxRatePerMinute(final MetricsInterval interval) {
        return rate.getLastMaxRatePerMinute(interval);
    }

    /**
     * Gets the max rate of actions received per 12 hours for the last time.
     *
     * @param interval Measurement interval. Cannot be less than {@link MetricsInterval#HALF_DAY}.
     * @return The max rate of actions received per second for the last time.
     */
    @Override
    public final long getLastMaxRatePer12Hours(final MetricsInterval interval) {
        return rate.getLastMaxRatePer12Hours(interval);
    }
}
