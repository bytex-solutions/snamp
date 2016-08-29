package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.notifications.advanced.MonitoringNotification;
import com.bytex.snamp.connector.notifications.advanced.MonitoringNotificationSource;
import com.bytex.snamp.connector.notifications.advanced.SpanNotification;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Represents abstract class for message-driven resource connector.
 * @since 2.0
 * @version 2.0
 */
public abstract class MessageDrivenConnector extends AbstractManagedResourceConnector {
    private final MonitoringNotificationSource source;
    private final ImmutableMap<Class<? extends MonitoringNotification>, BiConsumer<MessageDrivenConnector, ? extends MonitoringNotification>> dispatcher;

    protected MessageDrivenConnector(final String resourceName,
                                     final Map<String, String> parameters,
                                     final MessageDrivenConnectorConfigurationDescriptor descriptor) {
        final String componentInstance = descriptor.parseComponentInstance(parameters, resourceName);
        final String componentName = descriptor.parseComponentName(parameters);
        source = new MonitoringNotificationSource(componentName, componentInstance);
        dispatcher = ImmutableMap.of(SpanNotification.class, (BiConsumer<MessageDrivenConnector, SpanNotification>) MessageDrivenConnector::processSpanMessage);
    }

    private void processSpanMessage(final SpanNotification notification){

    }

    protected abstract MonitoringNotification parseNotification(final Map<String, ?> headers,
                                                                final Object body);

    protected final void postMessage(final Map<String, ?> headers,
                                     final Object body){
        final MonitoringNotification notification = parseNotification(headers, body);
        //dispatching notification
        final BiConsumer handler = dispatcher.get(notification.getClass());
        handler.accept(this, notification);
    }
}