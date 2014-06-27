package com.itworks.snamp.adapters.jmx;

import javax.management.*;
import javax.management.openmbean.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;

/**
 * Represents proxy MBean that is used to expose attributes and notifications
 * via JMX.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ProxyMBean extends NotificationBroadcasterSupport implements DynamicMBean {
    private final Map<String, JmxAttribute> attributes;
    private final String resourceName;

    public ProxyMBean(final String resourceName, final Map<String, JmxAttribute> attributes){
        this.attributes = Collections.unmodifiableMap(attributes);
        this.resourceName = resourceName;
    }

    /**
     * Obtain the value of a specific attribute of the Dynamic MBean.
     *
     * @param attributeName The name of the attribute to be retrieved
     * @return The value of the attribute retrieved.
     * @throws javax.management.AttributeNotFoundException
     * @throws javax.management.ReflectionException        Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the getter.
     * @see #setAttribute
     */
    @Override
    public Object getAttribute(final String attributeName) throws AttributeNotFoundException, ReflectionException {
        if(attributes.containsKey(attributeName)){
            final JmxAttribute attribute = attributes.get(attributeName);
            try {
                return attribute.getValue();
            }
            catch (final Exception e) {
                throw new ReflectionException(e);
            }
        }
        else throw new AttributeNotFoundException(String.format("Attribute %s doesn't exist", attributeName));
    }

    /**
     * Set the value of a specific attribute of the Dynamic MBean.
     *
     * @param attributeHolder The identification of the attribute to
     *                  be set and  the value it is to be set to.
     * @throws javax.management.AttributeNotFoundException
     * @throws javax.management.ReflectionException            Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the MBean's setter.
     * @see #getAttribute
     */
    @Override
    public void setAttribute(final Attribute attributeHolder) throws AttributeNotFoundException, ReflectionException {
        if(attributes.containsKey(attributeHolder.getName())){
            final JmxAttribute attribute = attributes.get(attributeHolder.getName());
            try {
                attribute.setValue(attributeHolder.getValue());
            }
            catch (final Exception e) {
                throw new ReflectionException(e);
            }
        }
        else throw new AttributeNotFoundException(String.format("Attribute %s doesn't exist", attributeHolder.getName()));
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
        final AttributeList result = new AttributeList();
        for(final String attributeName: attributes)
            try {
                result.add(new Attribute(attributeName, getAttribute(attributeName)));
            }
            catch (final JMException e) {
                JmxAdapterHelpers.getLogger().log(Level.WARNING, String.format("Unable to get value of %s attribute", attributeName));
            }
        return result;
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
        final AttributeList result = new AttributeList();
        for(final Object entry: attributes)
            if(entry instanceof Attribute)
                try {
                    setAttribute((Attribute)entry);
                    result.add(entry);
                }
                catch (final JMException e) {
                    JmxAdapterHelpers.getLogger().log(Level.WARNING, String.format("Unable to set attribute %s", entry), e);
                }
        return result;
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
     */
    @Override
    public Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException {
        throw new MBeanException(new UnsupportedOperationException("Operation invocation is not supported."));
    }

    private OpenMBeanAttributeInfoSupport[] getAttributes() {
        final Collection<OpenMBeanAttributeInfoSupport> result = new ArrayList<>(attributes.size());
        for(final String attributeName: attributes.keySet())
            try {
                result.add(attributes.get(attributeName).createFeature(attributeName));
            }
            catch (final OpenDataException e) {
                JmxAdapterHelpers.getLogger().log(Level.WARNING, String.format("Unable to expose attribute %s", attributeName), e);
            }
        return result.toArray(new OpenMBeanAttributeInfoSupport[result.size()]);
    }

    private MBeanNotificationInfo[] getNotifications(){
        return new MBeanNotificationInfo[0];
    }

    /**
     * Provides the exposed attributes and actions of the Dynamic MBean using an MBeanInfo object.
     *
     * @return An instance of <CODE>MBeanInfo</CODE> allowing all attributes and actions
     * exposed by this Dynamic MBean to be retrieved.
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return new OpenMBeanInfoSupport(getClass().getName(),
                String.format("Represents %s resource as MBean", resourceName),
                getAttributes(),
                new OpenMBeanConstructorInfo[0],
                new OpenMBeanOperationInfo[0],
                getNotifications());
    }
}
