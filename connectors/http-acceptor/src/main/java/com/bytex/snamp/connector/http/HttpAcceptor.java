package com.bytex.snamp.connector.http;

import com.bytex.snamp.connector.md.MessageDrivenConnector;
import com.bytex.snamp.connector.md.NotificationParser;
import com.bytex.snamp.instrumentation.Measurement;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

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
    protected NotificationParser createNotificationParser(final String resourceName, final String instanceName, final String componentName, final Map<String, String> parameters) {
        return null;
    }

    boolean dispatch(final Measurement measurement) {
        final boolean dispatched = dispatcher.getInstanceName().equals(measurement.getInstanceName()) &&
                dispatcher.getComponentName().equals(measurement.getComponentName());
        if (dispatched) {
            dispatcher.handleNotification(ImmutableMap.of(), measurement, this);
        }
        return dispatched;
    }
}
