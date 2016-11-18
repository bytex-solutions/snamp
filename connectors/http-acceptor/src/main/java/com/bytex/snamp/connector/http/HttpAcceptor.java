package com.bytex.snamp.connector.http;

import com.bytex.snamp.connector.md.MessageDrivenConnector;
import com.bytex.snamp.connector.md.NotificationParser;
import com.bytex.snamp.connector.md.groovy.GroovyNotificationParserLoader;
import com.bytex.snamp.connector.md.notifications.NotificationSource;
import groovy.lang.Binding;

import java.util.Map;

import static com.bytex.snamp.internal.Utils.callUnchecked;

/**
 * Represents HTTP acceptor.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class HttpAcceptor extends MessageDrivenConnector {

    HttpAcceptor(final String resourceName, final Map<String, String> parameters) {
        super(resourceName, parameters, HttpConnectorConfigurationDescriptor.getInstance());
    }

    @Override
    protected NotificationParser createNotificationParser(final String resourceName, final NotificationSource source, final Map<String, String> parameters) {
        return callUnchecked(() -> {
            final GroovyNotificationParserLoader loader = new GroovyNotificationParserLoader(this, parameters);
            return loader.createScript("MeasurementParser.groovy", new Binding());
        });
    }
}
