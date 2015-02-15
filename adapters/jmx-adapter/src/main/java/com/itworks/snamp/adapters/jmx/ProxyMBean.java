package com.itworks.snamp.adapters.jmx;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.adapters.ReadAttributeLogicalOperation;
import com.itworks.snamp.adapters.WriteAttributeLogicalOperation;
import com.itworks.snamp.core.LogicalOperation;
import com.itworks.snamp.internal.AbstractKeyedObjects;
import com.itworks.snamp.internal.KeyedObjects;
import com.itworks.snamp.jmx.DescriptorUtils;
import com.itworks.snamp.jmx.NotificationFilterBuilder;
import com.itworks.snamp.management.jmx.OpenMBeanProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.management.*;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanConstructorInfo;
import javax.management.openmbean.OpenMBeanInfoSupport;
import javax.management.openmbean.OpenMBeanOperationInfo;
import java.util.Collection;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Represents proxy MBean that is used to expose attributes and notifications
 * via JMX.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ProxyMBean extends NotificationBroadcasterSupport implements DynamicMBean {
    /**
     * The name of the field in descriptor which contains the name of the
     * associated resource.
     */
    static final String RESOURCE_NAME_FIELD = "resourceName";

    private final KeyedObjects<String, JmxAttributeMapping> attributes;
    private final NotificationBroadcaster notifications;
    private final String resourceName;
    private ServiceRegistration<DynamicMBean> registration;
    private final NotificationFilter filterByResource;

    ProxyMBean(final String resourceName,
                      final Collection<JmxAttributeMapping> attributes,
                      final NotificationBroadcaster notifications){
        this.attributes = new AbstractKeyedObjects<String, JmxAttributeMapping>(attributes.size()) {
            @Override
            public String getKey(final JmxAttributeMapping item) {
                return item.getAttributeInfo().getName();
            }
        };
        //filter out attributes
        for(final JmxAttributeMapping mapping: attributes)
            if(Objects.equals(resourceName, DescriptorUtils.getField(mapping.getAttributeInfo().getDescriptor(), RESOURCE_NAME_FIELD, String.class)))
                this.attributes.put(mapping);
        this.notifications = notifications;
        registration = null;
        filterByResource = createFilterByResource(this.resourceName = resourceName);
    }

    private static NotificationFilter createFilterByResource(final String resourceName){
        return new NotificationFilter() {
            @Override
            public boolean isNotificationEnabled(final Notification notification) {
                return notification.getType().contains(resourceName);
            }
        };
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
        if (attributes.containsKey(attributeName)) {
            final JmxAttributeMapping attribute = attributes.get(attributeName);
            try (final LogicalOperation ignored = new ReadAttributeLogicalOperation(attribute.getOriginalName(), attributeName)) {
                return attribute.getValue();
            }
        } else throw new AttributeNotFoundException(String.format("Attribute %s doesn't exist", attributeName));
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
        if (attributes.containsKey(attributeHolder.getName())) {
            final JmxAttributeMapping attribute = attributes.get(attributeHolder.getName());
            try (final LogicalOperation ignored = new WriteAttributeLogicalOperation(attribute.getOriginalName(), attributeHolder.getName())) {
                attribute.setValue(attributeHolder.getValue());
            }
        } else
            throw new AttributeNotFoundException(String.format("Attribute %s doesn't exist", attributeHolder.getName()));
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

    public OpenMBeanAttributeInfoSupport[] getAttributeInfo() {
        return ArrayUtils.toArray(Collections2.transform(attributes.values(), new Function<JmxAttributeMapping, OpenMBeanAttributeInfoSupport>() {
            @Override
            public OpenMBeanAttributeInfoSupport apply(final JmxAttributeMapping attribute) {
                return attribute.getAttributeInfo();
            }
        }), OpenMBeanAttributeInfoSupport.class);
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        return ArrayUtils.filter(notifications.getNotificationInfo(), new Predicate<MBeanNotificationInfo>() {
            @Override
            public boolean apply(final MBeanNotificationInfo info) {
                return Objects.equals(resourceName, DescriptorUtils.getField(info.getDescriptor(), ProxyMBean.RESOURCE_NAME_FIELD, String.class));
            }
        }, MBeanNotificationInfo.class);
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
        notifications.addNotificationListener(listener,
                new NotificationFilterBuilder(filter).and(filterByResource).build(),
                handback);
    }
}
