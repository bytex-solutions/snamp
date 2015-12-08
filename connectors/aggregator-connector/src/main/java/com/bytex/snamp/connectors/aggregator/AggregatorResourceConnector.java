package com.bytex.snamp.connectors.aggregator;

import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.concurrent.Repeater;
import com.bytex.snamp.connectors.AbstractManagedResourceConnector;
import com.bytex.snamp.connectors.ResourceEventListener;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.AttributeSupport;
import com.bytex.snamp.connectors.attributes.OpenAttributeRepository;
import com.bytex.snamp.connectors.metrics.MetricsReader;
import com.bytex.snamp.connectors.notifications.AbstractNotificationRepository;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.bytex.snamp.connectors.notifications.NotificationListenerInvoker;
import com.bytex.snamp.connectors.notifications.NotificationListenerInvokerFactory;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.internal.Utils;
import org.osgi.framework.BundleContext;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanNotificationInfo;
import javax.management.openmbean.CompositeData;
import java.beans.IntrospectionException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an aggregator of other managed resources.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class AggregatorResourceConnector extends AbstractManagedResourceConnector implements AttributeSupport {
    private static final class AttributeAggregationRepository extends OpenAttributeRepository<AbstractAttributeAggregation> {
        private AttributeAggregationRepository(final String resourceName){
            super(resourceName, AbstractAttributeAggregation.class);
        }

        private BundleContext getBundleContext(){
            return Utils.getBundleContextOfObject(this);
        }

        @Override
        protected AbstractAttributeAggregation connectAttribute(final String attributeName, final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException, JMException {
            switch (descriptor.getName(attributeName)){
                case PatternMatcher.NAME: return new PatternMatcher(attributeName, descriptor);
                case UnaryComparison.NAME: return new UnaryComparison(attributeName, descriptor);
                case BinaryComparison.NAME: return new BinaryComparison(attributeName, descriptor);
                case BinaryPercent.NAME: return new BinaryPercent(attributeName, descriptor);
                case UnaryPercent.NAME: return new UnaryPercent(attributeName, descriptor);
                case Counter.NAME: return new Counter(attributeName, descriptor);
                case Average.NAME: return new Average(attributeName, descriptor);
                case Peak.NAME: return new Peak(attributeName, descriptor);
                case Decomposer.NAME: return new Decomposer(attributeName, descriptor);
                case Stringifier.NAME: return new Stringifier(attributeName, descriptor);
                case Composer.NAME: return new Composer(attributeName, descriptor, getBundleContext());
                default: return null;
            }
        }

        @Override
        protected void failedToConnectAttribute(final String attributeName, final Exception e) {
            failedToConnectAttribute(getLoggerImpl(), Level.SEVERE, attributeName, e);
        }

        @Override
        protected void failedToGetAttribute(final String attributeName, final Exception e) {
            failedToGetAttribute(getLoggerImpl(), Level.SEVERE, attributeName, e);
        }

        @Override
        protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
            failedToSetAttribute(getLoggerImpl(), Level.SEVERE, attributeID, value, e);
        }
    }

    private static final class NotificationAggregationRepository extends AbstractNotificationRepository<AbstractAggregatorNotification> {
        private final NotificationListenerInvoker invoker;

        private NotificationAggregationRepository(final String resourceName,
                                                  final BundleContext context) {
            super(resourceName,
                    AbstractAggregatorNotification.class,
                    DistributedServices.getDistributedCounter(context, "notifications-".concat(resourceName)));
            invoker = NotificationListenerInvokerFactory.createSequentialInvoker();
        }

        /**
         * Determines whether raising of registered events is suspended.
         *
         * @return {@literal true}, if events are suspended; otherwise {@literal false}.
         */
        @Override
        public boolean isSuspended() {
            return super.isSuspended() && !DistributedServices.isActiveNode(Utils.getBundleContextOfObject(this));
        }

        @Override
        protected NotificationListenerInvoker getListenerInvoker() {
            return invoker;
        }

        @Override
        protected AbstractAggregatorNotification enableNotifications(final String notifType,
                                                             final NotificationDescriptor metadata) throws AbsentAggregatorNotificationParameterException {
            switch (metadata.getName(notifType)){
                case PeriodicAttributeQuery.CATEGORY:
                    return new PeriodicAttributeQuery(notifType, metadata, getLoggerImpl());
                case HealthCheckNotification.CATEGORY:
                    return new HealthCheckNotification(notifType, metadata, getLoggerImpl());
                default: return null;
            }
        }

        @Override
        protected void failedToEnableNotifications(final String category, final Exception e) {
            failedToEnableNotifications(getLoggerImpl(), Level.SEVERE, category, e);
        }

        private final class NotificationEnqueueImpl extends NotificationCollector implements NotificationEnqueue{
            private static final long serialVersionUID = -3816463826583470313L;

            @Override
            protected void process(final AbstractAggregatorNotification metadata) {
                try {
                    metadata.process(this);
                } catch (InstanceNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void sendNotification(final MBeanNotificationInfo metadata, final String message, final Object userData) {
                enqueue(metadata, message, userData);
            }
        }

        private void emitAll() {
            fire(new NotificationEnqueueImpl());
        }
    }

    private static final class NotificationSender extends Repeater{
        private final NotificationAggregationRepository notifications;

        private NotificationSender(final TimeSpan period,
                                   final NotificationAggregationRepository notifs) {
            super(period);
            this.notifications = notifs;
        }

        @Override
        protected void doAction() {
            notifications.emitAll();
        }
    }

    @Aggregation
    private final AttributeAggregationRepository attributes;
    @Aggregation
    private final NotificationAggregationRepository notifications;
    @Aggregation
    private final MetricsReader metrics;
    private final NotificationSender sender;

    AggregatorResourceConnector(final String resourceName,
                                final TimeSpan notificationFrequency) throws IntrospectionException {
        attributes = new AttributeAggregationRepository(resourceName);
        notifications = new NotificationAggregationRepository(resourceName, Utils.getBundleContextOfObject(this));
        metrics = assembleMetricsReader(attributes, notifications);
        sender = new NotificationSender(notificationFrequency, notifications);
        sender.run();
    }

    /**
     * Adds a new listener for the connector-related events.
     * <p/>
     * The managed resource connector should holds a weak reference to all added event listeners.
     *
     * @param listener An event listener to add.
     */
    @Override
    public void addResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, attributes, notifications);
    }

    /**
     * Removes connector event listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, attributes, notifications);
    }

    boolean addAttribute(final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options) {
        return attributes.addAttribute(attributeName, readWriteTimeout, options) != null;
    }

    boolean enableNotifications(final String category, final CompositeData options){
        return notifications.enableNotifications(category, options) != null;
    }

    void removeAttributesExcept(final Set<String> attributes) {
        this.attributes.removeAllExcept(attributes);
    }

    void disableNotificationsExcept(final Set<String> events) {
        this.notifications.removeAllExcept(events);
    }

    static Logger getLoggerImpl(){
        return getLogger(getConnectorType(AggregatorResourceConnector.class));
    }

    @Override
    public void close() throws Exception {
        super.close();
        sender.close();
        attributes.close();
        notifications.close();
    }
}
