package com.bytex.snamp.connector.metrics;

/**
 * Represents metrics that measures deviation from normative.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Normative extends Rate {

    /**
     * Gets total count of received measurements that are less than confidence interval.
     * @return Total count of received measurements that are less than confidence interval.
     */
    long getCountOfLessThanNormative();

    /**
     * Gets percent of received measurements that are less than confidence interval.
     * @return Percent of received measurements that are less than confidence interval.
     */
    double getPercentOfLessThanNormative();

    /**
     * Gets count of received measurements for the last time that are less than confidence interval.
     * @return Count of received measurements for the last time that are less than confidence interval.
     */
    long getCountOfLessThanNormative(final MetricsInterval interval);

    /**
     * Gets percent of received measurements for the last time that are less than confidence interval.
     * @return Percent of received measurements for the last time that are less than confidence interval.
     */
    double getPercentOfLessThanNormative(final MetricsInterval interval);

    /**
     * Gets total count of received measurements that are greater than confidence interval.
     * @return Total count of received measurements that are less than confidence interval.
     */
    long getCountOfGreaterThanNormative();

    /**
     * Gets percent of received measurements that are greater than confidence interval.
     * @return Percent of received measurements that are less than confidence interval.
     */
    double getPercentOfGreaterThanNormative();

    /**
     * Gets count of received measurements for the last time that are greater than confidence interval.
     * @return Count of received measurements for the last time that are less than confidence interval.
     */
    long getCountOfGreaterThanNormative(final MetricsInterval interval);

    /**
     * Gets percent of received measurements for the last time that are greater than confidence interval.
     * @return Percent of received measurements for the last time that are less than confidence interval.
     */
    double getPercentOfGreaterThanNormative(final MetricsInterval interval);

    /**
     * Gets total count of received measurements that are in normal range.
     * @return Total count of received measurements that are in normal range.
     */
    long getCountOfNormalValues();

    /**
     * Gets percent of received measurements that are in normal range.
     * @return Percent of received measurements that are in normal range.
     */
    double getPercentOfNormalValues();

    /**
     * Gets count of received measurements for the last time that are in normal range.
     * @return Count of received measurements for the last time that are in normal range.
     */
    long getCountOfNormalValues(final MetricsInterval interval);

    /**
     * Gets percent of received measurements for the last time that are in normal range.
     * @return Percent of received measurements for the last time that are in normal range.
     */
    double getPercentOfNormalValues(final MetricsInterval interval);

    @Override
    Normative clone();
}
