package com.bytex.snamp.instrumentation;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface MeasurementScope extends AutoCloseable {
    /**
     * Closes measurement scope.
     */
    @Override
    void close();
}
