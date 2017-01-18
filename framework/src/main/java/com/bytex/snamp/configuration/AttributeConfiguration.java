package com.bytex.snamp.configuration;

import java.time.Duration;

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

    /**
     * Sets attribute value invoke/write operation timeout.
     * @param value A new value of the timeout.
     */
    void setReadWriteTimeout(final Duration value);

    /**
     * Copies management attributes.
     * @param source The attribute to copy.
     * @param dest The attribute to fill.
     */
    static void copy(final AttributeConfiguration source, final AttributeConfiguration dest){
        dest.setReadWriteTimeout(source.getReadWriteTimeout());
        dest.load(source);
    }
}
