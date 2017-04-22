package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.connector.operations.reflection.JavaBeanOperationRepository;
import com.bytex.snamp.connector.operations.reflection.ManagementOperation;
import com.bytex.snamp.connector.operations.reflection.OperationParameter;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.core.SharedCounter;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.bytex.snamp.core.SharedObjectType.COUNTER;
import static com.bytex.snamp.core.DistributedServices.getDistributedObject;
import static com.bytex.snamp.internal.Utils.callUnchecked;
import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents abstract class for stream-driven resource connector.
 * <p />
 *     The structure of attributes:
 *     1. Metric-based attribute which holds a whole gauge, rate or timer.
 *     2. Scalar-based attribute which extracts a counter from metric attribute
 * @since 2.0
 * @version 2.0
 */
public abstract class DataStreamConnector extends AbstractManagedResourceConnector {
    @Aggregation(cached = true)
    protected final SyntheticAttributeRepository attributes;
    @Aggregation(cached = true)
    protected final SyntheticNotificationRepository notifications;
    @Aggregation(cached = true)
    private final NotificationParser notificationParser;
    @Aggregation(cached = true)
    private final JavaBeanOperationRepository operations;
    private final SharedCounter sequenceNumberProvider;
    /**
     * Represents thread pool for parallel operations.
     */
    protected final ExecutorService threadPool;
    private final String instanceName;

    protected DataStreamConnector(final String resourceName,
                                  final ManagedResourceInfo configuration,
                                  final DataStreamConnectorConfigurationDescriptionProvider descriptor) {
        super(configuration);
        instanceName = resourceName;
        threadPool = descriptor.parseThreadPool(configuration);
        //init parser
        notificationParser = createNotificationParser();
        assert notificationParser != null;
        //init attributes
        attributes = createAttributeRepository(resourceName, descriptor.parseSyncPeriod(configuration));
        assert attributes != null;
        attributes.init(threadPool, descriptor);
        //init notifications
        notifications = createNotificationRepository(resourceName);
        assert notifications != null;
        notifications.init(threadPool, descriptor);
        notifications.setSource(this);
        
        final BeanInfo info = callUnchecked(() -> Introspector.getBeanInfo(getClass(), AbstractManagedResourceConnector.class));
        operations = JavaBeanOperationRepository.create(resourceName, this, info);
        sequenceNumberProvider = getDistributedObject(getBundleContextOfObject(this), "SequenceGenerator-".concat(resourceName), COUNTER)
                .orElseThrow(AssertionError::new);
    }

    protected final String getInstanceName(){
        return instanceName;
    }

    protected final String getGroupName() {
        final String groupName = getConfiguration().getGroupName();
        return isNullOrEmpty(groupName) ? getInstanceName() : groupName;
    }

    @Override
    protected final MetricsSupport createMetricsReader() {
        return assembleMetricsReader(attributes, notifications, operations);
    }

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @ManagementOperation(description = "Resets all metrics")
    public void resetAllMetrics() {
        attributes.resetAllMetrics();
    }

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @ManagementOperation(description = "Resets the specified metrics")
    public boolean resetMetric(@OperationParameter(name = "attributeName", description = "The name of the attribute to reset") final String attributeName) {
        final SyntheticAttribute attribute = attributes.getAttributeInfo(attributeName);
        final boolean success;
        if (success = attribute instanceof MetricHolderAttribute<?, ?>)
            ((MetricHolderAttribute<?, ?>) attribute).reset();
        return success;
    }

    public final void dispatch(final Map<String, ?> headers, final Object body) throws Exception {
        try (final Stream<Notification> notifications = notificationParser.parse(headers, body).filter(Objects::nonNull)) {
            notifications.forEach(this::handleNotification);
        }
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
        notification.setSequenceNumber(sequenceNumberProvider.getAsLong());
        attributes.handleNotification(notification, this::attributeProcessed);
        notifications.accept(notification);
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    private void attributeProcessed(final SyntheticAttribute attribute, final SyntheticAttribute.NotificationProcessingResult result) {
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
                notifications.accept(notification);
            }
        }
    }

    /**
     * Creates a new notification parser.
     * @return A new instance of notification parser.
     */
    protected abstract NotificationParser createNotificationParser();

    /**
     * Creates a new instance of repository for attributes.
     * @param resourceName Resource name.
     * @param syncPeriod Cluster-wide synchronization period. Cannot be {@literal null}.
     * @return A new instance of repository.
     */
    protected SyntheticAttributeRepository createAttributeRepository(final String resourceName, final Duration syncPeriod){
        return new SyntheticAttributeRepository(resourceName, syncPeriod);
    }

    protected SyntheticNotificationRepository createNotificationRepository(final String resourceName){
        return new SyntheticNotificationRepository(resourceName);
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