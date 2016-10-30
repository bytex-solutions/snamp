package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.Acceptor;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.Repeater;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.core.Communicator;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.bytex.snamp.jmx.JMExceptionUtils;
import com.google.common.eventbus.Subscribe;
import groovy.lang.Closure;
import groovy.lang.Script;
import org.osgi.framework.BundleContext;

import javax.management.*;
import java.io.IOException;
import java.util.Dictionary;
import java.util.EventListener;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.logging.Logger;

import com.bytex.snamp.configuration.ManagedResourceConfiguration;

/**
 * Represents an abstract class for all Groovy-bases scenarios.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
abstract class ManagedResourceScript extends Script implements ManagedResourceScriptBase {

    private static abstract class NotificationOperation<E extends JMException> implements Acceptor<ManagedResourceConnector, E> {
        protected abstract void accept(final NotificationSupport connector) throws E;

        @Override
        public final void accept(final ManagedResourceConnector connector) throws E {
            accept(connector.queryObject(NotificationSupport.class));
        }
    }

    private static final class NotificationAddListener extends NotificationOperation<MBeanException> {
        private final NotificationListener listener;
        private final NotificationFilter filter;
        private final Object handback;

        private NotificationAddListener(final NotificationListener listener,
                                        final NotificationFilter filter,
                                        final Object handback) {
            this.listener = Objects.requireNonNull(listener);
            this.filter = filter;
            this.handback = handback;
        }

        @Override
        protected void accept(final NotificationSupport connector) throws MBeanException {
            if (connector == null)
                throw new MBeanException(new NullPointerException("Managed resource doesn't support notifications"));
            else
                connector.addNotificationListener(listener, filter, handback);
        }
    }

    private static final class NotificationRemoveListener extends NotificationOperation<ListenerNotFoundException> {
        private final NotificationListener listener;

        private NotificationRemoveListener(final NotificationListener listener) {
            this.listener = Objects.requireNonNull(listener);
        }

        @Override
        protected void accept(final NotificationSupport connector) throws ListenerNotFoundException {
            if (connector == null)
                throw new ListenerNotFoundException("Managed resource doesn't support notifications");
            else
                connector.removeNotificationListener(listener);
        }
    }

    private static final class NotificationMetaReader extends NotificationOperation<MBeanException> implements Supplier<Dictionary<String, ?>> {
        private Dictionary<String, ?> metadata;
        private final String notificationType;

        private NotificationMetaReader(final String notificationType) {
            this.notificationType = notificationType;
        }

        @Override
        protected void accept(final NotificationSupport connector) throws MBeanException {
            if (connector == null)
                throw new MBeanException(new NullPointerException("Managed resource doesn't support notifications"));
            for (final MBeanNotificationInfo notification : connector.getNotificationInfo())
                if (ArrayUtils.containsAny(notification.getNotifTypes(), notificationType)) {
                    metadata = DescriptorUtils.asDictionary(notification.getDescriptor());
                    return;
                }
        }

        @Override
        public Dictionary<String, ?> get() {
            return metadata;
        }
    }

    private static abstract class AttributeOperation<E extends JMException> implements Acceptor<ManagedResourceConnector, E> {
        private final String attributeName;

        private AttributeOperation(final String name) {
            this.attributeName = name;
        }

        @Override
        public abstract void accept(final ManagedResourceConnector connector) throws E;
    }

    private static final class AttributeValueReader extends AttributeOperation<JMException> implements Supplier {
        private Object attributeValue;

        private AttributeValueReader(final String name) {
            super(name);
        }

        @Override
        public void accept(final ManagedResourceConnector connector) throws JMException {
            attributeValue = connector.getAttribute(super.attributeName);
        }

        @Override
        public Object get() {
            return attributeValue;
        }
    }

    private static final class AttributeMetaReader extends AttributeOperation<AttributeNotFoundException> implements Supplier<Dictionary<String, ?>> {
        private Dictionary<String, ?> metadata;

        private AttributeMetaReader(final String name) {
            super(name);
        }

        @Override
        public void accept(final ManagedResourceConnector connector) throws AttributeNotFoundException {
            for (final MBeanAttributeInfo attributeInfo : connector.getMBeanInfo().getAttributes())
                if (Objects.equals(attributeInfo.getName(), super.attributeName)) {
                    metadata = DescriptorUtils.asDictionary(attributeInfo.getDescriptor());
                    return;
                }
            throw JMExceptionUtils.attributeNotFound(super.attributeName);
        }

        @Override
        public Dictionary<String, ?> get() {
            return metadata;
        }
    }

    private static final class AttributeValueWriter extends Attribute implements Acceptor<ManagedResourceConnector, JMException> {
        private static final long serialVersionUID = 2544352906527154257L;

        private AttributeValueWriter(final String name, final Object value) {
            super(name, value);
        }

        @Override
        public void accept(final ManagedResourceConnector connector) throws JMException {
            connector.setAttribute(this);
        }
    }

    /**
     * Creates a new timer which executes the specified action.
     *
     * @param job    The action to execute periodically. Cannot be {@literal null}.
     * @param period Execution period, in millis.
     * @return A new timer.
     */
    @SpecialUse
    protected static Repeater createTimer(final Closure<?> job, final long period) {
        return new Repeater(period) {
            @Override
            protected void doAction() {
                job.call();
            }
        };
    }

    /**
     * Schedules a new periodic task
     *
     * @param job    The action to execute periodically. Cannot be {@literal null}.
     * @param period Execution period, in millis.
     * @return Executed timer.
     */
    @SpecialUse
    protected static Repeater schedule(final Closure<?> job, final long period) {
        final Repeater timer = createTimer(job, period);
        timer.run();
        return timer;
    }

    private static Logger getLogger(){
        return ResourceConnectorInfo.getLogger();
    }

    @SpecialUse
    protected static void error(final String message) {
        getLogger().severe(message);
    }

    @SpecialUse
    protected static void warning(final String message) {
        getLogger().warning(message);
    }

    @SpecialUse
    protected static void info(final String message) {
        getLogger().info(message);
    }

    @SpecialUse
    protected static void debug(final String message) {
        getLogger().config(message);
    }

    @SpecialUse
    protected static void fine(final String message) {
        getLogger().fine(message);
    }

    private static BundleContext getBundleContext() {
        return Utils.getBundleContext(ManagedResourceScript.class);
    }

    private static <E extends Throwable> void processResourceConnector(final String resourceName,
                                                                       final Acceptor<ManagedResourceConnector, E> acceptor) throws E, InstanceNotFoundException {
        final BundleContext context = getBundleContext();
        final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(context, resourceName);
        try {
            acceptor.accept(client.getService());
        } finally {
            client.release(context);
        }
    }

    /**
     * Reads value of the managed resource attribute.
     *
     * @param resourceName  The name of the managed resource.
     * @param attributeName The name of the attribute.
     * @return The value of the attribute.
     * @throws JMException Unable to
     */
    @SpecialUse
    protected static Object getResourceAttribute(final String resourceName,
                                                 final String attributeName) throws JMException {
        final AttributeValueReader reader = new AttributeValueReader(attributeName);
        processResourceConnector(resourceName, reader);
        return reader.get();
    }

    /**
     * Reads attribute metadata.
     *
     * @param resourceName  The name of the managed resource.
     * @param attributeName The name of the attribute.
     * @return A dictionary of attribute parameters.
     * @throws AttributeNotFoundException The attribute doesn't exist in the specified managed resource.
     */
    @SpecialUse
    protected static Dictionary<String, ?> getResourceAttributeInfo(final String resourceName,
                                                                    final String attributeName) throws AttributeNotFoundException, InstanceNotFoundException {
        final AttributeMetaReader reader = new AttributeMetaReader(attributeName);
        processResourceConnector(resourceName, reader);
        return reader.get();
    }

    /**
     * Sets value of the managed resource attribute.
     *
     * @param resourceName  The name of the managed resource.
     * @param attributeName The name of the attribute.
     * @param value         The value of the attribute.
     * @throws JMException Unable to set attribute value.
     */
    @SpecialUse
    protected static void setResourceAttribute(final String resourceName,
                                               final String attributeName,
                                               final Object value) throws JMException {
        processResourceConnector(resourceName, new AttributeValueWriter(attributeName, value));
    }

    /**
     * Reads notification metadata.
     *
     * @param resourceName The name of the managed resource.
     * @param notifType    The notification type as it configured in the managed resource.
     * @return A dictionary of attribute parameters.
     * @throws MBeanException Notification is not declared by managed resource.
     */
    @SpecialUse
    protected static Dictionary<String, ?> getResourceNotificationInfo(final String resourceName,
                                                                       final String notifType) throws MBeanException, InstanceNotFoundException {
        final NotificationMetaReader reader = new NotificationMetaReader(notifType);
        processResourceConnector(resourceName, reader);
        return reader.get();
    }

    @SpecialUse
    protected static void addNotificationListener(final String resourceName,
                                                  final NotificationListener listener) throws MBeanException, InstanceNotFoundException {
        addNotificationListener(resourceName, listener, null, null);
    }

    @SpecialUse
    protected static void addNotificationListener(final String resourceName,
                                                  final NotificationListener listener,
                                                  final NotificationFilter filter,
                                                  final Objects handback) throws MBeanException, InstanceNotFoundException {
        processResourceConnector(resourceName, new NotificationAddListener(listener, filter, handback));
    }

    @SpecialUse
    protected static void removeNotificationListener(final String resourceName,
                                                     final NotificationListener listener) throws ListenerNotFoundException, InstanceNotFoundException {
        processResourceConnector(resourceName, new NotificationRemoveListener(listener));
    }

    @SpecialUse
    protected static ManagedResourceConfiguration getResourceConfiguration(final String resourceName) throws IOException {

        return ManagedResourceConnectorClient.getResourceConfiguration(getBundleContext(), resourceName);
    }

    @SpecialUse
    protected static Communicator getCommunicator(final String sessionName) {
        final BundleContext context = getBundleContext();
        return context == null ? DistributedServices.getProcessLocalCommunicator(sessionName) : DistributedServices.getDistributedCommunicator(context, sessionName);
    }

    @SpecialUse
    protected static boolean isActiveClusterNode(){
        return DistributedServices.isActiveNode(getBundleContext());
    }
}