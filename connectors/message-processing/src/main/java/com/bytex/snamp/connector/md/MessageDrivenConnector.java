package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.metrics.MetricsSupport;

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
    /**
     * Represents channel that can be used to process notifications.
     */
    protected final NotificationDispatcher channel;
    @Aggregation(cached = true)
    private final SpecialOperationsRepository operations;

    protected MessageDrivenConnector(final String resourceName,
                                     final Map<String, String> parameters,
                                     final MessageDrivenConnectorConfigurationDescriptor descriptor) {
        final String componentInstance = descriptor.parseComponentInstance(parameters, resourceName);
        final String componentName = descriptor.parseComponentName(parameters);
        final ExecutorService threadPool = descriptor.parseThreadPool(parameters);
        //init parser
        final NotificationParser parser = createNotificationParser(resourceName, parameters);
        assert parser != null;
        //init attributes
        final MessageDrivenAttributeRepository attributes = createAttributeRepository(resourceName, descriptor.parseSyncPeriod(parameters));
        assert attributes != null;
        attributes.init(threadPool, getLogger());
        //init notifications
        final MessageDrivenNotificationRepository notifications = createNotificationRepository(resourceName);
        assert notifications != null;
        notifications.init(threadPool, getLogger());

        channel = new NotificationDispatcher(componentName, componentInstance, attributes, notifications, getLogger(), parser);

        operations = new SpecialOperationsRepository(resourceName, channel.attributes, getLogger());
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
     * @param resourceName Resource name.
     * @param parameters Set of parameters that may be used by notification parser.
     * @return A new instance of notification parser.
     */
    protected abstract NotificationParser createNotificationParser(final String resourceName, final Map<String, String> parameters);

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
        addResourceEventListener(listener, channel.attributes, channel.notifications, operations);
    }

    @Override
    public final void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, channel.attributes, channel.notifications, operations);
    }

    /**
     * Releases all resources associated with this connector.
     * @throws Exception Unable to release resource clearly.
     */
    @Override
    public void close() throws Exception {
        channel.attributes.close();
        channel.notifications.close();
        operations.close();
        super.close();
    }
}