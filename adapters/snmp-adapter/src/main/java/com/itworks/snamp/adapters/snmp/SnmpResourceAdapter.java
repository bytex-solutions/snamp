package com.itworks.snamp.adapters.snmp;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.itworks.snamp.adapters.*;
import com.itworks.snamp.adapters.profiles.PolymorphicResourceAdapter;
import com.itworks.snamp.adapters.runtime.AttributeBinding;
import com.itworks.snamp.adapters.runtime.FeatureBinding;
import com.itworks.snamp.adapters.snmp.runtime.SnmpAttributeBinding;
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
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static com.itworks.snamp.adapters.snmp.SnmpAdapterConfigurationDescriptor.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpResourceAdapter extends PolymorphicResourceAdapter<SnmpResourceAdapterProfile> {
    static final String NAME = SnmpHelpers.ADAPTER_NAME;

    private static final class SnmpNotificationMappingImpl extends NotificationAccessor implements SnmpNotificationMapping{
        private static final Pattern IPv4_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+");
        private WeakReference<NotificationOriginator> notificationOriginator;
        private final String resourceName;
        private SnmpTypeMapper typeMapper;
        private final OID notificationID;

        private SnmpNotificationMappingImpl(final MBeanNotificationInfo metadata,
                                            final String resourceName) throws IllegalArgumentException, ParseException {
            super(metadata);
            this.notificationOriginator = null;
            this.resourceName = resourceName;
            notificationID = parseOID(this);
        }

        @Override
        public void setTypeMapper(final SnmpTypeMapper value) {
            typeMapper = value;
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
            return SnmpHelpers.toOctetString(parseTargetName(getMetadata()));
        }

        @Override
        public int getTimeout() {
            return parseNotificationTimeout(this);
        }

        @Override
        public int getRetryCount() {
            return parseRetryCount(this);
        }

        @Override
        public OID getID() {
            return notificationID;
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
                                               final MBeanNotificationInfo metadata,
                                               final SnmpTypeMapper mapper) throws ParseException {
            notification.setSource(resourceName);
            @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
            final SnmpNotification snmpTrap = new SnmpNotification(notification, metadata, mapper);
            originator.notify(new OctetString(), snmpTrap.notificationID, snmpTrap.getBindings()); //for SNMPv3 sending
            originator.notify(SnmpHelpers.toOctetString("public"), snmpTrap.notificationID, snmpTrap.getBindings()); //for SNMPv2 sending
        }

        @Override
        public void handleNotification(final Notification notification, final Object handback) {
            final WeakReference<NotificationOriginator> originatorRef = this.notificationOriginator;
            final NotificationOriginator originator = originatorRef != null ?
                    originatorRef.get() :
                    null;
            if (originator != null && typeMapper != null)
                try {
                    handleNotification(originator, notification, resourceName, getMetadata(), typeMapper);
                } catch (final ParseException e) {
                    SnmpHelpers.log(Level.WARNING, "Unable to handle notification '%s'", notification.getType(), e);
                }
        }

        @Override
        public void disconnected() {
            final WeakReference<NotificationOriginator> originatorRef = this.notificationOriginator;
            this.notificationOriginator = null;
            if(originatorRef != null) originatorRef.clear();
            typeMapper = null;
        }
    }

    private static final class SnmpAdapterUpdateManager extends ResourceAdapterUpdateManager {
        private final SnmpAgent agent;
        private final SnmpResourceAdapterProfile profile;

        private SnmpAdapterUpdateManager(final String adapterInstanceName,
                                         final SnmpResourceAdapterProfile profile,
                                         final DirContextFactory contextFactory) throws IOException, SnmpAdapterAbsentParameterException{
            super(adapterInstanceName, profile.getRestartTimeout());
            this.profile = Objects.requireNonNull(profile);
            agent = profile.createSnmpAgent(contextFactory,
                    profile.createThreadPoolFactory(adapterInstanceName));
        }

        private void startAgent(final Iterable<? extends AttributeAccessor> attributes,
                                final Iterable<? extends SnmpNotificationMapping> notifications) throws IOException, DuplicateRegistrationException, ParseException {
            agent.start(attributes, notifications, profile);
        }

        @Override
        protected void beginUpdate() {
            agent.suspend();
        }

        private ResourceAdapterUpdatedCallback getCallback(){
            return agent;
        }

        @Override
        public void close() throws Exception {
            try {
                agent.stop();
            }
            finally {
                super.close();
            }
        }

        private void registerManagedObject(final AttributeAccessor accessor) throws DuplicateRegistrationException, ParseException {
            agent.registerManagedObject(accessor, profile);
        }

        private void unregisterManagedObject(final AttributeAccessor accessor) throws ParseException {
            agent.unregisterManagedObject(accessor);
        }

        private void registerNotificationTarget(final SnmpNotificationMappingImpl mapping) {
            agent.registerNotificationTarget(mapping, profile);
        }

        private void unregisterNotificationTarget(final SnmpNotificationMapping mapping) {
            agent.unregisterNotificationTarget(mapping);
        }
    }

    private SnmpAdapterUpdateManager updateManager;
    private final DirContextFactory contextFactory;
    private final HashMultimap<String, SnmpNotificationMappingImpl> notifications;
    private final HashMultimap<String, AttributeAccessor> attributes;

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
            public DirContext create(final Hashtable<String, ?> env) throws NamingException {
                return contextManager.newInitialDirContext(env);
            }
        };
    }

    /**
     * Creates a new instance of the profile using its name and configuration parameters.
     *
     * @param profileName The name of the profile.
     * @param parameters  A set of configuration parameters.
     * @return A new instance of the profile. Cannot be {@literal null}.
     */
    @Override
    protected SnmpResourceAdapterProfile createProfile(final String profileName,
                                                       final Map<String, String> parameters) {
        switch (profileName) {
            case SnmpResourceAdapterProfile.PROFILE_NAME:
            default:
                return SnmpResourceAdapterProfile.createDefault(parameters);
        }
    }

    @Override
    protected synchronized void start(final SnmpResourceAdapterProfile profile) throws IOException, DuplicateRegistrationException, SnmpAdapterAbsentParameterException, ParseException {
        //initialize restart manager and start SNMP agent
        updateManager = new SnmpAdapterUpdateManager(getInstanceName(),
                profile,
                contextFactory);
        updateManager.startAgent(attributes.values(), notifications.values());
    }

    private SnmpNotificationMappingImpl addNotification(final String resourceName,
                                                        final MBeanNotificationInfo metadata) throws ParseException {
        if(!isValidNotification(metadata)) return null;
        final SnmpNotificationMappingImpl mapping = new SnmpNotificationMappingImpl(metadata, resourceName);
        notifications.put(resourceName, mapping);
        if(updateManager != null)
            updateManager.registerNotificationTarget(mapping);
        return mapping;
    }

    private AttributeAccessor addAttribute(final String resourceName,
                                           final MBeanAttributeInfo metadata) throws DuplicateRegistrationException, ParseException {
        final AttributeAccessor accessor = new AttributeAccessor(metadata);
        attributes.put(resourceName, accessor);
        if(updateManager != null)
            updateManager.registerManagedObject(accessor);
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
        final Collection<AttributeAccessor> accessors = attributes.removeAll(resourceName);
        final SnmpAdapterUpdateManager updateManager = this.updateManager;
        if(updateManager != null){
            beginUpdate(updateManager, updateManager.getCallback());
            for (final SnmpNotificationMapping mapping : notifs)
                updateManager.unregisterNotificationTarget(mapping);
            for (final AttributeAccessor mapping : accessors)
                updateManager.unregisterManagedObject(mapping);
        }
        return Iterables.concat(accessors, notifs);
    }

    private AttributeAccessor removeAttribute(final String resourceName,
                                              final MBeanAttributeInfo metadata) throws ParseException {
        final AttributeAccessor accessor = AttributeAccessor.remove(this.attributes.get(resourceName), metadata);
        if(accessor != null && updateManager != null)
            updateManager.unregisterManagedObject(accessor);
        return accessor;
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
    protected synchronized void stop() throws Exception {
        try {
            if (updateManager != null)
                updateManager.close();
            //remove all notifications
            for (final FeatureAccessor<?, ?> mapping : notifications.values())
                mapping.disconnect();
            //remove all attributes
            for (final FeatureAccessor<?, ?> mapping : attributes.values())
                mapping.disconnect();
        } finally {
            updateManager = null;
            notifications.clear();
            attributes.clear();
        }
    }

    private static Collection<SnmpAttributeBinding> getBindings(final Multimap<String, AttributeAccessor> accessors,
                                                                final SnmpTypeMapper typeMapper) {
        final List<SnmpAttributeBinding> result = Lists.newArrayListWithExpectedSize(accessors.size());
        for (final String declaredResource : accessors.keySet())
            for (final AttributeAccessor accessor : accessors.get(declaredResource))
                try {
                    result.add(new SnmpAttributeBinding(declaredResource, accessor, typeMapper));
                } catch (final ParseException ignored) {
                }
        return result;
    }

    /**
     * Gets information about binding of the features.
     *
     * @param bindingType Type of the feature binding.
     * @return A collection of features
     */
    @Override
    protected synchronized  <B extends FeatureBinding> Collection<? extends B> getBindings(final Class<B> bindingType) {
        final SnmpAdapterUpdateManager updateManager = this.updateManager;
        if (updateManager == null)
            return super.getBindings(bindingType);
        else if (bindingType.isAssignableFrom(SnmpAttributeBinding.class))
            return (Collection<B>) getBindings(attributes, updateManager.profile);
        else return super.getBindings(bindingType);
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