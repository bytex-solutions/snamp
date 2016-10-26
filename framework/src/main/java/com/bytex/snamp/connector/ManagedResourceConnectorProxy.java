package com.bytex.snamp.connector;

import javax.management.*;
import java.util.Map;
import java.util.logging.Logger;

import static com.bytex.snamp.connector.ManagedResourceActivator.ManagedResourceConnectorFactory;
import static com.bytex.snamp.core.AbstractBundleActivator.RequiredService;

/**
 * Represents proxy for {@link ManagedResourceConnector} used in composition of connectors.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ManagedResourceConnectorProxy<TConnector extends ManagedResourceConnector> implements ManagedResourceConnector {
    @FunctionalInterface
    private interface ManagedResourceConnectorUpdater<TConnector extends ManagedResourceConnector>{
        TConnector update(final TConnector connector,
                          final String connectionString,
                          final Map<String, String> parameters) throws Exception;
    }

    private volatile TConnector connector;
    private final ManagedResourceConnectorUpdater<TConnector> updater;

    ManagedResourceConnectorProxy(final ManagedResourceConnectorFactory<TConnector> factory,
                                  final String resourceName,
                                  final String connectionString,
                                  final Map<String, String> parameters,
                                  final RequiredService<?>... dependencies) throws Exception {
        this.connector = factory.createConnector(resourceName, connectionString, parameters, dependencies);
        this.updater = (connector, cstr, params) -> factory.updateConnector(connector, resourceName, cstr, params, dependencies);
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return connector.getLogger();
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the requested object.
     * @return An instance of the aggregated object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(final Class<T> objectType) {
        return connector.queryObject(objectType);
    }

    /**
     * Updates resource connector with a new connection options.
     *
     * @param connectionString     A new connection string.
     * @param connectionParameters A new connection parameters.
     * @throws Exception                                                    Unable to update managed resource connector.
     * @throws UnsupportedUpdateOperationException This operation is not supported
     *                                                                      by this resource connector.
     */
    @Override
    public synchronized void update(final String connectionString, final Map<String, String> connectionParameters) throws Exception {
        connector = updater.update(connector, connectionString, connectionParameters);
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
        connector.addResourceEventListener(listener);
    }

    /**
     * Removes connector event listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeResourceEventListener(final ResourceEventListener listener) {
        connector.removeResourceEventListener(listener);
    }

    @Override
    public synchronized void close() throws Exception {
        connector.close();
        connector = null;
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
        return connector.getAttribute(attribute);
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
        connector.setAttribute(attribute);
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
        return connector.getAttributes(attributes);
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
        return connector.setAttributes(attributes);
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
        return connector.invoke(actionName, params, signature);
    }

    /**
     * Provides the exposed attributes and actions of the Dynamic MBean using an MBeanInfo object.
     *
     * @return An instance of <CODE>MBeanInfo</CODE> allowing all attributes and actions
     * exposed by this Dynamic MBean to be retrieved.
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return connector.getMBeanInfo();
    }
}
