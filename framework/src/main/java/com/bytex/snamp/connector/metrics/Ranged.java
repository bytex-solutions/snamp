package com.bytex.snamp.connector.metrics;

/**
 * Represents metrics that measures deviation from normative range.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface Ranged extends Rate {

    /**
     * Gets percent of received measurements that are less than confidence interval.
     * @return Percent of received measurements that are less than confidence interval.
     */
    double getPercentOfLessThanRange();

    /**
     * Gets percent of received measurements for the last time that are less than confidence interval.
     * @return Percent of received measurements for the last time that are less than confidence interval.
     */
    double getPercentOfLessThanRange(final MetricsInterval interval);

    /**
     * Gets percent of received measurements that are greater than confidence interval.
     * @return Percent of received measurements that are less than confidence interval.
     */
    double getPercentOfGreaterThanRange();

    /**
     * Gets percent of received measurements for the last time that are greater than confidence interval.
     * @return Percent of received measurements for the last time that are less than confidence interval.
     */
    double getPercentOfGreaterThanRange(final MetricsInterval interval);

    /**
     * Gets percent of received measurements that are in normal range.
     * @return Percent of received measurements that are in normal range.
     */
    double getPercentOfValuesIsInRange();

    /**
     * Gets percent of received measurements for the last time that are in normal range.
     * @return Percent of received measurements for the last time that are in normal range.
     */
    double getPercentOfValuesIsInRange(final MetricsInterval interval);

    @Override
    Ranged clone();
}
