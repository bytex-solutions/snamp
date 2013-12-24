package com.snamp.connectors;

import com.snamp.*;

import java.beans.*;
import java.lang.annotation.*;
import java.lang.ref.*;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.lang.reflect.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents SNAMP in-process management connector that exposes Java Bean properties through connector attributes.
 * <p>
 *     Use this class as base class for your custom management connector, if schema of the management information base
 *     is well known at the compile time and stable through connector instantiations.
 *     The following example demonstrates management connector bean:
 *     <pre>{@code
 *     public final class CustomConnector extends ManagementConnectorBean{
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
 *     To support custom type (such as {@link Table}, {@link Map} or array) you apply do the following steps:
 *     <ul>
 *      <li>Creates your own type system provider that derives from {@link WellKnownTypeSystem}.</li>
 *      <li>Declares public instance parameterless method that have {@link ManagementEntityType} return type in custom type system provider.</li>
 *      <li>Annotates property getter or setter with {@link AttributeInfo} annotation and specify method name(declared
 *      and implemented in custom type system  provider) in {@link AttributeInfo#typeProvider()} parameter.</li>
 *     </ul>
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Lifecycle(InstanceLifecycle.NORMAL)
public class ManagementConnectorBean extends AbstractManagementConnector implements NotificationSupport {

    /**
     * Associated additional attribute info with the bean property getter and setter.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    protected static @interface AttributeInfo{
        /**
         * Determines whether the attribute if cached.
         * @return {@literal true}, if attribute value is cached in the private field; otherwise, {@literal false}.
         */
        public boolean cached() default false;

        /**
         * Gets the name of the public instance parameterless method in {@link WellKnownTypeSystem} class that returns
         * {@link ManagementEntityType} for the specified bean property.
         * @return The name of the public instance method that produces {@link WellKnownTypeSystem} instance
         * for the annotated bean property.
         */
        public String typeProvider() default "";
    }

    private  final static class JavaBeanPropertyMetadata extends GenericAttributeMetadata<WellKnownTypeSystem.AbstractManagementEntityType>{
        private final Map<String, String> properties;
        private final Class<?> propertyType;
        private final Method getter;
        private final Method setter;
        private final Reference<WellKnownTypeSystem> typeBuilder;

        public JavaBeanPropertyMetadata(final PropertyDescriptor descriptor, final WellKnownTypeSystem typeBuilder, final Map<String, String> props){
            super(descriptor.getName(), "");
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

        private final AttributeInfo getAttributeInfo(){
            final AttributeInfo info;
            if(getter != null && getter.isAnnotationPresent(AttributeInfo.class))
                info = getter.getAnnotation(AttributeInfo.class);
            else if(setter != null && setter.isAnnotationPresent(AttributeInfo.class))
                info = setter.getAnnotation(AttributeInfo.class);
            else info = new AttributeInfo(){
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
                        return AttributeInfo.class;
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
         * By default, returns {@literal true}.
         *
         * @return
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
        protected final WellKnownTypeSystem.AbstractManagementEntityType detectAttributeType() {
            WellKnownTypeSystem.AbstractManagementEntityType typeInfo = null;
            final String typeProviderMethodName = getAttributeInfo().typeProvider();
            final WellKnownTypeSystem typeBuilder = this.typeBuilder.get();
            try {
                final Method typeProviderImpl = typeBuilder.getClass().getMethod(typeProviderMethodName);
                typeInfo = (WellKnownTypeSystem.AbstractManagementEntityType)typeProviderImpl.invoke(typeBuilder);
            }
            catch (final ReflectiveOperationException e) {
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

        @Override
        public final Set<String> keySet() {
            return properties.keySet();
        }

        @Override
        public final Collection<String> values() {
            return properties.values();
        }

        @Override
        public final Set<Entry<String, String>> entrySet() {
            return properties.entrySet();
        }
    }

    private static final class JavaBeanNotification extends HashMap<String, Object> implements Notification{
        private final Date timeStamp;
        private final Severity severity;
        private final long seqnum;
        private final String message;
        private final WellKnownTypeSystem typeSystem;
        private final Map<String, Object> attachments;

        public JavaBeanNotification(final WellKnownTypeSystem typeSys,
                                    final Severity severity,
                                    final long sequenceNumber,
                                    final String message,
                                    final Map<String, Object> attachments){
            this.timeStamp = new Date();
            this.severity = severity != null ? severity : Severity.UNKNOWN;
            this.seqnum = sequenceNumber;
            this.message = message != null ? message : "";
            this.typeSystem = typeSys;
            this.attachments = attachments != null ? Collections.unmodifiableMap(attachments) : new HashMap<String, Object>();

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

        /**
         * Gets attachments associated with this notification.
         * <p>
         * The key of the returned map contains name of the attachment.
         * </p>
         *
         * @return A read-only collection of attachments associated with this notification.
         */
        @Override
        public final Map<String, Object> getAttachments() {
            return attachments;
        }
    }

    private static final class JavaBeanEventMetadata extends GenericNotificationMetadata{
        private final AtomicLong sequenceCounter;
        private final WellKnownTypeSystem typeSystem;
        private final Map<String, String> options;

        public JavaBeanEventMetadata(final WellKnownTypeSystem typeSys,
                                     final String category,
                                     final Map<String, String> options){
            super(category);
            sequenceCounter = new AtomicLong(0L);
            typeSystem = typeSys;
            this.options = options != null ? Collections.unmodifiableMap(options) : new HashMap<String, String>();
        }

        public final void fireListeners(final Notification.Severity severity, final String message, final Map<String, Object> attachments){
            final JavaBeanNotification notif = new JavaBeanNotification(typeSystem, severity, sequenceCounter.getAndIncrement(), message, attachments);
            for(final Pair<NotificationListener, Object> listener: getListeners())
                listener.first.handle(notif);
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
        public final ManagementEntityType getAttachmentType(final Object attachment) {
            if(attachment == null) return typeSystem.createFallbackEntityType();
            final ManagementEntityType typeInfo = typeSystem.createEntitySimpleType(attachment.getClass());
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
            return options.containsKey(value);
        }

        @Override
        public final String get(final Object key) {
            return options.get(key);
        }

        @Override
        public final Set<String> keySet() {
            return options.keySet();
        }


        @Override
        public final Collection<String> values() {
            return options.values();
        }

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
        public final void emitNotification(final String category, final Notification.Severity severity, final String message, final Map<String, Object> attachments){
            for(final JavaBeanEventMetadata eventMetadata: getEnabledNotifications(category, JavaBeanEventMetadata.class).values())
                eventMetadata.fireListeners(severity, message, attachments);
        }

        /**
         * Enables event listening for the specified category of events.
         *
         * @param category The name of the category to listen.
         * @param options  Event discovery options.
         * @return The metadata of the event to listen; or {@literal null}, if the specified category is not supported.
         */
        @Override
        protected final GenericNotificationMetadata enableNotificationsCore(final String category, final Map<String, String> options) {
            return new JavaBeanEventMetadata(attachmentTypeSystem, category, options);
        }

        /**
         * Disable all notifications associated with the specified event.
         * <p>
         * In the default implementation this method does nothing.
         * </p>
         *
         * @param notificationType The event descriptor.
         */
        @Override
        protected final void disableNotificationsCore(final NotificationMetadata notificationType) {
            if(notificationType instanceof GenericNotificationMetadata)
                ((GenericNotificationMetadata)notificationType).removeListeners();
        }

        /**
         * Adds a new listener for the specified notification.
         *
         * @param notificationType The event type.
         * @param listener         The event listener.
         * @return Any custom data associated with the subscription.
         */
        @Override
        @MethodStub
        protected Object subscribeCore(final NotificationMetadata notificationType, final NotificationListener listener) {
            return null;
        }

        /**
         * Cancels the notification listening.
         *
         * @param metadata The event type.
         * @param listener The notification listener to remove.
         * @param data     The custom data associated with subscription that returned from {@link #subscribeCore(NotificationMetadata, NotificationListener)}
         */
        @Override
        @MethodStub
        protected void unsubscribeCore(final NotificationMetadata metadata, final NotificationListener listener, final Object data) {

        }
    }


    private final BeanInfo beanMetadata;
    private final WellKnownTypeSystem typeInfoBuilder;
    private final Object beanInstance;
    private final JavaBeanNotificationSupport notifications;

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

    /**
     * Initializes a new management connector that reflects properties of this class as
     * connector attributes.
     * @param typeBuilder Type information provider that provides property type converter.
     * @throws IllegalArgumentException typeBuilder is {@literal null}.
     */
    protected ManagementConnectorBean(final WellKnownTypeSystem typeBuilder) throws IntrospectionException {
        if(typeBuilder == null) throw new IllegalArgumentException("typeBuilder is null.");
        this.typeInfoBuilder = typeBuilder;
        this.beanMetadata = Introspector.getBeanInfo(getClass(), ManagementConnectorBean.class);
        this.beanInstance = null;
        this.notifications = new JavaBeanNotificationSupport(typeBuilder);
    }

    /**
     * Initializes a new management connector that reflects properties of the specified instance
     * as connector attributes.
     * @param beanInstance An instance of JavaBean to reflect. Cannot be {@literal null}.
     * @param introspector An introspector that reflects the specified JavaBean. Cannot be {@literal null}.
     * @param typeBuilder Type information provider that provides property type converter. Cannot be {@literal null}.
     * @param <T>  Type of JavaBean to reflect.
     * @throws IntrospectionException Cannot reflect the specified instance.
     * @throws IllegalArgumentException At least one of the specified arguments is {@literal null}.
     */
    protected <T> ManagementConnectorBean(final T beanInstance, final BeanIntrospector<T> introspector, final WellKnownTypeSystem typeBuilder) throws IntrospectionException {
        if(beanInstance == null) throw new IllegalArgumentException("beanInstance is null.");
        else if(introspector == null) throw new IllegalArgumentException("introspector is null.");
        else if(typeBuilder == null) throw new IllegalArgumentException("typeBuilder is null.");
        this.beanInstance = beanInstance;
        this.beanMetadata = introspector.getBeanInfo(beanInstance);
        this.typeInfoBuilder = typeBuilder;
        this.notifications = new JavaBeanNotificationSupport(typeBuilder);
    }

    /**
     * Creates SNAMP management connector from the specified Java Bean.
     * @param beanInstance An instance of the Java Bean to wrap.
     * @param typeBuilder Bean property type converter.
     * @param <T> Type of the Java Bean to wrap.
     * @return A new instance of the management connector that wraps the Java Bean.
     * @throws IntrospectionException
     */
    public static <T> ManagementConnectorBean wrap(final T beanInstance, final WellKnownTypeSystem typeBuilder) throws IntrospectionException {
        return new ManagementConnectorBean(beanInstance, new StandardBeanIntrospector<>(), typeBuilder);
    }

    /**
     * Returns an array of all discovered attributes available for registration.
     * @return An array of all discovered attributes available for registration.
     */
    public final String[] availableAttributes(){
        final PropertyDescriptor[] properties = beanMetadata.getPropertyDescriptors();
        final String[] result = new String[properties.length];
        for(int i = 0; i < properties.length; i++)
            result[i] = properties[i].getName();
        return result;
    }

    /**
     * Connects the specified Java Bean property.
     * @param property Java Bean property to connect.
     * @param options Additional connection options.
     * @return An information about registered attribute.
     */
    protected final GenericAttributeMetadata connectAttribute(final PropertyDescriptor property, final Map<String, String> options){
        return new JavaBeanPropertyMetadata(property, typeInfoBuilder, options);
    }

    /**
     * Connects to the specified attribute.
     *
     * @param attributeName The name of the attribute.
     * @param options       Attribute discovery options.
     * @return The description of the attribute.
     */
    @Override
    protected final GenericAttributeMetadata connectAttributeCore(final String attributeName, final Map<String, String> options) {
        for(final PropertyDescriptor pd: beanMetadata.getPropertyDescriptors())
            if(Objects.equals(pd.getName(), attributeName))
                return connectAttribute(pd, options);
        return null;
    }

    /**
     * Returns the value of the attribute.
     *
     * @param attribute    The metadata of the attribute to get.
     * @param readTimeout
     * @param defaultValue The default value of the attribute if reading fails.
     * @return The value of the attribute.
     * @throws java.util.concurrent.TimeoutException
     *
     */
    @Override
    protected final Object getAttributeValue(final AttributeMetadata attribute, final TimeSpan readTimeout, final Object defaultValue) throws TimeoutException {
        if(attribute instanceof JavaBeanPropertyMetadata)
            try {
                return ((JavaBeanPropertyMetadata)attribute).getValue(beanInstance != null ? beanInstance : this);
            }
            catch (final ReflectiveOperationException e) {
                return null;
            }
        else return null;
    }

    /**
     * Sends the attribute value to the remote agent.
     *
     * @param attribute    The metadata of the attribute to set.
     * @param writeTimeout
     * @param value
     * @return {@literal true} if attribute is overridden successfully; otherwise, {@literal false}.
     */
    @Override
    protected final boolean setAttributeValue(final AttributeMetadata attribute, final TimeSpan writeTimeout, final Object value) {
        if(attribute.canWrite() && attribute instanceof JavaBeanPropertyMetadata){
            try {
                ((JavaBeanPropertyMetadata)attribute).setValue(beanInstance != null ? beanInstance : this, value);
                return true;
            }
            catch (final ReflectiveOperationException e) {
                return false;
            }
        }
        else return false;
    }

    /**
     * Invokes the Java Bean method.
     * @param action An action to execute.
     * @param args Action invocation arguments.
     * @return The invocation result.
     */
    protected final Object doAction(final MethodDescriptor action, final Arguments args){
        try {
            return action.getMethod().invoke(beanInstance != null ? beanInstance : this, args.values().toArray());
        } catch (final ReflectiveOperationException e) {
            return null;
        }
    }

    /**
     * Executes remote action.
     *
     * @param actionName The name of the action,
     * @param args       The invocation arguments.
     * @param timeout    The Invocation timeout.
     * @return The invocation result.
     */
    @Override
    public final Object doAction(final String actionName, final Arguments args, final TimeSpan timeout) throws UnsupportedOperationException, TimeoutException {
        for(final MethodDescriptor md: beanMetadata.getMethodDescriptors())
            if(Objects.equals(md.getName(), actionName))
                return doAction(md, args);
        return null;
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
    protected final void emitNotification(final String category, final Notification.Severity severity, final String message, final Map<String, Object> attachments){
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
     * Attaches the notification listener.
     *
     * @param listId   The identifier of the subscription list.
     * @param listener The notification listener.
     * @return An identifier of the notification listener generated by this connector.
     */
    @Override
    public final Object subscribe(final String listId, final NotificationListener listener) {
        verifyInitialization();
        return notifications.subscribe(listId, listener);
    }

    /**
     * Removes the notification listener.
     *
     * @param listenerId An identifier previously returned by {@link #subscribe(String, com.snamp.connectors.NotificationListener)}.
     * @return {@literal true} if listener is removed successfully; otherwise, {@literal false}.
     */
    @Override
    public final boolean unsubscribe(final Object listenerId) {
        verifyInitialization();
        return notifications.unsubscribe(listenerId);
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        super.close();
        notifications.clear();
    }
}
