package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.connector.notifications.measurement.NotificationSource;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Represents abstract class for message-driven resource connector.
 * <p>
 *     The structure of attributes:
 *     1. Metric-based attribute which holds a whole gauge, rate or timer.
 *     2. Scalar-based attribute which extracts a counter from metric attribute
 * @since 2.0
 * @version 2.0
 */
public abstract class MessageDrivenConnector extends AbstractManagedResourceConnector {
    protected final NotificationSource source;
    protected final NotificationChannel channel;

    protected MessageDrivenConnector(final String resourceName,
                                     final Map<String, String> parameters,
                                     final MessageDrivenConnectorConfigurationDescriptor descriptor) {
        final String componentInstance = descriptor.parseComponentInstance(parameters, resourceName);
        final String componentName = descriptor.parseComponentName(parameters);
        source = new NotificationSource(componentName, componentInstance);
        final ExecutorService threadPool = descriptor.parseThreadPool(parameters);
        //init parser
        final NotificationParser parser = createNotificationParser(parameters);
        assert parser != null;
        //init attributes
        final MessageDrivenAttributeRepository attributes = createAttributeRepository(resourceName, descriptor.parseSyncPeriod(parameters));
        assert attributes != null;
        attributes.init(threadPool, getLogger());
        //init notifications
        final MessageDrivenNotificationRepository notifications = createNotificationRepository(resourceName);
        assert notifications != null;
        notifications.init(threadPool, getLogger());

        channel = new NotificationChannel(attributes, notifications, parser);
    }

    @Aggregation(cached = true)
    protected final MessageDrivenAttributeRepository getAttributes(){
        return channel.attributes;
    }

    @Aggregation(cached = true)
    protected final MessageDrivenNotificationRepository getNotifications(){
        return channel.notifications;
    }

    @Override
    protected final MetricsSupport createMetricsReader() {
        return assembleMetricsReader(channel.attributes, channel.notifications);
    }

    /**
     * Creates a new notification parser.
     * @param parameters Set of parameters that may be used by notification parser.
     * @return A new instance of notification parser.
     */
    protected NotificationParser createNotificationParser(final Map<String, String> parameters){
        return null;
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

    protected MessageDrivenNotificationRepository createNotificationRepository(final String resourceName){
        return new MessageDrivenNotificationRepository(resourceName);
    }

    @Override
    public final void addResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, channel.attributes, channel.notifications);
    }

    @Override
    public final void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, channel.attributes, channel.notifications);
    }

    /**
     * Releases all resources associated with this connector.
     * @throws Exception Unable to release resource clearly.
     */
    @Override
    public void close() throws Exception {
        channel.attributes.close();
        channel.notifications.close();
        super.close();
    }
}