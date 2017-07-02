package com.bytex.snamp.web.serviceModel;

/**
 * Represents a service for SNAMP Web Console.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface WebConsoleService extends AutoCloseable {
    void attachSession(final WebConsoleSession session);
}
