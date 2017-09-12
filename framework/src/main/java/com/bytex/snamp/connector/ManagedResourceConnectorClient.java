package com.bytex.snamp.connector;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.configuration.ConfigurationEntityDescription;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.bytex.snamp.configuration.EntityConfiguration;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.health.HealthCheckSupport;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.OkStatus;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.core.SupportService;
import org.osgi.framework.*;

import javax.annotation.Nonnull;
import javax.management.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.concurrent.SpinWait.untilNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents a client of resource connector that can be used by resource consumers.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class ManagedResourceConnectorClient extends ServiceHolder<ManagedResourceConnector> implements ManagedResourceConnector, SafeCloseable, HealthCheckSupport {
    private final BundleContext context;

    public ManagedResourceConnectorClient(@Nonnull final BundleContext context,
                                          final ServiceReference<ManagedResourceConnector> reference) throws InstanceNotFoundException {
        super(context, reference);
        this.context = context;
    }

    public static Optional<ManagedResourceConnectorClient> tryCreate(final BundleContext context,
                                                           final String resourceName,
                                                           final Duration instanceTimeout) throws TimeoutException, InterruptedException{
        final ServiceReference<ManagedResourceConnector> ref = untilNull(context, resourceName, ManagedResourceConnectorClient::getResourceConnector, instanceTimeout);
        try {
            return Optional.of(new ManagedResourceConnectorClient(context, ref));
        } catch (final InstanceNotFoundException e) {
            return Optional.empty();
        }
    }

    public static Optional<ManagedResourceConnectorClient> tryCreate(final BundleContext context, final String resourceName) {
        return Optional.ofNullable(getResourceConnector(context, resourceName))
                .map(ref -> {
                    try {
                        return new ManagedResourceConnectorClient(context, ref);
                    } catch (final InstanceNotFoundException e) {
                        return null;
                    }
                });
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
        final String version = getConnectorBundleHeader(context, connectorType, Constants.BUNDLE_VERSION, null);
        return isNullOrEmpty(version) ? Version.emptyVersion : new Version(version);
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
            ref = selector()
                    .setConnectorType(connectorType)
                    .setServiceType(ConfigurationEntityDescriptionProvider.class)
                    .getServiceReference(context, ConfigurationEntityDescriptionProvider.class)
                    .orElseThrow(() -> unsupportedServiceRequest(connectorType, ConfigurationEntityDescriptionProvider.class));
            final ConfigurationEntityDescriptionProvider provider = context.getService(ref);
            return provider.getDescription(configurationEntity);
        } finally {
            if (ref != null) context.ungetService(ref);
        }
    }

    /**
     * Creates a new instance of the specified connector using service {@link ManagedResourceConnectorFactoryService}.
     * @param context The context of the caller bundle.
     * @param connectorType Type of the connector to instantiate.
     * @param resourceName Name of the resource
     * @param configuration Configuration of the managed resource.
     * @return A new instance of resource connector.
     * @throws Exception An exception occurred by {@link ManagedResourceConnector} constructor.
     * @throws InstantiationException Not enough parameters to instantiate {@link ManagedResourceConnector}.
     * @throws UnsupportedOperationException The specified connector type doesn't provide a factory.
     * @since 2.0
     */
    public static ManagedResourceConnector createConnector(final BundleContext context,
                                                           final String connectorType,
                                                           final String resourceName,
                                                           final ManagedResourceInfo configuration) throws Exception {
        ServiceReference<ManagedResourceConnectorFactoryService> ref = null;
        try {
            ref = selector()
                    .setConnectorType(connectorType)
                    .setServiceType(ManagedResourceConnectorFactoryService.class)
                    .getServiceReference(context, ManagedResourceConnectorFactoryService.class)
                    .orElseThrow(() -> unsupportedServiceRequest(connectorType, ManagedResourceConnectorFactoryService.class));
            final ManagedResourceConnectorFactoryService service = context.getService(ref);
            return service.createConnector(resourceName, configuration);
        } finally {
            if (ref != null) context.ungetService(ref);
        }
    }

    public String getConnectorType(){
        return ManagedResourceConnector.getConnectorType(getBundle());
    }

    /**
     * Gets connection string used by management connector by its reference.
     * @return The connection string used by management connector.
     */
    public String getConnectionString(){
        return ManagedResourceSelector.getConnectionString(this);
    }

    /**
     * Gets name of the management target that is represented by the specified management
     * connector reference.
     * @return The name of the management target.
     */
    public String getManagedResourceName(){
        return ManagedResourceSelector.getManagedResourceName(this);
    }

    public String getGroupName() {
        final String groupName = ManagedResourceSelector.getGroupName(this);
        return isNullOrEmpty(groupName) ? getManagedResourceName() : groupName;
    }

    private static ServiceReference<ManagedResourceConnector> getResourceConnector(final BundleContext context,
                                                                          final String resourceName) {
        return selector()
                .setResourceName(resourceName)
                .getServiceReference(context, ManagedResourceConnector.class)
                .orElse(null);
    }

    /**
     * Constructs a new filter builder used to query instances of {@link ManagedResourceConnector}.
     * @return A new filter builder.
     */
    public static ManagedResourceSelector selector() {
        final ManagedResourceSelector result = new ManagedResourceSelector();
        result.setServiceType(ManagedResourceConnector.class);
        return result;
    }

    public AttributeList getAttributes() throws ReflectionException, MBeanException {
        final Optional<AttributeSupport> attributeSupport = queryObject(AttributeSupport.class);
        return attributeSupport.isPresent()? attributeSupport.get().getAttributes() : new AttributeList();
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the requested object.
     * @return An instance of the aggregated object; or {@literal null} if object is not available.
     */
    @Override
    public <T> Optional<T> queryObject(@Nonnull final Class<T> objectType) {
        return getService().flatMap(connector -> connector.queryObject(objectType));
    }

    /**
     * Adds a new listener for the connector-related events.
     * <p>
     * The managed resource connector should holds a weak reference to all added event listeners.
     *
     * @param listener An event listener to add.
     */
    @Override
    public void addResourceEventListener(final ResourceEventListener listener) {
        getService().ifPresent(connector -> connector.addResourceEventListener(listener));
    }

    /**
     * Removes connector event listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeResourceEventListener(final ResourceEventListener listener) {
        getService().ifPresent(connector -> connector.removeResourceEventListener(listener));
    }

    /**
     * Gets mutable set of characteristics of this managed resource connector.
     *
     * @return Characteristics of this managed resource connector.
     */
    @Nonnull
    @Override
    public ManagedResourceInfo getConfiguration() {
        return getService().map(ManagedResourceConnector::getConfiguration).orElse(EMPTY_CONFIGURATION);
    }

    @Override
    public void close() {
        release(context);
    }

    /**
     * Updates resource connector with a new connection configuration.
     *
     * @param configuration A new configuration.
     * @throws Exception                                                    Unable to update managed resource connector.
     * @throws UnsupportedUpdateOperationException This operation is not supported
     *                                                                      by this resource connector.
     */
    @Override
    public void update(final ManagedResourceInfo configuration) throws Exception {
        final Optional<ManagedResourceConnector> connector = getService();
        if (connector.isPresent())
            connector.get().update(configuration);
    }

    @Override
    public Collection<? extends MBeanFeatureInfo> expandAll() {
        return getService().map(ManagedResourceConnector::expandAll).orElse(Collections.emptyList());
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
        return get().getAttribute(attribute);
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
        get().setAttribute(attribute);
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
        return get().getAttributes(attributes);
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
        return get().setAttributes(attributes);
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
        return get().invoke(actionName, params, signature);
    }

    /**
     * Provides the exposed attributes and actions of the Dynamic MBean using an MBeanInfo object.
     *
     * @return An instance of <CODE>MBeanInfo</CODE> allowing all attributes and actions
     * exposed by this Dynamic MBean to be retrieved.
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return get().getMBeanInfo();
    }

    /**
     * Returns resource name.
     * @return The name of the resource.
     */
    @Override
    public String toString() {
        return getManagedResourceName();
    }

    @Override
    @Nonnull
    public HealthStatus getStatus(){
        return queryObject(HealthCheckSupport.class).map(HealthCheckSupport::getStatus).orElseGet(OkStatus::new);
    }

    /**
     * Gets health status of the managed resource.
     * @param context Bundle context.
     * @param resourceName Name of managed resource.
     * @return Health status of resource; or empty if resource doesn't exist.
     */
    public static Optional<HealthStatus> getStatus(final BundleContext context, final String resourceName) {
        return tryCreate(context, resourceName).map(client -> {
            try {
                return client.getStatus();
            } finally {
                client.close();
            }
        });
    }
}
