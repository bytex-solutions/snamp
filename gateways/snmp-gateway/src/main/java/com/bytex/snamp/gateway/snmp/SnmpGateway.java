package com.bytex.snamp.gateway.snmp;

import com.bytex.snamp.gateway.AbstractGateway;
import com.bytex.snamp.gateway.GatewayUpdateManager;
import com.bytex.snamp.gateway.GatewayUpdatedCallback;
import com.bytex.snamp.gateway.modeling.AttributeAccessor;
import com.bytex.snamp.gateway.modeling.FeatureAccessor;
import com.bytex.snamp.gateway.modeling.NotificationAccessor;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.osgi.service.jndi.JNDIContextManager;
import org.snmp4j.agent.DuplicateRegistrationException;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.bytex.snamp.gateway.snmp.SnmpGatewayDescriptionProvider.isValidNotification;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class SnmpGateway extends AbstractGateway {
    private static final class SnmpAdapterUpdateManager extends GatewayUpdateManager {
        private final SnmpAgent agent;

        private SnmpAdapterUpdateManager(final String adapterInstanceName,
                                         final long restartTimeout,
                                         final Callable<SnmpAgent> agentFactory) throws Exception {
            super(adapterInstanceName, restartTimeout);
            agent = agentFactory.call();
        }

        private void startAgent(final Iterable<? extends SnmpAttributeAccessor> attributes,
                                final Iterable<? extends SnmpNotificationMapping> notifications) throws IOException, DuplicateRegistrationException {
            agent.start(attributes, notifications);
        }

        @Override
        protected void beginUpdate() {
            agent.suspend();
        }

        private GatewayUpdatedCallback getCallback(){
            return agent;
        }

        @Override
        public void close() throws Exception {
            try {
                agent.close();
            }
            finally {
                super.close();
            }
        }

        private void registerManagedObject(final SnmpAttributeAccessor accessor) throws DuplicateRegistrationException {
            agent.registerManagedObject(accessor);
        }

        private void unregisterManagedObject(final SnmpAttributeAccessor accessor) {
            agent.unregisterManagedObject(accessor);
        }

        private void registerNotificationTarget(final SnmpNotificationAcessor mapping) {
            agent.registerNotificationTarget(mapping);
        }

        private void unregisterNotificationTarget(final SnmpNotificationMapping mapping) {
            agent.unregisterNotificationTarget(mapping);
        }
    }

    private SnmpAdapterUpdateManager updateManager;
    private final DirContextFactory contextFactory;
    private final Multimap<String, SnmpNotificationAcessor> notifications;
    private final Multimap<String, SnmpAttributeAccessor> attributes;

    SnmpGateway(final String adapterInstanceName, final JNDIContextManager contextManager) {
        super(adapterInstanceName);
        contextFactory = contextManager::newInitialDirContext;
        updateManager = null;
        notifications = HashMultimap.create();
        attributes = HashMultimap.create();
    }

    /**
     * Starts the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     *
     * @param parameters Adapter startup parameters.
     * @throws Exception Unable to start adapter.
     */
    @Override
    protected void start(final Map<String, String> parameters) throws Exception {
        final SnmpGatewayDescriptionProvider parser = SnmpGatewayDescriptionProvider.getInstance();
        final Callable<SnmpAgent> agentFactory = () -> new SnmpAgent(parser.parseContext(parameters),
                parser.parseEngineID(parameters),
                parser.parsePort(parameters),
                parser.parseAddress(parameters),
                parser.parseSecurityConfiguration(parameters, contextFactory),
                parser.parseSocketTimeout(parameters),
                parser.getThreadPool(parameters));
        //initialize restart manager and start SNMP agent
        updateManager = new SnmpAdapterUpdateManager(getInstanceName(), parser.parseRestartTimeout(parameters), agentFactory);
        updateManager.startAgent(attributes.values(), notifications.values());
    }

    private SnmpNotificationAcessor addNotification(final String resourceName,
                                                        final MBeanNotificationInfo metadata) throws ParseException {
        if(!isValidNotification(metadata)) return null;
        final SnmpNotificationAcessor mapping = new SnmpNotificationAcessor(metadata, resourceName);
        notifications.put(resourceName, mapping);
        if(updateManager != null)
            updateManager.registerNotificationTarget(mapping);
        return mapping;
    }

    private AttributeAccessor addAttribute(final String resourceName,
                                           final MBeanAttributeInfo metadata) throws DuplicateRegistrationException, ParseException {
        final SnmpAttributeAccessorImpl accessor = new SnmpAttributeAccessorImpl(metadata);
        attributes.put(resourceName, accessor);
        if(updateManager != null)
            updateManager.registerManagedObject(accessor);
        return accessor;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected synchronized <M extends MBeanFeatureInfo> FeatureAccessor<M> addFeature(final String resourceName, final M feature) throws Exception {
        final SnmpAdapterUpdateManager updateManager = this.updateManager;
        if(updateManager != null)
            beginUpdate(updateManager, updateManager.getCallback());
        if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M>)addNotification(resourceName, (MBeanNotificationInfo)feature);
        else if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>)addAttribute(resourceName, (MBeanAttributeInfo) feature);
        else return null;
    }

    @Override
    protected synchronized Stream<? extends FeatureAccessor<?>> removeAllFeatures(final String resourceName) throws Exception {
        final Collection<? extends SnmpNotificationAcessor> notifs = notifications.removeAll(resourceName);
        final Collection<? extends SnmpAttributeAccessor> accessors = attributes.removeAll(resourceName);
        final SnmpAdapterUpdateManager updateManager = this.updateManager;
        if(updateManager != null){
            beginUpdate(updateManager, updateManager.getCallback());
            notifs.forEach(updateManager::unregisterNotificationTarget);
            accessors.forEach(updateManager::unregisterManagedObject);
        }
        return Stream.concat(accessors.stream(), notifs.stream());
    }

    private SnmpAttributeAccessor removeAttribute(final String resourceName,
                                              final MBeanAttributeInfo metadata) {
        final SnmpAttributeAccessor accessor = AttributeAccessor.remove(this.attributes.get(resourceName), metadata);
        if(accessor != null && updateManager != null)
            updateManager.unregisterManagedObject(accessor);
        return accessor;
    }

    private NotificationAccessor removeNotification(final String resourceName,
                                                    final MBeanNotificationInfo metadata){
        final Iterator<SnmpNotificationAcessor> notifications = this.notifications.get(resourceName).iterator();
        while (notifications.hasNext()){
            final SnmpNotificationAcessor mapping = notifications.next();
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
    protected synchronized <M extends MBeanFeatureInfo> FeatureAccessor<M> removeFeature(final String resourceName, final M feature) throws Exception {
        final SnmpAdapterUpdateManager updateManager = this.updateManager;
        if(updateManager != null)
            beginUpdate(updateManager, updateManager.agent);
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>)removeAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M>)removeNotification(resourceName, (MBeanNotificationInfo) feature);
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
            notifications.values().forEach(FeatureAccessor::close);
            //remove all attributes
            attributes.values().forEach(FeatureAccessor::close);
        } finally {
            updateManager = null;
            notifications.clear();
            attributes.clear();
        }
    }

    private static Multimap<String, ? extends FeatureBindingInfo<MBeanAttributeInfo>> getAttributes(final Multimap<String, SnmpAttributeAccessor> attributes){
        final Multimap<String, ReadOnlyFeatureBindingInfo<MBeanAttributeInfo>> result =
                HashMultimap.create(attributes.keySet().size(), 10);
        for(final String resourceName: attributes.keySet())
            for(final SnmpAttributeAccessor accessor: attributes.get(resourceName))
                result.put(resourceName, new ReadOnlyFeatureBindingInfo<>(accessor,
                        FeatureBindingInfo.MAPPED_TYPE, accessor.getSnmpType(),
                        "OID", accessor.getID()
                        ));
        return result;
    }

    private static Multimap<String, ? extends FeatureBindingInfo<MBeanNotificationInfo>> getNotifications(final Multimap<String, SnmpNotificationAcessor> notifs){
        final Multimap<String, ReadOnlyFeatureBindingInfo<MBeanNotificationInfo>> result =
                HashMultimap.create(notifs.keySet().size(), 10);
        for(final String resourceName: notifs.keySet())
            for(final SnmpNotificationAcessor accessor: notifs.get(resourceName))
                result.put(resourceName, new ReadOnlyFeatureBindingInfo<>(accessor,
                        FeatureBindingInfo.MAPPED_TYPE, accessor.getSnmpType(),
                        "OID", accessor.getID()
                ));
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <M extends MBeanFeatureInfo> Multimap<String, ? extends FeatureBindingInfo<M>> getBindings(final Class<M> featureType) {
        final SnmpAdapterUpdateManager updateManager = this.updateManager;
        if(updateManager == null)
            return super.getBindings(featureType);
        else if(featureType.isAssignableFrom(MBeanAttributeInfo.class))
            return (Multimap<String, ? extends FeatureBindingInfo<M>>)getAttributes(attributes);
        else if(featureType.isAssignableFrom(MBeanNotificationInfo.class))
            return (Multimap<String, ? extends FeatureBindingInfo<M>>)getNotifications(notifications);
        else return super.getBindings(featureType);
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return getLoggerImpl();
    }


    static Logger getLoggerImpl() {
        return getLogger(SnmpGateway.class);
    }
}