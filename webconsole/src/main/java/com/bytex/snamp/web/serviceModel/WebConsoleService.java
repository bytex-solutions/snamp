package com.bytex.snamp.web.serviceModel;

/**
 * Represents a service for SNAMP Web Console.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface WebConsoleService extends AutoCloseable {
    String URL_CONTEXT = "com.bytex.snamp.web.console.service.urlContext";
    String NAME = "com.bytex.snamp.web.console.service.name";

    void addWebEventListener(final WebEventListener listener);
    void removeWebEventListener(final WebEventListener listener);
}
