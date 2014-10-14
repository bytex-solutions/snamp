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
     * Gets IDs of attributes exposed by the specified managed resources.
     * @param resourceName The name of the managed resource.
     * @return A collection of connected attributes.
     */
    Set<String> getAttributes(final String resourceName);

    /**
     * Gets an attribute accessor.
     * @param attributeID ID of the attribute.
     * @return The attribute accessor; or {@literal null}, if attribute doesn't exist.
     */
    SshAttributeView getAttribute(final String attributeID);

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
