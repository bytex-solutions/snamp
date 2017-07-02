package com.bytex.snamp.core;

/**
 * Represents state of the framework service.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public enum FrameworkServiceState {
    /**
     * Service instance is created but not started.
     */
    CREATED,

    /**
     * Service instance is started.
     */
    STARTED,

    /**
     * Service instance is stopped but can be started again.
     */
    STOPPED,

    /**
     * Service is closed and cannot be started again.
     */
    CLOSED
}
