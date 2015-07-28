package com.bytex.snamp.adapters.jmx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.adapters.modeling.*;
import com.bytex.snamp.concurrent.ThreadSafeObject;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.notifications.NotificationListenerList;
import com.bytex.snamp.internal.RecordReader;
import com.bytex.snamp.io.Buffers;
import com.bytex.snamp.jmx.WellKnownType;
import com.bytex.snamp.management.OpenMBeanProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.management.*;
import javax.management.NotificationListener;
import javax.management.openmbean.*;
import java.io.Closeable;
import java.lang.management.ManagementFactory;
import java.nio.*;
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
final class ProxyMBean extends ThreadSafeObject implements DynamicMBean, NotificationBroadcaster, NotificationListener, Closeable {
    private enum MBeanResources{
        NOTIFICATIONS,
        ATTRIBUTES
    }

    private static final class ReadOnlyAttributeAccessor extends JmxAttributeAccessor{

        private ReadOnlyAttributeAccessor(final MBeanAttributeInfo metadata) {
            super(metadata);
        }

        @Override
        public OpenMBeanAttributeInfo cloneMetadata() {
            return new OpenMBeanAttributeInfoSupport(getName(),
                    getMetadata().getDescription(),
                    SimpleType.STRING,
                    true,
                    false,
                    false,
                    getMetadata().getDescriptor());
        }

        @Override
        protected Object interceptSet(final Object value) throws InvalidAttributeValueException  {
            throw new InvalidAttributeValueException(String.format("Attribute %s is read-only", getName()));
        }

        @Override
        protected Object interceptGet(final Object value) {
            return Objects.toString(value);
        }
    }

    private static final class OpenTypeAttributeAccessor extends JmxAttributeAccessor{
        private OpenTypeAttributeAccessor(final OpenMBeanAttributeInfo metadata){
            super((MBeanAttributeInfo)metadata);
        }

        @Override
        public OpenMBeanAttributeInfoSupport cloneMetadata() {
            final OpenMBeanAttributeInfo source = (OpenMBeanAttributeInfo)getMetadata();
            return new OpenMBeanAttributeInfoSupport(source.getName(),
                    source.getDescription(),
                    source.getOpenType(),
                    source.isReadable(),
                    source.isWritable(),
                    source.isIs(),
                    getMetadata().getDescriptor());
        }

        @Override
        protected Object interceptSet(final Object value) {
            return value;
        }

        @Override
        protected Object interceptGet(final Object value) {
            return value;
        }
    }

    private static final class BufferAttributeAccessor extends JmxAttributeAccessor{
        private final ArrayType<?> arrayType;

        private BufferAttributeAccessor(final MBeanAttributeInfo metadata,
                                        final Class<? extends Buffer> bufferType) {
            super(metadata);
            this.arrayType = ArrayType.getPrimitiveArrayType(Buffers.getArrayType(bufferType));
        }

        @Override
        public final OpenMBeanAttributeInfoSupport cloneMetadata() {
            return new OpenMBeanAttributeInfoSupport(getMetadata().getName(),
                    getMetadata().getDescription(),
                    arrayType,
                    true,
                    true,
                    false,
                    getMetadata().getDescriptor());
        }

        @Override
        protected Buffer interceptSet(final Object value) throws InvalidAttributeValueException, InterceptionException {
            if(value instanceof byte[])
                return Buffers.wrap((byte[])value);
            else if(value instanceof char[])
                return Buffers.wrap((char[])value);
            else if(value instanceof short[])
                return Buffers.wrap((short[])value);
            else if(value instanceof int[])
                return Buffers.wrap((int[])value);
            else if(value instanceof long[])
                return Buffers.wrap((long[])value);
            else if(value instanceof float[])
                return Buffers.wrap((float[])value);
            else if(value instanceof double[])
                return Buffers.wrap((double[])value);
            else throw new InvalidAttributeValueException(String.format("Unexpected array type %s", value));
        }

        @Override
        protected Object interceptGet(final Object value) throws InterceptionException {
            if(value instanceof Buffer)
                return Buffers.readRemaining((Buffer)value);
            else throw new InterceptionException(new IllegalArgumentException(String.format("Buffer expected but %s found", value)));
        }
    }

    private final ResourceNotificationList<JmxNotificationAccessor> notifications;
    private final ResourceAttributeList<JmxAttributeAccessor> attributes;
    private final NotificationListenerList listeners;
    private final String resourceName;
    private ServiceRegistration<?> registration;

    ProxyMBean(final String resourceName){
        super(MBeanResources.class);
        this.resourceName = resourceName;
        this.notifications = new ResourceNotificationList<>();
        this.attributes = new ResourceAttributeList<>();
        this.listeners = new NotificationListenerList();
        this.registration = null;
    }

    String getResourceName(){
        return resourceName;
    }

    void register(final BundleContext context, final ObjectName beanName){
        registration = context.registerService(DynamicMBean.class, this, OpenMBeanProvider.createIdentity(beanName));
    }

    void register(final ObjectName beanName) throws NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        ManagementFactory.getPlatformMBeanServer().registerMBean(this, beanName);
    }

    void unregister(final ObjectName beanName) throws MBeanRegistrationException, InstanceNotFoundException {
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(beanName);
    }

    private void unregister(){
        if(registration != null) {
            registration.unregister();
        }
        registration = null;
    }

    @Override
    public void close() {
        unregister();
        attributes.clear();
        notifications.clear();
        listeners.clear();
    }

    Iterable<? extends FeatureAccessor<?>> getAccessorsAndClose(){
        final ImmutableList<? extends FeatureAccessor<?>> result =
                ImmutableList.copyOf(Iterables.concat(attributes.values(), notifications.values()));
        close();
        return result;
    }

    NotificationAccessor addNotification(final MBeanNotificationInfo metadata){
        try(final LockScope ignored = beginWrite(MBeanResources.NOTIFICATIONS)){
            final JmxNotificationAccessor result;
            if(notifications.containsKey(metadata))
                result = notifications.get(metadata);
            else notifications.put(result = new JmxNotificationAccessor(resourceName, metadata, listeners));
            return result;
        }
    }

    NotificationAccessor removeNotification(final MBeanNotificationInfo metadata) {
        try (final LockScope ignored = beginWrite(MBeanResources.NOTIFICATIONS)) {
            return notifications.remove(metadata);
        }
    }

    AttributeAccessor addAttribute(final MBeanAttributeInfo metadata){
        try(final LockScope ignored = beginWrite(MBeanResources.ATTRIBUTES)){
            final JmxAttributeAccessor accessor;
            if(attributes.containsKey(metadata))
                accessor = attributes.get(metadata);
            else if(metadata instanceof OpenMBeanAttributeInfo)
                accessor = new OpenTypeAttributeAccessor((OpenMBeanAttributeInfo)metadata);
            else{
                final WellKnownType attributeType = AttributeDescriptor.getType(metadata);
                if(attributeType != null)
                    switch (attributeType){
                        case BYTE_BUFFER:
                            accessor = new BufferAttributeAccessor(metadata, ByteBuffer.class);
                            break;
                        case CHAR_BUFFER:
                            accessor = new BufferAttributeAccessor(metadata, CharBuffer.class);
                            break;
                        case SHORT_BUFFER:
                            accessor = new BufferAttributeAccessor(metadata, ShortBuffer.class);
                            break;
                        case INT_BUFFER:
                            accessor = new BufferAttributeAccessor(metadata, IntBuffer.class);
                            break;
                        case LONG_BUFFER:
                            accessor = new BufferAttributeAccessor(metadata, LongBuffer.class);
                            break;
                        case FLOAT_BUFFER:
                            accessor = new BufferAttributeAccessor(metadata, FloatBuffer.class);
                            break;
                        case DOUBLE_BUFFER:
                            accessor = new BufferAttributeAccessor(metadata, DoubleBuffer.class);
                            break;
                        default:
                            accessor = new ReadOnlyAttributeAccessor(metadata);
                            break;
                    }
                else accessor = new ReadOnlyAttributeAccessor(metadata);
            }
            attributes.put(accessor);
            return accessor;
        }
    }

    AttributeAccessor removeAttribute(final MBeanAttributeInfo metadata){
        try(final LockScope ignored = beginWrite(MBeanResources.ATTRIBUTES)){
            return attributes.remove(metadata);
        }
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
        try(final LockScope ignored = beginRead(MBeanResources.ATTRIBUTES)){
            return attributes.getAttribute(attributeName);
        }
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
        try(final LockScope ignored = beginRead(MBeanResources.ATTRIBUTES)){
            attributes.setAttribute(attributeHolder.getName(), attributeHolder.getValue());
        }
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

    private OpenMBeanAttributeInfo[] getAttributeInfo() {
        try(final LockScope ignored = beginRead(MBeanResources.ATTRIBUTES)){
            final Collection<OpenMBeanAttributeInfo> result = Lists.newArrayListWithExpectedSize(attributes.size());
            for(final JmxAttributeAccessor accessor: attributes.values())
                result.add(accessor.cloneMetadata());
            return ArrayUtils.toArray(result, OpenMBeanAttributeInfo.class);
        }
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        try(final LockScope ignored = beginRead(MBeanResources.NOTIFICATIONS)){
            final Collection<MBeanNotificationInfo> result = Lists.newArrayListWithExpectedSize(notifications.size());
            for(final JmxNotificationAccessor accessor: notifications.values())
                result.add(accessor.cloneMetadata());
            return ArrayUtils.toArray(result, MBeanNotificationInfo.class);
        }
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
        listeners.addNotificationListener(listener, filter, handback);
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
        listeners.removeNotificationListener(listener);
    }

    /**
     * Invoked when a JMX notification occurs.
     * The implementation of this method should return as soon as possible, to avoid
     * blocking its notification broadcaster.
     *
     * @param notification The notification.
     * @param handback     An opaque object which helps the listener to associate
     *                     information regarding the MBean emitter. This object is passed to the
     *                     addNotificationListener call and resent, without modification, to the
     */
    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        listeners.handleNotification(notification, handback);
    }

    <E extends Exception> boolean forEachAttribute(final RecordReader<String, ? super JmxAttributeAccessor, E> attributeReader) throws E {
        try(final LockScope ignored = beginRead(MBeanResources.ATTRIBUTES)){
            for(final JmxAttributeAccessor accessor: attributes.values())
                if(!attributeReader.read(resourceName, accessor)) return false;
        }
        return true;
    }

    <E extends Exception> boolean forEachNotification(final RecordReader<String, ? super JmxNotificationAccessor, E> notificationReader) throws E {
        try(final LockScope ignored = beginRead(MBeanResources.NOTIFICATIONS)){
            for(final JmxNotificationAccessor accessor: notifications.values())
                if(!notificationReader.read(resourceName, accessor)) return false;
        }
        return true;
    }
}
