package com.bytex.snamp.configuration;

import com.bytex.snamp.jmx.DescriptorUtils;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents attribute configuration.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public interface AttributeConfiguration extends FeatureConfiguration {
    String UNIT_OF_MEASUREMENT_KEY = "units";
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

    default String getUnitOfMeasurement(){
        return getParameters().get(UNIT_OF_MEASUREMENT_KEY);
    }

    default void setUnitOfMeasurement(final String value) {
        if (isNullOrEmpty(value))
            getParameters().remove(UNIT_OF_MEASUREMENT_KEY);
        else
            getParameters().put(UNIT_OF_MEASUREMENT_KEY, value);
    }

    default void setReadWriteTimeout(final long amount, final TemporalUnit unit){
        setReadWriteTimeout(Duration.of(amount, unit));
    }
}
