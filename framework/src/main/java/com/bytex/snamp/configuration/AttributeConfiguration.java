package com.bytex.snamp.configuration;

import java.time.Duration;
import java.time.temporal.TemporalUnit;

/**
 * Represents attribute configuration.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public interface AttributeConfiguration extends FeatureConfiguration {
    /**
     * Recommended timeout for read/write of attribute in smart mode.
     */
    Duration TIMEOUT_FOR_SMART_MODE = Duration.ofSeconds(10);

    /**
     * Gets attribute value invoke/write operation timeout.
     * @return Gets attribute value invoke/write operation timeout.
     */
    Duration getReadWriteTimeout();

    default long getReadWriteTimeout(final TemporalUnit unit){
        return getReadWriteTimeout().get(unit);
    }

    /**
     * Sets attribute value invoke/write operation timeout.
     * @param value A new value of the timeout.
     */
    void setReadWriteTimeout(final Duration value);

    default void setReadWriteTimeout(final long amount, final TemporalUnit unit){
        setReadWriteTimeout(Duration.of(amount, unit));
    }
}
