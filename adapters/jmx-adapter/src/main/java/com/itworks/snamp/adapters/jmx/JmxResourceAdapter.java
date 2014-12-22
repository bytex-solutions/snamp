package com.itworks.snamp.adapters.jmx;

import com.google.common.eventbus.EventBus;
import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.connectors.notifications.Notification;
import com.itworks.snamp.connectors.notifications.NotificationMetadata;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.licensing.LicensingException;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationEmitter;
import javax.management.ObjectName;
import javax.management.openmbean.OpenDataException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents JMX adapter. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxResourceAdapter extends AbstractResourceAdapter {
    static final String NAME = JmxAdapterHelpers.ADAPTER_NAME;

    private static final class JmxNotifications extends AbstractNotificationsModel<JmxNotificationMapping> {
        private final EventBus notificationBus;
        private static final String ID_SEPARATOR = "::";

        private JmxNotifications() {
            notificationBus = new EventBus();
        }

        /**
         * Creates a new notification metadata representation.
         *
         * @param resourceName User-defined name of the managed resource.
         * @param eventName    The resource-local identifier of the event.
         * @param notifMeta    The notification metadata to wrap.
         * @return A new notification metadata representation.
         */
        @Override
        protected JmxNotificationMapping createNotificationView(final String resourceName, final String eventName, final NotificationMetadata notifMeta) {
            return new JmxNotificationMapping(notifMeta, eventName);
        }

        /**
         * Processes SNMP notification.
         *
         * @param sender               The name of the managed resource which emits the notification.
         * @param notif                The notification to process.
         * @param notificationMetadata The metadata of the notification.
         */
        @Override
        protected void handleNotification(final String sender, Notification notif, final JmxNotificationMapping notificationMetadata) {
            Object attachment = notif.getAttachment();
            try {
                attachment = notificationMetadata.convertAttachment(attachment);
            } catch (final OpenDataException e) {
                JmxAdapterHelpers.log(Level.WARNING, "Unable to parse attachment %s from notification %s",
                        attachment,
                        notificationMetadata.getCategory(),
                        e);
                attachment = null;
            }
            notificationBus.post(new JmxNotificationSurrogate(notificationMetadata.getCategory(), sender, notif.getMessage(), notif.getTimeStamp(), attachment));
            notif = notif.getNext();
            if (notif != null) handleNotification(sender, notif, notificationMetadata);
        }

        /**
         * Creates subscription list ID.
         *
         * @param resourceName User-defined name of the managed resource which can emit the notification.
         * @param eventName    User-defined name of the event.
         * @return A new unique subscription list ID.
         */
        @Override
        protected String makeSubscriptionListID(final String resourceName, final String eventName) {
            return hashCode() + ID_SEPARATOR + resourceName + ID_SEPARATOR + eventName;
        }

        public Map<String, JmxNotificationMapping> get(final String resourceName) {
            final Map<String, JmxNotificationMapping> attributes = new HashMap<>(10);
            for (final String id : keySet()) {
                final String[] parts = id.split(ID_SEPARATOR);
                if (parts.length != 3 || !Objects.equals(parts[0], resourceName)) continue;
                attributes.put(parts[2], super.get(id));
            }
            return attributes;
        }

        public <L extends NotificationEmitter & JmxNotificationHandler> void addListener(final L listener) {
            notificationBus.register(listener);
        }
    }

    private static final class JmxAttributes extends AbstractAttributesModel<JmxAttributeMapping>{
        private static final String ID_SEPARATOR = "::";
        private boolean pureSerialization = false;

        /**
         * Creates a new domain-specific representation of the management attribute.
         *
         * @param resourceName             User-defined name of the managed resource.
         * @param userDefinedAttributeName User-defined name of the attribute.
         * @param accessor                 An accessor for the individual management attribute.
         * @return A new domain-specific representation of the management attribute.
         */
        @Override
        protected JmxAttributeMapping createAttributeView(final String resourceName, final String userDefinedAttributeName, final AttributeAccessor accessor) {
            return new JmxAttributeMapping(accessor, pureSerialization);
        }

        /**
         * Creates a new unique identifier of the management attribute.
         * <p>
         * The identifier must be unique through all instances of the resource adapter.
         * </p>
         *
         * @param resourceName             User-defined name of the managed resource which supply the attribute.
         * @param userDefinedAttributeName User-defined name of the attribute.
         * @return A new unique identifier of the management attribute.
         */
        @Override
        protected String makeAttributeID(final String resourceName, final String userDefinedAttributeName) {
            return String.format("%s" + ID_SEPARATOR + "%s", resourceName, userDefinedAttributeName);
        }

        public Map<String, JmxAttributeMapping> get(final String resourceName){
            final Map<String, JmxAttributeMapping> attributes = new HashMap<>(10);
            for(final String id: keySet()){
                final String[] parts = id.split(ID_SEPARATOR);
                if(parts.length != 2 || !Objects.equals(parts[0], resourceName)) continue;
                attributes.put(parts[1], super.get(id));
            }
            return attributes;
        }

        public void usePureSerialization() {
            pureSerialization = true;
        }
    }

    private final ObjectName rootObjectName;
    private final boolean usePlatformMBean;
    private final JmxAttributes attributes;
    private final JmxNotifications notifications;
    private final Map<ObjectName, ProxyMBean> exposedBeans;

    JmxResourceAdapter(final String adapterInstanceName,
                              final ObjectName rootObjectName,
                              final boolean usePlatformMBean) throws MalformedObjectNameException {
        super(adapterInstanceName);
        this.rootObjectName = rootObjectName;
        this.usePlatformMBean = usePlatformMBean;
        this.attributes = new JmxAttributes();
        this.exposedBeans = new HashMap<>(10);
        this.notifications = new JmxNotifications();
    }

    void usePureSerialization(){
        attributes.usePureSerialization();
    }

    private static ObjectName createObjectName(final ObjectName rootObjectName, final String resourceName) throws MalformedObjectNameException {
        final Hashtable<String, String> attrs = new Hashtable<>(rootObjectName.getKeyPropertyList());
        attrs.put("resource", resourceName);
        return new ObjectName(rootObjectName.getDomain(), attrs);
    }

    private void registerMBean(final ObjectName name, final ProxyMBean mbean) throws JMException{
        if(usePlatformMBean)
            ManagementFactory.getPlatformMBeanServer().registerMBean(mbean, name);
        else
            mbean.registerAsService(Utils.getBundleContextByObject(this), name);
        exposedBeans.put(name, mbean);
    }

    private void unregisterMBean(final ObjectName name, final ProxyMBean bean) throws JMException {
        if (usePlatformMBean)
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(name);
        else
            bean.unregister();
    }

    /**
     * Starts the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     * @throws java.lang.Exception Internal adapter error.
     */
    @Override
    protected void start() throws Exception {
        populateModel(attributes);
        try {
            JmxAdapterLicenseLimitations.current().verifyJmxNotificationsFeature();
            populateModel(notifications);
        } catch (final LicensingException e) {
            JmxAdapterHelpers.log(Level.INFO, "JMX notifications are not allowed by your SNAMP license", e);
        }
        for (final String resourceName : getHostedResources())
            try {
                final ProxyMBean bean = new ProxyMBean(resourceName, attributes.get(resourceName), notifications.get(resourceName));
                final ObjectName beanName = createObjectName(rootObjectName, resourceName);
                registerMBean(beanName, bean);
                notifications.addListener(bean);
                JmxAdapterHelpers.log(Level.FINE, "Bean %s is registered by %s adapter", beanName, getInstanceName(), null);
            } catch (final JMException e) {
                JmxAdapterHelpers.log(Level.SEVERE, "Unable to register MBean for resource %s", resourceName, e);
            }
    }

    /**
     * Stops the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     */
    @Override
    protected void stop() {
        clearModel(attributes);
        clearModel(notifications);
        for (final ObjectName name : exposedBeans.keySet())
            try {
                unregisterMBean(name, exposedBeans.get(name));
            } catch (final JMException e) {
                JmxAdapterHelpers.log(Level.SEVERE, "Unable to unregister MBean %s", name, e);
            }
        exposedBeans.clear();
        System.gc();
    }

    /**
     * Gets withLogger associated with this service.
     *
     * @return The withLogger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return getLogger(NAME);
    }
}
