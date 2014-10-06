package com.itworks.snamp.adapters.ssh;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

/**
 * Represents mediation layer between SNAMP infrastructure and Secure Shell Interpreter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface AdapterController {
    /**
     * Gets a collection of connected managed resources.
     * @return A collection of connected managed resources.
     */
    Set<String> getConnectedResources();

    /**
     * Gets the execute service that can be used to schedule asynchronous operations.
     * @return The asynchronous task scheduler.
     */
    ExecutorService getCommandExecutorService();

    /**
     * Gets logger associated with adapter.
     * @return The logger.
     */
    Logger getLogger();
}
