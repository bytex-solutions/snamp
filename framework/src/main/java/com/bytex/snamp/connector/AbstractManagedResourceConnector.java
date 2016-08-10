package com.bytex.snamp.connector;

import com.bytex.snamp.Localizable;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.ThreadSafe;
import com.bytex.snamp.concurrent.LazyValueFactory;
import com.bytex.snamp.concurrent.LazyValue;
import com.bytex.snamp.connector.attributes.AbstractAttributeRepository;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.metrics.Metrics;
import com.bytex.snamp.connector.metrics.MetricsReader;
import com.bytex.snamp.connector.notifications.AbstractNotificationRepository;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.connector.operations.OperationSupport;
import com.bytex.snamp.core.AbstractFrameworkService;
import com.bytex.snamp.internal.IllegalStateFlag;
import com.bytex.snamp.jmx.JMExceptionUtils;
import org.osgi.framework.FrameworkUtil;

import javax.management.*;
import java.util.*;
import java.util.logging.Logger;

import static com.bytex.snamp.ArrayUtils.emptyArray;

/**
 * Represents an abstract class for building custom management connector.
 * <p>
 *     This class provides a base support for the following management mechanisms:
 *     <ul>
 *         <li>{@link AbstractAttributeRepository} for resource management using attributes.</li>
 *         <li>{@link AbstractNotificationRepository} to receive management notifications from the managed resource.</li>
 *         <li>{@link com.bytex.snamp.connector.operations.AbstractOperationRepository} for resource management using operations.</li>
 *     </ul>
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public abstract class AbstractManagedResourceConnector extends AbstractFrameworkService implements ManagedResourceConnector, Localizable {
    private final IllegalStateFlag closed = createConnectorStateFlag();
    private final LazyValue<MetricsReader> metrics;

    protected AbstractManagedResourceConnector() {
        metrics = LazyValueFactory.THREAD_SAFE_SOFT_REFERENCED.of(this::createMetricsReader);
    }

    private static IllegalStateFlag createConnectorStateFlag(){
        return new IllegalStateFlag() {
            @Override
            public IllegalStateException create() {
                return new IllegalStateException("Management connector is closed.");
            }
        };
    }

    /**
     * Assembles reader of metrics from the set of feature repositories.
     * @param repositories A set of repositories.
     * @return A new instance of metrics reader.
     */
    @SafeVarargs
    protected static MetricsReader assembleMetricsReader(final AbstractFeatureRepository<? extends MBeanFeatureInfo>... repositories) {
        return new MetricsReader() {
            @Override
            public Metrics getMetrics(final Class<? extends MBeanFeatureInfo> featureType) {
                for (final AbstractFeatureRepository<?> repository : repositories)
                    if (featureType.isAssignableFrom(repository.metadataType))
                        return repository.getMetrics();
                return null;
            }

            @Override
            public void resetAll() {
                for (final AbstractFeatureRepository<?> repository : repositories)
                    repository.getMetrics().reset();
            }

            @Override
            public <T> T queryObject(final Class<T> objectType) {
                for (final AbstractFeatureRepository<?> repository : repositories) {
                    final Metrics metrics = repository.getMetrics();
                    if (objectType.isInstance(metrics))
                        return objectType.cast(metrics);
                }
                return null;
            }
        };
    }

    /**
     *  Throws an {@link IllegalStateException} if the connector is closed.
     *  <p>
     *      You should call the base implementation from the overridden method.
     *  </p>
     *  @throws IllegalStateException Connector is closed.
     */
    protected void verifyClosedState() throws IllegalStateException{
        closed.verify();
    }

    private void verifyClosedStateChecked() throws MBeanException{
        try{
            verifyClosedState();
        }
        catch (final Exception e){
            throw new MBeanException(e);
        }
    }
    /**
     * Releases all resources associated with this connector.
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    @ThreadSafe(false)
    public void close() throws Exception {
        //change state of the connector
        closed.set();
        metrics.reset();
        clearCache();
    }

    /**
     * Obtain the value of a specific attribute of the managed resource.
     *
     * @param attribute The name of the attribute to be retrieved
     * @return The value of the attribute retrieved.
     * @throws javax.management.AttributeNotFoundException
     * @throws javax.management.MBeanException             Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's getter.
     * @throws javax.management.ReflectionException        Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the getter.
     * @see #setAttribute(javax.management.Attribute)
     */
    @Override
    public Object getAttribute(final String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        verifyClosedStateChecked();
        final AttributeSupport attributeSupport = queryObject(AttributeSupport.class);
        if(attributeSupport != null)
            return attributeSupport.getAttribute(attribute);
        else throw JMExceptionUtils.attributeNotFound(attribute);
    }

    /**
     * Set the value of a specific attribute of the managed resource.
     *
     * @param attribute The identification of the attribute to
     *                  be set and  the value it is to be set to.
     * @throws javax.management.AttributeNotFoundException
     * @throws javax.management.InvalidAttributeValueException
     * @throws javax.management.MBeanException                 Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's setter.
     * @throws javax.management.ReflectionException            Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the MBean's setter.
     * @see #getAttribute
     */
    @Override
    public void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        verifyClosedStateChecked();
        final AttributeSupport attributeSupport = queryObject(AttributeSupport.class);
        if(attributeSupport != null)
            attributeSupport.setAttribute(attribute);
        else throw JMExceptionUtils.attributeNotFound(attribute.getName());
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
        verifyClosedState();
        final AttributeSupport attributeSupport = queryObject(AttributeSupport.class);
        return attributeSupport != null ? attributeSupport.getAttributes(attributes) : new AttributeList();
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
        verifyClosedState();
        final AttributeSupport attributeSupport = queryObject(AttributeSupport.class);
        return attributeSupport != null ? attributeSupport.setAttributes(attributes) : new AttributeList();
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
     * @throws javax.management.MBeanException      Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's invoked method.
     * @throws javax.management.ReflectionException Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the method
     */
    @Override
    public Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
        final OperationSupport ops = queryObject(OperationSupport.class);
        if(ops != null)
            return ops.invoke(actionName, params, signature);
        else throw new MBeanException(new UnsupportedOperationException("Operation invocation is not supported."));
    }

    private String getClassName(){
        return getClass().getName();
    }

    /**
     * Returns the localized description of this connector.
     *
     * @param locale The locale of the description. If it is {@literal null} then returns description
     *               in the default locale.
     * @return The localized description of this connector.
     */
    @Override
    public String toString(final Locale locale) {
        return getClassName();
    }

    /**
     * Gets an array of supported attributes.
     * @return An array of supported attributes.
     */
    public MBeanAttributeInfo[] getAttributeInfo() {
        final AttributeSupport attributes = queryObject(AttributeSupport.class);
        return attributes != null ? attributes.getAttributeInfo() : emptyArray(MBeanAttributeInfo[].class);
    }

    public MBeanAttributeInfo getAttributeInfo(final String attributeName){
        final AttributeSupport attributes = queryObject(AttributeSupport.class);
        return attributes != null ? attributes.getAttributeInfo(attributeName) : null;
    }

    /**
     * Gets an array of supported notifications.
     * @return An array of supported notifications.
     */
    public MBeanNotificationInfo[] getNotificationInfo(){
        final NotificationSupport notifs = queryObject(NotificationSupport.class);
        return notifs != null ? notifs.getNotificationInfo() : emptyArray(MBeanNotificationInfo[].class);
    }

    public MBeanNotificationInfo getNotificationInfo(final String notificationType){
        final NotificationSupport notifs = queryObject(NotificationSupport.class);
        return notifs != null ? notifs.getNotificationInfo(notificationType) : null;
    }

    /**
     * Gets an array of supported operations.
     * @return An array of supported operations.
     */
    public MBeanOperationInfo[] getOperationInfo(){
        final OperationSupport ops = queryObject(OperationSupport.class);
        return ops != null ? ops.getOperationInfo() : emptyArray(MBeanOperationInfo[].class);
    }

    public MBeanOperationInfo getOperationInfo(final String operationName){
        final OperationSupport ops = queryObject(OperationSupport.class);
        return ops != null ? ops.getOperationInfo(operationName) : null;
    }

    /**
     * Creates a new reader of metrics provided by this resource connector.
     * <p>
     *     You should not mark implementation method
     *     with annotation {@link Aggregation}.
     *     The easiest way to implement this
     *     method is to call method {@link #assembleMetricsReader(AbstractFeatureRepository[])}.
     * @return A new reader of metrics provided by this resource connector.
     */
    protected abstract MetricsReader createMetricsReader();

    /**
     * Gets metrics associated with this instance of the resource connector.
     * @return Connector metrics.
     * @throws IllegalStateException This connector is closed.
     */
    @Aggregation(cached = true)
    @SpecialUse
    public final MetricsReader getMetrics(){
        verifyClosedState();
        return metrics.get();
    }

    /**
     * Provides the exposed attributes and actions of the Dynamic MBean using an MBeanInfo object.
     *
     * @return An instance of <CODE>MBeanInfo</CODE> allowing all attributes and actions
     * exposed by this Dynamic MBean to be retrieved.
     */
    @Override
    public final MBeanInfo getMBeanInfo() {
        return new MBeanInfo(getClassName(),
                toString(Locale.getDefault()),
                getAttributeInfo(),
                emptyArray(MBeanConstructorInfo[].class),
                getOperationInfo(),
                getNotificationInfo());
    }

    /**
     * This method may be used for implementing {@link #addResourceEventListener(ResourceEventListener)}
     * method.
     * <p>
     *     You can use instances of {@link AbstractAttributeRepository} and {@link AbstractNotificationRepository}
     *     as arguments for this method.
     *
     * @param listener The listener to be added to the specified modelers.
     * @param modelers A set of modelers.
     */
    protected static void addResourceEventListener(final ResourceEventListener listener,
                                                   final AbstractFeatureRepository<?>... modelers){
        Arrays.stream(modelers).forEach(modeler -> modeler.addModelEventListener(listener));
    }

    /**
     * This method may be used for implementing {@link #removeResourceEventListener(ResourceEventListener)}
     * method.
     * @param listener The listener to be removed from the specified modelers.
     * @param modelers A set of modelers.
     */
    protected static void removeResourceEventListener(final ResourceEventListener listener,
                                                      final AbstractFeatureRepository<?>... modelers){
        Arrays.stream(modelers).forEach(modeler -> modeler.removeModelEventListener(listener));
    }

    /**
     * Updates resource connector with a new connection options.
     * <p>
     *     In the default implementation this method always throws
     *     {@link UnsupportedUpdateOperationException}.
     * @param connectionString     A new connection string.
     * @param connectionParameters A new connection parameters.
     * @throws Exception Internal connector non-recoverable error.                                                                                 Unable to update managed resource connector.
     * @throws UnsupportedUpdateOperationException This operation is not supported
     *                                                                                                   by this resource connector.
     */
    @Override
    public void update(final String connectionString, final Map<String, String> connectionParameters) throws Exception {
        throw new UnsupportedUpdateOperationException("Update operation is not supported");
    }

    /**
     * Returns logger name based on the management connector name.
     * @param connectorName The name of the connector.
     * @return The logger name.
     */
    public static String getLoggerName(final String connectorName){
        return String.format("com.bytex.snamp.connector.%s", connectorName);
    }

    /**
     * Returns a logger associated with the specified management connector.
     * @param connectorName The name of the connector.
     * @return An instance of the logger.
     */
    public static Logger getLogger(final String connectorName){
        return Logger.getLogger(getLoggerName(connectorName));
    }

    public static Logger getLogger(final Class<? extends ManagedResourceConnector> connectorType){
        return getLogger(getConnectorType(connectorType));
    }

    /**
     * Determines whether the connector may automatically expanded with features without predefined configuration.
     * @param featureType Type of the feature. Cannot be {@literal null}.
     * @return {@literal true}, if this connector supports automatic registration of its features; otherwise, {@literal false}.
     */
    @Override
    public boolean canExpandWith(final Class<? extends MBeanFeatureInfo> featureType) {
        return false;
    }

    /**
     * Expands this connector with features of the specified type.
     * @param featureType The type of the feature that this connector may automatically registers.
     * @param <F> Type of the feature class.
     * @return A collection of registered features.
     */
    @Override
    public <F extends MBeanFeatureInfo> Collection<? extends F> expand(final Class<F> featureType){
        return Collections.emptyList();
    }

    /**
     * Fully expands connector with all possible features.
     * @param connector An instance of the connector to expand.
     * @return A list of features that automatically discovered and registered by connector itself.
     */
    public static Collection<? extends MBeanFeatureInfo> expandAll(final ManagedResourceConnector connector) {
        final List<MBeanFeatureInfo> result = new LinkedList<>();
        if (connector.canExpandWith(MBeanAttributeInfo.class))
            result.addAll(connector.expand(MBeanAttributeInfo.class));
        if (connector.canExpandWith(MBeanNotificationInfo.class))
            result.addAll(connector.expand(MBeanNotificationInfo.class));
        if (connector.canExpandWith(MBeanOperationInfo.class))
            result.addAll(connector.expand(MBeanOperationInfo.class));
        return result;
    }

    /**
     * Fully expands this connector with all possible features.
     * @return A list of features that automatically discovered and registered by connector itself.
     */
    public final Collection<? extends MBeanFeatureInfo> expandAll(){
        return expandAll(this);
    }

    /**
     * Determines whether the Smart-mode is supported by the specified connector.
     * @param connector An instance of the connector. Cannot be {@literal null}.
     * @return {@literal true}, if Smart-mode is supported; otherwise, {@literal false}.
     */
    public static boolean isSmartModeSupported(final ManagedResourceConnector connector){
        return connector.canExpandWith(MBeanAttributeInfo.class) ||
                connector.canExpandWith(MBeanNotificationInfo.class) ||
                connector.canExpandWith(MBeanOperationInfo.class);
    }

    /**
     * Determines whether the Smart-mode is supported by the this connector.
     * @return {@literal true}, if Smart-mode is supported; otherwise, {@literal false}.
     */
    public final boolean isSmartModeSupported(){
        return isSmartModeSupported(this);
    }

    /**
     * Returns system name of the connector using its implementation class.
     * @param connectorImpl A class that represents implementation of resource connector.
     * @return System name of the connector.
     */
    public static String getConnectorType(final Class<? extends ManagedResourceConnector> connectorImpl){
        return ManagedResourceConnector.getResourceConnectorType(FrameworkUtil.getBundle(connectorImpl));
    }

    /**
     * Gets logger associated with this connector.
     * @return A logger associated with this connector.
     */
    @Override
    @Aggregation
    public Logger getLogger() {
        return getLogger(getClass());
    }
}
