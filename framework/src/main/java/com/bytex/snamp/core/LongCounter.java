package com.bytex.snamp.core;

import java.util.function.LongSupplier;

/**
 * Represents cluster-wide generator of sequence numbers.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public interface LongCounter extends LongSupplier, DistributedService {
    /**
     * Generates a new cluster-wide unique identifier.
     * @return A new cluster-wide unique identifier.
     */
    @Override
    long getAsLong();
}
