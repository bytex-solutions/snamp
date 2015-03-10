package com.itworks.snamp.adapters.jmx;

import com.itworks.snamp.management.OpenMBeanProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.management.*;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanConstructorInfo;
import javax.management.openmbean.OpenMBeanInfoSupport;
import javax.management.openmbean.OpenMBeanOperationInfo;
import java.util.logging.Level;

/**
 * Represents proxy MBean that is used to expose attributes and notifications
 * via JMX.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ProxyMBean implements DynamicMBean, NotificationSupport, AttributeSupport {

    private final AttributeSupport attributes;
    private final NotificationSupport notifications;
    private final String resourceName;
    private ServiceRegistration<DynamicMBean> registration;

    ProxyMBean(final String resourceName,
                      final AttributeSupport attributes,
                      final NotificationSupport notifications){
        this.attributes = attributes;
        this.notifications = notifications;
        registration = null;
        this.resourceName = resourceName;
    }

    final void registerAsService(final BundleContext context, final ObjectName beanName){
        context.registerService(DynamicMBean.class, this, OpenMBeanProvider.createIdentity(beanName));
    }

    final void unregister(){
        if(registration != null) registration.unregister();
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
    public Object getAttribute(final String attributeName) throws AttributeNotFoundException, ReflectionException, MBeanException {
        return attributes.getAttribute(attributeName);
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
    public void setAttribute(final Attribute attributeHolder) throws AttributeNotFoundException, ReflectionException, InvalidAttributeValueException, MBeanException {
        setAttribute(attributeHolder.getName(), attributeHolder.getValue());
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
        for (final String attributeName : attributes)
            try {
                result.add(new Attribute(attributeName, getAttribute(attributeName)));
            } catch (final JMException e) {
                JmxAdapterHelpers.log(Level.WARNING, "Unable to get value of %s attribute", attributeName, e);
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
                    JmxAdapterHelpers.log(Level.WARNING,
                            "Unable to set attribute %s",
                            entry,
                            e);
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

    @Override
    public OpenMBeanAttributeInfo[] getAttributeInfo() {
        return attributes.getAttributeInfo();
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        return notifications.getNotificationInfo();
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
                getAttributeInfo(),
                new OpenMBeanConstructorInfo[0],
                new OpenMBeanOperationInfo[0],
                getNotificationInfo());
    }

    /**
     * Adds a listener.
     *
     * @param listener The listener to receive notifications.
     * @param filter   The filter object. If filter is null, no
     *                 filtering will be performed before handling notifications.
     * @param handback An opaque object to be sent back to the
     *                 listener when a notification is emitted. This object cannot be
     *                 used by the Notification broadcaster object. It should be
     *                 resent unchanged with the notification to the listener.
     * @throws IllegalArgumentException thrown if the listener is null.
     * @see #removeNotificationListener
     */
    @Override
    public void addNotificationListener(final NotificationListener listener,
                                        final NotificationFilter filter,
                                        final Object handback) {
        notifications.addNotificationListener(listener, filter, handback);
    }

    /**
     * Removes a listener from this MBean.  If the listener
     * has been registered with different handback objects or
     * notification filters, all entries corresponding to the listener
     * will be removed.
     *
     * @param listener A listener that was previously added to this
     *                 MBean.
     * @throws javax.management.ListenerNotFoundException The listener is not
     *                                                    registered with the MBean.
     * @see #addNotificationListener
     * @see javax.management.NotificationEmitter#removeNotificationListener
     */
    @Override
    public void removeNotificationListener(final NotificationListener listener) throws ListenerNotFoundException {
        notifications.removeNotificationListener(listener);
    }

    /**
     * Set the value of a specific attribute of the Dynamic MBean.
     *
     * @param attributeName The identification of the attribute to
     *                      be set and  the value it is to be set to.
     * @param value         The value of the attribute.
     * @throws javax.management.AttributeNotFoundException
     * @throws javax.management.InvalidAttributeValueException
     * @throws javax.management.MBeanException                 Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's setter.
     * @throws javax.management.ReflectionException            Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the MBean's setter.
     * @see #getAttribute
     */
    @Override
    public void setAttribute(final String attributeName, final Object value) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        attributes.setAttribute(attributeName, value);
    }
}
