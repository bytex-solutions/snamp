package com.itworks.snamp.connectors.aggregator;

import com.google.common.base.Function;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.concurrent.Repeater;
import com.itworks.snamp.connectors.AbstractManagedResourceConnector;
import com.itworks.snamp.connectors.ResourceEventListener;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.attributes.OpenAttributeSupport;
import com.itworks.snamp.connectors.notifications.AbstractNotificationSupport;
import com.itworks.snamp.connectors.notifications.NotificationDescriptor;
import com.itworks.snamp.connectors.notifications.NotificationListenerInvoker;
import com.itworks.snamp.connectors.notifications.NotificationListenerInvokerFactory;
import com.itworks.snamp.core.OSGiLoggingContext;
import com.itworks.snamp.internal.Utils;
import org.osgi.framework.BundleContext;

import javax.management.*;
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
    private static final class AttributeAggregationSupport extends OpenAttributeSupport<AbstractAttributeAggregation>{
        private AttributeAggregationSupport(final String resourceName){
            super(resourceName, AbstractAttributeAggregation.class);
        }

        @Override
        protected AbstractAttributeAggregation connectAttribute(final String attributeID, final AttributeDescriptor descriptor) throws Exception {
            switch (descriptor.getAttributeName()){
                case PatternMatcher.NAME: return new PatternMatcher(attributeID, descriptor);
                case UnaryComparison.NAME: return new UnaryComparison(attributeID, descriptor);
                case BinaryComparison.NAME: return new BinaryComparison(attributeID, descriptor);
                case BinaryPercent.NAME: return new BinaryPercent(attributeID, descriptor);
                case UnaryPercent.NAME: return new UnaryPercent(attributeID, descriptor);
                case Counter.NAME: return new Counter(attributeID, descriptor);
                case Average.NAME: return new Average(attributeID, descriptor);
                case Peak.NAME: return new Peak(attributeID, descriptor);
                case Decomposer.NAME: return new Decomposer(attributeID, descriptor);
                case Stringifier.NAME: return new Stringifier(attributeID, descriptor);
                default: return null;
            }
        }

        @Override
        protected void failedToConnectAttribute(final String attributeID, final String attributeName, final Exception e) {
            failedToConnectAttribute(getLoggerImpl(), Level.SEVERE, attributeID, attributeName, e);
        }

        @Override
        protected void failedToGetAttribute(final String attributeID, final Exception e) {
            failedToGetAttribute(getLoggerImpl(), Level.SEVERE, attributeID, e);
        }

        @Override
        protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
            failedToSetAttribute(getLoggerImpl(), Level.SEVERE, attributeID, value, e);
        }
    }

    private static final class NotificationAggregationSupport extends AbstractNotificationSupport<AbstractAggregatorNotification>{
        private final NotificationListenerInvoker invoker;

        private NotificationAggregationSupport(final String resourceName) {
            super(resourceName, AbstractAggregatorNotification.class);
            invoker = NotificationListenerInvokerFactory.createSequentialInvoker();
        }

        @Override
        protected NotificationListenerInvoker getListenerInvoker() {
            return invoker;
        }

        @Override
        protected AbstractAggregatorNotification enableNotifications(final String notifType,
                                                             final NotificationDescriptor metadata) throws AbsentAggregatorNotificationParameterException {
            switch (metadata.getNotificationCategory()){
                case PeriodicAttributeQuery.CATEGORY:
                    return new PeriodicAttributeQuery(notifType, metadata, getLoggerImpl());
                case HealthCheckNotification.CATEGORY:
                    return new HealthCheckNotification(notifType, metadata, getLoggerImpl());
                default: return null;
            }
        }

        @Override
        protected void failedToEnableNotifications(final String listID, final String category, final Exception e) {
            try(final OSGiLoggingContext logger = OSGiLoggingContext.get(getLoggerImpl(), getBundleContext())){
                failedToEnableNotifications(logger, Level.SEVERE, listID, category, e);
            }
        }

        private BundleContext getBundleContext(){
            return Utils.getBundleContextByObject(this);
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
        private final NotificationAggregationSupport notifications;

        private NotificationSender(final TimeSpan period,
                                   final NotificationAggregationSupport notifs) {
            super(period);
            this.notifications = notifs;
        }

        @Override
        protected void doAction() {
            notifications.emitAll();
        }
    }

    private final AttributeAggregationSupport attributes;
    private final NotificationAggregationSupport notifications;
    private final NotificationSender sender;

    AggregatorResourceConnector(final String resourceName,
                                final TimeSpan notificationFrequency) throws IntrospectionException {
        attributes = new AttributeAggregationSupport(resourceName);
        notifications = new NotificationAggregationSupport(resourceName);
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

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the aggregated object.
     * @return An instance of the requested object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(final Class<T> objectType) {
        return findObject(objectType,
                new Function<Class<T>, T>() {
                    @Override
                    public T apply(final Class<T> objectType) {
                        return AggregatorResourceConnector.super.queryObject(objectType);
                    }
                }, attributes, notifications);
    }

    boolean addAttribute(final String attributeID, final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options) {
        return attributes.addAttribute(attributeID, attributeName, readWriteTimeout, options) != null;
    }

    boolean enableNotifications(final String listID, final String category, final CompositeData options){
        return notifications.enableNotifications(listID, category, options) != null;
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
        attributes.removeAll(true);
        notifications.removeAll(true, true);
    }
}
