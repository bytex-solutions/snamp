package com.bytex.snamp.scripting.groovy;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.core.Communicator;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.bytex.snamp.jmx.JMExceptionUtils;
import groovy.lang.Closure;
import groovy.lang.Script;
import org.osgi.framework.BundleContext;

import javax.management.*;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Represents implementation of {@link Scriptlet}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class Scriptlet extends Script implements ScriptingAPI {
    private static final String LOGGER_VAR = "logger";
    private static final String BUNDLE_CONTEXT_VAR = "bundleContext";

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

    private static abstract class NotificationOperation<E extends JMException> implements Acceptor<ManagedResourceConnector, E> {
        abstract void accept(final NotificationSupport connector) throws E;

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

    protected static <T> T invokeDslStatement(Closure<?> statement, final Supplier<T> delegate, final Object... args){
        final T result;
        statement = statement.rehydrate(result = delegate.get(), statement.getOwner(), statement.getThisObject());
        statement.call(args);
        return result;
    }

    /**
     * Gets logger.
     *
     * @return Logger.
     */
    @Override
    public final Logger getLogger() {
        return LoggerProvider.getLoggerForObject(this);
    }

    private BundleContext getBundleContext(){
        return (BundleContext) getProperty(BUNDLE_CONTEXT_VAR);
    }

    public final void setBundleContext(final BundleContext value){
        setProperty(BUNDLE_CONTEXT_VAR, value);
    }

    private <E extends Throwable> void processResourceConnector(final String resourceName,
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
    @Override
    public final Object getResourceAttribute(final String resourceName, final String attributeName) throws JMException {
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
    @Override
    public final Dictionary<String, ?> getResourceAttributeInfo(final String resourceName, final String attributeName) throws AttributeNotFoundException, InstanceNotFoundException {
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
    @Override
    public final void setResourceAttribute(final String resourceName, final String attributeName, final Object value) throws JMException {
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
    @Override
    public final Dictionary<String, ?> getResourceNotificationInfo(final String resourceName, final String notifType) throws MBeanException, InstanceNotFoundException {
        final NotificationMetaReader reader = new NotificationMetaReader(notifType);
        processResourceConnector(resourceName, reader);
        return reader.get();
    }

    @Override
    public final void addNotificationListener(final String resourceName, final NotificationListener listener) throws MBeanException, InstanceNotFoundException {
        addNotificationListener(resourceName, listener, null, null);
    }

    @Override
    public final void addNotificationListener(final String resourceName, final NotificationListener listener, final NotificationFilter filter, final Objects handback) throws MBeanException, InstanceNotFoundException {
        processResourceConnector(resourceName, new NotificationAddListener(listener, filter, handback));
    }

    @Override
    public final void removeNotificationListener(final String resourceName, final NotificationListener listener) throws ListenerNotFoundException, InstanceNotFoundException {
        processResourceConnector(resourceName, new NotificationRemoveListener(listener));
    }

    @Override
    public final ManagedResourceConfiguration getResourceConfiguration(final String resourceName) throws IOException {
        final ServiceHolder<ConfigurationManager> manager = ServiceHolder.tryCreate(getBundleContext(), ConfigurationManager.class);
        if (manager == null)
            return null;
        else
            try {
                return manager.get().transformConfiguration(config -> config.getEntities(ManagedResourceConfiguration.class).get(resourceName));
            } finally {
                manager.release(getBundleContext());
            }
    }

    @Override
    public final boolean isActiveClusterNode() {
        return DistributedServices.isActiveNode(getBundleContext());
    }

    @Override
    public final Communicator getCommunicator(final String sessionName) {
        return DistributedServices.getDistributedCommunicator(getBundleContext(), sessionName);
    }
}
