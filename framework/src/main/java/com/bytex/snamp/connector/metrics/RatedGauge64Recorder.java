package com.bytex.snamp.connector.metrics;

/**
 * Represents implementation of {@link RatedGauge64}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class RatedGauge64Recorder extends Gauge64Recorder implements RatedGauge64 {
    private final RateRecorder rate;

    public RatedGauge64Recorder(final String name, final int samplingSize) {
        super(name, samplingSize);
        rate = new RateRecorder(name);
    }

    public RatedGauge64Recorder(final String name) {
        super(name);
        rate = new RateRecorder(name);
    }

    @Override
    public void accept(final long value) {
        rate.mark();
        super.accept(value);
    }

    /**
     * Resets all metrics.
     */
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
     * Gets the mean rate of actions received for the last time.
     *
     * @param interval Measurement interval.
     * @return The mean rate of actions received for the last time.
     */
    @Override
    public final double getLastMeanRate(final MetricsInterval interval) {
        return rate.getLastMeanRate(interval);
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
}
