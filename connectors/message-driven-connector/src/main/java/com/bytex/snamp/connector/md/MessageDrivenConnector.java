package com.bytex.snamp.connector.md;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.md.notifications.NotificationSource;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.connector.operations.reflection.JavaBeanOperationRepository;
import com.bytex.snamp.connector.operations.reflection.ManagementOperation;
import com.bytex.snamp.connector.operations.reflection.OperationParameter;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

import static com.bytex.snamp.internal.Utils.callUnchecked;
import static com.google.common.base.Strings.isNullOrEmpty;

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
    @Aggregation(cached = true)
    private final NotificationSource source;
    @Aggregation(cached = true)
    protected final MessageDrivenAttributeRepository attributes;
    @Aggregation(cached = true)
    protected final MessageDrivenNotificationRepository notifications;
    @Aggregation(cached = true)
    private final NotificationParser notificationParser;
    @Aggregation(cached = true)
    private final JavaBeanOperationRepository operations;

    protected MessageDrivenConnector(final String resourceName,
                                     final Map<String, String> parameters,
                                     final MessageDrivenConnectorConfigurationDescriptor descriptor) {
        {
            String componentInstance = descriptor.parseComponentInstance(parameters);
            if (isNullOrEmpty(componentInstance))
                componentInstance = resourceName;
            final String componentName = descriptor.parseComponentName(parameters);
            source = new NotificationSource(componentName, componentInstance);
        }
        final ExecutorService threadPool = descriptor.parseThreadPool(parameters);
        //init parser
        notificationParser = createNotificationParser(resourceName, source, parameters);
        assert notificationParser != null;
        //init attributes
        attributes = createAttributeRepository(resourceName, descriptor.parseSyncPeriod(parameters));
        assert attributes != null;
        attributes.init(threadPool, descriptor, getLogger());
        //init notifications
        notifications = createNotificationRepository(resourceName);
        assert notifications != null;
        notifications.init(threadPool, getLogger());

        final BeanInfo info = callUnchecked(() -> Introspector.getBeanInfo(getClass(), AbstractManagedResourceConnector.class));
        operations = JavaBeanOperationRepository.create(resourceName, this, info);
    }

    @Override
    protected final MetricsSupport createMetricsReader() {
        return assembleMetricsReader(attributes, notifications);
    }

    @SpecialUse
    @ManagementOperation(description = "Resets all metrics")
    public void resetAllMetrics() {
        attributes.resetAllMetrics();
    }

    @SpecialUse
    @ManagementOperation(description = "Resets the specified metrics")
    public boolean resetMetric(@OperationParameter(name = "attributeName", description = "The name of the attribute to reset") final String attributeName) {
        final MessageDrivenAttribute attribute = attributes.getAttributeInfo(attributeName);
        final boolean success;
        if (success = attribute instanceof MetricHolderAttribute<?, ?>)
            ((MetricHolderAttribute<?, ?>) attribute).reset();
        return success;
    }

    public final boolean dispatch(final Map<String, ?> headers, final Object body) throws Exception{
        final Notification n = notificationParser.parse(headers, body);
        final boolean success;
        if (success = n != null)
            handleNotification(n);
        else
            getLogger().warning(String.format("Notification '%s' with headers '%s' is ignored by parser", body, headers));
        return success;
    }

    /**
     * Invoked when a JMX notification occurs.
     * The implementation of this method should return as soon as possible, to avoid
     * blocking its notification broadcaster.
     *
     * @param notification The notification.
     */
    public void handleNotification(final Notification notification) {
        notification.setSource(this);
        attributes.handleNotification(notification, this::attributeProcessed);
        notifications.handleNotification(notification);
    }

    private void attributeProcessed(final MessageDrivenAttribute attribute, final MessageDrivenAttribute.NotificationProcessingResult result) {
        if(result.isProcessed()) {
            //log processing error if it was happened
            final Optional<Throwable> processingError = result.getProcessingError();
            if (processingError.isPresent()) {
                getLogger().log(Level.SEVERE, String.format("Attribute '%s' has processing error", attribute.getName()), processingError.get());
                return;
            }
            //fire AttributeChangeNotification
            final Optional<Object> newAttributeValue = result.getAttributeValue();
            if (newAttributeValue.isPresent()) {
                final AttributeChangeNotification notification = new AttributeChangeNotification(this,
                        0L,
                        System.currentTimeMillis(),
                        String.format("Attribute %s was changed", attribute.getName()),
                        attribute.getName(),
                        attribute.getType(),
                        newAttributeValue.get(),
                        newAttributeValue.get());
                notifications.handleNotification(notification);
            }
        }
    }

    public final boolean represents(final NotificationSource value){
        return source.equals(value);
    }

    /**
     * Creates a new notification parser.
     * @param resourceName Resource name.
     * @param source Component identity.
     * @param parameters Set of parameters that may be used by notification parser.
     * @return A new instance of notification parser.
     */
    protected abstract NotificationParser createNotificationParser(final String resourceName,
                                                                   final NotificationSource source,
                                                                   final Map<String, String> parameters);

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
        addResourceEventListener(listener, attributes, notifications, operations);
    }

    @Override
    public final void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, attributes, notifications, operations);
    }

    /**
     * Releases all resources associated with this connector.
     * @throws Exception Unable to release resource clearly.
     */
    @Override
    public void close() throws Exception {
        attributes.close();
        notifications.close();
        operations.close();
        super.close();
    }
}