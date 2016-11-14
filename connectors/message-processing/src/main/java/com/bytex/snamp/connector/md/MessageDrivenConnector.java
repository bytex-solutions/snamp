package com.bytex.snamp.connector.md;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.md.notifications.NotificationSource;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.connector.operations.reflection.JavaBeanOperationRepository;
import com.bytex.snamp.connector.operations.reflection.ManagementOperation;
import com.bytex.snamp.connector.operations.reflection.OperationParameter;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import static com.bytex.snamp.internal.Utils.callUnchecked;

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
    @Aggregation(cached = true)
    protected final NotificationDispatcher dispatcher;
    @Aggregation(cached = true)
    private final JavaBeanOperationRepository operations;

    protected MessageDrivenConnector(final String resourceName,
                                     final Map<String, String> parameters,
                                     final MessageDrivenConnectorConfigurationDescriptor descriptor) {
        final String componentInstance = descriptor.parseComponentInstance(parameters, resourceName);
        final String componentName = descriptor.parseComponentName(parameters);
        final ExecutorService threadPool = descriptor.parseThreadPool(parameters);
        //init parser
        final NotificationParser parser = createNotificationParser(resourceName, componentInstance, componentName, parameters);
        assert parser != null;
        //init attributes
        final MessageDrivenAttributeRepository attributes = createAttributeRepository(resourceName, descriptor.parseSyncPeriod(parameters));
        assert attributes != null;
        attributes.init(threadPool, getLogger());
        //init notifications
        final MessageDrivenNotificationRepository notifications = createNotificationRepository(resourceName);
        assert notifications != null;
        notifications.init(threadPool, getLogger());

        dispatcher = new NotificationDispatcher(componentName, componentInstance, attributes, notifications, getLogger(), parser);

        final BeanInfo info = callUnchecked(() -> Introspector.getBeanInfo(getClass(), AbstractManagedResourceConnector.class));
        operations = JavaBeanOperationRepository.create(resourceName, this, info);
    }

    @Aggregation(cached = true)
    protected final MessageDrivenAttributeRepository getAttributes(){
        return dispatcher.attributes;
    }

    @Aggregation(cached = true)
    protected final MessageDrivenNotificationRepository getNotifications(){
        return dispatcher.notifications;
    }

    @Override
    protected final MetricsSupport createMetricsReader() {
        return assembleMetricsReader(dispatcher.attributes, dispatcher.notifications);
    }

    @SpecialUse
    @ManagementOperation(description = "Resets all metrics")
    public void resetAllMetrics(){
        dispatcher.attributes.resetAllMetrics();
    }

    @SpecialUse
    @ManagementOperation(description = "Resets the specified metrics")
    public boolean resetMetric(@OperationParameter(name = "attributeName", description = "The name of the attribute to reset") final String attributeName) {
        final MessageDrivenAttribute attribute = dispatcher.attributes.getAttributeInfo(attributeName);
        final boolean success;
        if (success = attribute instanceof MetricHolderAttribute<?, ?>)
            ((MetricHolderAttribute<?, ?>) attribute).reset();
        return success;
    }

    public final void dispatch(final Map<String, ?> headers, final Object body){
        dispatcher.handleNotification(headers, body, this);
    }

    public final boolean represents(final NotificationSource source){
        return dispatcher.equals(source);
    }

    /**
     * Creates a new notification parser.
     * @param resourceName Resource name.
     * @param instanceName Instance of the component that can be used as a filter in parser.
     * @param componentName Component name that can be used as a filter in parser.
     * @param parameters Set of parameters that may be used by notification parser.
     * @return A new instance of notification parser.
     */
    protected abstract NotificationParser createNotificationParser(final String resourceName,
                                                                   final String instanceName,
                                                                   final String componentName,
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
        addResourceEventListener(listener, dispatcher.attributes, dispatcher.notifications, operations);
    }

    @Override
    public final void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, dispatcher.attributes, dispatcher.notifications, operations);
    }

    /**
     * Releases all resources associated with this connector.
     * @throws Exception Unable to release resource clearly.
     */
    @Override
    public void close() throws Exception {
        dispatcher.attributes.close();
        dispatcher.notifications.close();
        operations.close();
        super.close();
    }
}