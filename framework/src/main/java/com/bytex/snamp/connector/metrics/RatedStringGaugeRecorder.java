package com.bytex.snamp.connector.metrics;

/**
 * Represents implementation of {@link RatedStringGauge}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class RatedStringGaugeRecorder extends StringGaugeRecorder implements RatedStringGauge {
    private static final long serialVersionUID = 956217566486645152L;
    private final RateRecorder rate;

    /**
     * Initializes a new string gauge.
     *
     * @param name The name of the gauge.
     */
    public RatedStringGaugeRecorder(final String name) {
        super(name);
        rate = new RateRecorder(name);
    }

    protected RatedStringGaugeRecorder(final RatedStringGaugeRecorder source){
        super(source);
        rate = source.rate.clone();
    }

    @Override
    public RatedStringGaugeRecorder clone() {
        return new RatedStringGaugeRecorder(this);
    }

    @Override
    protected void writeValue(final String value) {
        rate.mark();
        super.writeValue(value);
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
}
