package com.bytex.snamp.instrumentation;

import java.io.Closeable;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface MeasurementScope extends Closeable {
    /**
     * Closes measurement scope.
     */
    @Override
    void close();
}
