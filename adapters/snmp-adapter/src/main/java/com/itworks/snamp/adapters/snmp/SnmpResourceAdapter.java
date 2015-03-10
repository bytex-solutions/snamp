package com.itworks.snamp.adapters.snmp;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.concurrent.ThreadSafeObject;
import com.itworks.snamp.internal.AbstractKeyedObjects;
import com.itworks.snamp.internal.KeyedObjects;
import org.osgi.service.jndi.JNDIContextManager;
import org.snmp4j.agent.mo.snmp.TransportDomains;
import org.snmp4j.mp.MPv3;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TransportIpAddress;
import org.snmp4j.smi.UdpAddress;

import javax.management.JMException;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.adapters.snmp.SnmpAdapterConfigurationDescriptor.*;
import static com.itworks.snamp.adapters.snmp.SnmpHelpers.DateTimeFormatter;
import static com.itworks.snamp.jmx.DescriptorUtils.getField;
import static com.itworks.snamp.jmx.DescriptorUtils.hasField;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpResourceAdapter extends AbstractResourceAdapter {
    static final String NAME = SnmpHelpers.ADAPTER_NAME;

    private static final class SnmpNotificationMappingImpl implements SnmpNotificationMapping{
        private final MBeanNotificationInfo metadata;
        private final DateTimeFormatter formatter;
        private final String resourceName;

        private SnmpNotificationMappingImpl(final String resourceName,
                                            final MBeanNotificationInfo metadata) throws IllegalArgumentException{
            if(hasField(metadata.getDescriptor(), TARGET_ADDRESS_PARAM) &&
                    hasField(metadata.getDescriptor(), TARGET_NAME_PARAM) &&
                    hasField(metadata.getDescriptor(), OID_PARAM_NAME)) {
                this.metadata = metadata;
                this.formatter = SnmpHelpers.createDateTimeFormatter(getDateTimeDisplayFormat(metadata));
            } else throw new IllegalArgumentException("Target address, target name and event OID parameters are not specified for SNMP trap");
            this.resourceName = resourceName;
        }

        @Override
        public String getSource() {
            return resourceName;
        }

        @Override
        public DateTimeFormatter getTimestampFormatter() {
            return formatter;
        }

        @Override
        public OID getTransportDomain() {
            return kindOfIP(getField(metadata.getDescriptor(), TARGET_ADDRESS_PARAM, String.class));
        }

        private static OID kindOfIP(final String addr){
            if (addr.contains(":"))
                return TransportDomains.transportDomainUdpIpv6;
            else if (addr.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+"))
                return TransportDomains.transportDomainUdpIpv4;
            return TransportDomains.transportDomainUdpDns;
        }

        @Override
        public OctetString getReceiverAddress() {
            final TransportIpAddress addr = new UdpAddress(getField(metadata.getDescriptor(), TARGET_ADDRESS_PARAM, String.class));
            return new OctetString(addr.getValue());
        }

        @Override
        public OctetString getReceiverName() {
            return new OctetString(getField(metadata.getDescriptor(), TARGET_NAME_PARAM, String.class));
        }

        @Override
        public int getTimeout() {
            return hasField(metadata.getDescriptor(), TARGET_NOTIF_TIMEOUT_PARAM) ?
                    Integer.valueOf(getField(metadata.getDescriptor(), TARGET_NOTIF_TIMEOUT_PARAM, String.class)) : 0;
        }

        @Override
        public int getRetryCount() {
            return hasField(metadata.getDescriptor(), TARGET_RETRY_COUNT_PARAM) ?
                 Integer.valueOf(getField(metadata.getDescriptor(), TARGET_RETRY_COUNT_PARAM, String.class)) : 3;
        }

        @Override
        public OID getID() {
            return new OID(getOID(metadata));
        }

        @Override
        public MBeanNotificationInfo getMetadata() {
            return metadata;
        }
    }

    private static final class SnmpNotificationsModel extends ThreadSafeObject implements NotificationsModel{
        private static enum SNMResource{
            LISTENERS,
            NOTIFICATIONS
        }
        private final Collection<SnmpNotificationListener> listeners;
        private final KeyedObjects<String, SnmpNotificationMapping> notifications;
        private final String adapterInstanceName;

        private SnmpNotificationsModel(final String instanceName){
            super(SNMResource.class);
            listeners = Lists.newArrayListWithExpectedSize(3);
            notifications = createNotifs();
            adapterInstanceName = instanceName;
        }

        private void removeAllListeners() {
            beginWrite(SNMResource.LISTENERS);
            try{
                listeners.clear();
            }
            finally {
                endWrite(SNMResource.LISTENERS);
            }
        }

        private ImmutableList<SnmpNotificationMapping> getNotifications(){
            beginRead(SNMResource.NOTIFICATIONS);
            try{
                return ImmutableList.copyOf(notifications.values());
            }
            finally {
                endRead(SNMResource.NOTIFICATIONS);
            }
        }

        private static KeyedObjects<String, SnmpNotificationMapping> createNotifs(){
            return new AbstractKeyedObjects<String, SnmpNotificationMapping>(10) {
                private static final long serialVersionUID = 8947442745955339289L;

                @Override
                public String getKey(final SnmpNotificationMapping item) {
                    return item.getMetadata().getNotifTypes()[0];
                }
            };
        }

        private void subscribe(final SnmpNotificationListener listener){
            beginWrite(SNMResource.LISTENERS);
            try {
                listeners.add(listener);
            }
            finally {
                endWrite(SNMResource.LISTENERS);
            }
        }

        private void unsubscribe(final SnmpNotificationListener listener){
            beginWrite(SNMResource.LISTENERS);
            try {
                listeners.remove(listener);
            }
            finally {
                endWrite(SNMResource.LISTENERS);
            }
        }

        private String makeListID(final String resourceName, final String userDefinedName){
            return adapterInstanceName + '.' + resourceName + '.' + userDefinedName;
        }

        @Override
        public void addNotification(final String resourceName,
                                    final String userDefinedName,
                                    final String category,
                                    final NotificationConnector connector) {
            final String listID = makeListID(resourceName, userDefinedName);
            beginWrite(SNMResource.NOTIFICATIONS);
            try{
                if(notifications.containsKey(listID))
                    return;
                notifications.put(new SnmpNotificationMappingImpl(resourceName, connector.enable(listID)));
            } catch (final JMException e) {
                SnmpHelpers.log(Level.SEVERE, "Failed to enable notification %s", listID, e);
            } finally {
                endWrite(SNMResource.NOTIFICATIONS);
            }
        }

        @Override
        public MBeanNotificationInfo removeNotification(final String resourceName,
                                                        final String userDefinedName,
                                                        final String category) {
            final String listID = makeListID(resourceName, userDefinedName);
            beginWrite(SNMResource.NOTIFICATIONS);
            try{
                return notifications.containsKey(listID) ?
                        notifications.remove(listID).getMetadata():
                        null;
            }
            finally {
                endWrite(SNMResource.NOTIFICATIONS);
            }
        }

        /**
         * Determines whether this model is empty.
         *
         * @return {@literal true}, if this model is empty; otherwise, {@literal false}.
         */
        @Override
        public boolean isEmpty() {
            beginRead(SNMResource.NOTIFICATIONS);
            try{
                return notifications.isEmpty();
            }
            finally {
                endRead(SNMResource.NOTIFICATIONS);
            }
        }

        /**
         * Removes all notifications from this model.
         */
        @Override
        public void clear() {
            beginWrite(SNMResource.NOTIFICATIONS);
            try{
                notifications.clear();
            }
            finally {
                endWrite(SNMResource.NOTIFICATIONS);
            }
        }

        /**
         * Invoked when a JMX notification occurs.
         * The implementation of this method should return as soon as possible, to avoid
         * blocking its notification broadcaster.
         *
         * @param notification The notification.
         * @param handback     An opaque object which helps the listener to associate
         *                     information regarding the MBean emitter. This object is passed to the
         *                     addNotificationListener call and resent, without modification, to the
         */
        @Override
        public void handleNotification(final Notification notification, final Object handback) {
            final SnmpNotificationMapping metadata;
            beginRead(SNMResource.NOTIFICATIONS);
            try{
                if(notifications.containsKey(notification.getType()))
                    metadata = notifications.get(notification.getType());
                else return;
            }
            finally {
                endRead(SNMResource.NOTIFICATIONS);
            }
            notification.setSource(metadata.getSource());
            final SnmpNotification snmpTrap = new SnmpNotification(notification, metadata.getMetadata());
            beginRead(SNMResource.LISTENERS);
            try{
                for(final SnmpNotificationListener listener: listeners)
                    listener.processNotification(snmpTrap);
            }
            finally {
                endRead(SNMResource.LISTENERS);
            }
        }
    }

    private static final class SnmpAttributesModel extends ThreadSafeObject implements AttributesModel{
        private final String adapterInstanceName;
        private final KeyedObjects<String, SnmpAttributeMapping> attributes;

        private SnmpAttributesModel(final String instanceName){
            this.adapterInstanceName = instanceName;
            this.attributes = createAttrs();
        }

        private static KeyedObjects<String, SnmpAttributeMapping> createAttrs(){
            return new AbstractKeyedObjects<String, SnmpAttributeMapping>(10) {
                private static final long serialVersionUID = 8303706968265686050L;

                @Override
                public String getKey(final SnmpAttributeMapping item) {
                    return item.getMetadata().getName();
                }
            };
        }

        private String makeAttributeID(final String resourceName,
                                              final String userDefinedName){
            return adapterInstanceName + '/' + resourceName + '/' + userDefinedName;
        }

        @Override
        public void addAttribute(final String resourceName,
                                 final String userDefinedName,
                                 final String attributeName,
                                 final AttributeConnector connector) {
            final String attributeID = makeAttributeID(resourceName, userDefinedName);
            beginWrite();
            try{
                final AttributeAccessor accessor = connector.connect(attributeID);
                if(hasField(accessor.getMetadata().getDescriptor(), OID_PARAM_NAME)){
                    final SnmpType type = SnmpType.map(accessor.getType());
                    if(type != null){
                        final SnmpAttributeMapping mapping = type.createManagedObject(accessor);
                        attributes.put(mapping);
                    }
                    else
                        SnmpHelpers.log(Level.WARNING, "Attribute %s has no SNMP-compliant type projection.", userDefinedName, null);
                }
                else
                    SnmpHelpers.log(Level.WARNING, "Attribute %s has no OID parameter.", userDefinedName, null);
            } catch (final JMException e) {
                SnmpHelpers.log(Level.SEVERE, "Unable to connect %s attribute", attributeID, e);
            } finally {
                endWrite();
            }
        }

        @Override
        public AttributeAccessor removeAttribute(final String resourceName,
                                                 final String userDefinedName,
                                                 final String attributeName) {
            final String attributeID = makeAttributeID(resourceName, userDefinedName);
            beginWrite();
            try{
                return attributes.containsKey(attributeID) ?
                    attributes.remove(attributeID).queryObject(AttributeAccessor.class):
                    null;
            }
            finally {
                endWrite();
            }
        }

        /**
         * Removes all attributes from this model.
         */
        @Override
        public void clear() {
            beginWrite();
            try{
                for(final SnmpAttributeMapping mapping: attributes.values()){
                    final AttributeAccessor accessor = mapping.queryObject(AttributeAccessor.class);
                    if(accessor != null) accessor.disconnect();
                }
            }
            finally {
                endWrite();
            }
        }

        /**
         * Determines whether this model is empty.
         *
         * @return {@literal true}, if this model is empty; otherwise, {@literal false}.
         */
        @Override
        public boolean isEmpty() {
            beginRead();
            try{
                return attributes.isEmpty();
            }
            finally {
                endRead();
            }
        }

        private ImmutableList<SnmpAttributeMapping> getAttributes(){
            beginRead();
            try{
                return ImmutableList.copyOf(attributes.values());
            }
            finally {
                endRead();
            }
        }
    }

    private SnmpAgent agent;
    private final SnmpAttributesModel attributes;
    private final SnmpNotificationsModel notifications;
    private final DirContextFactory contextFactory;

    SnmpResourceAdapter(final String adapterInstanceName, final JNDIContextManager contextManager) {
        super(adapterInstanceName);
        attributes = new SnmpAttributesModel(adapterInstanceName);
        notifications = new SnmpNotificationsModel(adapterInstanceName);
        contextFactory = createFactory(contextManager);
    }

    private static DirContextFactory createFactory(final JNDIContextManager contextManager){
        return new DirContextFactory() {
            @Override
            public DirContext create(final Hashtable<?, ?> env) throws NamingException {
                return contextManager.newInitialDirContext(env);
            }
        };
    }

    private void start(final int port,
                       final String address,
                       final SecurityConfiguration security,
                       final int socketTimeout,
                       final Supplier<ExecutorService> threadPoolFactory) throws Exception {
        populateModel(attributes);
        populateModel(notifications);
        final SnmpAgent agent = new SnmpAgent(port, address, security, socketTimeout);
        notifications.subscribe(agent);
        agent.start(attributes.getAttributes(), notifications.getNotifications(), threadPoolFactory.get());
        this.agent = agent;
    }

    @Override
    protected void start(final Map<String, String> parameters) throws Exception {
        SnmpAdapterLimitations.current().verifyServiceVersion(SnmpResourceAdapter.class);
        final String port = parameters.containsKey(PORT_PARAM_NAME) ? parameters.get(PORT_PARAM_NAME) : "161";
        final String address = parameters.containsKey(HOST_PARAM_NAME) ? parameters.get(HOST_PARAM_NAME) : "127.0.0.1";
        final String socketTimeout = parameters.containsKey(SOCKET_TIMEOUT_PARAM) ? parameters.get(SOCKET_TIMEOUT_PARAM) : "0";
        if (parameters.containsKey(SNMPv3_GROUPS_PARAM) || parameters.containsKey(LDAP_GROUPS_PARAM)) {
            SnmpAdapterLimitations.current().verifyAuthenticationFeature();
            final SecurityConfiguration security = new SecurityConfiguration(MPv3.createLocalEngineID(), contextFactory);
            security.read(parameters);
            start(Integer.valueOf(port), address, security, Integer.valueOf(socketTimeout), new SnmpThreadPoolConfig(parameters, getInstanceName()));
        } else
            start(Integer.valueOf(port), address, null, Integer.valueOf(socketTimeout), new SnmpThreadPoolConfig(parameters, getInstanceName()));
    }

    /**
     * Stops the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     */
    @Override
    protected void stop() throws Exception {
        try {
            agent.stop();
            notifications.unsubscribe(agent);
        } finally {
            clearModel(attributes);
            clearModel(notifications);
            notifications.removeAllListeners();
            agent = null;
        }
        //it is a good time for GC because attributes and notifications mapping
        //detached from it models
        System.gc();
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return getLogger(NAME);
    }
}