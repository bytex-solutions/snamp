package com.itworks.snamp.adapters.snmp;

import com.google.common.base.Supplier;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.itworks.snamp.adapters.*;
import org.osgi.service.jndi.JNDIContextManager;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.NotificationOriginator;
import org.snmp4j.agent.mo.snmp.TransportDomains;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TransportIpAddress;
import org.snmp4j.smi.UdpAddress;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static com.itworks.snamp.adapters.snmp.SnmpAdapterConfigurationDescriptor.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpResourceAdapter extends AbstractResourceAdapter {
    static final String NAME = SnmpHelpers.ADAPTER_NAME;

    private static final class SnmpNotificationMappingImpl extends NotificationAccessor implements SnmpNotificationMapping{
        private static final Pattern IPv4_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+");
        private WeakReference<NotificationOriginator> notificationOriginator;
        private final String resourceName;

        private SnmpNotificationMappingImpl(final MBeanNotificationInfo metadata,
                                            final String resourceName) throws IllegalArgumentException{
            super(metadata);
            if(isValidNotification(metadata)) {
                this.notificationOriginator = null;
                this.resourceName = resourceName;
            }
            else throw new IllegalArgumentException("Target address, target name and event OID parameters are not specified for SNMP trap");
        }

        @Override
        public OID getTransportDomain() {
            return kindOfIP(parseTargetAddress(getMetadata()));
        }

        private static OID kindOfIP(final String addr){
            if (addr.contains(":"))
                return TransportDomains.transportDomainUdpIpv6;
            else if (IPv4_PATTERN.matcher(addr).matches())
                return TransportDomains.transportDomainUdpIpv4;
            return TransportDomains.transportDomainUdpDns;
        }

        @Override
        public OctetString getReceiverAddress() {
            final TransportIpAddress addr = new UdpAddress(parseTargetAddress(getMetadata()));
            return new OctetString(addr.getValue());
        }

        @Override
        public OctetString getReceiverName() {
            return new OctetString(parseTargetName(getMetadata()));
        }

        @Override
        public int getTimeout() {
            return parseNotificationTimeout(getMetadata());
        }

        @Override
        public int getRetryCount() {
            return parseRetryCount(getMetadata());
        }

        @Override
        public OID getID() {
            return new OID(parseOID(getMetadata()));
        }

        @Override
        public boolean equals(final MBeanNotificationInfo metadata) {
            return Objects.equals(parseTargetName(getMetadata()), parseTargetName(metadata));
        }

        @Override
        public void setNotificationOriginator(final NotificationOriginator originator) {
            notificationOriginator = new WeakReference<>(originator);
        }

        private static void handleNotification(final NotificationOriginator originator,
                                               final Notification notification,
                                               final String resourceName,
                                               final MBeanNotificationInfo metadata){
            notification.setSource(resourceName);
            @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
            final SnmpNotification snmpTrap = new SnmpNotification(notification, metadata);
            originator.notify(new OctetString(), snmpTrap.notificationID, snmpTrap.getBindings()); //for SNMPv3 sending
            originator.notify(new OctetString("public"), snmpTrap.notificationID, snmpTrap.getBindings()); //for SNMPv2 sending
        }

        @Override
        public void handleNotification(final Notification notification, final Object handback) {
            final WeakReference<NotificationOriginator> originatorRef = this.notificationOriginator;
            if(originatorRef != null){
                final NotificationOriginator originator = originatorRef.get();
                handleNotification(originator, notification, resourceName, getMetadata());
            }
        }

        @Override
        public void disconnected() {
            final WeakReference<NotificationOriginator> originatorRef = this.notificationOriginator;
            this.notificationOriginator = null;
            if(originatorRef != null) originatorRef.clear();
        }
    }

    private static final class SnmpAdapterUpdateManager extends ResourceAdapterUpdateManager {
        private final SnmpAgent agent;

        private SnmpAdapterUpdateManager(final String adapterInstanceName,
                                         final long restartTimeout,
                                         final SnmpAgent agent){
            super(adapterInstanceName, restartTimeout);
            this.agent = Objects.requireNonNull(agent);
        }

        @Override
        protected void beginUpdateCore() {
            agent.suspend();
        }

        private ResourceAdapterUpdatedCallback getCallback(){
            return agent;
        }
    }

    private SnmpAdapterUpdateManager updateManager;
    private final DirContextFactory contextFactory;
    private final HashMultimap<String, SnmpNotificationMappingImpl> notifications;
    private final HashMultimap<String, SnmpAttributeMapping> attributes;

    SnmpResourceAdapter(final String adapterInstanceName, final JNDIContextManager contextManager) {
        super(adapterInstanceName);
        contextFactory = createFactory(contextManager);
        updateManager = null;
        notifications = HashMultimap.create();
        attributes = HashMultimap.create();
    }

    private static DirContextFactory createFactory(final JNDIContextManager contextManager){
        return new DirContextFactory() {
            @Override
            public DirContext create(final Hashtable<?, ?> env) throws NamingException {
                return contextManager.newInitialDirContext(env);
            }
        };
    }

    private void start(final OID prefix,
                       final OctetString engineID,
                       final int port,
                       final String address,
                       final SecurityConfiguration security,
                       final int socketTimeout,
                       final long restartTimeout,
                       final Supplier<ExecutorService> threadPoolFactory) throws IOException, DuplicateRegistrationException {
        final SnmpAgent agent = new SnmpAgent(prefix,
                engineID,
                port,
                address,
                security,
                socketTimeout,
                threadPoolFactory.get());
        //start SNMP agent
        agent.start(attributes.values(), notifications.values());
        //initialize restart manager
        updateManager = new SnmpAdapterUpdateManager(getInstanceName(), restartTimeout, agent);
    }

    @Override
    protected void start(final Map<String, String> parameters) throws IOException, DuplicateRegistrationException {
        start(new OID(parseContext(parameters)),
                parseEngineID(parameters),
                parsePort(parameters),
                parseAddress(parameters),
                parseSecurityConfiguration(parameters, contextFactory),
                parseSocketTimeout(parameters),
                parseRestartTimeout(parameters),
                new SnmpThreadPoolConfig(parameters, getInstanceName()));
    }

    private SnmpNotificationMappingImpl addNotification(final String resourceName,
                                                        final MBeanNotificationInfo metadata) {
        final SnmpNotificationMappingImpl mapping = new SnmpNotificationMappingImpl(metadata, resourceName);
        notifications.put(resourceName, mapping);
        if(updateManager != null)
            updateManager.agent.registerNotificationTarget(mapping);
        return mapping;
    }

    private AttributeAccessor addAttribute(final String resourceName,
                                           final MBeanAttributeInfo metadata) throws DuplicateRegistrationException {
        final AttributeAccessor accessor = new AttributeAccessor(metadata);
        final SnmpType type = SnmpType.map(accessor.getType());
        final SnmpAttributeMapping mapping;
        attributes.put(resourceName, mapping = type.createManagedObject(accessor));
        if(updateManager != null)
            updateManager.agent.registerManagedObject(mapping);
        return accessor;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected synchronized <M extends MBeanFeatureInfo, S> FeatureAccessor<M, S> addFeature(final String resourceName, final M feature) throws Exception {
        final SnmpAdapterUpdateManager updateManager = this.updateManager;
        if(updateManager != null)
            beginUpdate(updateManager, updateManager.getCallback());
        if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M, S>)addNotification(resourceName, (MBeanNotificationInfo)feature);
        else if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M, S>)addAttribute(resourceName, (MBeanAttributeInfo) feature);
        else return null;
    }

    @Override
    protected synchronized Iterable<? extends FeatureAccessor<?, ?>> removeAllFeatures(final String resourceName) throws Exception {
        final Iterable<SnmpNotificationMappingImpl> notifs = notifications.removeAll(resourceName);
        final Collection<SnmpAttributeMapping> attrs = attributes.removeAll(resourceName);
        final Collection<AttributeAccessor> accessors = Lists.newArrayListWithExpectedSize(attrs.size());
        final SnmpAdapterUpdateManager updateManager = this.updateManager;
        if(updateManager != null){
            beginUpdate(updateManager, updateManager.getCallback());
            for (final SnmpNotificationMapping mapping : notifs)
                updateManager.agent.unregisterNotificationTarget(mapping);
            for (final SnmpAttributeMapping mapping : attrs)
                accessors.add(updateManager.agent.unregisterManagedObject(mapping));
        }
        return Iterables.concat(accessors, notifs);
    }

    private AttributeAccessor removeAttribute(final String resourceName,
                                              final MBeanAttributeInfo metadata){
        final Iterator<SnmpAttributeMapping> attributes = this.attributes.get(resourceName).iterator();
        while (attributes.hasNext()){
            final SnmpAttributeMapping mapping = attributes.next();
            if(mapping.equals(metadata)){
                attributes.remove();
                return updateManager != null ?
                        updateManager.agent.unregisterManagedObject(mapping) :
                        mapping.disconnect(null);
            }
        }
        return null;
    }

    private NotificationAccessor removeNotification(final String resourceName,
                                                    final MBeanNotificationInfo metadata){
        final Iterator<SnmpNotificationMappingImpl> notifications = this.notifications.get(resourceName).iterator();
        while (notifications.hasNext()){
            final SnmpNotificationMappingImpl mapping = notifications.next();
            if(mapping.equals(metadata)){
                notifications.remove();
                if(updateManager != null)
                    updateManager.agent.unregisterNotificationTarget(mapping);
                return mapping;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected synchronized <M extends MBeanFeatureInfo> FeatureAccessor<M, ?> removeFeature(final String resourceName, final M feature) throws Exception {
        final SnmpAdapterUpdateManager updateManager = this.updateManager;
        if(updateManager != null)
            beginUpdate(updateManager, updateManager.agent);
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M, ?>)removeAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M, ?>)removeNotification(resourceName, (MBeanNotificationInfo) feature);
        else return null;
    }

    /**
     * Stops the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     */
    @Override
    protected void stop() throws InterruptedException{
        try {
            if(updateManager != null) {
                updateManager.agent.stop();
                updateManager.close();
            }
            //remove all notifications
            for(final String resourceName: notifications.keySet())
                for(final FeatureAccessor<?, ?> mapping: notifications.get(resourceName))
                    mapping.disconnect();
            //remove all attributes
            for(final String resourceName: attributes.keySet())
                for(final SnmpAttributeMapping mapping: attributes.get(resourceName))
                    mapping.disconnect(null);
        }
        finally {
            updateManager = null;
            notifications.clear();
            attributes.clear();
        }
        //it is a good time for GC because attributes and notifications
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