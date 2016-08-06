package com.bytex.snamp.core;

/**
 * Represents cluster-wide generator of sequence numbers.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.2
 */
@FunctionalInterface
public interface LongCounter {
    /**
     * Generates a new cluster-wide unique identifier.
     * @return A new cluster-wide unique identifier.
     */
    long increment();
}
