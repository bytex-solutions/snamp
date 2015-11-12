package com.bytex.snamp.core;

/**
 * Represents cluster-wide generator of unique numbers.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface IDGenerator {
    /**
     * Generates a new cluster-wide unique identifier.
     * @param generatorName Name of generator.
     * @return A new cluser-wide unique identifier.
     */
    long generateID(final String generatorName);

    /**
     * Resets the specified generator.
     * @param generatorName The name of the generator to reset.
     */
    void reset(final String generatorName);
}
