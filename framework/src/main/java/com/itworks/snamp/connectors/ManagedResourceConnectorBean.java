package com.itworks.snamp.connectors;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.itworks.snamp.Descriptive;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.concurrent.WriteOnceRef;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.notifications.*;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.internal.annotations.Internal;
import com.itworks.snamp.jmx.JMExceptionUtils;
import com.itworks.snamp.jmx.WellKnownType;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenType;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents SNAMP in-process management connector that exposes Java Bean properties through connector managementAttributes.
 * <p>
 *     Use this class as base class for your custom management connector, if schema of the management information base
 *     is well known at the compile time and stable through connector instantiations.
 *     The following example demonstrates management connector bean:
 *     <pre>{@code
 *     public final class CustomConnector extends ManagedResourceConnectorBean{
 *       private String prop1;
 *
 *       public CustomConnector(){
 *           prop1 = "Hello, world!";
 *       }
 *
 *       public String getProperty1(){
 *         return prop1;
 *       }
 *
 *       public String setProperty1(final String value){
 *         prop1 = value;
 *       }
 *     }
 *
 *     final CustomConnector c = new CustomConnector();
 *     c.connectProperty("001", "property1", new HashMap<>());
 *     System.out.println(c.getAttribute("001"));//output is: Hello, world!
 *     }</pre>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class ManagedResourceConnectorBean extends AbstractManagedResourceConnector<ManagedResourceConnectorBean.ManagedBeanDescriptor<?>>
        implements NotificationSupport, AttributeSupport {

    /**
     * Represents attribute reading or writing context.
     * This class cannot be inherited or instantiated directly in your code.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static final class AttributeContext implements AutoCloseable{
        private final static ThreadLocal<AttributeContext> currentContext = new ThreadLocal<>();
        private final MBeanAttributeInfo metadata;

        private AttributeContext(final MBeanAttributeInfo metadata){
            this.metadata = metadata;
            currentContext.set(this);
        }

        /**
         * Gets metadata of the calling attribute.
         * @return The metadata of the calling attribute.
         */
        public MBeanAttributeInfo getMetadata(){
            return metadata;
        }

        /**
         * Gets timeout for the attribute get/set operation.
         * @return The operation timeout.
         */
        public TimeSpan getOperationTimeout(){
            return AttributeDescriptor.getReadWriteTimeout(metadata);
        }

        /**
         * Gets current context.
         * <p>
         *     This method can be called inside of bean property and allows
         *     to capture connector specific information.
         * @return The current context.
         */
        public static AttributeContext get(){
            return currentContext.get();
        }

        @Override
        public void close() {
            currentContext.remove();
        }
    }

    public static final class NotificationContext implements AutoCloseable{
        private final static ThreadLocal<NotificationContext> currentContext = new ThreadLocal<>();
        private final ManagementNotificationType<?> metadata;

        private NotificationContext(final ManagementNotificationType<?> metadata){
            this.metadata = metadata;
            currentContext.set(this);
        }

        public ManagementNotificationType<?> getMetadata(){
            return metadata;
        }

        public static NotificationContext get(){
            return currentContext.get();
        }

        @Override
        public void close() {
            currentContext.remove();
        }
    }

    /**
     * Represents description of the bean to be managed by this connector.
     * <p>You should not implement this interface directly.</p>
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    @Internal
    public static interface ManagedBeanDescriptor<T>{
        /**
         * Gets metadata of the manageable bean.
         * @return The metadata of the manageable bean.
         */
        BeanInfo getBeanInfo();

        /**
         * Gets a manageable instance.
         * @return A manageable instance.
         */
        T getInstance();
    }

    private static final class BeanDescriptor<T> implements ManagedBeanDescriptor<T> {
        private final T instance;
        private final BeanInfo metadata;

        private BeanDescriptor(final T beanInstance, final BeanIntrospector introspector) throws IntrospectionException {
            if(beanInstance == null) throw new IllegalArgumentException("beanInstance is null.");
            else if(introspector == null) throw new IllegalArgumentException("introspector is null.");
            else {
                this.instance = beanInstance;
                this.metadata = introspector.getBeanInfo(beanInstance.getClass());
            }
        }

        /**
         * Gets metadata of the manageable bean.
         *
         * @return The metadata of the manageable bean.
         */
        @Override
        public BeanInfo getBeanInfo() {
            return metadata;
        }

        /**
         * Gets a manageable instance.
         *
         * @return A manageable instance.
         */
        @Override
        public T getInstance() {
            return instance;
        }
    }

    private static final class SelfDescriptor implements ManagedBeanDescriptor<ManagedResourceConnectorBean> {
        private Reference<ManagedResourceConnectorBean> connectorRef;
        private final WriteOnceRef<BeanInfo> metadata = new WriteOnceRef<>();

        private void setSelfReference(final ManagedResourceConnectorBean instance) throws IntrospectionException {
            connectorRef = new WeakReference<>(instance);
            metadata.set(Introspector.getBeanInfo(instance.getClass(), ManagedResourceConnectorBean.class));
        }

        /**
         * Gets metadata of the manageable bean.
         *
         * @return The metadata of the manageable bean.
         */
        @Override
        public BeanInfo getBeanInfo() {
            return metadata.get();
        }

        /**
         * Gets a manageable instance.
         *
         * @return A manageable instance.
         */
        @Override
        public ManagedResourceConnectorBean getInstance() {
            return connectorRef != null ? connectorRef.get() : null;
        }
    }

    /**
     * Describes management notification type supported by this connector.
     * @param <T> Well-known type of the user data to be associated with each notification.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static interface ManagementNotificationType<T> extends Descriptive{
        /**
         * Gets user data type.
         * @return The user data type; or {@literal} null if user data is not supported.
         */
        OpenType<T> getUserDataType();

        /**
         * The category of the notification.
         * @return The category of the notification.
         */
        String getCategory();
    }

    private static enum EmptyManagementNotificationType implements ManagementNotificationType<Void>{
        ;

        @Override
        public OpenType<Void> getUserDataType() {
            return null;
        }

        @Override
        public String getCategory() {
            return null;
        }

        @Override
        public String getDescription(final Locale locale) {
            return getCategory();
        }
    }

    /**
     * Represents attribute formatter for custom attribute types.
     * @param <T> JMX-compliant type.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static interface ManagementAttributeFormatter<T>{
        /**
         * Gets JMX-compliant type of the attribute.
         * @return JMX-compliant type of the attribute.
         */
        OpenType<T> getAttributeType();

        /**
         * Converts attribute value to the JMX-compliant type.
         * @param attributeValue The value of the attribute.
         * @return JMX-compliant attribute value.
         */
        T toJmxValue(final Object attributeValue);

        /**
         * Converts JMX-compliant attribute value into the native Java object.
         * @param jmxValue The value to convert.
         * @return The converted attribute value.
         */
        Object fromJmxValue(final Object jmxValue);
    }

    private static final class DefaultManagementAttributeFormatter implements ManagementAttributeFormatter<Object>{
        @Override
        public OpenType<Object> getAttributeType() {
            return null;
        }

        @Override
        public Object toJmxValue(final Object attributeValue) {
            return attributeValue;
        }

        @Override
        public Object fromJmxValue(final Object jmxValue) {
            return jmxValue;
        }
    }

    /**
     * Associated additional attribute info with the bean property getter and setter.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    protected static @interface ManagementAttribute {
        /**
         * Determines whether the attribute if cached.
         * @return {@literal true}, if attribute value is cached in the private field; otherwise, {@literal false}.
         */
        boolean cached() default false;

        /**
         * Gets the description of the attribute.
         * @return The description of the attribute.
         */
        String description() default "";

        /**
         * Represents attribute formatter that is used to convert custom Java type to
         * JMX-compliant value and vice versa.
         * @return The attribute formatter.
         */
        Class<? extends ManagementAttributeFormatter<?>> formatter() default DefaultManagementAttributeFormatter.class;
    }



    /**
     * Provides introspection for the specified bean type.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static interface BeanIntrospector{
        /**
         * Reflects the specified JavaBean.
         * @param beanType A type of JavaBean to reflect.
         * @return Metadata of the specified JavaBean.
         * @throws IntrospectionException Cannot reflect the specified JavaBean.
         */
        BeanInfo getBeanInfo(final Class<?> beanType) throws IntrospectionException;
    }

    /**
     * Provides the standard implementation of JavaBean reflection that simply
     * calls {@link Introspector#getBeanInfo(Class)} method. This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static final class StandardBeanIntrospector implements BeanIntrospector{

        /**
         * Reflects the specified JavaBean.
         * <p>
         *     This method simply calls {@link Introspector#getBeanInfo(Class)} method.
         * @param beanType A type of JavaBean to reflect.
         * @return Metadata of the specified JavaBean.
         * @throws java.beans.IntrospectionException
         *          Cannot reflect the specified JavaBean.
         */
        @Override
        public final BeanInfo getBeanInfo(final Class<?> beanType) throws IntrospectionException {
            return Introspector.getBeanInfo(beanType);
        }
    }

    private static class JavaBeanAttributeInfo extends MBeanAttributeInfo{
        private final Method getter;
        private final Method setter;
        protected final ManagementAttributeFormatter formatter;

        private JavaBeanAttributeInfo(final String attributeName,
                                      final PropertyDescriptor property,
                                      final AttributeDescriptor descriptor) throws ReflectionException {
            super(attributeName,
                    getDescription(property, descriptor),
                    property.getPropertyType().getName(),
                    canRead(property),
                    canWrite(property),
                    isFlag(property),
                    descriptor);
            getter = property.getReadMethod();
            setter = property.getWriteMethod();
            final ManagementAttribute info = getAdditionalInfo(getter, setter);
            if(info != null)
                try {
                    formatter = info.formatter().newInstance();
                } catch (final ReflectiveOperationException e){
                    throw new ReflectionException(e);
                }
            else formatter = new DefaultManagementAttributeFormatter();
        }

        final Object getValue(final Object owner) throws ReflectionException{
            if(getter != null)
                try(final AttributeContext ignored = new AttributeContext(this)) {
                    return formatter.toJmxValue(getter.invoke(owner));
                } catch (final ReflectiveOperationException e) {
                    throw new ReflectionException(e);
                }
            else throw new ReflectionException(new UnsupportedOperationException("Attribute is write-only"));
        }

        final void setValue(final Object owner, final Object value) throws ReflectionException, InvalidAttributeValueException {
            if (setter != null)
                try (final AttributeContext ignored = new AttributeContext(this)) {
                    setter.invoke(owner, formatter.fromJmxValue(value));
                } catch (final IllegalArgumentException e) {
                    throw new InvalidAttributeValueException(e.getMessage());
                } catch (final ReflectiveOperationException e) {
                    throw new ReflectionException(e);
                }
            else throw new ReflectionException(new UnsupportedOperationException("Attribute is read-only"));
        }

        private static boolean canRead(final PropertyDescriptor property){
            return property.getReadMethod() != null;
        }

        private static boolean canWrite(final PropertyDescriptor property){
            return property.getWriteMethod() != null;
        }

        private static boolean isFlag(final PropertyDescriptor property){
            final Method getter = property.getReadMethod();
            return getter != null && getter.getName().startsWith("is");
        }

        private static ManagementAttribute getAdditionalInfo(final Method... methods){
            for(final Method m: methods)
                if(m != null && m.isAnnotationPresent(ManagementAttribute.class))
                    return m.getAnnotation(ManagementAttribute.class);
            return null;
        }

        private static String getDescription(final PropertyDescriptor property,
                                             final AttributeDescriptor descriptor) {
            String description = descriptor.getDescription();
            if (description == null || description.isEmpty()) {
                final ManagementAttribute attr = getAdditionalInfo(property.getReadMethod(), property.getWriteMethod());
                description = attr != null ? attr.description() : null;
                if (description == null || description.isEmpty())
                    description = property.getName();
            }
            return description;
        }
    }

    private static final class JavaBeanOpenAttributeInfo extends JavaBeanAttributeInfo implements OpenMBeanAttributeInfo{
        private final OpenType<?> openType;

        private JavaBeanOpenAttributeInfo(final String attributeName,
                                          final PropertyDescriptor property,
                                          final AttributeDescriptor descriptor) throws ReflectionException, OpenDataException {
            super(attributeName, property, descriptor);
            OpenType<?> type = formatter.getAttributeType();
            //tries to detect open type via WellKnownType
            if(type == null){
                final WellKnownType knownType = WellKnownType.getType(property.getPropertyType());
                if(knownType != null && knownType.isOpenType())
                    type = knownType.getOpenType();
                else throw new OpenDataException();
            }
            this.openType = type;
        }

        @Override
        public OpenType<?> getOpenType() {
            return openType;
        }

        @Override
        public Object getDefaultValue() {
            return null;
        }

        @Override
        public Set<?> getLegalValues() {
            return null;
        }

        @Override
        public Comparable<?> getMinValue() {
            return null;
        }

        @Override
        public Comparable<?> getMaxValue() {
            return null;
        }

        @Override
        public boolean hasDefaultValue() {
            return false;
        }

        @Override
        public boolean hasLegalValues() {
            return false;
        }

        @Override
        public boolean hasMinValue() {
            return false;
        }

        @Override
        public boolean hasMaxValue() {
            return false;
        }

        @Override
        public boolean isValue(final Object obj) {
            return openType.isValue(obj);
        }
    }

    private static final class JavaBeanAttributeSupport extends AbstractAttributeSupport<JavaBeanAttributeInfo>{
        private final Logger logger;
        private final ManagedBeanDescriptor<?> bean;

        private JavaBeanAttributeSupport(final ManagedBeanDescriptor<?> beanDesc,
                                         final Logger logger){
            super(JavaBeanAttributeInfo.class);
            this.logger = Objects.requireNonNull(logger);
            this.bean = Objects.requireNonNull(beanDesc);
        }

        @Override
        protected JavaBeanAttributeInfo connectAttribute(final String attributeID,
                                                         final AttributeDescriptor descriptor) throws AttributeNotFoundException, ReflectionException {
            final BeanInfo info = bean.getBeanInfo();
            for(final PropertyDescriptor property: info.getPropertyDescriptors())
                if(Objects.equals(property.getName(), descriptor.getAttributeName()))
                    try{
                        //try to connect as Open Type attribute
                        return new JavaBeanOpenAttributeInfo(attributeID, property, descriptor);
                    }
                    catch (final OpenDataException e){
                        //bean property type is not Open Type
                        return new JavaBeanAttributeInfo(attributeID, property, descriptor);
                    }
            throw JMExceptionUtils.attributeNotFound(descriptor.getAttributeName());
        }

        @Override
        protected void failedToConnectAttribute(final String attributeID, final String attributeName, final Exception e) {
            failedToConnectAttribute(logger, Level.SEVERE, attributeID, attributeName, e);
        }

        @Override
        protected Object getAttribute(final JavaBeanAttributeInfo metadata) throws ReflectionException {
            return metadata.getValue(bean.getInstance());
        }

        @Override
        protected void failedToGetAttribute(final String attributeID, final Exception e) {
            failedToGetAttribute(logger, Level.WARNING, attributeID, e);
        }

        @Override
        protected void setAttribute(final JavaBeanAttributeInfo attribute, final Object value) throws ReflectionException, InvalidAttributeValueException {
            attribute.setValue(bean.getInstance(), value);
        }

        @Override
        protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
            failedToSetAttribute(logger, Level.WARNING, attributeID, value, e);
        }
    }

    private static final class JavaBeanNotificationSupport extends AbstractNotificationSupport<CustomNotificationInfo>{
        private final Logger logger;
        private final Set<? extends ManagementNotificationType<?>> notifTypes;
        private final NotificationListenerInvoker listenerInvoker;

        private JavaBeanNotificationSupport(final Set<? extends ManagementNotificationType<?>> notifTypes,
                                            final Logger logger){
            super(CustomNotificationInfo.class);
            this.logger = Objects.requireNonNull(logger);
            this.notifTypes = Objects.requireNonNull(notifTypes);
            this.listenerInvoker = NotificationListenerInvokerFactory.createSequentialInvoker();
        }

        @Override
        protected NotificationListenerInvoker getListenerInvoker() {
            return listenerInvoker;
        }

        @Override
        protected CustomNotificationInfo enableNotifications(final String category,
                                                            final NotificationDescriptor metadata) throws IllegalArgumentException {
            //find the suitable notification type
            final ManagementNotificationType<?> type = Iterables.find(notifTypes, new Predicate<ManagementNotificationType<?>>() {
                @Override
                public boolean apply(final ManagementNotificationType<?> type) {
                    return Objects.equals(type.getCategory(), metadata.getNotificationCategory());
                }
            });
            if(type != null){
                String description = type.getDescription(Locale.getDefault());
                if(description == null || description.isEmpty()) {
                    description = metadata.getDescription();
                    if(description == null || description.isEmpty())
                        description = type.getCategory();
                }
                return new CustomNotificationInfo(category, description, metadata);
            }
            else throw new IllegalArgumentException(String.format("Unsupported notification %s", metadata.getNotificationCategory()));
        }

        @Override
        protected boolean disableNotifications(final CustomNotificationInfo metadata) {
            return true;
        }

        @Override
        protected void failedToEnableNotifications(final String listID, final String category, final Exception e) {
            failedToEnableNotifications(logger, Level.WARNING, listID, category, e);
        }

        private void fire(final ManagementNotificationType<?> category, final String message, final Object userData) {
            try(final NotificationContext ignored = new NotificationContext(category)){
                fire(category.getCategory(), message, userData);
            }
        }
    }

    private final JavaBeanAttributeSupport attributes;
    private final JavaBeanNotificationSupport notifications;

    private ManagedResourceConnectorBean(final ManagedBeanDescriptor<?> descriptor,
                                         final Set<? extends ManagementNotificationType<?>> notifTypes){
        super(descriptor);
        attributes = new JavaBeanAttributeSupport(descriptor, getLogger());
        notifications = new JavaBeanNotificationSupport(notifTypes, getLogger());
    }

    /**
     * Initializes a new managed resource connector that reflects properties of the specified instance
     * as connector managementAttributes.
     * @param beanInstance An instance of JavaBean to reflect. Cannot be {@literal null}.
     * @param introspector An introspector that reflects the specified JavaBean. Cannot be {@literal null}.
     * @throws IntrospectionException Cannot reflect the specified instance.
     * @throws IllegalArgumentException At least one of the specified arguments is {@literal null}.
     */
    protected ManagedResourceConnectorBean(final Object beanInstance,
                                               final BeanIntrospector introspector) throws IntrospectionException {
        this(new BeanDescriptor<>(beanInstance, introspector), Collections.<ManagementNotificationType<?>>emptySet());
    }

    /**
     * Initializes a new managed resource connector that reflects itself.
     * @throws IntrospectionException Unable to reflect managed resource connector.
     */
    protected ManagedResourceConnectorBean() throws IntrospectionException {
        this(EnumSet.noneOf(EmptyManagementNotificationType.class));
    }

    /**
     * Initializes a new managed resource connector that reflects itself.
     * @param notifTypes A set of notifications supported by this connector.
     * @param <N> Type of the notification category provider.
     * @throws IntrospectionException Unable to reflect managed resource connector.
     */
    protected <N extends Enum<N> & ManagementNotificationType<?>> ManagedResourceConnectorBean(final EnumSet<N> notifTypes) throws IntrospectionException {
        this(new SelfDescriptor(), notifTypes);
        //creates weak reference to this object
        Utils.safeCast(getConnectionOptions(), SelfDescriptor.class).setSelfReference(this);
    }

    /**
     * Creates SNAMP management connector from the specified Java Bean.
     * @param connectorName The name of the managed resource connector.
     * @param beanInstance An instance of the Java Bean to wrap.
     * @param <T> Type of the Java Bean to wrap.
     * @return A new instance of the management connector that wraps the Java Bean.
     * @throws IntrospectionException Cannot reflect the specified instance.
     */
    public static <T> ManagedResourceConnectorBean wrap(final String connectorName,
                                                        final T beanInstance) throws IntrospectionException {
        return new ManagedResourceConnectorBean(beanInstance, new StandardBeanIntrospector()){

            @Override
            public Logger getLogger() {
                return getLogger(connectorName);
            }
        };
    }

    /**
     * <p>Returns an array indicating, for each notification this
     * MBean may send, the name of the Java class of the notification
     * and the notification type.</p>
     * <p/>
     * <p>It is not illegal for the MBean to send notifications not
     * described in this array.  However, some clients of the MBean
     * server may depend on the array being complete for their correct
     * functioning.</p>
     *
     * @return the array of possible notifications.
     */
    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        return new MBeanNotificationInfo[0];
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
     * @see NotificationEmitter#removeNotificationListener
     */
    @Override
    public void removeNotificationListener(final NotificationListener listener) throws ListenerNotFoundException {

    }

    /**
     * Adds a listener to this MBean.
     *
     * @param listener The listener object which will handle the
     *                 notifications emitted by the broadcaster.
     * @param filter   The filter object. If filter is null, no
     *                 filtering will be performed before handling notifications.
     * @param handback An opaque object to be sent back to the
     *                 listener when a notification is emitted. This object cannot be
     *                 used by the Notification broadcaster object. It should be
     *                 resent unchanged with the notification to the listener.
     * @throws IllegalArgumentException Listener parameter is null.
     * @see #removeNotificationListener
     */
    @Override
    public void addNotificationListener(final NotificationListener listener, final NotificationFilter filter, final Object handback) throws IllegalArgumentException {

    }

    /**
     * Disables event listening for the specified category of events.
     * <p>
     * This method removes all listeners associated with the specified subscription list.
     * </p>
     *
     * @param listId The identifier of the subscription list.
     * @return {@literal true}, if notifications for the specified category is previously enabled; otherwise, {@literal false}.
     */
    @Override
    public boolean disableNotifications(final String listId) {
        return false;
    }

    /**
     * Enables event listening for the specified category of events.
     * <p/>
     * category can be used for enabling notifications for the same category
     * but with different options.
     * <p/>
     * listId parameter
     * is used as a value of {@link javax.management.Notification#getType()}.
     *
     * @param listId   An identifier of the subscription list.
     * @param category The name of the event category to listen.
     * @param options  Event discovery options.
     * @return The metadata of the event to listen; or {@literal null}, if the specified category is not supported.
     * @throws javax.management.JMException Internal connector error.
     */
    @Override
    public MBeanNotificationInfo enableNotifications(final String listId, final String category, final CompositeData options) throws JMException {
        return null;
    }

    /**
     * Removes the attribute from the connector.
     *
     * @param id The unique identifier of the attribute.
     * @return {@literal true}, if the attribute successfully disconnected; otherwise, {@literal false}.
     */
    @Override
    public final boolean disconnectAttribute(final String id) {
        return attributes.disconnectAttribute(id);
    }

    /**
     * Gets an array of connected attributes.
     *
     * @return An array of connected attributes.
     */
    @Override
    public final MBeanAttributeInfo[] getAttributeInfo() {
        verifyInitialization();
        return attributes.getAttributeInfo();
    }

    /**
     * Connects to the specified attribute.
     *
     * @param id               A key string that is used to invoke attribute from this connector.
     * @param attributeName    The name of the attribute.
     * @param readWriteTimeout A read/write timeout using for attribute read/write operation.
     * @param options          The attribute discovery options.
     * @return The description of the attribute.
     * @throws javax.management.AttributeNotFoundException The managed resource doesn't provide the attribute with the specified name.
     * @throws javax.management.JMException                Internal connector error.
     */
    @Override
    public final MBeanAttributeInfo connectAttribute(final String id,
                                               final String attributeName,
                                               final TimeSpan readWriteTimeout,
                                               final CompositeData options) throws JMException {
        verifyInitialization();
        return attributes.connectAttribute(id, attributeName, readWriteTimeout, options);
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
    public final Object getAttribute(final String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        verifyInitialization();
        return attributes.getAttribute(attribute);
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
    public final void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        verifyInitialization();
        attributes.setAttribute(attribute);
    }

    /**
     * Get the values of several attributes of the Dynamic MBean.
     *
     * @param attributes A list of the attributes to be retrieved.
     * @return The list of attributes retrieved.
     * @see #setAttributes
     */
    @Override
    public final AttributeList getAttributes(final String[] attributes) {
        verifyInitialization();
        return this.attributes.getAttributes(attributes);
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
    public final AttributeList setAttributes(final AttributeList attributes) {
        verifyInitialization();
        return this.attributes.setAttributes(attributes);
    }

    private void emitNotificationImpl(final ManagementNotificationType<?> category,
                                      final String message,
                                      final Object userData){
        notifications.fire(category, message, userData);
    }

    protected final void emitNotification(final ManagementNotificationType<?> category,
                                          final String message){
        emitNotificationImpl(category, message, null);
    }

    protected final <T> void emitNotification(final ManagementNotificationType<T> category,
                                              final String message,
                                              final T userData){
        emitNotificationImpl(category, message, userData);
    }
}
