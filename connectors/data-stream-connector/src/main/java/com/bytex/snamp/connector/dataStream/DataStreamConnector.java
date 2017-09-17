package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.Convert;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.LazyReference;
import com.bytex.snamp.concurrent.Timeout;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.AbstractFeatureRepository;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.health.*;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.connector.operations.reflection.JavaBeanOperationRepository;
import com.bytex.snamp.connector.operations.reflection.ManagementOperation;
import com.bytex.snamp.connector.operations.reflection.OperationParameter;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.core.ReplicationSupport;
import com.bytex.snamp.core.SharedCounter;
import com.bytex.snamp.instrumentation.measurements.Health;
import com.bytex.snamp.instrumentation.measurements.jmx.HealthNotification;
import com.bytex.snamp.instrumentation.measurements.jmx.SpanNotification;

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
 * @version 2.1
 */
public abstract class DataStreamConnector extends AbstractManagedResourceConnector implements HealthCheckSupport, Consumer<Notification>, ReplicationSupport<Replica> {
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

    /**
     * Represents name of the metric represents information collected from input {@link com.bytex.snamp.instrumentation.measurements.jmx.SpanNotification}.
     */
    public static final String ARRIVALS_METRIC = SpanArrivalsRecorder.NAME;

    private final LazyReference<SyntheticAttributeRepository> attributes;
    private final LazyReference<SyntheticNotificationRepository> notifications;
    private final LazyReference<NotificationParser> notificationParser;

    private final JavaBeanOperationRepository operations;
    private final SharedCounter sequenceNumberProvider;
    private final HeartbeatTimer heartbeat;
    private final DataStreamConnectorConfigurationDescriptionProvider descriptionProvider;
    private volatile SpanArrivalsRecorder arrivals;
    private final ExecutorService threadPool;
    private final String instanceName;

    protected DataStreamConnector(final String resourceName,
                                  final ManagedResourceInfo configuration,
                                  final DataStreamConnectorConfigurationDescriptionProvider descriptor) {
        super(configuration);
        descriptionProvider = Objects.requireNonNull(descriptor);
        attributes = LazyReference.strong();
        notifications = LazyReference.strong();
        notificationParser = LazyReference.strong();
        arrivals = new SpanArrivalsRecorder(descriptor.getSamplingSize(configuration));
        this.heartbeat = descriptor.getHeartbeat(configuration).map(HeartbeatTimer::new).orElse(null);
        instanceName = resourceName;
        threadPool = descriptor.parseThreadPool(configuration);
        final BeanInfo info = callUnchecked(() -> Introspector.getBeanInfo(getClass(), AbstractManagedResourceConnector.class));
        operations = JavaBeanOperationRepository.create(resourceName, this, info);
        sequenceNumberProvider = ClusterMember.get(getBundleContextOfObject(this))
                .getCounters()
                .getSharedObject("SequenceGenerator-".concat(resourceName));
    }

    /**
     * Creates a new instance of repository for attributes.
     * @param resourceName Resource name.
     * @return A new instance of repository.
     */
    @Nonnull
    protected SyntheticAttributeRepository createAttributeRepository(final String resourceName){
        return new SyntheticAttributeRepository(resourceName);
    }

    private SyntheticAttributeRepository createAttributeRepositoryImpl(){
        final SyntheticAttributeRepository repository = createAttributeRepository(instanceName);
        repository.init(threadPool, descriptionProvider);
        return repository;
    }

    @Aggregation
    protected final SyntheticAttributeRepository getAttributes() {
        return attributes.get(this, DataStreamConnector::createAttributeRepositoryImpl);
    }

    @Nonnull
    protected SyntheticNotificationRepository createNotificationRepository(final String resourceName){
        return new SyntheticNotificationRepository(resourceName);
    }

    private SyntheticNotificationRepository createNotificationRepositoryImpl(){
        final SyntheticNotificationRepository repository = createNotificationRepository(instanceName);
        repository.init(threadPool, descriptionProvider);
        repository.setSource(this);
        return repository;
    }

    @Aggregation
    protected final SyntheticNotificationRepository getNotifications() {
        return notifications.get(this, DataStreamConnector::createNotificationRepositoryImpl);
    }

    protected final String getInstanceName(){
        return instanceName;
    }

    protected final String getGroupName() {
        final String groupName = getConfiguration().getGroupName();
        return isNullOrEmpty(groupName) ? getInstanceName() : groupName;
    }

    @Override
    public final String getReplicaName() {
        return instanceName;
    }

    @Nonnull
    @Override
    @OverridingMethodsMustInvokeSuper
    public Replica createReplica() throws ReplicationException {
        final Replica replica = new Replica();
        replica.addToReplica(getAttributes());
        replica.addToReplica(arrivals);
        return replica;
    }

    /**
     * Loads replica.
     *
     * @param replica Replica to load. Cannot be {@literal null}.
     */
    @Override
    public void loadFromReplica(@Nonnull final Replica replica) throws ReplicationException {
        replica.restoreFromReplica(getAttributes());
        arrivals = replica.restoreFromReplica();
    }

    @Override
    protected final MetricsSupport createMetricsReader() {
        return assembleMetricsReader(getAttributes().getMetrics(), getNotifications().getMetrics(), operations.getMetrics(), arrivals);
    }

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @ManagementOperation(description = "Resets all metrics")
    public void resetAllMetrics() {
        getAttributes().resetAllMetrics();
    }

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @ManagementOperation(description = "Resets the specified metrics")
    public boolean resetMetric(@OperationParameter(name = "attributeName", description = "The name of the attribute to reset") final String attributeName) {
        return getAttributes().getAttributeInfo(attributeName)
                .flatMap(Convert.toType(MetricHolderAttribute.class))
                .map(attribute -> {
                    attribute.reset();
                    return true;
                })
                .orElse(false);
    }

    public final void dispatch(final Map<String, ?> headers, final Object body) throws Exception {
        final NotificationParser notificationParser = this.notificationParser.get(this, DataStreamConnector::createNotificationParser);
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
        getAttributes().handleNotification(notification, this::attributeProcessed);
        getNotifications().accept(notification);
        if (heartbeat != null)                 //update heartbeat if it is enabled
            Convert.toType(notification, HealthNotification.class).ifPresent(heartbeat);
        Convert.toType(notification, SpanNotification.class).ifPresent(arrivals::accept);
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
                getNotifications().accept(notification);
            });
        }
    }

    /**
     * Creates a new notification parser.
     * @return A new instance of notification parser.
     */
    protected abstract NotificationParser createNotificationParser();

    @Override
    public final void addResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, getAttributes(), getNotifications(), operations);
    }

    @Override
    public final void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, getAttributes(), getNotifications(), operations);
    }

    /**
     * Releases all resources associated with this connector.
     * @throws Exception Unable to release resource clearly.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void close() throws Exception {
        attributes.remove().ifPresent(AbstractFeatureRepository::close);
        notifications.remove().ifPresent(AbstractFeatureRepository::close);
        notificationParser.remove();
        arrivals = null;
        operations.close();
        super.close();
    }
}