package com.bytex.snamp.connector.metrics;

/**
 * Represents combination of {@link Timer} and {@link Rate}.
 * @since 2.0
 * @version 2.0
 */
public interface RatedTimer extends Rate, Timer {
    @Override
    RatedTimer clone();
}
