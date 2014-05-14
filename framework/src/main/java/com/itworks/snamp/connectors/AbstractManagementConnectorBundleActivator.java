package com.itworks.snamp.connectors;

import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.ConfigurationManager;
import com.itworks.snamp.core.AbstractLoggableServiceLibrary;
import com.itworks.snamp.internal.semantics.MethodStub;
import org.apache.commons.collections4.FactoryUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.osgi.framework.*;
import org.osgi.service.event.EventAdmin;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Logger;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.EventConfiguration;
import static com.itworks.snamp.connectors.NotificationSupport.Notification;
import static com.itworks.snamp.connectors.NotificationSupport.NotificationListener;
import static com.itworks.snamp.connectors.util.NotificationUtils.NotificationEvent;
import static com.itworks.snamp.internal.ReflectionUtils.getProperty;

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
public abstract class AbstractManagementConnectorBundleActivator<TConnector extends ManagementConnector<?>> extends AbstractLoggableServiceLibrary {
    /**
     * Represents name of the manifest header which contains the name of the management connector.
     * <p>
     *     The following example demonstrates how to set the name of the management connector
     *     in the connector's bundle manifest:
     *     <pre><tt>
     *          SNAMP-Management-Connector: jmx
     *     </tt></pre>
     * </p>
     */
    public static String CONNECTOR_NAME_MANIFEST_HEADER = "SNAMP-Management-Connector";
    private static final String MGMT_TARGET_NAME_IDENTITY_PROPERTY = "managementTarget";
    private static final String CONNECTION_STRING_IDENTITY_PROPERTY = "connectionString";
    private static final String CONNECTION_TYPE_IDENTITY_PROPERTY = "connectionType";
    private static final String PREFIX_IDENTITY_PROPERTY = "prefix";

    private static final ActivationProperty<CompliantTargets> COMPLIANT_TARGETS_HOLDER = defineActivationProperty(CompliantTargets.class, CompliantTargets.EMPTY);

    private static final class CompliantTargets extends HashMap<String, ManagementTargetConfiguration>{

        private CompliantTargets(){

        }

        public static final CompliantTargets EMPTY = new CompliantTargets();

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
         * @param activationProperties A collection of activation properties to read.
         * @return A new instance of the management connector factory.
         */
        protected abstract ManagementConnectorProvider<TConnectorImpl> createConnectorFactory(final String targetName,
                                                                                              final ActivationPropertyReader activationProperties);

        /**
         * Exposes all provided services via the input collection.
         *
         * @param services      A collection of provided services to fill.
         * @param activationProperties Activation properties to read.
         * @param bundleLevelDependencies A collection of bundle-level dependencies.
         */
        @Override
        public final void provide(final Collection<ProvidedService<?, ?>> services,
                                  final ActivationPropertyReader activationProperties,
                                  final RequiredService<?>... bundleLevelDependencies) {
            //iterates through each compliant target and instantate factory for the management connector
            final Map<String, ManagementTargetConfiguration> targets = activationProperties.getValue(COMPLIANT_TARGETS_HOLDER);
            for(final String targetName: targets != null ? targets.keySet() : Collections.<String>emptySet())
                services.add(createConnectorFactory(targetName, activationProperties));
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
            return getProperty(getActivationPropertyValue(COMPLIANT_TARGETS_HOLDER),
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
            createIdentity(managementTargetName, getConfiguration(), identity);
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
         *     This constructor calls {@link #NotificationSupportProvider(String, com.itworks.snamp.core.AbstractServiceLibrary.RequiredServiceAccessor, com.itworks.snamp.core.AbstractServiceLibrary.RequiredService[])}
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
     * @param managementTarget The name of the management target.
     * @param config The management target configuration used to create identity.
     * @param identity The identity map to fill.
     */
    public static void createIdentity(final String managementTarget,
                                      final ManagementTargetConfiguration config,
                                      final Map<String, Object> identity){
        identity.put(MGMT_TARGET_NAME_IDENTITY_PROPERTY, managementTarget);
        identity.put(CONNECTION_TYPE_IDENTITY_PROPERTY, config.getConnectionType());
        identity.put(CONNECTION_STRING_IDENTITY_PROPERTY, config.getConnectionString());
        identity.put(PREFIX_IDENTITY_PROPERTY, config.getNamespace());
        for(final Map.Entry<String, String> option: config.getAdditionalElements().entrySet())
            identity.put(option.getKey(), option.getValue());
    }

    /**
     * Gets type of the management connector by its reference.
     * @param connectorRef The reference to the management connector.
     * @return The type of the management connector.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static String getConnectorType(final ServiceReference<ManagementConnector<?>> connectorRef){
        return connectorRef != null ?
                Objects.toString(connectorRef.getProperty(CONNECTION_TYPE_IDENTITY_PROPERTY), ""):
                "";
    }

    /**
     * Gets connection string used by management connector by its reference.
     * @param connectorRef The reference to the management connector.
     * @return The connection string used by management connector.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static String getConnectionString(final ServiceReference<ManagementConnector<?>> connectorRef){
        return connectorRef != null ?
                Objects.toString(connectorRef.getProperty(CONNECTION_STRING_IDENTITY_PROPERTY), ""):
                "";
    }

    /**
     * Gets prefix of the management connector by its reference.
     * @param connectorRef The reference to the management connector.
     * @return The prefix of the management connector.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static String getPrefix(final ServiceReference<ManagementConnector<?>> connectorRef){
        return connectorRef != null ?
                Objects.toString(connectorRef.getProperty(PREFIX_IDENTITY_PROPERTY), ""):
                "";
    }

    /**
     * Gets name of the management target that is represented by the specified management
     * connector reference.
     * @param connectorRef The reference to the management connector.
     * @return The name of the management target.
     */
    public static String getManagementTarget(final ServiceReference<ManagementConnector<?>> connectorRef){
        return connectorRef != null ?
                Objects.toString(connectorRef.getProperty(MGMT_TARGET_NAME_IDENTITY_PROPERTY), ""):
                "";
    }

    /**
     * Gets a map of available management connectors in the current OSGi environment.
     * @param context The context of the callee bundle.
     * @return A map of management connector references where the key of the map represents
     *          a name of the management target.
     */
    public static Map<String, ServiceReference<ManagementConnector<?>>> getManagementConnectorInstances(final BundleContext context){
        if(context == null) return Collections.emptyMap();
        else try {
            ServiceReference<?>[] connectors = context.getAllServiceReferences(ManagementConnector.class.getName(), null);
            if(connectors == null) connectors = new ServiceReference<?>[0];
            final Map<String, ServiceReference<ManagementConnector<?>>> result = new HashMap<>(connectors.length);
            for(final ServiceReference<?> serviceRef: connectors) {
                @SuppressWarnings("unchecked")
                final ServiceReference<ManagementConnector<?>> connectorRef = (ServiceReference<ManagementConnector<?>>)serviceRef;
                result.put(getManagementTarget(connectorRef), connectorRef);
            }
            return result;
        }
        catch (final InvalidSyntaxException e) {
            return Collections.emptyMap();
        }
    }

    /**
     * Initializes the library.
     * @param bundleLevelDependencies A collection of library-level dependencies to fill.
     * @throws Exception An error occurred during bundle initialization.
     */
    @Override
    protected final void start(final Collection<RequiredService<?>> bundleLevelDependencies) throws Exception {
        super.start(bundleLevelDependencies);
        bundleLevelDependencies.add(new SimpleDependency<>(ConfigurationManager.class));
    }

    /**
     * Activates this service library.
     * @param activationProperties A collection of library activation properties to fill.
     * @param dependencies         A collection of resolved library-level dependencies.
     * @throws Exception Unable to activate this library.
     */
    @Override
    protected final void activate(final ActivationPropertyPublisher activationProperties, final RequiredService<?>... dependencies) throws Exception {
        super.activate(activationProperties, dependencies);
        final ConfigurationManager configManager = getDependency(RequiredServiceAccessor.class, ConfigurationManager.class, dependencies);
        activationProperties.publish(COMPLIANT_TARGETS_HOLDER, new CompliantTargets(connectorName, configManager.getCurrentConfiguration()));
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
        return factory instanceof AbstractManagementConnectorBundleActivator && equals((AbstractManagementConnectorBundleActivator<?>) factory);
    }

    /**
     * Determines whether the specified bundle provides implementation of the SNAMP Management Connector.
     * @param bnd The bundle to check.
     * @return {@literal true}, if the specified bundle provides implementation of the management connector;
     *      otherwise, {@literal false}.
     */
    public static boolean isManagementConnectorBundle(final Bundle bnd){
        return bnd != null && bnd.getHeaders().get(CONNECTOR_NAME_MANIFEST_HEADER) != null;
    }

    private static Collection<Bundle> getManagementConnectorBundles(final BundleContext context){
        final Bundle[] bundles = context.getBundles();
        final Collection<Bundle> result = new ArrayList<>(bundles.length);
        for(final Bundle bnd: bundles)
            if(isManagementConnectorBundle(bnd)) result.add(bnd);
        return result;
    }

    private static Collection<Bundle> getManagementConnectorBundles(final BundleContext context, final String connectorName){
        final Bundle[] bundles = context.getBundles();
        final Collection<Bundle> result = new ArrayList<>(bundles.length);
        for(final Bundle bnd: bundles)
            if(Objects.equals(bnd.getHeaders().get(CONNECTOR_NAME_MANIFEST_HEADER), connectorName))
                result.add(bnd);
        return result;
    }

    /**
     * Stops all bundles with management connectors.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     * @throws org.osgi.framework.BundleException Unable to stop management connectors.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static void stopManagementConnectors(final BundleContext context) throws BundleException {
        if(context == null) throw new IllegalArgumentException("context is null.");
        for(final Bundle bnd: getManagementConnectorBundles(context))
            bnd.stop();
    }

    /**
     * Stops the specified management connector.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorName The name of the connector to stop.
     * @throws BundleException Unable to stop management connector.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     */
    public static void stopManagementConnector(final BundleContext context, final String connectorName) throws BundleException {
        for(final Bundle bnd: getManagementConnectorBundles(context, connectorName))
            bnd.stop();
    }

    /**
     * Starts all bundles with management connectors.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     * @throws org.osgi.framework.BundleException Unable to start management connectors.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static void startManagementConnectors(final BundleContext context) throws BundleException{
        if(context == null) throw new IllegalArgumentException("context is null.");
        for(final Bundle bnd: getManagementConnectorBundles(context))
            bnd.stop();
    }

    /**
     * Starts management connector.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorName The name of the connector to start.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     * @throws BundleException Unable to start management connector.
     */
    public static void startManagementConnector(final BundleContext context, final String connectorName) throws BundleException{
        if(context == null) throw new IllegalArgumentException("context is null.");
        for(final Bundle bnd: getManagementConnectorBundles(context, connectorName))
            bnd.start();
    }

    /**
     * Deactivates this library.
     *
     * @param activationProperties A collection of library activation properties to read.
     */
    @Override
    @MethodStub
    protected final void deactivate(final ActivationPropertyReader activationProperties) {

    }
}
