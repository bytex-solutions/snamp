package com.bytex.snamp.connectors;

import com.bytex.snamp.Aggregator;
import com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.FeatureConfiguration;
import com.bytex.snamp.configuration.ConfigurationEntityDescription;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.bytex.snamp.configuration.PersistentConfigurationManager;
import com.bytex.snamp.connectors.discovery.DiscoveryService;
import com.bytex.snamp.core.FrameworkService;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.core.SupportService;
import com.bytex.snamp.management.Maintainable;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Futures;
import org.osgi.framework.*;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.management.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;

import static com.bytex.snamp.ArrayUtils.emptyArray;
import static com.bytex.snamp.configuration.AgentConfiguration.EntityConfiguration;

/**
 * Represents a client of resource connector that can be used by resource consumers.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class ManagedResourceConnectorClient extends ServiceHolder<ManagedResourceConnector> implements Aggregator, DynamicMBean {
    /**
     * Initializes a new client of the specified managed resource.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param resourceName The name of the managed resource. Cannot be {@literal null} or empty.
     * @throws InstanceNotFoundException The specified resource doesn't exist.
     */
    public ManagedResourceConnectorClient(final BundleContext context,
                                          final String resourceName) throws InstanceNotFoundException {
        super(context, getResourceConnectorAndCheck(context, resourceName));
    }

    private static ServiceReference<ManagedResourceConnector> getResourceConnectorAndCheck(final BundleContext context,
                                                                                           final String resourceName) throws InstanceNotFoundException {
        final ServiceReference<ManagedResourceConnector> result =
                getResourceConnector(context, resourceName);
        if (result == null)
            throw new InstanceNotFoundException(String.format("Managed resource '%s' doesn't exist", resourceName));
        else return result;
    }

    private static UnsupportedOperationException unsupportedServiceRequest(final String connectorType,
                                                                           final Class<? extends SupportService> serviceType){
        return new UnsupportedOperationException(String.format("Resource connector %s doesn't expose %s service", connectorType, serviceType));
    }

    private static String getConnectorBundleHeader(final BundleContext context,
                                                   final String connectorType,
                                                   final String header,
                                                   final Locale loc){
        final List<Bundle> candidates = ManagedResourceActivator.getResourceConnectorBundles(context, connectorType);
        return candidates.isEmpty() ? null : candidates.get(0).getHeaders(loc != null ? loc.toString() : null).get(header);
    }

    /**
     * Gets a reference to the service exposed by managed resource connector.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorType The system name of the connector.
     * @param filter Additional service selector. May be {@literal null}.
     * @param serviceType Requested service contract.
     * @param <S> Type of the requested service.
     * @return A reference to the service; or {@literal null}, if service is not available.
     * @throws org.osgi.framework.InvalidSyntaxException Invalid filter.
     */
    public static <S extends FrameworkService> ServiceReference<S> getServiceReference(final BundleContext context,
                                                                                final String connectorType,
                                                                                String filter,
                                                                                final Class<S> serviceType) throws InvalidSyntaxException {
        filter = ManagedResourceActivator.createFilter(connectorType, filter);
        final Collection<ServiceReference<S>> refs = context.getServiceReferences(serviceType, filter);
        return refs.isEmpty() ? null : refs.iterator().next();
    }

    /**
     * Gets bundle state of the specified connector.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorType The system name of the connector.
     * @return The state of the bundle.
     */
    public static int getState(final BundleContext context, final String connectorType){
        final List<Bundle> bnds = ManagedResourceActivator.getResourceConnectorBundles(context, connectorType);
        return bnds.isEmpty() ? Bundle.UNINSTALLED : bnds.get(0).getState();
    }

    /**
     * Gets version of the specified resource connector.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorType The system name of the connector.
     * @return The version of the connector.
     */
    public static Version getVersion(final BundleContext context, final String connectorType){
        return new Version(getConnectorBundleHeader(context, connectorType, Constants.BUNDLE_VERSION, null));
    }

    /**
     * Gets localized description of the managed resource connector.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorType The system name of the connector.
     * @param loc The locale of the description. May be {@literal null}.
     * @return The description of the connector.
     */
    public static String getDescription(final BundleContext context, final String connectorType, final Locale loc) {
        return getConnectorBundleHeader(context, connectorType, Constants.BUNDLE_DESCRIPTION, loc);
    }

    /**
     * Gets localized display name of the managed resource connector.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorType The system name of the connector.
     * @param loc The locale of the display name. May be {@literal null}.
     * @return The display name of the connector.
     */
    public static String getDisplayName(final BundleContext context, final String connectorType, final Locale loc) {
        return getConnectorBundleHeader(context, connectorType, Constants.BUNDLE_NAME, loc);
    }

    /**
     * Gets configuration descriptor for the specified connector.
     * <p>
     *     The connector bundle should expose {@link com.bytex.snamp.configuration.ConfigurationEntityDescriptionProvider} service.
     * </p>
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorType The system name of the connector.
     * @param configurationEntity Type of the configuration entity.
     * @param <T> Type of the configuration entity.
     * @return Configuration entity descriptor.
     * @throws java.lang.UnsupportedOperationException The specified connector doesn't provide
     *          configuration descriptor.
     */
    public static <T extends EntityConfiguration> ConfigurationEntityDescription<T> getConfigurationEntityDescriptor(final BundleContext context,
                                                          final String connectorType,
                                                          final Class<T> configurationEntity) throws UnsupportedOperationException {
        if (context == null || configurationEntity == null) return null;
        ServiceReference<ConfigurationEntityDescriptionProvider> ref = null;
        try {
            ref = getServiceReference(context, connectorType, null, ConfigurationEntityDescriptionProvider.class);
            if (ref == null)
                throw unsupportedServiceRequest(connectorType, ConfigurationEntityDescriptionProvider.class);
            final ConfigurationEntityDescriptionProvider provider = context.getService(ref);
            return provider.getDescription(configurationEntity);
        } catch (final InvalidSyntaxException ignored) {
            ref = null;
            return null;
        } finally {
            if (ref != null) context.ungetService(ref);
        }
    }

    /**
     * Discovers elements for the managed resource.
     * <p>
     *     The connector bundle should expose {@link com.bytex.snamp.connectors.discovery.DiscoveryService} service.
     * </p>
     * @param context The context of the caller bundle.
     * @param connectorType The system name of the connector.
     * @param connectionString Managed resource connection string.
     * @param connectionOptions Managed resource connection options.
     * @param entityType Type of the managed resource element.
     * @param <T> Type of the managed resource element.
     * @return A collection of discovered managed resource elements
     * @throws java.lang.UnsupportedOperationException Managed resource doesn't support metadata discovery.
     */
    public static <T extends FeatureConfiguration> Collection<T> discoverEntities(final BundleContext context,
                                                                                          final String connectorType,
                                                                                          final String connectionString,
                                                                                          final Map<String, String> connectionOptions,
                                                                                          final Class<T> entityType) throws UnsupportedOperationException{
        if(context == null || entityType == null) return Collections.emptyList();
        ServiceReference<DiscoveryService> ref = null;
        try {
            ref = getServiceReference(context, connectorType, null, DiscoveryService.class);
            if(ref == null) throw unsupportedServiceRequest(connectorType, DiscoveryService.class);
            final DiscoveryService service = context.getService(ref);
            return service.discover(connectionString, connectionOptions, entityType);
        }
        catch (final InvalidSyntaxException ignored) {
            return Collections.emptyList();
        }
        finally {
            if(ref != null) context.ungetService(ref);
        }
    }

    /**
     * Gets a collection of connector maintenance actions.
     * <p>
     *     The connector bundle should expose {@link com.bytex.snamp.management.Maintainable} service.
     * </p>
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorType The system name of the connector.
     * @param loc The locale of the description. May be {@literal null}.
     * @return A map of supported actions and its description.
     * @throws UnsupportedOperationException Resource connector doesn't support maintenance.
     */
    public static Map<String, String> getMaintenanceActions(final BundleContext context,
                                                            final String connectorType,
                                                            final Locale loc) throws UnsupportedOperationException{
        if(context == null) return Collections.emptyMap();
        ServiceReference<Maintainable> ref = null;
        try {
            ref = getServiceReference(context, connectorType, null, Maintainable.class);
            if(ref == null) throw unsupportedServiceRequest(connectorType, Maintainable.class);
            final Maintainable service = context.getService(ref);
            final Map<String, String> result = new HashMap<>(service.getActions().size());
            for(final String actionName: service.getActions())
                result.put(actionName, service.getActionDescription(actionName, loc));
            return result;
        }
        catch (final InvalidSyntaxException ignored) {
            ref = null;
            return Collections.emptyMap();
        }
        finally {
            if(ref != null) context.ungetService(ref);
        }
    }

    /**
     * Invokes maintenance action.
     * <p>
     *     The connector bundle should expose {@link com.bytex.snamp.management.Maintainable} service.
     * </p>
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorType The system name of the connector.
     * @param actionName The name of the maintenance action to invoke.
     * @param arguments Invocation arguments.
     * @param resultLocale The locale of the input arguments and result.
     * @return An object that represents asynchronous state of the action invocation.
     * @throws UnsupportedOperationException
     */
    public static Future<String> invokeMaintenanceAction(final BundleContext context,
                                                         final String connectorType,
                                                         final String actionName,
                                                         final String arguments,
                                                         final Locale resultLocale) throws UnsupportedOperationException{
        if(context == null) return null;
        ServiceReference<Maintainable> ref = null;
        try {
            ref = getServiceReference(context, connectorType, null, Maintainable.class);
            if(ref == null) throw unsupportedServiceRequest(connectorType, Maintainable.class);
            return context.getService(ref).doAction(actionName, arguments, resultLocale);
        }
        catch (final InvalidSyntaxException e) {
            return Futures.immediateFailedCheckedFuture(e);
        }
        finally {
            if(ref != null) context.ungetService(ref);
        }
    }

    /**
     * Gets a map of available management connectors in the current OSGi environment.
     * @param context The context of the caller bundle.
     * @return A map of management connector references where the key of the map represents
     *          a name of the management target.
     */
    public static Map<String, ServiceReference<ManagedResourceConnector>> getConnectors(final BundleContext context){
        if(context == null) return Collections.emptyMap();
        else try {
            ServiceReference<?>[] connectors = context.getAllServiceReferences(ManagedResourceConnector.class.getName(), null);
            if(connectors == null) connectors = emptyArray(ServiceReference[].class);
            final Map<String, ServiceReference<ManagedResourceConnector>> result = new HashMap<>(connectors.length);
            for(final ServiceReference<?> serviceRef: connectors) {
                @SuppressWarnings("unchecked")
                final ServiceReference<ManagedResourceConnector> connectorRef = (ServiceReference<ManagedResourceConnector>)serviceRef;
                result.put(getManagedResourceName(connectorRef), connectorRef);
            }
            return result;
        }
        catch (final InvalidSyntaxException ignored) {
            return Collections.emptyMap();
        }
    }

    /**
     * Gets type of the management connector by its reference.
     * @param connectorRef The reference to the management connector.
     * @return The type of the management connector.
     */
    public static String getConnectorType(final ServiceReference<ManagedResourceConnector> connectorRef){
        return ManagedResourceActivator.getConnectorType(connectorRef);
    }

    public String getConnectorType(){
        return getConnectorType(this);
    }

    /**
     * Gets connection string used by management connector by its reference.
     * @param connectorRef The reference to the management connector.
     * @return The connection string used by management connector.
     */
    public static String getConnectionString(final ServiceReference<ManagedResourceConnector> connectorRef){
        return ManagedResourceActivator.getConnectionString(connectorRef);
    }

    public String getConnectionString(){
        return getConnectionString(this);
    }

    /**
     * Gets name of the management target that is represented by the specified management
     * connector reference.
     * @param connectorRef The reference to the management connector.
     * @return The name of the management target.
     */
    public static String getManagedResourceName(final ServiceReference<ManagedResourceConnector> connectorRef){
        return ManagedResourceActivator.getManagedResourceName(connectorRef);
    }

    /**
     * Gets a reference to the managed resource connector.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param resourceName The name of the managed resource.
     * @return A reference to the managed resource connector that serves the specified resource; or {@literal null}, if connector doesn't exist.
     */
    @SuppressWarnings("unchecked")
    public static ServiceReference<ManagedResourceConnector> getResourceConnector(final BundleContext context,
                                                                          final String resourceName) {
        try {
            return Iterables.<ServiceReference>getFirst(context.getServiceReferences(ManagedResourceConnector.class, ManagedResourceActivator.createFilter(resourceName)), null);
        }
        catch (final InvalidSyntaxException ignored) {
            return null;
        }
    }

    /**
     * Exposes a new object that listen for the managed resource connector service.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param listener The managed resource listener. Cannot be {@literal null}.
     * @return {@literal true}, if listener is registered successfully; otherwise, {@literal false}.
     */
    public static boolean addResourceListener(final BundleContext context, final ServiceListener listener){
        try {
            context.addServiceListener(listener, ManagedResourceActivator.createFilter("*", String.format("(%s=%s)", Constants.OBJECTCLASS, ManagedResourceConnector.class.getName())));
            return true;
        } catch (final InvalidSyntaxException ignored) {
            return false;
        }
    }

    /**
     * Determines whether the specified reference is a reference to {@link com.bytex.snamp.connectors.ManagedResourceConnector} service.
     * @param ref A reference to check.
     * @return {@literal true}, if the specified object is a reference to the {@link com.bytex.snamp.connectors.ManagedResourceConnector} service; otherwise, {@literal false}.
     */
    public static boolean isResourceConnector(final ServiceReference<?> ref){
        return ManagedResourceActivator.isResourceConnector(ref);
    }

    public static ManagedResourceConfiguration getResourceConfiguration(final BundleContext context,
                                                                        final ServiceReference<ManagedResourceConnector> connectorRef) throws IOException{
        return getResourceConfiguration(context, getManagedResourceName(connectorRef));
    }

    public static ManagedResourceConfiguration getResourceConfiguration(final BundleContext context,
                                                                        final String resourceName) throws IOException {
        final ServiceHolder<ConfigurationAdmin> admin = ServiceHolder.tryCreate(context,
                ConfigurationAdmin.class);
        if (admin != null)
            try {
                return PersistentConfigurationManager.readResourceConfiguration(admin.getService(), resourceName);
            } finally {
                admin.release(context);
            }
        else return null;
    }

    public ManagedResourceConfiguration getResourceConfiguration(final BundleContext context) throws IOException {
        return getResourceConfiguration(context, this);
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the requested object.
     * @return An instance of the aggregated object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(final Class<T> objectType) {
        return getService().queryObject(objectType);
    }

    /**
     * Obtain the value of a specific attribute of the Dynamic MBean.
     *
     * @param attribute The name of the attribute to be retrieved
     * @return The value of the attribute retrieved.
     * @throws AttributeNotFoundException
     * @throws MBeanException             Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's getter.
     * @throws ReflectionException        Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the getter.
     * @see #setAttribute
     */
    @Override
    public Object getAttribute(final String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        return getService().getAttribute(attribute);
    }

    /**
     * Set the value of a specific attribute of the Dynamic MBean.
     *
     * @param attribute The identification of the attribute to
     *                  be set and  the value it is to be set to.
     * @throws AttributeNotFoundException
     * @throws InvalidAttributeValueException
     * @throws MBeanException                 Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's setter.
     * @throws ReflectionException            Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the MBean's setter.
     * @see #getAttribute
     */
    @Override
    public void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        getService().setAttribute(attribute);
    }

    /**
     * Get the values of several attributes of the Dynamic MBean.
     *
     * @param attributes A list of the attributes to be retrieved.
     * @return The list of attributes retrieved.
     * @see #setAttributes
     */
    @Override
    public AttributeList getAttributes(final String[] attributes) {
        return getService().getAttributes(attributes);
    }

    /**
     * Sets the values of several attributes of the Dynamic MBean.
     *
     * @param attributes A list of attributes: The identification of the
     *                   attributes to be set and  the values they are to be set to.
     * @return The list of attributes that were set, with their new values.
     * @see #getAttributes
     */
    @Override
    public AttributeList setAttributes(final AttributeList attributes) {
        return getService().setAttributes(attributes);
    }

    /**
     * Allows an action to be invoked on the Dynamic MBean.
     *
     * @param actionName The name of the action to be invoked.
     * @param params     An array containing the parameters to be set when the action is
     *                   invoked.
     * @param signature  An array containing the signature of the action. The class objects will
     *                   be loaded through the same class loader as the one used for loading the
     *                   MBean on which the action is invoked.
     * @return The object returned by the action, which represents the result of
     * invoking the action on the MBean specified.
     * @throws MBeanException      Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's invoked method.
     * @throws ReflectionException Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the method
     */
    @Override
    public Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
        return getService().invoke(actionName, params, signature);
    }

    /**
     * Provides the exposed attributes and actions of the Dynamic MBean using an MBeanInfo object.
     *
     * @return An instance of <CODE>MBeanInfo</CODE> allowing all attributes and actions
     * exposed by this Dynamic MBean to be retrieved.
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return getService().getMBeanInfo();
    }

    /**
     * Returns resource name.
     * @return The name of the resource.
     */
    @Override
    public String toString() {
        return getManagedResourceName(this);
    }
}
