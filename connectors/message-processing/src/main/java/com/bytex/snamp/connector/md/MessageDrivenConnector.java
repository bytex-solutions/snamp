package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;
import com.bytex.snamp.connector.notifications.measurement.MeasurementSource;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.time.Duration;
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
    @Aggregation
    protected final MessageDrivenAttributeRepository attributes;

    protected MessageDrivenConnector(final String resourceName,
                                     final Map<String, String> parameters,
                                     final MessageDrivenConnectorConfigurationDescriptor descriptor) {
        final String componentInstance = descriptor.parseComponentInstance(parameters, resourceName);
        final String componentName = descriptor.parseComponentName(parameters);
        source = new MeasurementSource(componentName, componentInstance);
        final ExecutorService threadPool = descriptor.parseThreadPool(parameters);
        parser = descriptor.createNotificationParser(parameters);
        attributes = createAttributeRepository(resourceName, descriptor.parseSyncPeriod(parameters));
        assert attributes != null;
        attributes.init(threadPool, getLogger());
    }

    /**
     * Creates a new instance of repository for attributes.
     * @param resourceName Resource name.
     * @param syncPeriod Cluster-wide synchronization period. Cannot be {@literal null}.
     * @return A new instance of repository.
     */
    protected MessageDrivenAttributeRepository createAttributeRepository(final String resourceName, final Duration syncPeriod){
        return new MessageDrivenAttributeRepository(resourceName, syncPeriod);
    }

    protected Notification parseNotification(final Map<String, ?> headers,
                                             final Object body) throws Exception{
        return parser.parse(headers, body);
    }

    @Override
    public final void handleNotification(final Notification notification, final Object handback) {
        attributes.handleNotification(notification, handback);
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
            handleNotification(notification, this);
    }

    @Override
    public final void addResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, attributes);
    }

    @Override
    public final void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, attributes);
    }

    @Override
    public void close() throws Exception {
        attributes.close();
        super.close();
    }
}