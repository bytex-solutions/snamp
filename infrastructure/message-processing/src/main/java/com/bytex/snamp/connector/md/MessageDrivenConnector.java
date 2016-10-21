package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;
import com.bytex.snamp.connector.notifications.measurement.MeasurementSource;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

/**
 * Represents abstract class for message-driven resource connector.
 * <p>
 *     The structure of attributes:
 *     1. Metric-based attribute which holds a whole gauge, rate or timer.
 *     2. Scalar-based attribute which extracts a counter from metric attribute
 * @since 2.0
 * @version 2.0
 */
public abstract class MessageDrivenConnector extends AbstractManagedResourceConnector implements NotificationListener {
    private final MeasurementSource source;
    private NotificationParser parser;

    protected MessageDrivenConnector(final String resourceName,
                                     final Map<String, String> parameters,
                                     final MessageDrivenConnectorConfigurationDescriptor descriptor) {
        final String componentInstance = descriptor.parseComponentInstance(parameters, resourceName);
        final String componentName = descriptor.parseComponentName(parameters);
        source = new MeasurementSource(componentName, componentInstance);
        final ExecutorService threadPool = descriptor.parseThreadPool(parameters);
        parser = descriptor.createNotificationParser(parameters);
    }

    @Aggregation
    protected abstract MessageDrivenAttributeRepository getAttributes();

    protected Notification parseNotification(final Map<String, ?> headers,
                                             final Object body) throws Exception{
        return parser.parse(headers, body);
    }

    @Override
    public final void handleNotification(final Notification notification, final Object handback) {
        if (notification instanceof MeasurementNotification)
            getAttributes().post((MeasurementNotification) notification);
    }

    public final void postMessage(final Map<String, ?> headers,
                                     final Object body) {
        final Notification notification;
        try {
            notification = parseNotification(headers, body);
        } catch (final Exception e) {
            getLogger().log(Level.SEVERE, String.format("Unable to parse notification: %s", body), e);
            return;
        }
        //dispatching notification
        if (notification != null)
            handleNotification(notification, null);
    }

    @Override
    public final void addResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, getAttributes());
    }

    @Override
    public final void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, getAttributes());
    }

    @Override
    public void close() throws Exception {
        getAttributes().close();
        super.close();
    }
}