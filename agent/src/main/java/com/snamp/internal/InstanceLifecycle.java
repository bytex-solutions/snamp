package com.snamp.internal;

/**
 * Represents utility class that describes object's instance lifecycle.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public enum InstanceLifecycle {
    /**
     * Represents singleton instance
     */
    SINGLETON,

    /**
     * Indicates that the annotated class can be created once per process.
     */
    SINGLE_PER_PROCESS,

    /**
     * Indicates that the annotated class can be created many times per process.
     */
    NORMAL,
}
