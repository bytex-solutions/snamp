package com.itworks.snamp.connectors;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.TypeConverter;
import com.itworks.snamp.connectors.attributes.AttributeMetadata;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.notifications.*;
import com.itworks.snamp.internal.semantics.Internal;
import com.itworks.snamp.internal.semantics.MethodStub;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.internal.Utils.safeCast;

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
 *           super(new WellKnownTypeSystem());
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
 *     System.out.println(c.getAttribute("001", TimeSpan.INFINITE, ""));//output is: Hello, world!
 *     }</pre>
 * </p>
 * <p>
 *     By default, {@link WellKnownTypeSystem} supports only primitive types. Therefore, if you use
 *     this type system then your Java Bean properties should have only primitive types:
 *     <ul>
 *         <li>{@link Byte}</li>
 *         <li>{@link Short}</li>
 *         <li>{@link Integer}</li>
 *         <li>{@link Long}</li>
 *         <li>{@link Boolean}</li>
 *         <li>{@link Date}</li>
 *         <li>{@link String}</li>
 *         <li>{@link Float}</li>
 *         <li>{@link Double}</li>
 *         <li>{@link java.math.BigInteger}</li>
 *         <li>{@link java.math.BigDecimal}</li>
 *     </ul>
 *     To support custom type (such as {@link com.itworks.snamp.Table}, {@link Map} or array) you apply do the following steps:
 *     <ul>
 *      <li>Creates your own type system provider that derives from {@link WellKnownTypeSystem}.</li>
 *      <li>Declares public instance parameterless method that have {@link ManagedEntityType} return type in custom type system provider.</li>
 *      <li>Annotates property getter or setter with {@link ManagedResourceConnectorBean.ManagementAttribute} annotation and specify method name(declared
 *      and implemented in custom type system  provider) in {@link ManagedResourceConnectorBean.ManagementAttribute#typeProvider()} parameter.</li>
 *     </ul>
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public class ManagedResourceConnectorBean extends AbstractManagedResourceConnector<ManagedResourceConnectorBean.ManagedBeanDescriptor<?>> implements NotificationSupport, AttributeSupport {

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

        private BeanDescriptor(final T beanInstance, final BeanIntrospector<T> introspector) throws IntrospectionException {
            if(beanInstance == null) throw new IllegalArgumentException("beanInstance is null.");
            else if(introspector == null) throw new IllegalArgumentException("introspector is null.");
            else
                this.metadata = introspector.getBeanInfo(this.instance = beanInstance);
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
        private BeanInfo metadata;

        private void setSelfReference(final ManagedResourceConnectorBean instance) throws IntrospectionException {
            connectorRef = new WeakReference<>(instance);
            metadata = Introspector.getBeanInfo(instance.getClass(), ManagedResourceConnectorBean.class);
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
        public ManagedResourceConnectorBean getInstance() {
            return connectorRef != null ? connectorRef.get() : null;
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
        public boolean cached() default false;

        /**
         * Gets the name of the public instance parameterless method in {@link WellKnownTypeSystem} class that returns
         * {@link ManagedEntityType} for the specified bean property.
         * @return The name of the public instance method that produces {@link WellKnownTypeSystem} instance
         * for the annotated bean property.
         */
        public String typeProvider() default "";
    }

    private  final static class JavaBeanPropertyMetadata extends GenericAttributeMetadata<ManagedEntityTypeBuilder.AbstractManagedEntityType>{
        private final Map<String, String> properties;
        private final Class<?> propertyType;
        private final Method getter;
        private final Method setter;
        private final Reference<WellKnownTypeSystem> typeBuilder;

        public JavaBeanPropertyMetadata(final PropertyDescriptor descriptor, final WellKnownTypeSystem typeBuilder, final Map<String, String> props){
            super(descriptor.getName());
            properties = new HashMap<>(props);
            properties.put("displayName", descriptor.getDisplayName());
            properties.put("shortDescription", descriptor.getShortDescription());
            propertyType = descriptor.getPropertyType();
            getter = descriptor.getReadMethod();
            if(getter != null && !getter.isAccessible()) getter.setAccessible(true);
            setter = descriptor.getWriteMethod();
            if(setter != null && !setter.isAccessible()) setter.setAccessible(true);
            this.typeBuilder = new WeakReference<>(typeBuilder);
        }

        private ManagementAttribute getAttributeInfo(){
            final ManagementAttribute info;
            if(getter != null && getter.isAnnotationPresent(ManagementAttribute.class))
                info = getter.getAnnotation(ManagementAttribute.class);
            else if(setter != null && setter.isAnnotationPresent(ManagementAttribute.class))
                info = setter.getAnnotation(ManagementAttribute.class);
            else info = new ManagementAttribute(){
                    @Override
                    public boolean cached() {
                        return false;
                    }

                    @Override
                    public String typeProvider() {
                        return "";
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return ManagementAttribute.class;
                    }
                };
            return info;
        }

        /**
         * Determines whether the value of the attribute can be cached after first reading
         * and supplied as real attribute value before first write, return {@literal false} by default.
         *
         * @return {@literal true}, if the value of this attribute can be cached; otherwise, {@literal false}.
         */
        @Override
        public final boolean cacheable() {
            return getAttributeInfo().cached();
        }

        public final Object getValue(final Object beanInstance) throws ReflectiveOperationException {
            if(getter == null) return null;
            return getter.invoke(beanInstance);
        }

        public final void setValue(final Object beanInstance, final Object value) throws ReflectiveOperationException {
            final TypeConverter<?> converter = getType().getProjection(propertyType);
            if(converter != null)
                setter.invoke(beanInstance, converter.convertFrom(value));
        }

        /**
         * Determines whether this property available for read.
         *
         * @return {@literal true}, if this property has getter; otherwise, {@literal false}.
         */
        @Override
        public final boolean canRead() {
            return getter != null;
        }

        /**
         * Determines whether the value of this attribute can be changed, returns {@literal true} by default.
         *
         * @return {@literal true}, if the attribute value can be changed; otherwise, {@literal false}.
         */
        @Override
        public boolean canWrite() {
            return setter != null;
        }

        /**
         * Detects the attribute type (this method will be called by infrastructure once).
         *
         * @return Detected attribute type.
         */
        @Override
        protected final ManagedEntityTypeBuilder.AbstractManagedEntityType detectAttributeType() {
            ManagedEntityTypeBuilder.AbstractManagedEntityType typeInfo = null;
            final String typeProviderMethodName = getAttributeInfo().typeProvider();
            final WellKnownTypeSystem typeBuilder = this.typeBuilder.get();
            if(typeBuilder != null)
                try {
                    final Method typeProviderImpl = typeBuilder.getClass().getMethod(typeProviderMethodName);
                    typeInfo = (ManagedEntityTypeBuilder.AbstractManagedEntityType)typeProviderImpl.invoke(typeBuilder);
                }
                catch (final ReflectiveOperationException e) {
                    if(propertyType.isArray())
                        typeInfo = typeBuilder.createEntityArrayType(typeBuilder.createEntitySimpleType(propertyType.getComponentType()));
                    else
                        typeInfo = typeBuilder.createEntitySimpleType(propertyType);
                }
                finally {
                    this.typeBuilder.clear();
                }
            return typeInfo;
        }

        @Override
        public final int size() {
            return properties.size();
        }

        @Override
        public final boolean isEmpty() {
            return properties.isEmpty();
        }

        @Override
        public final boolean containsKey(final Object key) {
            return properties.containsKey(key);
        }

        @Override
        public final boolean containsValue(final Object value) {
            return properties.containsValue(value);
        }

        @Override
        public final String get(final Object key) {
            return properties.get(key);
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public final Set<String> keySet() {
            return properties.keySet();
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public final Collection<String> values() {
            return properties.values();
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public final Set<Entry<String, String>> entrySet() {
            return properties.entrySet();
        }
    }

    private static final class JavaBeanNotification extends HashMap<String, Object> implements Notification {
        private final Date timeStamp;
        private final Severity severity;
        private final long seqnum;
        private final String message;

        public JavaBeanNotification(final Severity severity,
                                    final long sequenceNumber,
                                    final String message,
                                    final Map<String, Object> attachments){
            this.timeStamp = new Date();
            this.severity = severity != null ? severity : Severity.UNKNOWN;
            this.seqnum = sequenceNumber;
            this.message = message != null ? message : "";
            putAll(attachments != null ? attachments : Collections.<String, Object>emptyMap());
        }



        /**
         * Gets the date and time at which the notification is generated.
         *
         * @return The date and time at which the notification is generated.
         */
        @Override
        public final Date getTimeStamp() {
            return timeStamp;
        }

        /**
         * Gets the order number of the notification message.
         *
         * @return The order number of the notification message.
         */
        @Override
        public final long getSequenceNumber() {
            return seqnum;
        }

        /**
         * Gets a severity of this event.
         *
         * @return The severity of this event.
         */
        @Override
        public final Severity getSeverity() {
            return severity;
        }

        /**
         * Gets a message description of this notification.
         *
         * @return The message description of this notification.
         */
        @Override
        public final String getMessage() {
            return message;
        }
    }

    private static final class JavaBeanEventMetadata extends GenericNotificationMetadata{
        private final AtomicLong sequenceCounter;
        private final WellKnownTypeSystem typeSystem;
        private final Map<String, String> options;
        private final NotificationListenerInvoker listenerInvoker;

        public JavaBeanEventMetadata(final WellKnownTypeSystem typeSys,
                                     final String category,
                                     final Map<String, String> options,
                                     final NotificationListenerInvoker listenerInvoker){
            super(category);
            if(listenerInvoker == null) throw new IllegalArgumentException("listenerInvoker is null.");
            sequenceCounter = new AtomicLong(0L);
            typeSystem = typeSys;
            this.options = options != null ? Collections.unmodifiableMap(options) : new HashMap<String, String>();
            this.listenerInvoker = listenerInvoker;
        }

        public final void fireListeners(final Severity severity, final String message, final Map<String, Object> attachments){
            fire(new JavaBeanNotification(severity, sequenceCounter.getAndIncrement(), message, attachments), listenerInvoker);
        }

        /**
         * Gets listeners invocation model for this notification type.
         * @return Listeners invocation model for this notification type.
         */
        @Override
        public final NotificationModel getNotificationModel() {
            return NotificationModel.MULTICAST_SEQUENTIAL;
        }

        /**
         * Returns the type descriptor for the specified attachment.
         *
         * @param attachment The notification attachment.
         * @return The type descriptor for the specified attachment; or {@literal null} if the specified
         *         attachment is not supported.
         */
        @Override
        public final ManagedEntityType getAttachmentType(final Object attachment) {
            if(attachment == null) return typeSystem.createFallbackEntityType();
            final ManagedEntityType typeInfo = typeSystem.createEntitySimpleType(attachment.getClass());
            return typeInfo != null ? typeInfo: typeSystem.createFallbackEntityType();
        }

        @Override
        public final int size() {
            return options.size();
        }

        @Override
        public final boolean isEmpty() {
            return options.isEmpty();
        }

        @Override
        public final boolean containsKey(final Object key) {
            return options.containsKey(key);
        }

        @Override
        public final boolean containsValue(final Object value) {
            return options.containsValue(value);
        }

        @Override
        public final String get(final Object key) {
            return options.get(key);
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public final Set<String> keySet() {
            return options.keySet();
        }


        @SuppressWarnings("NullableProblems")
        @Override
        public final Collection<String> values() {
            return options.values();
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public final Set<Entry<String, String>> entrySet() {
            return options.entrySet();
        }
    }

    private static final class JavaBeanNotificationSupport extends AbstractNotificationSupport{
        private final WellKnownTypeSystem attachmentTypeSystem;

        public JavaBeanNotificationSupport(final WellKnownTypeSystem typeSystem){
            this.attachmentTypeSystem = typeSystem;
        }
        /**
         * Raises notification.
         * @param category The category of the event to raise.
         * @param severity The severity of the event to raise.
         * @param message Human-readable description of the event.
         * @param attachments A set of notification attachments. May be {@literal null}.
         */
        public final void emitNotification(final String category, final Severity severity, final String message, final Map<String, Object> attachments){
            for(final JavaBeanEventMetadata eventMetadata: getEnabledNotifications(category, JavaBeanEventMetadata.class).values())
                eventMetadata.fireListeners(severity, message, attachments);
        }

        /**
         * Creates a new listeners invocation strategy.
         * <p>
         *     This method automatically calls from {@link #enableNotifications(String, java.util.Map)} method.
         *     By default, this method uses {@link NotificationListenerInvokerFactory#createParallelInvoker(java.util.concurrent.ExecutorService)}
         *     strategy.
         * </p>
         * @return A new listeners invocation strategy.
         */
        protected NotificationListenerInvoker createListenerInvoker(){
            return NotificationListenerInvokerFactory.createParallelInvoker(Executors.newSingleThreadExecutor());
        }

        /**
         * Enables event listening for the specified category of events.
         *
         * @param category The name of the category to listen.
         * @param options  Event discovery options.
         * @return The metadata of the event to listen; or {@literal null}, if the specified category is not supported.
         */
        @Override
        protected final GenericNotificationMetadata enableNotifications(final String category, final Map<String, String> options) {
            return new JavaBeanEventMetadata(attachmentTypeSystem, category, options, createListenerInvoker());
        }

        /**
         * Adds a new listener for the specified notification.
         *
         * @param listener The event listener.
         * @return Any custom data associated with the subscription.
         */
        @Override
        @MethodStub
        protected Object subscribe(final NotificationListener listener) {
            return null;
        }

        /**
         * Cancels the notification listening.
         *
         * @param listener The notification listener to remove.
         * @param data     The custom data associated with subscription that returned from {@link #subscribe(com.itworks.snamp.connectors.notifications.NotificationListener)}
         */
        @Override
        @MethodStub
        protected void unsubscribe(final NotificationListener listener, final Object data) {

        }
    }

    private static final class JavaBeanAttributeSupport extends AbstractAttributeSupport{
        private final WellKnownTypeSystem typeSystem;
        private final ManagedBeanDescriptor<?> descriptor;
        private final Logger logger;

        public JavaBeanAttributeSupport(final ManagedBeanDescriptor<?> descriptor,
                                        final WellKnownTypeSystem typeSystem,
                                        final Logger connectorLogger){
            this.typeSystem = typeSystem;
            this.descriptor = descriptor;
            this.logger = connectorLogger;
        }

        /**
         * Connects the specified Java Bean property.
         * @param property Java Bean property to connect.
         * @param options Additional connection options.
         * @return An information about registered attribute.
         */
        protected final GenericAttributeMetadata connectAttribute(final PropertyDescriptor property, final Map<String, String> options){
            return new JavaBeanPropertyMetadata(property, typeSystem, options);
        }

        /**
         * Connects to the specified attribute.
         *
         * @param attributeName The name of the attribute.
         * @param options       Attribute discovery options.
         * @return The description of the attribute.
         */
        @Override
        protected final GenericAttributeMetadata connectAttribute(final String attributeName, final Map<String, String> options) {
            for(final PropertyDescriptor pd: descriptor.getBeanInfo().getPropertyDescriptors())
                if(Objects.equals(pd.getName(), attributeName))
                    return connectAttribute(pd, options);
            return null;
        }

        /**
         * Returns the value of the attribute.
         *
         * @param attribute    The metadata of the attribute to get.
         * @param readTimeout Attribute read timeout.
         * @param defaultValue The default value of the attribute if reading fails.
         * @return The value of the attribute.
         * @throws java.util.concurrent.TimeoutException Attribute value cannot be obtained in the specified time.
         *
         */
        @Override
        protected final Object getAttributeValue(final AttributeMetadata attribute, final TimeSpan readTimeout, final Object defaultValue) throws TimeoutException {
            if(attribute instanceof JavaBeanPropertyMetadata)
                try {
                    return ((JavaBeanPropertyMetadata)attribute).getValue(descriptor.getInstance());
                }
                catch (final ReflectiveOperationException e) {
                    logger.log(Level.WARNING, "Unable to get attribute.", e);
                    return null;
                }
            else return null;
        }

        /**
         * Sends the attribute value to the remote agent.
         *
         * @param attribute    The metadata of the attribute to set.
         * @param writeTimeout Attribute write timeout.
         * @param value A new attribute value.
         * @return {@literal true} if attribute is overridden successfully; otherwise, {@literal false}.
         */
        @Override
        protected final boolean setAttributeValue(final AttributeMetadata attribute, final TimeSpan writeTimeout, final Object value) {
            if(attribute.canWrite() && attribute instanceof JavaBeanPropertyMetadata){
                try {
                    ((JavaBeanPropertyMetadata)attribute).setValue(descriptor.getInstance(), value);
                    return true;
                }
                catch (final ReflectiveOperationException e) {
                    return false;
                }
            }
            else return false;
        }
    }

    /**
     * Provides introspection for the specified bean instance.
     * @param <T> Type of JavaBean to reflect.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static interface BeanIntrospector<T>{
        /**
         * Reflects the specified JavaBean.
         * @param beanInstance An instance of JavaBean to reflect.
         * @return Metadata of the specified JavaBean.
         * @throws IntrospectionException Cannot reflect the specified JavaBean.
         */
        public BeanInfo getBeanInfo(final T beanInstance) throws IntrospectionException;
    }

    /**
     * Provides the standard implementation of JavaBean reflection that simply
     * calls {@link Introspector#getBeanInfo(Class)} method. This class cannot be inherited.
     * @param <T> Type of JavaBean to reflect.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static final class StandardBeanIntrospector<T> implements BeanIntrospector<T>{

        /**
         * Reflects the specified JavaBean.
         * <p>
         *     This method simply calls {@link Introspector#getBeanInfo(Class)} method.
         * </p>
         * @param beanInstance An instance of JavaBean to reflect.
         * @return Metadata of the specified JavaBean.
         * @throws java.beans.IntrospectionException
         *          Cannot reflect the specified JavaBean.
         */
        @Override
        public final BeanInfo getBeanInfo(final T beanInstance) throws IntrospectionException {
            return Introspector.getBeanInfo(beanInstance.getClass());
        }
    }

    private final JavaBeanNotificationSupport notifications;
    private final JavaBeanAttributeSupport attributes;

    /**
     * Initializes a new management connector that reflects properties of this class as
     * connector managementAttributes.
     * @param typeBuilder Type information provider that provides property type converter.
     * @param logger A logger for this management connector.
     * @throws IllegalArgumentException typeBuilder is {@literal null}.
     */
    @SuppressWarnings("UnusedDeclaration")
    protected ManagedResourceConnectorBean(final WellKnownTypeSystem typeBuilder, final Logger logger) throws IntrospectionException {
        super(new SelfDescriptor(),  logger);
        //creates weak reference to this object
        safeCast(getConnectionOptions(), SelfDescriptor.class).setSelfReference(this);
        if(typeBuilder == null) throw new IllegalArgumentException("typeBuilder is null.");
        this.attributes = new JavaBeanAttributeSupport(getConnectionOptions(), typeBuilder, logger);
        this.notifications = new JavaBeanNotificationSupport(typeBuilder);
    }

    /**
     * Initializes a new management connector that reflects properties of the specified instance
     * as connector managementAttributes.
     * @param beanInstance An instance of JavaBean to reflect. Cannot be {@literal null}.
     * @param introspector An introspector that reflects the specified JavaBean. Cannot be {@literal null}.
     * @param typeBuilder Type information provider that provides property type converter. Cannot be {@literal null}.
     * @param <T>  Type of JavaBean to reflect.
     * @throws IntrospectionException Cannot reflect the specified instance.
     * @throws IllegalArgumentException At least one of the specified arguments is {@literal null}.
     */
    @SuppressWarnings("UnusedDeclaration")
    protected <T> ManagedResourceConnectorBean(final T beanInstance,
                                               final BeanIntrospector<T> introspector,
                                               final WellKnownTypeSystem typeBuilder,
                                               final Logger logger) throws IntrospectionException {
        super(new BeanDescriptor<>(beanInstance, introspector),  logger);
        if(typeBuilder == null) throw new IllegalArgumentException("typeBuilder is null.");
        this.attributes = new JavaBeanAttributeSupport(getConnectionOptions(), typeBuilder, logger);
        this.notifications = new JavaBeanNotificationSupport(typeBuilder);
    }

    /**
     * Creates SNAMP management connector from the specified Java Bean.
     * @param beanInstance An instance of the Java Bean to wrap.
     * @param typeBuilder Bean property type converter.
     * @param <T> Type of the Java Bean to wrap.
     * @return A new instance of the management connector that wraps the Java Bean.
     * @throws IntrospectionException Cannot reflect the specified instance.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static <T> ManagedResourceConnectorBean wrap(final T beanInstance, final WellKnownTypeSystem typeBuilder) throws IntrospectionException {
        return new ManagedResourceConnectorBean(beanInstance, new StandardBeanIntrospector<>(), typeBuilder, Logger.getLogger(getLoggerName("javabean")));
    }

    /**
     * Returns an array of all discovered managementAttributes available for registration.
     * @return An array of all discovered managementAttributes available for registration.
     */
    @SuppressWarnings("UnusedDeclaration")
    public final String[] availableAttributes(){
        final PropertyDescriptor[] properties = getConnectionOptions().getBeanInfo().getPropertyDescriptors();
        final String[] result = new String[properties.length];
        for(int i = 0; i < properties.length; i++)
            result[i] = properties[i].getName();
        return result;
    }



    /**
     * Enables event listening for the specified category of events.
     * <p>
     * categoryId can be used for enabling notifications for the same category
     * but with different options.
     * </p>
     *
     * @param listId   An identifier of the subscription list.
     * @param category The name of the category to listen.
     * @param options  Event discovery options.
     * @return The metadata of the event to listen; or {@literal null}, if the specified category is not supported.
     */
    @Override
    public final NotificationMetadata enableNotifications(final String listId, final String category, final Map<String, String> options) {
        verifyInitialization();
        return notifications.enableNotifications(listId, category, options);
    }

    /**
     * Raises notification.
     * <p>
     *     In the derived class you should write your own emitter for each notification category,
     *     for example:
     *     <pre>{@code
     *     protected final void emitPropertyChanged(final String propertyName, final Object oldValue, final Object newValue){
     *       final Map<String, Object> attachments =new HashMap<String, Object>(3){{
     *         put("propertyName", propertyName);
     *         put("oldValue", oldValue);
     *         put("newValue", newValue);
     *       }};
     *       emitNotification("propertyChanged", Notification.Severity.NOTICE, String.format("Property %s changed", propertyName), attachments);
     *     }
     *     }</pre>
     * </p>
     * @param category The category of the event to raise.
     * @param severity The severity of the event to raise.
     * @param message Human-readable description of the event.
     * @param attachments A set of notification attachments. May be {@literal null}.
     */
    protected final void emitNotification(final String category, final Severity severity, final String message, final Map<String, Object> attachments){
        notifications.emitNotification(category, severity, message, attachments);
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
    public final boolean disableNotifications(final String listId) {
        verifyInitialization();
        return notifications.disableNotifications(listId);
    }

    /**
     * Gets the notification metadata by its category.
     *
     * @param listId The identifier of the subscription list.
     * @return The metadata of the specified notification category; or {@literal null}, if notifications
     * for the specified category is not enabled by {@link #enableNotifications(String, String, java.util.Map)} method.
     */
    @Override
    public final NotificationMetadata getNotificationInfo(final String listId) {
        verifyInitialization();
        return notifications.getNotificationInfo(listId);
    }

    /**
     * Returns a read-only collection of enabled notifications (subscription list identifiers).
     *
     * @return A read-only collection of enabled notifications (subscription list identifiers).
     */
    @Override
    public final Collection<String> getEnabledNotifications() {
        verifyInitialization();
        return notifications.getEnabledNotifications();
    }

    /**
     * Attaches the notification listener.
     *
     * @param listenerId Unique identifier of the notification listener.
     * @param listener The notification listener.
     * @param delayed Specifies delayed subscription.
     * @return An identifier of the notification listener generated by this connector.
     */
    @Override
    public final boolean subscribe(final String listenerId, final NotificationListener listener, final boolean delayed) {
        verifyInitialization();
        return notifications.subscribe(listenerId, listener, delayed);
    }

    /**
     * Removes the notification listener.
     *
     * @param listenerId An identifier previously returned by {@link #subscribe(String, com.itworks.snamp.connectors.notifications.NotificationListener, boolean)}.
     * @return {@literal true} if listener is removed successfully; otherwise, {@literal false}.
     */
    @Override
    public final boolean unsubscribe(final String listenerId) {
        verifyInitialization();
        return notifications.unsubscribe(listenerId);
    }

    /**
     * Connects to the specified attribute.
     *
     * @param id            A key string that is used to invoke attribute from this connector.
     * @param attributeName The name of the attribute.
     * @param options       The attribute discovery options.
     * @return The description of the attribute.
     */
    @Override
    public final AttributeMetadata connectAttribute(final String id, final String attributeName, final Map<String, String> options) {
        verifyInitialization();
        return attributes.connectAttribute(id, attributeName, options);
    }

    /**
     * Returns the attribute value.
     *
     * @param id           A key string that is used to invoke attribute from this connector.
     * @param readTimeout  The attribute value invoke operation timeout.
     * @param defaultValue The default value of the attribute if it is real value is not available.
     * @return The value of the attribute, or default value.
     * @throws java.util.concurrent.TimeoutException The attribute value cannot be invoke in the specified duration.
     */
    @Override
    public final Object getAttribute(final String id, final TimeSpan readTimeout, final Object defaultValue) throws TimeoutException {
        verifyInitialization();
        return attributes.getAttribute(id, readTimeout, defaultValue);
    }

    /**
     * Reads a set of managementAttributes.
     *
     * @param output      The dictionary with set of attribute keys to invoke and associated default values.
     * @param readTimeout The attribute value invoke operation timeout.
     * @return The set of managementAttributes ids really written to the dictionary.
     * @throws java.util.concurrent.TimeoutException The attribute value cannot be invoke in the specified duration.
     */
    @Override
    public final Set<String> getAttributes(final Map<String, Object> output, final TimeSpan readTimeout) throws TimeoutException {
        verifyInitialization();
        return attributes.getAttributes(output, readTimeout);
    }

    /**
     * Writes the value of the specified attribute.
     *
     * @param id           An identifier of the attribute,
     * @param writeTimeout The attribute value write operation timeout.
     * @param value        The value to write.
     * @return {@literal true} if attribute set operation is supported by remote provider; otherwise, {@literal false}.
     * @throws java.util.concurrent.TimeoutException The attribute value cannot be write in the specified duration.
     */
    @Override
    public final boolean setAttribute(final String id, final TimeSpan writeTimeout, final Object value) throws TimeoutException {
        verifyInitialization();
        return attributes.setAttribute(id, writeTimeout, value);
    }

    /**
     * Writes a set of managementAttributes inside of the transaction.
     *
     * @param values       The dictionary of managementAttributes keys and its values.
     * @param writeTimeout Attribute write timeout.
     * @return {@literal null}, if the transaction is committed; otherwise, {@literal false}.
     * @throws java.util.concurrent.TimeoutException
     */
    @Override
    public final boolean setAttributes(final Map<String, Object> values, final TimeSpan writeTimeout) throws TimeoutException {
        verifyInitialization();
        return attributes.setAttributes(values, writeTimeout);
    }

    /**
     * Removes the attribute from the connector.
     *
     * @param id The unique identifier of the attribute.
     * @return {@literal true}, if the attribute successfully disconnected; otherwise, {@literal false}.
     */
    @Override
    public final boolean disconnectAttribute(final String id) {
        verifyInitialization();
        return attributes.disconnectAttribute(id);
    }

    /**
     * Returns the information about the connected attribute.
     *
     * @param id An identifier of the attribute.
     * @return The attribute descriptor; or {@literal null} if attribute is not connected.
     */
    @Override
    public final AttributeMetadata getAttributeInfo(final String id) {
        verifyInitialization();
        return attributes.getAttributeInfo(id);
    }

    /**
     * Returns a read-only collection of registered IDs of managementAttributes.
     *
     * @return A read-only collection of registered IDs of managementAttributes.
     */
    @Override
    public final Collection<String> getConnectedAttributes() {
        return attributes.getConnectedAttributes();
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        super.close();
        attributes.clear();
        notifications.clear();
    }
}
