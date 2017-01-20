package com.bytex.snamp.configuration;

/**
 * Represents event configuration.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public interface EventConfiguration extends FeatureConfiguration {
    /**
     * Copies management events.
     * @param source The event to copy.
     * @param dest The event to fill.
     */
    static void copy(final EventConfiguration source, final EventConfiguration dest){
        dest.load(source);
    }
}
