package com.bytex.snamp.gateway.jmx;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.EntryReader;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.LockDecorator;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.notifications.NotificationListenerList;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.gateway.modeling.*;
import com.bytex.snamp.io.Buffers;
import com.bytex.snamp.jmx.OpenMBeanServiceProvider;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.collect.ImmutableList;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.management.*;
import javax.management.openmbean.*;
import java.io.Closeable;
import java.lang.management.ManagementFactory;
import java.nio.*;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Represents proxy MBean that is used to expose attributes and notifications
 * via JMX.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class ProxyMBean implements DynamicMBean, NotificationBroadcaster, Closeable {
    private static final class OpenOperationAccessor extends JmxOperationAccessor{
        OpenOperationAccessor(final OpenMBeanOperationInfo metadata) {
            super((MBeanOperationInfo) metadata);
        }

        private static OpenMBeanParameterInfo[] getSignature(final OpenMBeanOperationInfo metadata){
            final Class<OpenMBeanParameterInfo> expectedType = OpenMBeanParameterInfo.class;
            return Arrays.stream(metadata.getSignature())
                    .filter(expectedType::isInstance)
                    .map(expectedType::cast)
                    .toArray(OpenMBeanParameterInfo[]::new);
        }

        @Override
        public OpenMBeanOperationInfoSupport cloneMetadata() {
            final OpenMBeanOperationInfo metadata = (OpenMBeanOperationInfo) getMetadata();
            return new OpenMBeanOperationInfoSupport(metadata.getName(),
                    metadata.getDescription(),
                    getSignature(metadata),
                    metadata.getReturnOpenType(),
                    metadata.getImpact(),
                    cloneDescriptor());
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
                    cloneDescriptor());
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
        public OpenMBeanAttributeInfoSupport cloneMetadata() {
            return new OpenMBeanAttributeInfoSupport(getMetadata().getName(),
                    getMetadata().getDescription(),
                    arrayType,
                    true,
                    true,
                    false,
                    cloneDescriptor());
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
    private final ResourceOperationList<JmxOperationAccessor> operations;
    private final NotificationListenerList listeners;
    private final String resourceName;
    private ServiceRegistration<?> registration;
    private final LockDecorator attributesReadLock, attributesWriteLock;
    private final LockDecorator notificationsReadLock, notificationsWriteLock;
    private final LockDecorator operationsReadLock, operationsWriteLock;

    ProxyMBean(final String resourceName){
        this.resourceName = resourceName;
        this.notifications = new ResourceNotificationList<>();
        this.attributes = new ResourceAttributeList<>();
        this.operations = new ResourceOperationList<>();
        this.listeners = new NotificationListenerList();
        this.registration = null;

        ReadWriteLock rwLock = new ReentrantReadWriteLock();
        attributesReadLock = LockDecorator.readLock(rwLock);
        attributesWriteLock = LockDecorator.writeLock(rwLock);

        rwLock = new ReentrantReadWriteLock();
        operationsReadLock = LockDecorator.readLock(rwLock);
        operationsWriteLock = LockDecorator.writeLock(rwLock);

        rwLock = new ReentrantReadWriteLock();
        notificationsReadLock = LockDecorator.readLock(rwLock);
        notificationsWriteLock = LockDecorator.writeLock(rwLock);
    }

    String getResourceName(){
        return resourceName;
    }

    void register(final BundleContext context, final ObjectName beanName){
        registration = context.registerService(DynamicMBean.class, this, OpenMBeanServiceProvider.createIdentity(beanName));
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
        operations.clear();
        listeners.clear();
    }

    Stream<? extends FeatureAccessor<?>> getAccessorsAndClose(){
        final ImmutableList<JmxAttributeAccessor> attributes = ImmutableList.copyOf(this.attributes.values());
        final ImmutableList<JmxNotificationAccessor> notifications = ImmutableList.copyOf(this.notifications.values());
        final ImmutableList<JmxOperationAccessor> operations = ImmutableList.copyOf(this.operations.values());
        final Stream<? extends FeatureAccessor<?>> result = Stream.concat(
                Stream.concat(attributes.stream(), notifications.stream()),operations.stream());
        close();
        return result;
    }

    private NotificationAccessor addNotificationImpl(final MBeanNotificationInfo metadata){
        final JmxNotificationAccessor result;
        if(notifications.containsKey(metadata))
            result = notifications.get(metadata);
        else notifications.put(result = new JmxNotificationAccessor(resourceName, metadata, listeners));
        return result;
    }

    NotificationAccessor addNotification(final MBeanNotificationInfo metadata){
        return notificationsWriteLock.apply(metadata, this::addNotificationImpl);
    }

    NotificationAccessor removeNotification(final MBeanNotificationInfo metadata) {
        return notificationsWriteLock.apply(metadata, (Function<MBeanNotificationInfo, NotificationAccessor>) notifications::remove);
    }


    private OperationAccessor addOperationImpl(final MBeanOperationInfo metadata){
        final JmxOperationAccessor result;
        if(operations.containsKey(metadata))
            result = operations.get(metadata);
        else if(metadata instanceof OpenMBeanOperationInfo)
            operations.put(result = new OpenOperationAccessor((OpenMBeanOperationInfo) metadata));
        else
            operations.put(result = new JmxOperationAccessor(metadata));
        return result;
    }

    OperationAccessor addOperation(final MBeanOperationInfo metadata){
        return operationsWriteLock.apply(metadata, this::addOperationImpl);
    }

    OperationAccessor removeOperation(final MBeanOperationInfo metadata) {
        return operationsWriteLock.apply(metadata, (Function<MBeanOperationInfo, OperationAccessor>) operations::remove);
    }

    private AttributeAccessor addAttributeImpl(final MBeanAttributeInfo metadata){
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
                        accessor = new JmxAttributeAccessor(metadata);
                        break;
                }
            else accessor = new JmxAttributeAccessor(metadata);
        }
        attributes.put(accessor);
        return accessor;
    }

    AttributeAccessor addAttribute(final MBeanAttributeInfo metadata){
        return attributesWriteLock.apply(this, metadata, ProxyMBean::addAttributeImpl);
    }

    AttributeAccessor removeAttribute(final MBeanAttributeInfo metadata){
        return attributesWriteLock.apply(attributes, metadata, ResourceAttributeList::remove);
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
        try {
            return attributesReadLock.call(() -> attributes.getAttribute(attributeName), null);
        } catch (final AttributeNotFoundException | ReflectionException | MBeanException e) {
            throw e;
        } catch (final Exception e) {
            throw new ReflectionException(e);
        }
    }

    private void setAttributeImpl(final Attribute attributeHolder) throws JMException{
        attributes.setAttribute(attributeHolder.getName(), attributeHolder.getValue());
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
        try {
            attributesReadLock.accept(attributeHolder, this::setAttributeImpl, (Duration) null);
        } catch (final AttributeNotFoundException | ReflectionException | InvalidAttributeValueException | MBeanException e) {
            throw e;
        } catch (final Exception e) {
            throw new ReflectionException(e);
        }
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
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
                getLogger().log(Level.WARNING, String.format("Unable to get value of %s attribute", attributeName), e);
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
        attributes.stream()
                .filter(entry -> entry instanceof Attribute)
                .forEach(entry -> {
                    try {
                        setAttribute((Attribute) entry);
                        result.add(entry);
                    } catch (final JMException e) {
                        getLogger().log(Level.WARNING,
                                String.format("Unable to set attribute %s",
                                        entry),
                                e);
                    }
        });
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
    public Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
        try {
            return operationsReadLock.call(() -> operations.invoke(actionName, params, signature));
        } catch (final ReflectionException | MBeanException e) {
            throw e;
        } catch (final Exception e) {
            throw new ReflectionException(e);
        }
    }

    private MBeanAttributeInfo[] getAttributeInfo() {
        return attributesReadLock.apply(attributes,
                attrs -> attrs.values().stream()
                        .map(JmxAttributeAccessor::cloneMetadata)
                        .toArray(MBeanAttributeInfo[]::new));
    }

    private MBeanOperationInfo[] getOperationInfo() {
        return operationsReadLock.apply(operations,
                opers -> opers.values().stream()
                        .map(JmxOperationAccessor::cloneMetadata)
                        .toArray(MBeanOperationInfo[]::new));
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        return notificationsReadLock.apply(notifications,
                notifs -> notifs.values().stream()
                        .map(JmxNotificationAccessor::cloneMetadata)
                        .toArray(MBeanNotificationInfo[]::new));
    }

    /**
     * Provides the exposed attributes and actions of the Dynamic MBean using an MBeanInfo object.
     *
     * @return An instance of <CODE>MBeanInfo</CODE> allowing all attributes and actions
     * exposed by this Dynamic MBean to be retrieved.
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return new MBeanInfo(getClass().getName(),
                String.format("Represents %s resource as MBean", resourceName),
                getAttributeInfo(),
                ArrayUtils.emptyArray(MBeanConstructorInfo[].class),
                getOperationInfo(),
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

    <E extends Throwable> boolean forEachAttribute(final EntryReader<String, ? super JmxAttributeAccessor, E> attributeReader) throws E {
        try (final SafeCloseable ignored = attributesReadLock.acquireLock()) {
            for (final JmxAttributeAccessor accessor : attributes.values())
                if (!attributeReader.accept(resourceName, accessor))
                    return false;
            return true;
        }
    }

    <E extends Throwable> boolean forEachNotification(final EntryReader<String, ? super JmxNotificationAccessor, E> notificationReader) throws E {
        try(final SafeCloseable ignored = notificationsReadLock.acquireLock()){
            for(final JmxNotificationAccessor accessor: notifications.values())
                if(!notificationReader.accept(resourceName, accessor))
                    return false;
            return true;
        }
    }

    <E extends Throwable> boolean forEachOperation(final EntryReader<String, ? super JmxOperationAccessor, E> operationReader) throws E {
        try(final SafeCloseable ignored = operationsReadLock.acquireLock()){
            for(final JmxOperationAccessor accessor: operations.values())
                if(!operationReader.accept(resourceName, accessor))
                    return false;
            return true;
        }
    }
}
