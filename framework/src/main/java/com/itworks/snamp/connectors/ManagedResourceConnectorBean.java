package com.itworks.snamp.connectors;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.reflect.TypeToken;
import com.itworks.snamp.*;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import com.itworks.snamp.configuration.InMemoryAgentConfiguration.InMemoryManagedResourceConfiguration.InMemoryAttributeConfiguration;
import com.itworks.snamp.configuration.InMemoryAgentConfiguration.InMemoryManagedResourceConfiguration.InMemoryEventConfiguration;
import com.itworks.snamp.connectors.attributes.AttributeMetadata;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.attributes.AttributeSupportException;
import com.itworks.snamp.connectors.attributes.UnknownAttributeException;
import com.itworks.snamp.connectors.notifications.*;
import com.itworks.snamp.internal.annotations.Internal;
import com.itworks.snamp.internal.annotations.MethodStub;
import com.itworks.snamp.mapping.TypeConverter;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
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
 *     System.out.println(c.getAttribute("001", TimeSpan.INFINITE));//output is: Hello, world!
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
 *     To support custom type (such as {@link com.itworks.snamp.mapping.Table}, {@link Map} or array) you apply do the following steps:
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
public class ManagedResourceConnectorBean extends AbstractManagedResourceConnector<ManagedResourceConnectorBean.ManagedBeanDescriptor<?>>
        implements NotificationSupport, AttributeSupport {

    /**
     * Represents attribute reading or writing context.
     * This class cannot be inherited or instantiated directly in your code.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static final class AttributeContext{
        private final static ThreadLocal<AttributeContext> currentContext = new ThreadLocal<>();
        private final AttributeMetadata metadata;
        private final TimeSpan operationTimeout;

        private AttributeContext(final AttributeMetadata metadata, final TimeSpan operationTimeout){
            this.metadata = metadata;
            this.operationTimeout = operationTimeout;
        }

        /**
         * Gets metadata of the calling attribute.
         * @return The metadata of the calling attribute.
         */
        public AttributeMetadata getMetadata(){
            return metadata;
        }

        /**
         * Gets timeout for the attribute get/set operation.
         * @return The operation timeout.
         */
        public TimeSpan getOperationTimeout(){
            return operationTimeout;
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

        private static void setContext(final AttributeMetadata metadata, final TimeSpan timeout){
            currentContext.set(new AttributeContext(metadata, timeout));
        }

        private static void unsetContext(){
            currentContext.remove();
        }
    }

    /**
     * Represents notification builder that can be used to construct
     * notifications based on the notification context.
     * <p>
     *      This class cannot be instantiated or inherited directly in your code.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static final class NotificationBuilder implements Supplier<JavaBeanNotification>{
        private Severity severity;
        private String message;
        private Object attachment;
        private String correlationID;
        private final JavaBeanEventMetadata metadata;
        private NotificationBuilder nextBuilder;

        private NotificationBuilder(final JavaBeanEventMetadata metadata){
            this.metadata = metadata;
            this.severity = Severity.UNKNOWN;
            this.message = "";
            this.attachment = this.correlationID = null;
            this.nextBuilder = null;
        }

        /**
         * Retrieves an instance of the appropriate type. The returned object may or
         * may not be a new instance, depending on the implementation.
         *
         * @return an instance of the appropriate type
         */
        @Override
        public JavaBeanNotification get() {
            return metadata.createNotification(severity,
                    message,
                    correlationID,
                    attachment,
                    nextBuilder);
        }

        /**
         * Creates a new builder for the correlated notification.
         * @return A new builder for correlated notification.
         */
        public NotificationBuilder newCorrelation(){
            if(nextBuilder == null) {
                nextBuilder = new NotificationBuilder(metadata);
                nextBuilder.setCorrelationID(correlationID);
                nextBuilder.setSeverity(severity);
            }
            return nextBuilder;
        }

        /**
         * Gets the notification metadata.
         * @return The notification metadata.
         */
        public final JavaBeanEventMetadata getMetadata(){
            return metadata;
        }

        /**
         * Sets severity of the notification.
         * @param value The severity of the notification.
         */
        public void setSeverity(final Severity value){
            severity = value;
        }

        /**
         * Sets a descriptive message that describes what happen.
         * @param value A message to be associated with notification.
         */
        public void setMessage(final String value){
            message = value;
        }

        /**
         * Sets additional notification payload.
         * @param value The additional payload.
         */
        public void setAttachment(final Object value){
            attachment = value;
        }

        /**
         * Sets additional notification payload with dynamically defined type.
         * @param value The additional payload.
         * @param valueType The type of the payload.
         * @param <T> The payload type descriptor.
         */
        public final <T extends ManagedEntityType> void setAttachment(final Object value,
                                        final T valueType){
            setAttachment(new ManagedEntityValue<>(value, valueType));
        }

        /**
         * Setup correlation identifier.
         * @param value The correlation identifier.
         */
        public final void setCorrelationID(final String value){
            this.correlationID = value;
        }

        private void emit() {
            metadata.fire(get());
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
         * Gets the name of the public instance parameterless method in {@link WellKnownTypeSystem} class that returns
         * {@link ManagedEntityType} for the specified bean property.
         * @return The name of the public instance method that produces {@link WellKnownTypeSystem} instance
         * for the annotated bean property.
         */
        String typeProvider() default "";
    }

    /**
     * Represents an interface that should be implemented by {@link java.lang.Enum} which represents
     * all possible management event types.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static interface ManagementEvent<E extends ManagementEvent<E>> extends Serializable, Comparable<E>{
        /**
         * Gets a string representation of this event.
         * @return A string representation of this event.
         */
        String getCategory();

        /**
         * Gets severity of this event.
         * @return The severity of this event.
         */
        Severity getSeverity();

        /**
         * Gets attachment type descriptor.
         * @return The attachment type descriptor; or {@literal null} if attachment is not supported.
         */
        ManagedEntityType getAttachmentType();
    }

    private  final static class JavaBeanPropertyMetadata extends GenericAttributeMetadata<ManagedEntityTypeBuilder.AbstractManagedEntityType>{
        private final Map<String, String> properties;
        private final TypeToken<?> propertyType;
        private final Method getter;
        private final Method setter;
        private final Reference<WellKnownTypeSystem> typeBuilder;

        public JavaBeanPropertyMetadata(final PropertyDescriptor descriptor, final WellKnownTypeSystem typeBuilder, final Map<String, String> props){
            super(descriptor.getName());
            properties = new HashMap<>(props);
            properties.put("displayName", descriptor.getDisplayName());
            properties.put("shortDescription", descriptor.getShortDescription());
            propertyType = TypeToken.of(descriptor.getPropertyType());
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

        private Object getValue(final Object beanInstance, final TimeSpan timeout) throws ReflectiveOperationException {
            if(getter == null) return null;
            AttributeContext.setContext(this, timeout);
            try{
                return getter.invoke(beanInstance);
            }
            finally {
                AttributeContext.unsetContext();
            }
        }

        private void setValue(final Object beanInstance, final Object value, final TimeSpan timeout) throws ReflectiveOperationException {
            final TypeConverter<?> converter = getType().getProjection(propertyType);
            if (converter == null) return;
            AttributeContext.setContext(this, timeout);
            try {
                setter.invoke(beanInstance, converter.convertFrom(value));
            } finally {
                AttributeContext.unsetContext();
            }
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

        private static boolean isAttribute(final PropertyDescriptor attr) {
            final Method getter = attr.getReadMethod(), setter = attr.getWriteMethod();
            return getter != null && getter.isAnnotationPresent(ManagementAttribute.class) ||
                    setter != null && setter.isAnnotationPresent(ManagementAttribute.class);
        }
    }

    private static final class JavaBeanNotification extends NotificationImpl {

        private JavaBeanNotification(final Severity severity,
                                    final long sequenceNumber,
                                    final String message,
                                    final String correlationID,
                                    final Object attachment,
                                    final Supplier<JavaBeanNotification> notification){
            super(severity, sequenceNumber, new Date(), message, correlationID, attachment, notification);
        }
    }

    private static final class JavaBeanEventMetadata extends GenericNotificationMetadata{
        private final AtomicLong sequenceCounter;
        private final Map<String, String> options;
        private final NotificationListenerInvoker listenerInvoker;
        private final Supplier<ManagedEntityType> attachmentType;

        private JavaBeanEventMetadata(final String category,
                                     final Map<String, String> options,
                                     final Supplier<ManagedEntityType> attachmentType,
                                     final NotificationListenerInvoker listenerInvoker){
            super(category);
            if(listenerInvoker == null) throw new IllegalArgumentException("listenerInvoker is null.");
            sequenceCounter = new AtomicLong(0L);
            this.options = options != null ? Collections.unmodifiableMap(options) : Collections.<String, String>emptyMap();
            this.listenerInvoker = listenerInvoker;
            this.attachmentType = attachmentType;
        }

        private void fire(final JavaBeanNotification notification){
            fire(notification, listenerInvoker);
        }

        private JavaBeanNotification createNotification(final Severity severity,
                                                        final String message,
                                                        final String correlationID,
                                                        final Object attachment,
                                                        final Supplier<JavaBeanNotification> next){
            return new JavaBeanNotification(severity,
                    sequenceCounter.getAndIncrement(),
                    message, correlationID,
                    attachment,
                    next);
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
         * Detects the attachment type.
         * <p/>
         * This method will be called automatically by SNAMP infrastructure
         * and once for this instance of notification metadata.
         *
         * @return The attachment type.
         */
        @Override
        protected ManagedEntityType detectAttachmentType() {
            return attachmentType.get();
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
        private final Logger logger;
        private final Collection<? extends ManagementEvent<?>> wellKnownEvents;

        private JavaBeanNotificationSupport(final Collection<? extends ManagementEvent<?>> events,
                                            final Logger logger){
            this.wellKnownEvents = events;
            this.logger = logger;
        }

        /**
         * Reports an error when disabling notifications.
         *
         * @param listID Subscription list identifier.
         * @param e      Internal connector error.
         * @see #failedToDisableNotifications(java.util.logging.Logger, java.util.logging.Level, String, Exception)
         */
        @Override
        protected void failedToDisableNotifications(final String listID, final Exception e) {
            failedToDisableNotifications(logger, Level.WARNING, listID, e);
        }

        /**
         * Reports an error when enabling notifications.
         *
         * @param listID   Subscription list identifier.
         * @param category An event category.
         * @param e        Internal connector error.
         * @see #failedToEnableNotifications(java.util.logging.Logger, java.util.logging.Level, String, String, Exception)
         */
        @Override
        protected void failedToEnableNotifications(final String listID, final String category, final Exception e) {
            failedToEnableNotifications(logger, Level.WARNING, listID, category, e);
        }

        /**
         * Reports an error when subscribing the listener.
         *
         * @param listenerID Subscription list identifier.
         * @param e          Internal connector error.
         * @see #failedToSubscribe(java.util.logging.Logger, java.util.logging.Level, String, Exception)
         */
        @Override
        protected void failedToSubscribe(final String listenerID, final Exception e) {
            failedToSubscribe(logger, Level.WARNING, listenerID, e);
        }

        private void emitNotification(final String category,
                                      final Severity severity,
                                      final String message,
                                      final String correlationID,
                                      final Object attachment) {
            emitNotification(category, new SafeConsumer<NotificationBuilder>() {
                @Override
                public void accept(final NotificationBuilder value) {
                    value.setSeverity(severity);
                    value.setMessage(message);
                    value.setAttachment(attachment);
                    value.setCorrelationID(correlationID);
                }
            });
        }

        private <E extends Exception> void emitNotification(final String category,
                                                            final Consumer<NotificationBuilder, E> notificationBuilder) throws E {
            for (final JavaBeanEventMetadata eventMetadata : getEnabledNotifications(category, JavaBeanEventMetadata.class).values()) {
                final NotificationBuilder builder = new NotificationBuilder(eventMetadata);
                notificationBuilder.accept(builder);
                builder.emit();
            }
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
            //find the appropriate notification descriptor to detect the attachment type
            for (final ManagementEvent<?> ev : wellKnownEvents)
                if (Objects.equals(ev.getCategory(), category))
                    return new JavaBeanEventMetadata(category, options, Suppliers.ofInstance(ev.getAttachmentType()), createListenerInvoker());
            return new JavaBeanEventMetadata(category, options, Suppliers.<ManagedEntityType>ofInstance(null), createListenerInvoker());
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

    private static final class JavaBeanAttributeSupport extends AbstractAttributeSupport {
        private final WellKnownTypeSystem typeSystem;
        private final ManagedBeanDescriptor<?> descriptor;
        private final Logger logger;

        private JavaBeanAttributeSupport(final ManagedBeanDescriptor<?> descriptor,
                                        final WellKnownTypeSystem typeSystem,
                                        final Logger logger) {
            this.typeSystem = typeSystem;
            this.descriptor = descriptor;
            this.logger = logger;
        }

        /**
         * Reports an error when connecting attribute.
         *
         * @param attributeID   The attribute identifier.
         * @param attributeName The name of the attribute.
         * @param e             Internal connector error.
         * @see #failedToConnectAttribute(java.util.logging.Logger, java.util.logging.Level, String, String, Exception)
         */
        @Override
        protected void failedToConnectAttribute(final String attributeID, final String attributeName, final Exception e) {
            failedToConnectAttribute(logger, Level.WARNING, attributeID, attributeName, e);
        }

        /**
         * Reports an error when getting attribute.
         *
         * @param attributeID The attribute identifier.
         * @param e           Internal connector error.
         * @see #failedToGetAttribute(java.util.logging.Logger, java.util.logging.Level, String, Exception)
         */
        @Override
        protected void failedToGetAttribute(final String attributeID, final Exception e) {
             failedToGetAttribute(logger, Level.WARNING, attributeID, e);
        }

        /**
         * Reports an error when updating attribute.
         *
         * @param attributeID The attribute identifier.
         * @param value       The value of the attribute.
         * @param e           Internal connector error.
         * @see #failedToSetAttribute(java.util.logging.Logger, java.util.logging.Level, String, Object, Exception)
         */
        @Override
        protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
            failedToSetAttribute(logger, Level.WARNING, attributeID, value, e);
        }

        /**
         * Connects the specified Java Bean property.
         *
         * @param property Java Bean property to connect.
         * @param options  Additional connection options.
         * @return An information about registered attribute.
         */
        protected final GenericAttributeMetadata connectAttribute(final PropertyDescriptor property, final Map<String, String> options) {
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
            for (final PropertyDescriptor pd : descriptor.getBeanInfo().getPropertyDescriptors())
                if (Objects.equals(pd.getName(), attributeName))
                    return connectAttribute(pd, options);
            return null;
        }

        /**
         * Returns the value of the attribute.
         *
         * @param attribute   The metadata of the attribute to get.
         * @param readTimeout Attribute read timeout.
         * @return The value of the attribute.
         * @throws java.lang.ReflectiveOperationException Unable to get attribute value.
         */
        @Override
        protected final Object getAttributeValue(final AttributeMetadata attribute, final TimeSpan readTimeout) throws ReflectiveOperationException {
            if (attribute.canRead() && attribute instanceof JavaBeanPropertyMetadata)
                    return ((JavaBeanPropertyMetadata) attribute).getValue(descriptor.getInstance(), readTimeout);
            else
                throw new ReflectiveOperationException(String.format("Attribute %s is write-only", attribute.getName()));
        }

        /**
         * Sends the attribute value to the remote agent.
         *
         * @param attribute    The metadata of the attribute to set.
         * @param writeTimeout Attribute write timeout.
         * @param value        A new attribute value.
         * @throws java.lang.ReflectiveOperationException Unable to update attribute.
         */
        @Override
        protected final void setAttributeValue(final AttributeMetadata attribute, final TimeSpan writeTimeout, final Object value) throws ReflectiveOperationException {
            if (attribute.canWrite() && attribute instanceof JavaBeanPropertyMetadata)
                ((JavaBeanPropertyMetadata) attribute).setValue(descriptor.getInstance(), value, writeTimeout);
            else
                throw new ReflectiveOperationException(String.format("Attribute %s is read-only", attribute.getName()));
        }
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

    private final JavaBeanNotificationSupport notifications;
    private final JavaBeanAttributeSupport attributes;

    private ManagedResourceConnectorBean(final WellKnownTypeSystem typeBuilder,
                                         final Collection<? extends ManagementEvent<?>> wellKnownEvents,
                                         final Logger logger) throws IntrospectionException{
        super(new SelfDescriptor(), logger);
        //creates weak reference to this object
        safeCast(getConnectionOptions(), SelfDescriptor.class).setSelfReference(this);
        if (typeBuilder == null) throw new IllegalArgumentException("typeBuilder is null.");
        this.attributes = new JavaBeanAttributeSupport(getConnectionOptions(), typeBuilder, logger);
        this.notifications = new JavaBeanNotificationSupport(wellKnownEvents, logger);
    }

    /**
     * Initializes a new management connector that reflects properties of this class as
     * connector managementAttributes.
     * @param typeBuilder Type information provider that provides property type converter.
     * @param logger A logger for this management connector.
     * @throws IllegalArgumentException typeBuilder is {@literal null}.
     */
    protected ManagedResourceConnectorBean(final WellKnownTypeSystem typeBuilder,
                                           final Logger logger) throws IntrospectionException {
        this(typeBuilder, Collections.<ManagementEvent<?>>emptyList(), logger);
    }

    /**
     * Initializes a new management connector that reflects properties of this class as
     * connector managementAttributes.
     * @param typeBuilder Type information provider that provides property type converter.
     * @param logger A logger for this management connector.
     * @param wellKnownEvents A set of well-known events (notifications).
     * @throws IllegalArgumentException typeBuilder is {@literal null}.
     */
    protected <E extends Enum<E> & ManagementEvent<E>> ManagedResourceConnectorBean(final WellKnownTypeSystem typeBuilder,
                                                                                    final Set<E> wellKnownEvents,
                                                                                    final Logger logger) throws IntrospectionException {
        this(typeBuilder, (Collection<? extends ManagementEvent<?>>) wellKnownEvents, logger);
    }

    /**
     * Initializes a new management connector that reflects properties of the specified instance
     * as connector managementAttributes.
     * @param beanInstance An instance of JavaBean to reflect. Cannot be {@literal null}.
     * @param introspector An introspector that reflects the specified JavaBean. Cannot be {@literal null}.
     * @param typeBuilder Type information provider that provides property type converter. Cannot be {@literal null}.
     * @throws IntrospectionException Cannot reflect the specified instance.
     * @throws IllegalArgumentException At least one of the specified arguments is {@literal null}.
     */
    protected ManagedResourceConnectorBean(final Object beanInstance,
                                               final BeanIntrospector introspector,
                                               final WellKnownTypeSystem typeBuilder,
                                               final Logger logger) throws IntrospectionException {
        super(new BeanDescriptor<>(beanInstance, introspector), logger);
        if (typeBuilder == null) throw new IllegalArgumentException("typeBuilder is null.");
        this.attributes = new JavaBeanAttributeSupport(getConnectionOptions(), typeBuilder, logger);
        this.notifications = new JavaBeanNotificationSupport(Collections.<ManagementEvent<?>>emptyList(), logger);
    }

    /**
     * Creates SNAMP management connector from the specified Java Bean.
     * @param beanInstance An instance of the Java Bean to wrap.
     * @param typeBuilder Bean property type converter.
     * @param <T> Type of the Java Bean to wrap.
     * @return A new instance of the management connector that wraps the Java Bean.
     * @throws IntrospectionException Cannot reflect the specified instance.
     */
    public static <T> ManagedResourceConnectorBean wrap(final T beanInstance, final WellKnownTypeSystem typeBuilder) throws IntrospectionException {
        return new ManagedResourceConnectorBean(beanInstance, new StandardBeanIntrospector(), typeBuilder, Logger.getLogger(getLoggerName("javabean")));
    }

    /**
     * Returns an array of all discovered managementAttributes available for registration.
     * @return An array of all discovered managementAttributes available for registration.
     */
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
     * @return The metadata of the event to listen;
     * @throws com.itworks.snamp.connectors.notifications.NotificationSupportException Internal connector error.
     * @throws java.lang.IllegalStateException Resource connector is closed.
     */
    @Override
    public final NotificationMetadata enableNotifications(final String listId, final String category, final Map<String, String> options) throws NotificationSupportException{
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
     *       emitNotification("propertyChanged", Notification.Severity.NOTICE, String.format("Property %s changed", propertyName), null, attachments);
     *     }
     *     }</pre>
     * @param category The category of the event to raise.
     * @param severity The severity of the event to raise.
     * @param message Human-readable description of the event.
     * @param correlationID Notification correlation identifier.
     * @param attachment Additional payload that can be associated with the notification. May be {@literal null}.
     */
    protected final void emitNotification(final String category,
                                          final Severity severity,
                                          final String message,
                                          final String correlationID,
                                          final Object attachment){
        notifications.emitNotification(category, severity, message, correlationID, attachment);
    }

    /**
     * Raises notification.
     * @param event The category of the event to raise.
     * @param message Human-readable description of the event.
     * @param correlationID Notification correlation identifier.
     * @param attachment Additional payload that can be associated with the notification. May be {@literal null}.
     * @param <E> Type of the event to emit.
     */
    protected final <E extends Enum<E> & ManagementEvent<E>> void emitNotification(final E event,
                                                                                   final String message,
                                                                                   final String correlationID,
                                                                                   final Object attachment){
        emitNotification(event.getCategory(), event.getSeverity(), message, correlationID, attachment);
    }

    /**
     * Raises notification.
     * @param category The category of the event to raise.
     * @param notificationBuilder An object that fills the notification content.
     * @param <E> Type of the exception that can be produced by builder.
     * @throws E Unable to emit notification.
     */
    protected final <E extends Exception> void emitNotification(final String category,
                                                                final Consumer<NotificationBuilder, E> notificationBuilder) throws E{
        notifications.emitNotification(category, notificationBuilder);
    }

    /**
     * Disables event listening for the specified category of events.
     * <p>
     * This method removes all listeners associated with the specified subscription list.
     * </p>
     *
     * @param listId The identifier of the subscription list.
     * @return {@literal true}, if notifications for the specified category is previously enabled; otherwise, {@literal false}.
     * @throws com.itworks.snamp.connectors.notifications.NotificationSupportException Internal connector error.
     */
    @Override
    public final boolean disableNotifications(final String listId) throws NotificationSupportException{
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
     * @throws com.itworks.snamp.connectors.notifications.NotificationSupportException Internal connector error.
     */
    @Override
    public final void subscribe(final String listenerId, final NotificationListener listener, final boolean delayed) throws NotificationSupportException{
        verifyInitialization();
        notifications.subscribe(listenerId, listener, delayed);
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
     * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
     */
    @Override
    public final AttributeMetadata connectAttribute(final String id, final String attributeName, final Map<String, String> options) throws AttributeSupportException {
        verifyInitialization();
        return attributes.connectAttribute(id, attributeName, options);
    }

    /**
     * Returns the attribute value.
     *
     * @param id           A key string that is used to invoke attribute from this connector.
     * @param readTimeout  The attribute value invoke operation timeout.
     * @return The value of the attribute, or default value.
     * @throws java.util.concurrent.TimeoutException The attribute value cannot be obtained in the specified time constraint.
     * @throws com.itworks.snamp.connectors.attributes.UnknownAttributeException The requested attribute doesn't exist.
     * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
     */
    @Override
    public final Object getAttribute(final String id, final TimeSpan readTimeout) throws TimeoutException, UnknownAttributeException, AttributeSupportException {
        verifyInitialization();
        return attributes.getAttribute(id, readTimeout);
    }

    /**
     * Reads a set of managementAttributes.
     *
     * @param output      The dictionary with set of attribute keys to invoke and associated default values.
     * @param readTimeout The attribute value invoke operation timeout.
     * @return The set of managementAttributes ids really written to the dictionary.
     * @throws java.util.concurrent.TimeoutException The attribute value cannot be invoke in the specified duration.
     * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
     */
    @Override
    public final Set<String> getAttributes(final Map<String, Object> output, final TimeSpan readTimeout) throws TimeoutException, AttributeSupportException {
        verifyInitialization();
        return attributes.getAttributes(output, readTimeout);
    }

    /**
     * Writes the value of the specified attribute.
     *
     * @param id           An identifier of the attribute,
     * @param writeTimeout The attribute value write operation timeout.
     * @param value        The value to write.
     * @throws java.util.concurrent.TimeoutException The attribute value cannot be write in the specified duration.
     * @throws com.itworks.snamp.connectors.attributes.UnknownAttributeException The requested attribute doesn't exist.
     * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
     */
    @Override
    public final void setAttribute(final String id, final TimeSpan writeTimeout, final Object value) throws TimeoutException, AttributeSupportException, UnknownAttributeException {
        verifyInitialization();
        attributes.setAttribute(id, writeTimeout, value);
    }

    /**
     * Writes a set of managementAttributes inside of the transaction.
     *
     * @param values       The dictionary of managementAttributes keys and its values.
     * @param writeTimeout Attribute write timeout.
     * @throws TimeoutException The attribute value cannot be written in the specified time constraint.
     * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
     */
    @Override
    public final void setAttributes(final Map<String, Object> values, final TimeSpan writeTimeout) throws TimeoutException, AttributeSupportException {
        verifyInitialization();
        attributes.setAttributes(values, writeTimeout);
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

    /**
     * Extracts information about attributes.
     * @param info An information about JavaBean. Cannot be {@literal null}.
     * @return A collection of discovered attributes.
     * @see com.itworks.snamp.connectors.discovery.DiscoveryService
     */
    public static Collection<AttributeConfiguration> discoverAttributes(final BeanInfo info) {
        final PropertyDescriptor[] attributes = info.getPropertyDescriptors();
        final Collection<AttributeConfiguration> config = new ArrayList<>(attributes.length);
        for (final PropertyDescriptor attr : attributes)
            if (JavaBeanPropertyMetadata.isAttribute(attr))
                config.add(new InMemoryAttributeConfiguration(attr.getName()));
        return config;
    }

    /**
     * Extracts information about attributes.
     * @param beanType A type of JavaBean to reflect. Cannot be {@literal null}.
     * @param introspector An introspector that reflects the specified JavaBean. Cannot be {@literal null}.
     * @return A collection of extracted attributes.
     * @throws IntrospectionException Unable to reflect JavaBean.
     * @see com.itworks.snamp.connectors.discovery.DiscoveryService
     */
    public static Collection<AttributeConfiguration> discoverAttributes(final Class<?> beanType,
                                                                            final BeanIntrospector introspector) throws IntrospectionException{
        return discoverAttributes(introspector.getBeanInfo(beanType));
    }

    /**
     * Extracts information about attributes.
     * @param bean A type of JavaBean to reflect. Cannot be {@literal null}.
     * @return A collection of extracted attributes.
     * @throws IntrospectionException Unable to reflect JavaBean.
     * @see com.itworks.snamp.connectors.discovery.DiscoveryService
     */
    public static Collection<AttributeConfiguration> discoverAttributes(final Class<?> bean) throws IntrospectionException {
        return discoverAttributes(bean, new StandardBeanIntrospector());
    }

    private static <E extends Enum<E> & ManagementEvent<E>> Collection<EventConfiguration> discoverEvents(final Set<E> notificationTypes){
        final Collection<EventConfiguration> result = new ArrayList<>(notificationTypes.size());
        for(final E notif: notificationTypes)
            result.add(new InMemoryEventConfiguration(notif.getCategory()));
        return result;
    }

    /**
     * Extracts information about notifications.
     * @param notificationTypes A type of the enum which describes notification types. Cannot be {@literal null}.
     * @param <E> A type of the enum which describes notification types. Cannot be {@literal null}.
     * @return A collection of discovered events.
     * @see com.itworks.snamp.connectors.discovery.DiscoveryService
     */
    protected static <E extends Enum<E> & ManagementEvent<E>> Collection<EventConfiguration> discoverEvents(final Class<E> notificationTypes){
        return discoverEvents(EnumSet.allOf(notificationTypes));
    }

    /**
     * Gets current attribute context.
     * <p>
     *     You should call this method inside of bean property getter or setter.
     * @return The current attribute context.
     */
    protected static AttributeContext getAttributeContext(){
        return AttributeContext.get();
    }
}
