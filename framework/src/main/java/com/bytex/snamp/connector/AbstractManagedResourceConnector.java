package com.bytex.snamp.connector;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.Localizable;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.connector.attributes.AbstractAttributeRepository;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.metrics.ImmutableMetrics;
import com.bytex.snamp.connector.metrics.Metric;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.connector.notifications.AbstractNotificationRepository;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.connector.operations.OperationSupport;
import com.bytex.snamp.jmx.JMExceptionUtils;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import javax.management.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

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
public abstract class AbstractManagedResourceConnector extends AbstractAggregator implements ManagedResourceConnector, Localizable {
    private final LazySoftReference<MetricsSupport> metrics;
    private ImmutableMap<String, String> configuration;

    protected AbstractManagedResourceConnector() {
        metrics = new LazySoftReference<>();
        configuration = ImmutableMap.of();
    }

    protected AbstractManagedResourceConnector(final Map<String, String> configuration) {
        metrics = new LazySoftReference<>();
        setConfiguration(configuration);
    }

    protected final void setConfiguration(final Map<String, String> configuration){
        this.configuration = ImmutableMap.copyOf(configuration);
    }

    @Nonnull
    @Override
    public final ImmutableMap<String, String> getRuntimeConfiguration() {
        return configuration;
    }

    /**
     * Assembles reader of metrics from the set of feature repositories.
     * @param repositories A set of repositories.
     * @return A new instance of metrics reader.
     */
    @SafeVarargs
    protected static MetricsSupport assembleMetricsReader(final AbstractFeatureRepository<? extends MBeanFeatureInfo>... repositories) {
        return new ImmutableMetrics(repositories, AbstractFeatureRepository::getMetrics);
    }

    @SafeVarargs
    protected static MetricsSupport assembleMetricsReader(final Supplier<? extends Metric>... metrics) {
        return new ImmutableMetrics(metrics, Supplier::get);
    }

    /**
     * Releases all resources associated with this connector.
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    public void close() throws Exception {
        //change state of the connector
        metrics.reset();
        configuration.clear();
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

    /**
     * Gets an array of supported notifications.
     * @return An array of supported notifications.
     */
    public MBeanNotificationInfo[] getNotificationInfo(){
        final NotificationSupport notifs = queryObject(NotificationSupport.class);
        return notifs != null ? notifs.getNotificationInfo() : emptyArray(MBeanNotificationInfo[].class);
    }

    /**
     * Gets an array of supported operations.
     * @return An array of supported operations.
     */
    public MBeanOperationInfo[] getOperationInfo(){
        final OperationSupport ops = queryObject(OperationSupport.class);
        return ops != null ? ops.getOperationInfo() : emptyArray(MBeanOperationInfo[].class);
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
    protected abstract MetricsSupport createMetricsReader();

    /**
     * Gets metrics associated with this instance of the resource connector.
     * @return Connector metrics.
     * @throws IllegalStateException This connector is closed.
     */
    @Aggregation    //already cached in the field
    @SpecialUse
    public final MetricsSupport getMetrics() {
        return metrics.lazyGet(this, AbstractManagedResourceConnector::createMetricsReader);
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
     * @param connectionParameters A new connection parameters.
     * @throws Exception Unable to update managed resource connector.
     * @throws UnsupportedUpdateOperationException This operation is not supported by this resource connector.
     */
    @Override
    public void updateConfiguration(final Map<String, ?> connectionParameters) throws Exception {
        throw new UnsupportedUpdateOperationException("Update operation is not supported");
    }
}
