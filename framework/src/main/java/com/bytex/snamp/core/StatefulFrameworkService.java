package com.bytex.snamp.core;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface StatefulFrameworkService extends FrameworkService, AutoCloseable {
    /**
     * Gets state of this service.
     * @return Service type.
     */
    FrameworkServiceState getState();
}
