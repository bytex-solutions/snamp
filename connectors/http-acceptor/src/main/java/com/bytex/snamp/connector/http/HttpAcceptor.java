package com.bytex.snamp.connector.http;

import com.bytex.snamp.connector.md.MessageDrivenConnector;
import com.bytex.snamp.connector.md.NotificationParser;
import com.bytex.snamp.connector.md.groovy.GroovyNotificationParserLoader;
import groovy.lang.Binding;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import java.util.Map;

/**
 * Represents HTTP acceptor.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class HttpAcceptor extends MessageDrivenConnector {
    private final GroovyNotificationParserLoader loader;

    HttpAcceptor(final String resourceName, final Map<String, String> parameters) throws IOException {
        super(resourceName, parameters, HttpConnectorConfigurationDescriptor.getInstance());
        loader = new GroovyNotificationParserLoader(this, parameters);
    }

    @Override
    protected NotificationParser createNotificationParser(final String resourceName, final String instanceName, final String componentName, final Map<String, String> parameters) {
        try {
            return loader.createScript("MeasurementParser.groovy", new Binding());
        } catch (final ResourceException | ScriptException e) {
            throw new IllegalStateException(e);
        }
    }
}
