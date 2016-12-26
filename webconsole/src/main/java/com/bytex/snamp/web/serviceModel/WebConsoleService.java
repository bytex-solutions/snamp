package com.bytex.snamp.web.serviceModel;

import java.io.Closeable;

/**
 * Represents a service for SNAMP Web Console.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface WebConsoleService extends Closeable {
    String URL_CONTEXT = "urlContext";
    String NAME = "name";

    void addWebEventListener(final WebEventListener listener);
    void removeWebEventListener(final WebEventListener listener);
}
