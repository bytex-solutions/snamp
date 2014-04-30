package com.itworks.snamp.connectors;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.*;

import com.itworks.snamp.configuration.*;
import com.itworks.snamp.core.AbstractLoggableBundleActivator;
import org.apache.commons.collections4.*;
import org.apache.commons.lang3.ArrayUtils;
import static com.itworks.snamp.connectors.NotificationSupport.*;
import static com.itworks.snamp.connectors.util.NotificationUtils.NotificationEvent;
import static com.itworks.snamp.internal.ReflectionUtils.getProperty;

import org.osgi.service.event.EventAdmin;

import java.lang.ref.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Represents a base class for management connector bundle.
 * <p>
 *     This bundle activator represents a factory of {@link com.itworks.snamp.connectors.ManagementConnector} implementations.
 *     Each connector should be registered as separated service in OSGi environment.
 * </p>
 * @param <TConnector> Type of the management connector.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class AbstractManagementConnectorBundleActivator<TConnector extends ManagementConnector<?>> extends AbstractLoggableBundleActivator {
    private static final String CONNECTION_STRING_IDENTITY_PROPERTY = "connectionString";
    private static final String CONNECTION_TYPE_IDENTITY_PROPERTY = "connectionType";
    private static final String PREFIX_IDENTITY_PROPERTY = "prefix";
    private static final String COMPLIANT_TARGETS_INIT_PROPERTY = "compliant-targets";

    private static final class CompliantTargets extends HashMap<String, ManagementTargetConfiguration>{
        public CompliantTargets(final String connectorName, final AgentConfiguration configuration){
            this(connectorName, configuration.getTargets());
        }

        public CompliantTargets(final String connectorName, final Map<String, ManagementTargetConfiguration> targets){
            super(targets.size());
            for(final Map.Entry<String, ManagementTargetConfiguration> entry: targets.entrySet())
                if(Objects.equals(connectorName, entry.getValue().getConnectionType()))
                    put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Represents a factory for management connectors.
     * @param <TConnectorImpl> Type of the management connector.
     * @author Roman Sakno
     * @since 1.0
     */
    protected static abstract class ProvidedManagementConnectors<TConnectorImpl extends ManagementConnector<?>> implements ProvidedServices{

        /**
         * Creates a new instance of the management connector factory.
         * @param targetName The The name of the management target.
         * @param sharedContext Shared context.
         * @return A new instance of the management connector factory.
         */
        protected abstract ManagementConnectorProvider<TConnectorImpl> createConnectorFactory(final String targetName, final Map<String, ?> sharedContext);

        /**
         * Exposes all provided services via the input collection.
         *
         * @param services      A collection of provided services to fill.
         * @param sharedContext Shared context.
         */
        @Override
        public final void provide(final Collection<ProvidedService<?, ?>> services, final Map<String, ?> sharedContext) {
            //iterates through each compliant target and instantate factory for the management connector
            Map<String, ManagementTargetConfiguration> targets = getProperty(sharedContext,
                    COMPLIANT_TARGETS_INIT_PROPERTY,
                    CompliantTargets.class,
                    FactoryUtils.<CompliantTargets>nullFactory());
            for(final String targetName: targets != null ? targets.keySet() : Collections.<String>emptySet())
                services.add(createConnectorFactory(targetName, sharedContext));
        }
    }

    /**
     * Represents factory for management connector.
     * @param <TConnectorImpl> Type of the management connector implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected abstract static class ManagementConnectorProvider<TConnectorImpl extends ManagementConnector<?>> extends LoggableProvidedService<ManagementConnector, TConnectorImpl>{
        /**
         * Represents name of the management target bounded to this management connector factory.
         */
        protected final String managementTargetName;

        /**
         * Initializes a new management connector factory.
         * @param targetName The name of the management target.
         * @param dependencies A collection of connector dependencies.
         * @throws IllegalArgumentException config is {@literal null}.
         */
        protected ManagementConnectorProvider(final String targetName, final RequiredService<?>... dependencies) {
            super(ManagementConnector.class, dependencies);
            this.managementTargetName = targetName;
        }

        private ManagementTargetConfiguration getConfiguration(){
            final CompliantTargets targets = getProperty(getSharedContext(),
                    COMPLIANT_TARGETS_INIT_PROPERTY,
                    CompliantTargets.class,
                    FactoryUtils.<CompliantTargets>nullFactory());
            return getProperty(targets,
                    managementTargetName,
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
         * Creates a new instance of the management connector.
         * @param connectionString The connection string.
         * @param connectionOptions The connection options.
         * @param dependencies A collection of connector dependencies.
         * @return A new instance of the management connector.
         * @throws Exception Failed to create management connector instance.
         */
        protected abstract TConnectorImpl newConnector(final String connectionString,
                                                    final Map<String, String> connectionOptions,
                                                    final RequiredService<?>... dependencies) throws Exception;

        /**
         * Creates a new instance of the service.
         *
         * @param identity     A dictionary of properties that uniquely identifies service instance.
         * @param dependencies A collection of dependencies.
         * @return A new instance of the service.
         * @throws Exception Failed to create management connector instance.
         */
        @Override
        protected final TConnectorImpl activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) throws Exception{
            final ManagementTargetConfiguration config = getConfiguration();
            final TConnectorImpl connector = newConnector(config.getConnectionString(), config.getAdditionalElements(), dependencies);
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
     * @param <TConnectorImpl> Type of the management connector implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class NotificationSupportProvider<TConnectorImpl extends ManagementConnector<?> & NotificationSupport> extends ManagementConnectorProvider<TConnectorImpl>{
        private static final String NOTIF_TRANSPORT_LISTENER_ID = "EventAdminTransport";

        /**
         * Initializes a new factory for the management connector with notification support.
         * @param targetName The name of the management target.
         * @param publisherDependency Notification delivery channel dependency. This dependency is mandatory.
         * @param dependencies A collection of connector dependencies.
         */
        protected NotificationSupportProvider(final String targetName,
                                              final RequiredServiceAccessor<EventAdmin> publisherDependency, final RequiredService<?>... dependencies){
            super(targetName, ArrayUtils.addAll(dependencies, publisherDependency));
        }

        /**
         * Initializes a new factory for the management connector with notification support.
         * <p>
         *     This constructor calls {@link #NotificationSupportProvider(String, com.itworks.snamp.core.AbstractBundleActivator.RequiredServiceAccessor, com.itworks.snamp.core.AbstractBundleActivator.RequiredService[])}
         *     and pass {@link com.itworks.snamp.core.AbstractBundleActivator.SimpleDependency} as dependency
         *     descriptor for {@link org.osgi.service.event.EventAdmin} service.
         * </p>
         * @param targetName The name of the management target.
         * @param dependencies A collection of connector dependencies.
         */
        @SuppressWarnings("UnusedDeclaration")
        protected NotificationSupportProvider(final String targetName,
                                              final RequiredService<?>... dependencies){
            this(targetName, new SimpleDependency<>(EventAdmin.class), dependencies);
        }

        /**
         * Creates a new instance of the management connector that supports notifications.
         * @param connectionString  The connection string.
         * @param connectionOptions The connection options.
         * @param dependencies      A collection of connector dependencies.
         * @return A new instance of the management connector.
         * @throws Exception Failed to create management connector instance.
         */
        protected abstract TConnectorImpl newNotificationSupport(final String connectionString,
                                                                 final Map<String, String> connectionOptions,
                                                                 final RequiredService<?>... dependencies) throws Exception;

        /**
         * Creates a new instance of the management connector.
         *
         * @param connectionString  The connection string.
         * @param connectionOptions The connection options.
         * @param dependencies      A collection of connector dependencies.
         * @return A new instance of the management connector.
         * @throws Exception Failed to create management connector instance.
         */
        @Override
        protected TConnectorImpl newConnector(final String connectionString, final Map<String, String> connectionOptions, final RequiredService<?>... dependencies) throws Exception {
            @SuppressWarnings("unchecked")
            final RequiredServiceAccessor<EventAdmin> eventAdmin = findDependency(RequiredServiceAccessor.class, EventAdmin.class, dependencies);
            final TConnectorImpl connector = newNotificationSupport(connectionString, connectionOptions, dependencies);
            if(!connector.subscribe(NOTIF_TRANSPORT_LISTENER_ID,
                    new EventAdminTransport(getConnectorName(), eventAdmin, connector)))
                getLogger().warning(String.format("Unable to attach notification transport for %s connector.", getConnectorName()));
            return connector;
        }
    }

    /**
     * Represents name of the management connector.
     */
    public final String connectorName;

    /**
     * Initializes a new connector factory.
     * @param connectorName The name of the connector.
     * @param connectorFactory A factory that exposes collection of management connector factories.
     * @throws IllegalArgumentException connectorName is {@literal null}.
     */
    @SuppressWarnings("UnusedDeclaration")
    protected AbstractManagementConnectorBundleActivator(final String connectorName, final ProvidedManagementConnectors<TConnector> connectorFactory){
        this(connectorName, connectorFactory, null);
    }

    /**
     * Initializes a new connector factory.
     * @param connectorName The name of the connector.
     * @param connectorFactory A factory that exposes collection of management connector factories.
     * @param explicitLogger An instance of the logger associated with the all management connector instances.
     * @throws IllegalArgumentException connectorName is {@literal null}.
     */
    protected AbstractManagementConnectorBundleActivator(final String connectorName, final ProvidedManagementConnectors<TConnector> connectorFactory, final Logger explicitLogger){
        super(explicitLogger != null ? explicitLogger : AbstractManagementConnector.getLogger(connectorName),
                connectorFactory);
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
     * Reads management targets from the SNAMP configuration manager.
     *
     * @param sharedContext           The activation context to initialize.
     * @param serviceReg              An object that provides access to the OSGi service registry.
     * @param bundleLevelDependencies A collection of bundle-level dependencies.
     */
    @Override
    protected void init(final Map<String, Object> sharedContext,
                              final ServiceRegistryProcessor serviceReg,
                              final Collection<BundleLevelDependency<?>> bundleLevelDependencies) throws Exception{
        super.init(sharedContext, serviceReg, bundleLevelDependencies);
        //read management targets from configuration
        if(!serviceReg.processService(ConfigurationManager.class, new Closure<ConfigurationManager>() {
            @Override
            public final void execute(final ConfigurationManager input) {
                sharedContext.put(COMPLIANT_TARGETS_INIT_PROPERTY, new CompliantTargets(connectorName, input.getCurrentConfiguration()));
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
    public final boolean equals(final AbstractManagementConnectorBundleActivator<?> factory){
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
        return factory instanceof AbstractManagementConnectorBundleActivator && equals((AbstractManagementConnectorBundleActivator<?>)factory);
    }
}
