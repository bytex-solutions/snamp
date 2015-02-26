package com.itworks.snamp.adapters.jmx;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.concurrent.ThreadSafeObject;
import com.itworks.snamp.connectors.attributes.CustomAttributeInfo;
import com.itworks.snamp.connectors.notifications.NotificationListenerList;
import com.itworks.snamp.internal.AbstractKeyedObjects;
import com.itworks.snamp.internal.KeyedObjects;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.io.Buffers;
import com.itworks.snamp.jmx.JMExceptionUtils;
import com.itworks.snamp.jmx.WellKnownType;
import com.itworks.snamp.licensing.LicensingException;

import javax.management.*;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.SimpleType;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.nio.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.adapters.jmx.JmxAdapterConfigurationProvider.OBJECT_NAME_PARAM;
import static com.itworks.snamp.adapters.jmx.JmxAdapterConfigurationProvider.USE_PLATFORM_MBEAN_PARAM;

/**
 * Represents JMX adapter. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxResourceAdapter extends AbstractResourceAdapter {
    static final String NAME = JmxAdapterHelpers.ADAPTER_NAME;

    private static final class ResourceNotificationManager extends NotificationListenerList implements NotificationSupport{
        private static final String ID_SEPARATOR = ".";
        private final KeyedObjects<String, MBeanNotificationInfo> notifications;

        private ResourceNotificationManager(){
            this.notifications = createNotifs();
        }

        private static KeyedObjects<String, MBeanNotificationInfo> createNotifs(){
            return new AbstractKeyedObjects<String, MBeanNotificationInfo>(10) {
                @Override
                public String getKey(final MBeanNotificationInfo item) {
                    return item.getNotifTypes()[0];
                }
            };
        }

        @Override
        public MBeanNotificationInfo[] getNotificationInfo() {
            return ArrayUtils.toArray(notifications.values(), MBeanNotificationInfo.class);
        }

        private static String makeListID(final String adapterInstanceName,
                                                   final String category){
            return category + ID_SEPARATOR + adapterInstanceName;
        }

        private void addNotification(final String adapterInstanceName,
                                     final String category,
                                     final NotificationConnector connector) throws JMException {
            MBeanNotificationInfo metadata = connector.enable(makeListID(adapterInstanceName, category));
            //clone metadata because resource connector may return unserializable metadata
            metadata = new MBeanNotificationInfo(metadata.getNotifTypes(),
                    metadata.getName(),
                    metadata.getDescription(),
                    metadata.getDescriptor());
            notifications.put(metadata);
        }

        private MBeanNotificationInfo removeNotification(final String adapterInstanceName,
                                        final String category){
            return notifications.remove(makeListID(adapterInstanceName, category));
        }

        private boolean hasNoNotifications(){
            return notifications.isEmpty();
        }

        @Override
        public void handleNotification(final Notification notification,
                                       final Object handback) {
            if(notifications.containsKey(notification.getType()))
                super.handleNotification(notification, handback);
        }
    }

    private static final class JmxNotifications extends ThreadSafeObject implements NotificationsModel  {
        //key is a name of the resource, values - enabled notifications
        private final Map<String, ResourceNotificationManager> notifications;
        private final String adapterInstanceName;

        private JmxNotifications(final String adapterInstanceName){
            this.adapterInstanceName = adapterInstanceName;
            this.notifications = new HashMap<>(10);
        }

        private ResourceNotificationManager getNotificationManager(final String resourceName){
            beginRead();
            try{
                return notifications.get(resourceName);
            }
            finally {
                endRead();
            }
        }

        @Override
        public void addNotification(final String resourceName,
                                    final String category,
                                    final NotificationConnector connector) {
            beginWrite();
            try {
                final ResourceNotificationManager manager;
                if(notifications.containsKey(resourceName))
                    manager = notifications.get(resourceName);
                else notifications.put(resourceName, manager = new ResourceNotificationManager());
                manager.addNotification(adapterInstanceName, category, connector);
            }
            catch (final JMException e){
                JmxAdapterHelpers.log(Level.SEVERE, "Failed to enable notification %s:%s", resourceName, category, e);
            }
            finally {
                endWrite();
            }
        }

        @Override
        public MBeanNotificationInfo removeNotification(final String resourceName,
                                                        final String category) {
            beginWrite();
            try{
                final ResourceNotificationManager manager;
                if(notifications.containsKey(resourceName))
                    manager = notifications.get(resourceName);
                else return null;
                final MBeanNotificationInfo metadata = manager.removeNotification(adapterInstanceName, category);
                if(manager.hasNoNotifications()){
                    manager.clear();
                    notifications.remove(resourceName);
                }
                return metadata;
            }
            finally {
                endWrite();
            }
        }

        /**
         * Removes all notifications from this model.
         */
        @Override
        public void clear() {
            beginWrite();
            try{
                notifications.clear();
            }
            finally {
                endWrite();
            }
        }

        /**
         * Determines whether this model is empty.
         *
         * @return {@literal true}, if this model is empty; otherwise, {@literal false}.
         */
        @Override
        public boolean isEmpty() {
            beginRead();
            try {
                return notifications.isEmpty();
            }
            finally {
                endRead();
            }
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
            beginRead();
            try{
                for(final ResourceNotificationManager manager: notifications.values())
                    manager.handleNotification(notification, handback);
            }
            finally {
                endRead();
            }
        }
    }

    private static abstract class AbstractAttributeMapping{
        protected final AttributeAccessor accessor;
        private final OpenMBeanAttributeInfoSupport metadata;

        private AbstractAttributeMapping(final AttributeAccessor accessor,
                                        final OpenMBeanAttributeInfoSupport metadata){
            this.accessor = accessor;
            this.metadata = metadata;
        }

        private OpenMBeanAttributeInfo getAttributeInfo() {
            return metadata;
        }

        abstract Object getValue() throws MBeanException, ReflectionException, AttributeNotFoundException;

        abstract void setValue(final Object value) throws MBeanException, ReflectionException, AttributeNotFoundException, InvalidAttributeValueException;
    }

    private static final class OpenTypeAttributeMapping extends AbstractAttributeMapping{

        private OpenTypeAttributeMapping(final AttributeAccessor accessor) {
            super(accessor, createMetadata((OpenMBeanAttributeInfo)accessor.getMetadata()));
        }

        private static OpenMBeanAttributeInfoSupport createMetadata(final OpenMBeanAttributeInfo source){
            return new OpenMBeanAttributeInfoSupport(source.getName(),
                    source.getDescription(),
                    source.getOpenType(),
                    source.isReadable(),
                    source.isWritable(),
                    source.isIs(),
                    source instanceof MBeanAttributeInfo ? ((MBeanAttributeInfo)source).getDescriptor() : new ImmutableDescriptor());
        }

        @Override
        public Object getValue() throws MBeanException, ReflectionException, AttributeNotFoundException {
            return accessor.getValue();
        }

        @Override
        public void setValue(final Object value) throws MBeanException, ReflectionException, AttributeNotFoundException, InvalidAttributeValueException {
            accessor.setValue(value);
        }
    }

    private static abstract class BufferAttributeMapping<T> extends AbstractAttributeMapping{
        private BufferAttributeMapping(final AttributeAccessor accessor,
                                           final Class<T> arrayType) {
            super(accessor, createMetadata(accessor.getMetadata(), arrayType));
        }

        private static OpenMBeanAttributeInfoSupport createMetadata(final MBeanAttributeInfo source,
                                                                    final Class<?> arrayType) {
            return new OpenMBeanAttributeInfoSupport(source.getName(),
                    source.getDescription(),
                    ArrayType.getPrimitiveArrayType(arrayType),
                    true,
                    true,
                    false,
                    source.getDescriptor());
        }

        @Override
        public abstract T getValue() throws MBeanException, ReflectionException, AttributeNotFoundException;
    }

    private static final class ByteBufferAttributeMapping extends BufferAttributeMapping<byte[]>{
        private ByteBufferAttributeMapping(final AttributeAccessor accessor) {
            super(accessor, byte[].class);
        }

        @Override
        public byte[] getValue() throws MBeanException, ReflectionException, AttributeNotFoundException {
            try {
                return Buffers.readRemaining(accessor.getValue(ByteBuffer.class));
            } catch (final InvalidAttributeValueException e) {
                throw new ReflectionException(e);
            }
        }

        @Override
        public void setValue(final Object value) throws MBeanException, ReflectionException, AttributeNotFoundException, InvalidAttributeValueException {
            if(value instanceof byte[])
                accessor.setValue(Buffers.wrap((byte[])value));
            else throw new InvalidAttributeValueException("byte[] expected");
        }
    }

    private static final class CharBufferAttributeMapping extends BufferAttributeMapping<char[]>{
        private CharBufferAttributeMapping(final AttributeAccessor accessor) {
            super(accessor, char[].class);
        }

        @Override
        public char[] getValue() throws MBeanException, ReflectionException, AttributeNotFoundException {
            try {
                return Buffers.readRemaining(accessor.getValue(CharBuffer.class));
            } catch (final InvalidAttributeValueException e) {
                throw new ReflectionException(e);
            }
        }

        @Override
        public void setValue(final Object value) throws MBeanException, ReflectionException, AttributeNotFoundException, InvalidAttributeValueException {
            if(value instanceof char[])
                accessor.setValue(Buffers.wrap((char[])value));
            else throw new InvalidAttributeValueException("char[] expected");
        }
    }

    private static final class ShortBufferAttributeMapping extends BufferAttributeMapping<short[]>{
        private ShortBufferAttributeMapping(final AttributeAccessor accessor) {
            super(accessor, short[].class);
        }

        @Override
        public short[] getValue() throws MBeanException, ReflectionException, AttributeNotFoundException {
            try {
                return Buffers.readRemaining(accessor.getValue(ShortBuffer.class));
            } catch (final InvalidAttributeValueException e) {
                throw new ReflectionException(e);
            }
        }

        @Override
        public void setValue(final Object value) throws MBeanException, ReflectionException, AttributeNotFoundException, InvalidAttributeValueException {
            if(value instanceof short[])
                accessor.setValue(Buffers.wrap((short[])value));
            else throw new InvalidAttributeValueException("short[] expected");
        }
    }

    private static final class IntBufferAttributeMapping extends BufferAttributeMapping<int[]>{
        private IntBufferAttributeMapping(final AttributeAccessor accessor) {
            super(accessor, int[].class);
        }

        @Override
        public int[] getValue() throws MBeanException, ReflectionException, AttributeNotFoundException {
            try {
                return Buffers.readRemaining(accessor.getValue(IntBuffer.class));
            } catch (final InvalidAttributeValueException e) {
                throw new ReflectionException(e);
            }
        }

        @Override
        public void setValue(final Object value) throws MBeanException, ReflectionException, AttributeNotFoundException, InvalidAttributeValueException {
            if(value instanceof int[])
                accessor.setValue(Buffers.wrap((int[])value));
            else throw new InvalidAttributeValueException("int[] expected");
        }
    }

    private static final class LongBufferAttributeMapping extends BufferAttributeMapping<long[]>{
        private LongBufferAttributeMapping(final AttributeAccessor accessor) {
            super(accessor, long[].class);
        }

        @Override
        public long[] getValue() throws MBeanException, ReflectionException, AttributeNotFoundException {
            try {
                return Buffers.readRemaining(accessor.getValue(LongBuffer.class));
            } catch (final InvalidAttributeValueException e) {
                throw new ReflectionException(e);
            }
        }

        @Override
        public void setValue(final Object value) throws MBeanException, ReflectionException, AttributeNotFoundException, InvalidAttributeValueException {
            if(value instanceof long[])
                accessor.setValue(Buffers.wrap((long[])value));
            else throw new InvalidAttributeValueException("long[] expected");
        }
    }

    private static final class FloatBufferAttributeMapping extends BufferAttributeMapping<float[]>{
        private FloatBufferAttributeMapping(final AttributeAccessor accessor) {
            super(accessor, float[].class);
        }

        @Override
        public float[] getValue() throws MBeanException, ReflectionException, AttributeNotFoundException {
            try {
                return Buffers.readRemaining(accessor.getValue(FloatBuffer.class));
            } catch (final InvalidAttributeValueException e) {
                throw new ReflectionException(e);
            }
        }

        @Override
        public void setValue(final Object value) throws MBeanException, ReflectionException, AttributeNotFoundException, InvalidAttributeValueException {
            if(value instanceof float[])
                accessor.setValue(Buffers.wrap((float[])value));
            else throw new InvalidAttributeValueException("float[] expected");
        }
    }

    private static final class DoubleBufferAttributeMapping extends BufferAttributeMapping<double[]>{
        private DoubleBufferAttributeMapping(final AttributeAccessor accessor) {
            super(accessor, double[].class);
        }

        @Override
        public double[] getValue() throws MBeanException, ReflectionException, AttributeNotFoundException {
            try {
                return Buffers.readRemaining(accessor.getValue(DoubleBuffer.class));
            } catch (final InvalidAttributeValueException e) {
                throw new ReflectionException(e);
            }
        }

        @Override
        public void setValue(final Object value) throws MBeanException, ReflectionException, AttributeNotFoundException, InvalidAttributeValueException {
            if(value instanceof double[])
                accessor.setValue(Buffers.wrap((double[])value));
            else throw new InvalidAttributeValueException("double[] expected");
        }
    }

    private static final class ReadOnlyAttributeMapping extends AbstractAttributeMapping{
        private ReadOnlyAttributeMapping(final AttributeAccessor accessor){
            super(accessor, createMetadata(accessor.getMetadata()));
        }

        private static OpenMBeanAttributeInfoSupport createMetadata(final MBeanAttributeInfo source){
            return new OpenMBeanAttributeInfoSupport(source.getName(),
                    source.getDescription(),
                    SimpleType.STRING,
                    true,
                    false,
                    false,
                    source.getDescriptor());
        }

        @Override
        public String getValue() throws MBeanException, ReflectionException, AttributeNotFoundException {
            return Objects.toString(accessor.getValue(), "");
        }

        @Override
        public void setValue(final Object value) throws MBeanException, ReflectionException, AttributeNotFoundException, InvalidAttributeValueException {
            throw new MBeanException(new UnsupportedOperationException("Not supported"));
        }
    }

    private static final class ResourceAttributeManager extends AbstractKeyedObjects<String, AbstractAttributeMapping> implements AttributeSupport{
        private static final String ID_SEPARATOR = "/";

        private ResourceAttributeManager(){
            super(10);
        }

        private static String makeAttributeID(final String adapterInstanceName,
                                              final String attributeName) {
            return adapterInstanceName + ID_SEPARATOR + attributeName;
        }

        @Override
        public void clear() {
            for(final AbstractAttributeMapping mapping: values())
                mapping.accessor.disconnect();
            super.clear();
        }

        @Override
        public OpenMBeanAttributeInfo[] getAttributeInfo() {
            return ArrayUtils.toArray(Collections2.transform(values(), new Function<AbstractAttributeMapping, OpenMBeanAttributeInfo>() {
                @Override
                public OpenMBeanAttributeInfo apply(final AbstractAttributeMapping mapping) {
                    return mapping.getAttributeInfo();
                }
            }), OpenMBeanAttributeInfo.class);
        }

        @Override
        public Object getAttribute(final String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
            final AbstractAttributeMapping mapping = get(attribute);
            if(mapping == null) throw JMExceptionUtils.attributeNotFound(attribute);
            else return mapping.getValue();
        }

        @Override
        public void setAttribute(final String attributeName, final Object value) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
            final AbstractAttributeMapping mapping = get(attributeName);
            if(mapping == null) throw JMExceptionUtils.attributeNotFound(attributeName);
            else mapping.setValue(value);
        }

        private static AbstractAttributeMapping createMapping(final AttributeAccessor accessor){
            if(accessor.getMetadata() instanceof OpenMBeanAttributeInfo)
                return new OpenTypeAttributeMapping(accessor);
            //try to detect the Buffer
            final WellKnownType type = CustomAttributeInfo.getType(accessor.getMetadata());
            if(type != null)
                switch (type){
                    case BYTE_BUFFER:
                        return new ByteBufferAttributeMapping(accessor);
                    case CHAR_BUFFER:
                        return new CharBufferAttributeMapping(accessor);
                    case SHORT_BUFFER:
                        return new ShortBufferAttributeMapping(accessor);
                    case INT_BUFFER:
                        return new IntBufferAttributeMapping(accessor);
                    case LONG_BUFFER:
                        return new LongBufferAttributeMapping(accessor);
                    case FLOAT_BUFFER:
                        return new FloatBufferAttributeMapping(accessor);
                    case DOUBLE_BUFFER:
                        return  new DoubleBufferAttributeMapping(accessor);
                }
            return new ReadOnlyAttributeMapping(accessor);
        }

        private void addAttribute(final String adapterInstanceName,
                                  final String attributeName,
                                  final AttributeConnector connector) throws JMException {
            final AbstractAttributeMapping mapping = createMapping(connector.connect(makeAttributeID(adapterInstanceName, attributeName)));
            put(mapping);
        }

        private AttributeAccessor removeAttribute(final String adapterInstanceName,
                                                  final String attributeName){
            final AbstractAttributeMapping mapping = remove(makeAttributeID(adapterInstanceName,
                    attributeName));
            return mapping != null ? mapping.accessor : null;
        }

        @Override
        public String getKey(final AbstractAttributeMapping item) {
            return item.getAttributeInfo().getName();
        }
    }

    private static final class JmxAttributes extends ThreadSafeObject implements AttributesModel{
        //key is a name of the resource, values - connected attributes
        private final Map<String, ResourceAttributeManager> attributes;
        private final String adapterInstanceName;

        private JmxAttributes(final String adapterInstanceName){
            this.adapterInstanceName = adapterInstanceName;
            this.attributes = new HashMap<>(10);
        }

        private AttributeSupport getAttributeManager(final String resourceName) {
            beginRead();
            try{
                return attributes.get(resourceName);
            }
            finally {
                endRead();
            }
        }

        @Override
        public void addAttribute(final String resourceName,
                                 final String attributeName,
                                 final AttributeConnector connector) {
            beginWrite();
            try{
                final ResourceAttributeManager manager;
                if(attributes.containsKey(resourceName))
                    manager = attributes.get(resourceName);
                else attributes.put(resourceName, manager = new ResourceAttributeManager());
                manager.addAttribute(adapterInstanceName, attributeName, connector);
            } catch (final JMException e) {
                JmxAdapterHelpers.log(Level.SEVERE, "Unable to connect attribute %s:%s", resourceName, attributeName, e);
            } finally {
                endWrite();
            }
        }

        @Override
        public AttributeAccessor removeAttribute(final String resourceName,
                                                 final String attributeName) {
            beginWrite();
            try{
                final ResourceAttributeManager manager;
                if(attributes.containsKey(resourceName))
                    manager = attributes.get(resourceName);
                else return null;
                final AttributeAccessor accessor = manager.removeAttribute(adapterInstanceName, attributeName);
                if(manager.isEmpty())
                    attributes.remove(resourceName);
                return accessor;
            }
            finally {
                endWrite();
            }
        }

        @Override
        public void clear() {
            beginWrite();
            try{
                for(final ResourceAttributeManager manager: attributes.values())
                    manager.clear();
                attributes.clear();
            }
            finally {
                endWrite();
            }
        }

        @Override
        public boolean isEmpty() {
            return attributes.isEmpty();
        }
    }

    private final JmxAttributes attributes;
    private final JmxNotifications notifications;
    private final Map<ObjectName, ProxyMBean> exposedBeans;
    private boolean usePlatformMBean;

    JmxResourceAdapter(final String adapterInstanceName) {
        super(adapterInstanceName);
        this.attributes = new JmxAttributes(adapterInstanceName);
        this.exposedBeans = new HashMap<>(10);
        this.notifications = new JmxNotifications(adapterInstanceName);
        this.usePlatformMBean = false;
    }

    private static ObjectName createObjectName(final ObjectName rootObjectName, final String resourceName) throws MalformedObjectNameException {
        final Hashtable<String, String> attrs = new Hashtable<>(rootObjectName.getKeyPropertyList());
        attrs.put("resource", resourceName);
        return new ObjectName(rootObjectName.getDomain(), attrs);
    }

    private void start(final ObjectName rootObjectName) throws Exception{
        attributes.clear();
        notifications.clear();
        populateModel(attributes);
        try {
            JmxAdapterLicenseLimitations.current().verifyJmxNotificationsFeature();
            populateModel(notifications);
        } catch (final LicensingException e) {
            JmxAdapterHelpers.log(Level.INFO, "JMX notifications are not allowed by your SNAMP license", e);
            notifications.clear();
        }
        for (final String resourceName : getHostedResources())
            try {
                final ProxyMBean bean = new ProxyMBean(resourceName,
                        attributes.getAttributeManager(resourceName), notifications.getNotificationManager(resourceName));
                final ObjectName beanName = createObjectName(rootObjectName, resourceName);
                //register bean
                if(usePlatformMBean)
                    ManagementFactory.getPlatformMBeanServer().registerMBean(bean, beanName);
                else
                    bean.registerAsService(Utils.getBundleContextByObject(this), beanName);
                exposedBeans.put(beanName, bean);
                JmxAdapterHelpers.log(Level.FINE, "Bean %s is registered by %s adapter", beanName, getInstanceName(), null);
            } catch (final JMException e) {
                JmxAdapterHelpers.log(Level.SEVERE, "Unable to register MBean for resource %s", resourceName, e);
            }
    }

    @Override
    protected void start(final Map<String, String> parameters) throws Exception{
        if (parameters.containsKey(OBJECT_NAME_PARAM)) {
            JmxAdapterLicenseLimitations.current().verifyServiceVersion(JmxResourceAdapter.class);
            final ObjectName rootObjectName = new ObjectName(parameters.get(OBJECT_NAME_PARAM));
            usePlatformMBean = parameters.containsKey(USE_PLATFORM_MBEAN_PARAM) &&
                    Boolean.valueOf(parameters.get(USE_PLATFORM_MBEAN_PARAM));
            start(rootObjectName);
        }
        else throw new MalformedURLException(String.format("Adapter configuration has no %s entry", OBJECT_NAME_PARAM));
    }

    /**
     * Stops the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     */
    @Override
    protected void stop(){
        clearModel(attributes);
        clearModel(notifications);
        for (final ObjectName name : exposedBeans.keySet())
            try {
                if (usePlatformMBean)
                    ManagementFactory.getPlatformMBeanServer().unregisterMBean(name);
                else
                    exposedBeans.get(name).unregister();
            } catch (final JMException e) {
                JmxAdapterHelpers.log(Level.SEVERE, "Unable to unregister MBean %s", name, e);
            }
        exposedBeans.clear();
        System.gc();
    }

    /**
     * Gets withLogger associated with this service.
     *
     * @return The withLogger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return getLogger(NAME);
    }
}
