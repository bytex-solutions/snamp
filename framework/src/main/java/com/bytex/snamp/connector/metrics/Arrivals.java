package com.bytex.snamp.connector.metrics;

/**
 * Represents a gauge for queuing system.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Arrivals extends RatedTimer {
    double getMeanAvailability(final int channels);

    double getInstantAvailability(final int channels);

    /**
     * Gets ratio between summary duration of all requests and server uptime.
     * @return Utilization of server uptime, in percents.
     */
    double getEfficiency();

    /**
     * Gets correlation between arrivals and response time.
     * @return The correlation between arrivals and response time.
     */
    double getCorrelation();

    @Override
    Arrivals clone();
}
