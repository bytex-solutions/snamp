package com.bytex.snamp.connector.http;

import com.bytex.snamp.connector.md.MessageDrivenConnector;
import com.bytex.snamp.connector.md.MessageDrivenConnectorConfigurationDescriptor;
import com.bytex.snamp.connector.md.NotificationParser;

import java.util.Map;

/**
 * Represents HTTP acceptor.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class HttpAcceptor extends MessageDrivenConnector {
    HttpAcceptor(final String resourceName, final Map<String, String> parameters, final MessageDrivenConnectorConfigurationDescriptor descriptor) {
        super(resourceName, parameters, descriptor);
    }

    /**
     * Creates a new notification parser.
     *
     * @param resourceName Resource name.
     * @param parameters   Set of parameters that may be used by notification parser.
     * @return A new instance of notification parser.
     */
    @Override
    protected NotificationParser createNotificationParser(final String resourceName, final Map<String, String> parameters) {
        return null;
    }
}
