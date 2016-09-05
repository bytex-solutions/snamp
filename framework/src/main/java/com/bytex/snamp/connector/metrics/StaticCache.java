package com.bytex.snamp.connector.metrics;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class StaticCache {
    private StaticCache(){
        throw new InstantiationError();
    }

    /**
     * Cached array of available intervals for fast iteration.
     */
    static final MetricsInterval[] INTERVALS = MetricsInterval.values();
}
