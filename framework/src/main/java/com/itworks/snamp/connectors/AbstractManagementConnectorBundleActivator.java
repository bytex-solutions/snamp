package com.itworks.snamp.connectors;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.*;
import com.itworks.snamp.configuration.ConfigurationManager;
import com.itworks.snamp.core.AbstractLoggableBundleActivator;
import org.apache.commons.collections4.*;
import org.apache.commons.lang3.ArrayUtils;
import static com.itworks.snamp.connectors.NotificationSupport.*;
import static com.itworks.snamp.connectors.util.NotificationUtils.NotificationEvent;
import static com.itworks.snamp.internal.ReflectionUtils.getProperty;

import org.osgi.service.event.EventAdmin;

import java.lang.ref.*;
import java.util.*;

/**
 * Represents a base class for management connector bundle.
 * <p>
 *     This bundle activator represents a factory of {@link com.itworks.snamp.connectors.ManagementConnector} implementations.
 *     Each connector should be registered as separated service in OSGi environment.
 * </p>
 * @param <TConnector> Type of the management connector.
 * @param <TConnectionOptions> Type of the management connection configuration model.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class AbstractManagementConnectorBundleActivator<TConnectionOptions, TConnector extends ManagementConnector<TConnectionOptions>> extends AbstractLoggableBundleActivator {
    private static final String CONNECTION_OPTIONS_INIT_PROPERTY = "management-connection-config";
    private static final String MGMT_TARGET_INIT_PROPERTY = "management-target";
    private static final String CONNECTION_STRING_IDENTITY_PROPERTY = "connectionString";
    private static final String CONNECTION_TYPE_IDENTITY_PROPERTY = "connectionType";
    private static final String PREFIX_IDENTITY_PROPERTY = "prefix";

    /**
     * Represents name of the management connector.
     */
    public final String connectorName;

    private static final class ManagementConnectorFactory implements ProvidedServices{

        /**
         * Exposes all provided services via the input collection.
         *
         * @param services      A collection of provided services to fill.
         * @param sharedContext Shared context.
         */
        @Override
        public void provide(final Collection<ProvidedService<?, ?>> services, final Map<String, Object> sharedContext) {

        }
    }

    /**
     * Represents factory for management connector.
     * @param <TConnectionOptions> Type of the connection options.
     * @param <TConnectorImpl> Type of the management connector implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected abstract static class ManagementConnectorProvider<TConnectionOptions, TConnectorImpl extends ManagementConnector> extends LoggableProvidedService<ManagementConnector, TConnectorImpl>{
        /**
         * Initializes a new management connector factory.
         * @param dependencies A collection of connector dependencies.
         * @throws IllegalArgumentException config is {@literal null}.
         */
        protected ManagementConnectorProvider(final RequiredService<?>... dependencies) {
            super(ManagementConnector.class, dependencies);
        }

        /**
         * Creates a new instance of the management connector.
         * @param options An object used to instantiate the management connector.
         * @param dependencies A collection of connector dependencies.
         * @return A new instance of the management connector.
         */
        protected abstract TConnectorImpl newConnector(TConnectionOptions options, final RequiredService<?>... dependencies);

        @SuppressWarnings("unchecked")
        private TConnectionOptions getConnectionOptions(){
            return (TConnectionOptions)getSharedContext().get(CONNECTION_OPTIONS_INIT_PROPERTY);
        }

        private ManagementTargetConfiguration getConfiguration(){
            return getProperty(getSharedContext(),
                    MGMT_TARGET_INIT_PROPERTY,
                    ManagementTargetConfiguration.class,
                    FactoryUtils.<ManagementTargetConfiguration>nullFactory());
        }

        /**
         * Gets name of this connector.
         * @return The name of this connector.
         */
        protected final String getConnectorName(){
            final ManagementTargetConfiguration config = getConfiguration();
            return config != null ? config.getConnectionType() : "";
        }

        private static void exposeAttribute(final String attributeId,
                                            final AttributeConfiguration attribute,
                                            final AttributeSupport connector){
            connector.connectAttribute(attributeId,
                    attribute.getAttributeName(),
                    attribute.getAdditionalElements());
        }

        private static boolean exposeAttributes(final Map<String, AttributeConfiguration> attributes,
                                                final AttributeSupport connector){
            if(attributes == null || connector == null) return false;
            for(final String attributeId: attributes.keySet())
                exposeAttribute(attributeId, attributes.get(attributeId), connector);
            return true;
        }

        private static void exposeEvent(final String listId,
                                        final EventConfiguration event,
                                        final NotificationSupport connector){
            connector.enableNotifications(listId, event.getCategory(), event.getAdditionalElements());
        }

        private static boolean exposeEvents(final Map<String, EventConfiguration> events,
                                            final NotificationSupport connector){
            if(events == null || connector == null) return false;
            for(final String listId: events.keySet())
                exposeEvent(listId, events.get(listId), connector);
            return true;
        }

        /**
         * Creates a new instance of the service.
         *
         * @param identity     A dictionary of properties that uniquely identifies service instance.
         * @param dependencies A collection of dependencies.
         * @return A new instance of the service.
         */
        @Override
        protected final TConnectorImpl activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) {
            final ManagementTargetConfiguration config = getConfiguration();
            final TConnectorImpl connector = newConnector(getConnectionOptions(), dependencies);
            exposeAttributes(config.getElements(AttributeConfiguration.class),
                    connector.queryObject(AttributeSupport.class));
            exposeEvents(config.getElements(EventConfiguration.class),
                    connector.queryObject(NotificationSupport.class));
            createIdentity(getConfiguration(), identity);
            return connector;
        }

        /**
         * Invokes {@link com.itworks.snamp.connectors.ManagementConnector#close()} method on the instantiated management connector.
         * @param serviceInstance An instance of the hosted service to cleanup.
         * @param stopBundle      {@literal true}, if this method calls when the owner bundle is stopping;
         *                        {@literal false}, if this method calls when loosing dependency.
         */
        @Override
        protected void cleanupService(final TConnectorImpl serviceInstance, final boolean stopBundle) throws Exception {
            serviceInstance.close();
        }
    }

    /**
     * Represents transport for {@link com.itworks.snamp.connectors.NotificationSupport.Notification} object
     * through OSGi environment using {@link org.osgi.service.event.EventAdmin} service.
     * This class cannot be inherited.
     */
    private static final class EventAdminTransport implements NotificationListener{
        private final String connectorName;
        private final RequiredServiceAccessor<EventAdmin> eventAdmin;
        private final Reference<NotificationSupport> notifications;

        public EventAdminTransport(final String connectorName,
                                   final RequiredServiceAccessor<EventAdmin> dependency,
                                   final NotificationSupport notifSupport){
            this.eventAdmin = dependency;
            this.notifications = new WeakReference<>(notifSupport);
            this.connectorName = connectorName;
        }

        /**
         * Handles the specified notification.
         *
         * @param listId An identifier of the subscription list.
         * @param n      The notification to handle.
         * @return {@literal true}, if notification is handled successfully; otherwise, {@literal false}.
         */
        @Override
        public final boolean handle(final String listId, final Notification n) {
            final NotificationSupport notifSupport = notifications.get();
            if(eventAdmin.isResolved() && notifSupport != null){
                final NotificationMetadata metadata = notifSupport.getNotificationInfo(listId);
                if(metadata == null) return false;
                final NotificationEvent event = new NotificationEvent(n, listId);
                eventAdmin.getService().postEvent(event.toEvent(connectorName, metadata.getCategory()));
                return true;
            }
            else return false;
        }
    }

    /**
     * Represents a base class for management connector factory which supports notifications.
     * @param <TConnectionOptions>Type of the connection options.
     * @param <TConnectorImpl> Type of the management connector implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class NotificationSupportProvider<TConnectionOptions, TConnectorImpl extends ManagementConnector<TConnectionOptions> & NotificationSupport> extends ManagementConnectorProvider<TConnectionOptions, TConnectorImpl>{
        private static final String NOTIF_TRANSPORT_LISTENER_ID = "EventAdminTransport";

        /**
         * Initializes a new factory for the management connector with notification support.
         * @param publisherDependency Notification delivery channel dependency. This dependency is mandatory.
         * @param dependencies A collection of connector dependencies.
         */
        protected NotificationSupportProvider(final RequiredServiceAccessor<EventAdmin> publisherDependency, final RequiredService<?>... dependencies){
            super(ArrayUtils.addAll(dependencies, publisherDependency));
        }

        /**
         * Creates a new instance of the management connector that supports notifications.
         * @param options Initialization parameters.
         * @param dependencies A collection of connector dependencies (without {@link org.osgi.service.event.EventAdmin} dependency).
         * @return A new instance of the management connector.
         */
        protected abstract TConnectorImpl newNotificationSupport(final TConnectionOptions options, final RequiredService<?>... dependencies);

        /**
         * Creates a new instance of the management connector.
         * <p>
         *     This method invokes {@link #newNotificationSupport(Object, com.itworks.snamp.core.AbstractBundleActivator.RequiredService[])} internally
         *     and attaches {@link org.osgi.service.event.EventAdmin}-based transport for SNAMP notifications.
         * </p>
         * @param options            Initialization parameters.
         * @param dependencies       A collection of connector dependencies.
         * @return A new instance of the management connector.
         */
        @Override
        protected final TConnectorImpl newConnector(final TConnectionOptions options, RequiredService<?>... dependencies) {
            @SuppressWarnings("unchecked")
            final RequiredServiceAccessor<EventAdmin> eventAdmin = findDependency(RequiredServiceAccessor.class, EventAdmin.class, dependencies);
            dependencies = ArrayUtils.removeElement(dependencies, eventAdmin);
            final TConnectorImpl connector = newNotificationSupport(options, dependencies);
            if(!connector.subscribe(NOTIF_TRANSPORT_LISTENER_ID,
                    new EventAdminTransport(getConnectorName(), eventAdmin, connector)))
                getLogger().warning(String.format("Unable to attach notification transport for %s connector.", getConnectorName()));
            return connector;
        }
    }

    /**
     * Initializes a new connector factory.
     * @param connectorName The name of the connector.
     * @exception IllegalArgumentException connectorName is {@literal null}.
     */
    protected AbstractManagementConnectorBundleActivator(final String connectorName){
        super(AbstractManagementConnector.getLoggerName(connectorName), new ManagementConnectorFactory());
        this.connectorName = connectorName;
    }

    /**
     * Configures management connector identity.
     * @param config The management target configuration used to create identity.
     * @param identity The identity map to fill.
     */
    public static void createIdentity(final ManagementTargetConfiguration config, final Map<String, Object> identity){
        identity.put(CONNECTION_TYPE_IDENTITY_PROPERTY, config.getConnectionType());
        identity.put(CONNECTION_STRING_IDENTITY_PROPERTY, config.getConnectionString());
        identity.put(PREFIX_IDENTITY_PROPERTY, config.getNamespace());
        for(final Map.Entry<String, String> option: config.getAdditionalElements().entrySet())
            identity.put(option.getKey(), option.getValue());
    }

    /**
     * Creates a new object used to initialize management connector.
     * @param connectionString The connection string.
     * @param parameters The connection parameters.
     * @return A new instance of the management connector configuration.
     * @throws ManagementConnectorConfigurationException The specified connection string and
     * parameters cannot be used to for initiating connection and further management operations.
     */
    protected abstract TConnectionOptions createConnectorConfig(final String connectionString,
                                                     final Map<String, String> parameters)
            throws ManagementConnectorConfigurationException;

    /**
     * Reads management targets from the SNAMP configuration manager.
     * <p>
     * In the default implementation this method does nothing.
     * </p>
     *
     * @param sharedContext           The activation context to initialize.
     * @param serviceReg              An object that provides access to the OSGi service registry.
     * @param bundleLevelDependencies A collection of bundle-level dependencies.
     */
    @Override
    protected final void init(final Map<String, Object> sharedContext,
                              final ServiceRegistryProcessor serviceReg,
                              final Collection<BundleLevelDependency<?>> bundleLevelDependencies) throws Exception{
        super.init(sharedContext, serviceReg, bundleLevelDependencies);
        //read management targets from configuration
        if(!serviceReg.processService(ConfigurationManager.class, new Closure<ConfigurationManager>() {
            @Override
            public final void execute(final ConfigurationManager input) {
                //filter targets by connector name
                for(final ManagementTargetConfiguration target: input.getCurrentConfiguration().getTargets().values())
                    if(Objects.equals(connectorName, target.getConnectionType()))
                        try{
                            final TConnectionOptions config = createConnectorConfig(target.getConnectionString(), target.getAdditionalElements());
                            //save collection of attributes and
                            sharedContext.put(MGMT_TARGET_INIT_PROPERTY, target);
                            sharedContext.put(CONNECTION_OPTIONS_INIT_PROPERTY, config);
                        }
                        catch(final ManagementConnectorConfigurationException e){
                            throw new FunctorException(e);
                        }
            }
        })) getLogger().severe("Configuration manager is not detected. No management connectors instantiated.");
    }

    /**
     * Returns the connector name.
     * @return The connector name.
     */
    @Override
    public final String toString(){
        return connectorName;
    }

    /**
     * Determines whether the specified factory equals to this factory and produces
     * the same type of the SNAMP management connector.
     * @param factory The factory to compare.
     * @return {@literal true}, if the specified factory equals to this factory and produces
     * the same type of the SNAMP management connector; otherwise, {@literal false}.
     */
    public final boolean equals(final AbstractManagementConnectorBundleActivator<?, ?> factory){
        return factory != null && connectorName.equals(factory.connectorName);
    }

    /**
     * Determines whether the specified factory equals to this factory and produces
     * the same type of the SNAMP management connector.
     * @param factory The factory to compare.
     * @return {@literal true}, if the specified factory equals to this factory and produces
     * the same type of the SNAMP management connector; otherwise, {@literal false}.
     */
    @Override
    public final boolean equals(final Object factory){
        return factory instanceof AbstractManagementConnectorBundleActivator && equals((AbstractManagementConnectorBundleActivator<?, ?>)factory);
    }
}
