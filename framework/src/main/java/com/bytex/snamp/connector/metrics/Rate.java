package com.bytex.snamp.connector.metrics;

/**
 * Provides rate of actions.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Rate extends Metric {
    /**
     * Represents empty rate.
     */
    Rate EMPTY = new Rate() {
        @Override
        public long getTotalRate() {
            return 0;
        }

        @Override
        public long getLastRate(final MetricsInterval interval) {
            return 0;
        }

        @Override
        public double getLastMeanRate(final MetricsInterval interval) {
            return 0;
        }

        @Override
        public double getMeanRate(final MetricsInterval scale) {
            return 0;
        }

        @Override
        public long getMaxRate(final MetricsInterval interval) {
            return 0;
        }

        @Override
        public long getLastMaxRatePerSecond(final MetricsInterval interval) {
            return 0;
        }

        @Override
        public long getLastMaxRatePerMinute(final MetricsInterval interval) {
            return 0;
        }

        @Override
        public String getName() {
            return "EMPTY";
        }

        @Override
        public void reset() {

        }

        @Override
        public Rate clone() {
            return this;
        }
    };

    /**
     * Gets the total rate.
     * @return The total rate.
     */
    long getTotalRate();

    /**
     * Gets the last measured rate of actions.
     *
     * @param interval Measurement interval.
     * @return The last measured rate of actions.
     */
    long getLastRate(final MetricsInterval interval);

    /**
     * Gets the mean rate of actions received for the last time.
     * @param interval Measurement interval.
     * @return The mean rate of actions received for the last time.
     */
    double getLastMeanRate(final MetricsInterval interval);

    /**
     * Gets the mean rate of actions per unit of time from the historical perspective.
     * @param scale Measurement interval.
     * @return Mean rate of actions per unit of time from the historical perspective.
     */
    double getMeanRate(final MetricsInterval scale);

    /**
     * Gets the max rate of actions observed in the specified interval.
     * @param interval Measurement interval.
     * @return The max rate of actions received in the specified interval.
     */
    long getMaxRate(final MetricsInterval interval);

    /**
     * Gets the max rate of actions received per second for the last time.
     * @param interval Measurement interval.
     * @return The max rate of actions received per second for the last time.
     */
    long getLastMaxRatePerSecond(final MetricsInterval interval);

    /**
     * Gets the max rate of actions received per second for the last time.
     * @param interval Measurement interval. Cannot be less than {@link MetricsInterval#MINUTE}.
     * @return The max rate of actions received per second for the last time.
     */
    long getLastMaxRatePerMinute(final MetricsInterval interval);

    @Override
    Rate clone();
}
