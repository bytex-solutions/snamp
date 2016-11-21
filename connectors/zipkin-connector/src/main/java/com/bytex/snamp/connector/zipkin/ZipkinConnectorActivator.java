package com.bytex.snamp.connector.zipkin;

import com.bytex.snamp.connector.md.MessageDrivenConnector;
import com.bytex.snamp.connector.md.MessageDrivenConnectorConfigurationDescriptor;
import com.bytex.snamp.connector.md.NotificationParser;
import com.bytex.snamp.connector.md.notifications.NotificationSource;

import java.util.Map;

/**
 * Collects spans compatible with Twitter Zipkin.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
final class ZipkinConnectorActivator extends MessageDrivenConnector {
    public ZipkinConnectorActivator(final String resourceName, final Map<String, String> parameters, final MessageDrivenConnectorConfigurationDescriptor descriptor) {
        super(resourceName, parameters, descriptor);
    }

    @Override
    protected NotificationParser createNotificationParser(final String resourceName, final NotificationSource source, final Map<String, String> parameters) {
        return null;
    }
}
