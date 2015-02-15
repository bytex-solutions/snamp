package com.itworks.snamp.adapters.jmx;

import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.concurrent.ConcurrentResourceAccessor;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.CustomAttributeInfo;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.io.Buffers;
import com.itworks.snamp.jmx.DescriptorUtils;
import com.itworks.snamp.jmx.JMExceptionUtils;
import com.itworks.snamp.jmx.WellKnownType;
import com.itworks.snamp.licensing.LicensingException;

import javax.management.*;
import javax.management.openmbean.*;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.nio.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.adapters.jmx.JmxAdapterConfigurationProvider.OBJECT_NAME_PARAM;
import static com.itworks.snamp.adapters.jmx.JmxAdapterConfigurationProvider.USE_PLATFORM_MBEAN_PARAM;
import static com.itworks.snamp.concurrent.AbstractConcurrentResourceAccessor.Action;
import static com.itworks.snamp.concurrent.AbstractConcurrentResourceAccessor.ConsistentAction;

/**
 * Represents JMX adapter. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxResourceAdapter extends AbstractResourceAdapter {
    static final String NAME = JmxAdapterHelpers.ADAPTER_NAME;

    private static final class NotificationListenerHolder implements NotificationListener, NotificationFilter{
        private final NotificationListener listener;
        private final NotificationFilter filter;
        private final Object handback;

        private NotificationListenerHolder(final NotificationListener listener,
                                           final NotificationFilter filter,
                                           final Object handback) throws IllegalArgumentException{
            if(listener == null) throw new IllegalArgumentException("listener is null.");
            this.listener = listener;
            this.filter = filter;
            this.handback = handback;
        }

        @Override
        public boolean isNotificationEnabled(final Notification notification) {
            return filter == null || filter.isNotificationEnabled(notification);
        }

        @Override
        public void handleNotification(final Notification notification, final Object handback) {
            listener.handleNotification(notification, handback == null ? this.handback : handback);
        }
    }

    private static final class JmxNotifications extends AbstractNotificationsModel<MBeanNotificationInfo> implements NotificationBroadcaster {
        private final ConcurrentResourceAccessor<List<NotificationListenerHolder>> listeners;
        private static final String ID_SEPARATOR = ".";

        private JmxNotifications() {
            listeners = new ConcurrentResourceAccessor<List<NotificationListenerHolder>>(new LinkedList<NotificationListenerHolder>());
        }

        /**
         * Creates subscription list ID.
         *
         * @param resourceName User-defined name of the managed resource which can emit the notification.
         * @param eventName    User-defined name of the event.
         * @return A new unique subscription list ID.
         */
        @Override
        protected String makeSubscriptionListID(final String resourceName, final String eventName) {
            return resourceName + ID_SEPARATOR + eventName;
        }

        /**
         * Creates a new notification metadata representation.
         *
         * @param resourceName User-defined name of the managed resource.
         * @param notifMeta    The notification metadata to wrap.
         * @return A new notification metadata representation.
         */
        @Override
        protected MBeanNotificationInfo createNotificationView(final String resourceName,
                                                               final MBeanNotificationInfo notifMeta) {
            final Map<String, Object> fields = new HashMap<>(DescriptorUtils.toMap(notifMeta.getDescriptor()));
            fields.put(ProxyMBean.RESOURCE_NAME_FIELD, resourceName);
            return new MBeanNotificationInfo(notifMeta.getNotifTypes(),
                    notifMeta.getName(),
                    notifMeta.getDescription(),
                    new ImmutableDescriptor(fields));
        }

        /**
         * Invoked when a resource notification occurs.
         *
         * @param metadata     The user-defined metadata associated with the notification.
         * @param notification The notification.
         */
        @Override
        protected void handleNotification(final MBeanNotificationInfo metadata,
                                          final Notification notification) {
            listeners.read(new ConsistentAction<List<NotificationListenerHolder>, Void>() {
                @Override
                public Void invoke(final List<NotificationListenerHolder> listeners) {
                    for(final NotificationListenerHolder holder: listeners)
                        if(holder.isNotificationEnabled(notification))
                            holder.handleNotification(notification, metadata);
                    return null;
                }
            });
        }

        @Override
        public void addNotificationListener(final NotificationListener listener,
                                            final NotificationFilter filter,
                                            final Object handback) throws IllegalArgumentException {
            listeners.write(new Action<List<NotificationListenerHolder>, Void, IllegalArgumentException>() {
                @Override
                public Void invoke(final List<NotificationListenerHolder> listeners) throws IllegalArgumentException{
                    listeners.add(new NotificationListenerHolder(listener, filter, handback));
                    return null;
                }
            });
        }

        @Override
        public void removeNotificationListener(final NotificationListener listener) throws ListenerNotFoundException {
            final boolean removed = listeners.write(new ConsistentAction<List<NotificationListenerHolder>, Boolean>() {
                @Override
                public Boolean invoke(final List<NotificationListenerHolder> listeners) {
                    final Iterator<NotificationListenerHolder> holders = listeners.iterator();
                    boolean removed = false;
                    while (holders.hasNext()){
                        final NotificationListenerHolder holder = holders.next();
                        if(holder.listener == listener) {
                            holders.remove();
                            removed = true;
                        }
                    }
                    return removed;
                }
            });
            if(!removed)
                throw JMExceptionUtils.listenerNotFound(listener);
        }

        @Override
        public MBeanNotificationInfo[] getNotificationInfo() {
            return ArrayUtils.toArray(values(), MBeanNotificationInfo.class);
        }

        /**
         * Removes all of the mappings from this map.
         */
        @Override
        public void clear() {
            super.clear();
            listeners.write(new ConsistentAction<List<NotificationListenerHolder>, Void>() {
                @Override
                public Void invoke(final List<NotificationListenerHolder> listeners) {
                    listeners.clear();
                    return null;
                }
            });
        }
    }

    private static abstract class AbstractAttributeMapping implements JmxAttributeMapping{
        protected final AttributeAccessor accessor;
        private final OpenMBeanAttributeInfoSupport metadata;

        private AbstractAttributeMapping(final AttributeAccessor accessor,
                                        final OpenMBeanAttributeInfoSupport metadata){
            this.accessor = accessor;
            this.metadata = metadata;
        }

        @Override
        public final OpenMBeanAttributeInfoSupport getAttributeInfo() {
            return metadata;
        }

        @Override
        public final String getOriginalName() {
            return AttributeDescriptor.getAttributeName(metadata.getDescriptor());
        }


    }

    private static final class OpenTypeAttributeMapping extends AbstractAttributeMapping{

        private OpenTypeAttributeMapping(final AttributeAccessor accessor,
                                         final ImmutableDescriptor fields) {
            super(accessor, createMetadata((OpenMBeanAttributeInfo)accessor.getMetadata(), fields));
        }

        private static OpenMBeanAttributeInfoSupport createMetadata(final OpenMBeanAttributeInfo source,
                                                                    final ImmutableDescriptor options){
            return new OpenMBeanAttributeInfoSupport(source.getName(),
                    source.getDescription(),
                    source.getOpenType(),
                    source.isReadable(),
                    source.isWritable(),
                    source.isIs(),
                    options);
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
                                           final Class<T> arrayType,
                                           final ImmutableDescriptor fields) {
            super(accessor, createMetadata(accessor.getMetadata(), arrayType, fields));
        }

        private static OpenMBeanAttributeInfoSupport createMetadata(final MBeanAttributeInfo source,
                                                                    final Class<?> arrayType,
                                                                    final ImmutableDescriptor fields) {
            return new OpenMBeanAttributeInfoSupport(source.getName(),
                    source.getDescription(),
                    ArrayType.getPrimitiveArrayType(arrayType),
                    true,
                    true,
                    false,
                    fields);
        }

        @Override
        public abstract T getValue() throws MBeanException, ReflectionException, AttributeNotFoundException;
    }

    private static final class ByteBufferAttributeMapping extends BufferAttributeMapping<byte[]>{
        private ByteBufferAttributeMapping(final AttributeAccessor accessor,
                                         final ImmutableDescriptor fields) {
            super(accessor, byte[].class, fields);
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
        private CharBufferAttributeMapping(final AttributeAccessor accessor,
                                           final ImmutableDescriptor fields) {
            super(accessor, char[].class, fields);
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
        private ShortBufferAttributeMapping(final AttributeAccessor accessor,
                                           final ImmutableDescriptor fields) {
            super(accessor, short[].class, fields);
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
        private IntBufferAttributeMapping(final AttributeAccessor accessor,
                                            final ImmutableDescriptor fields) {
            super(accessor, int[].class, fields);
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
        private LongBufferAttributeMapping(final AttributeAccessor accessor,
                                          final ImmutableDescriptor fields) {
            super(accessor, long[].class, fields);
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
        private FloatBufferAttributeMapping(final AttributeAccessor accessor,
                                           final ImmutableDescriptor fields) {
            super(accessor, float[].class, fields);
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
        private DoubleBufferAttributeMapping(final AttributeAccessor accessor,
                                            final ImmutableDescriptor fields) {
            super(accessor, double[].class, fields);
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
        private ReadOnlyAttributeMapping(final AttributeAccessor accessor,
                                         final ImmutableDescriptor fields){
            super(accessor, createMetadata(accessor.getMetadata(), fields));
        }

        private static OpenMBeanAttributeInfoSupport createMetadata(final MBeanAttributeInfo source,
                                                                    final ImmutableDescriptor fields){
            return new OpenMBeanAttributeInfoSupport(source.getName(),
                    source.getDescription(),
                    SimpleType.STRING,
                    true,
                    false,
                    false,
                    fields);
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

    private static final class JmxAttributes extends AbstractAttributesModel<JmxAttributeMapping>{
        private static final String ID_SEPARATOR = "/";

        /**
         * Creates a new unique identifier of the management attribute.
         * <p>
         * The identifier must be unique through all instances of the resource adapter.
         * </p>
         *
         * @param resourceName             User-defined name of the managed resource which supply the attribute.
         * @param userDefinedAttributeName User-defined name of the attribute.
         * @return A new unique identifier of the management attribute.
         */
        @Override
        protected String makeAttributeID(final String resourceName, final String userDefinedAttributeName) {
            return resourceName + ID_SEPARATOR + userDefinedAttributeName;
        }

        /**
         * Creates a new domain-specific representation of the management attribute.
         *
         * @param resourceName User-defined name of the managed resource.
         * @param accessor     An accessor for the individual management attribute.
         * @return A new domain-specific representation of the management attribute.
         */
        @Override
        protected AbstractAttributeMapping createAttributeView(final String resourceName,
                                                               final AttributeAccessor accessor) {
            final Map<String, Object> fields = new HashMap<>(DescriptorUtils.toMap(accessor.getMetadata().getDescriptor()));
            fields.put(ProxyMBean.RESOURCE_NAME_FIELD, resourceName);
            if(accessor.getMetadata() instanceof OpenMBeanAttributeInfo)
                return new OpenTypeAttributeMapping(accessor, new ImmutableDescriptor(fields));
            //try to detect the Buffer
            final WellKnownType type = CustomAttributeInfo.getType(accessor.getMetadata());
            if(type != null)
                switch (type){
                    case BYTE_BUFFER:
                        return new ByteBufferAttributeMapping(accessor, new ImmutableDescriptor(fields));
                    case CHAR_BUFFER:
                        return new CharBufferAttributeMapping(accessor, new ImmutableDescriptor(fields));
                    case SHORT_BUFFER:
                        return new ShortBufferAttributeMapping(accessor, new ImmutableDescriptor(fields));
                    case INT_BUFFER:
                        return new IntBufferAttributeMapping(accessor, new ImmutableDescriptor(fields));
                    case LONG_BUFFER:
                        return new LongBufferAttributeMapping(accessor, new ImmutableDescriptor(fields));
                    case FLOAT_BUFFER:
                        return new FloatBufferAttributeMapping(accessor, new ImmutableDescriptor(fields));
                    case DOUBLE_BUFFER:
                        return  new DoubleBufferAttributeMapping(accessor, new ImmutableDescriptor(fields));
                }
            return new ReadOnlyAttributeMapping(accessor, new ImmutableDescriptor(fields));
        }
    }

    private final JmxAttributes attributes;
    private final JmxNotifications notifications;
    private final Map<ObjectName, ProxyMBean> exposedBeans;
    private boolean usePlatformMBean;

    JmxResourceAdapter(final String adapterInstanceName) {
        super(adapterInstanceName);
        this.attributes = new JmxAttributes();
        this.exposedBeans = new HashMap<>(10);
        this.notifications = new JmxNotifications();
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
                final ProxyMBean bean = new ProxyMBean(resourceName, attributes.values(), notifications);
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
