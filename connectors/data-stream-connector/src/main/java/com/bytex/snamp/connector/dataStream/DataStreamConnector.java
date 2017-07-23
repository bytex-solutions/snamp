package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.Convert;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.Timeout;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.health.*;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.connector.operations.reflection.JavaBeanOperationRepository;
import com.bytex.snamp.connector.operations.reflection.ManagementOperation;
import com.bytex.snamp.connector.operations.reflection.OperationParameter;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.core.SharedCounter;
import com.bytex.snamp.instrumentation.measurements.Health;
import com.bytex.snamp.instrumentation.measurements.jmx.HealthNotification;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.bytex.snamp.core.SharedObjectType.COUNTER;
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
public abstract class DataStreamConnector extends AbstractManagedResourceConnector implements HealthCheckSupport, Consumer<Notification> {
    private static final class HeartbeatTimer extends Timeout implements Consumer<HealthNotification> {
        private static final long serialVersionUID = -9146452762715540071L;
        private volatile HealthStatus status;

        HeartbeatTimer(final Duration timeout) {
            super(timeout);
            status = new OkStatus();
        }

        private void update(final Health health) {
            switch (health.getStatus()) {
                case UP:
                    status = new OkStatus(Instant.ofEpochMilli(health.getTimeStamp()));
                    reset();
                    break;
                case DOWN:
                    ResourceSubsystemDownStatus downStatus = new ResourceSubsystemDownStatus(Instant.ofEpochMilli(health.getTimeStamp()), health.getName());
                    downStatus.getData().putAll(health.getAnnotations());
                    status = downStatus;
                    reset();
                    break;
                case OUT_OF_SERVICE:
                    downStatus = new ResourceSubsystemDownStatus(Instant.ofEpochMilli(health.getTimeStamp()), health.getName(), MalfunctionStatus.Level.LOW);
                    downStatus.getData().putAll(health.getAnnotations());
                    status = downStatus;
                    reset();
                    break;
            }
        }

        @Override
        public void accept(final HealthNotification notification) {
            update(notification.getMeasurement());
        }

        HealthStatus getStatus() {
            return isExpired() ? new ConnectionProblem(new IOException("Heartbeat is timed out")) : status;
        }
    }

    @Aggregation(cached = true)
    protected final SyntheticAttributeRepository attributes;
    @Aggregation(cached = true)
    protected final SyntheticNotificationRepository notifications;
    @Aggregation(cached = true)
    private final NotificationParser notificationParser;
    @Aggregation(cached = true)
    private final JavaBeanOperationRepository operations;
    private final SharedCounter sequenceNumberProvider;
    private final HeartbeatTimer heartbeat;

    /**
     * Represents thread pool for parallel operations.
     */
    protected final ExecutorService threadPool;
    private final String instanceName;

    protected DataStreamConnector(final String resourceName,
                                  final ManagedResourceInfo configuration,
                                  final DataStreamConnectorConfigurationDescriptionProvider descriptor) {
        super(configuration);
        this.heartbeat = descriptor.getHeartbeat(configuration).map(HeartbeatTimer::new).orElse(null);
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
        sequenceNumberProvider = ClusterMember.get(getBundleContextOfObject(this)).getService("SequenceGenerator-".concat(resourceName), COUNTER)
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
        return attributes.getAttributeInfo(attributeName)
                .flatMap(Convert.toType(MetricHolderAttribute.class))
                .map(attribute -> {
                    attribute.reset();
                    return true;
                })
                .orElse(false);
    }

    public final void dispatch(final Map<String, ?> headers, final Object body) throws Exception {
        try (final Stream<Notification> notifications = notificationParser.parse(headers, body).filter(Objects::nonNull)) {
            notifications.forEach(this);
        }
    }

    /**
     * Determines whether the connected managed resource is alive.
     *
     * @return Status of the remove managed resource.
     */
    @Override
    @Nonnull
    public HealthStatus getStatus() {
        return heartbeat == null ? new OkStatus() : heartbeat.getStatus();
    }

    final void acceptRaw(final Notification notification) {
        notification.setSource(this);
        attributes.handleNotification(notification, this::attributeProcessed);
        notifications.accept(notification);
        if (heartbeat != null)                 //update heartbeat if it is enabled
            Convert.toType(notification, HealthNotification.class).ifPresent(heartbeat);
    }

    /**
     * Invoked when a JMX notification occurs.
     * The implementation of this method should return as soon as possible, to avoid
     * blocking its notification broadcaster.
     *
     * @param notification The notification.
     */
    @Override
    public void accept(final Notification notification) {
        notification.setSequenceNumber(sequenceNumberProvider.getAsLong());
        acceptRaw(notification);
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    private void attributeProcessed(final SyntheticAttribute attribute, final SyntheticAttribute.NotificationProcessingResult result) {
        if (result.isProcessed()) {
            //log processing error if it was happened
            final Optional<Throwable> processingError = result.getProcessingError();
            if (processingError.isPresent()) {
                getLogger().log(Level.SEVERE, String.format("Attribute '%s' has processing error", attribute.getName()), processingError.get());
                return;
            }
            //fire AttributeChangeNotification
            result.getAttributeValue().ifPresent(newAttributeValue -> {
                final AttributeChangeNotification notification = new AttributeChangeNotification(this,
                        0L,
                        System.currentTimeMillis(),
                        String.format("Attribute %s is changed", attribute.getName()),
                        attribute.getName(),
                        attribute.getType(),
                        newAttributeValue,
                        newAttributeValue);
                notifications.accept(notification);
            });
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
    @OverridingMethodsMustInvokeSuper
    public void close() throws Exception {
        attributes.close();
        notifications.close();
        operations.close();
        super.close();
    }
}