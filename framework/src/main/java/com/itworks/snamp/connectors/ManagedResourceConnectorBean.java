package com.itworks.snamp.connectors;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.itworks.snamp.Descriptive;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.concurrent.ThreadLocalStack;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeDescriptorRead;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.notifications.*;
import com.itworks.snamp.internal.Utils;
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
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents SNAMP in-process management connector that exposes
 * Java Bean properties through connector managementAttributes.
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
public abstract class ManagedResourceConnectorBean extends AbstractManagedResourceConnector
        implements NotificationSupport, AttributeSupport {

    /**
     * Represents notification supported by this resource connector.
     * This class cannot be instantiated or inherited directly in your code.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static final class JavaBeanNotification implements AutoCloseable{
        private final static ThreadLocalStack<JavaBeanNotification> currentContext = new ThreadLocalStack<>();
        private final ManagementNotificationType<?> metadata;

        private JavaBeanNotification(final ManagementNotificationType<?> metadata){
            this.metadata = metadata;
            currentContext.push(this);
        }

        public ManagementNotificationType<?> getMetadata(){
            return metadata;
        }

        public static JavaBeanNotification current(){
            return currentContext.top();
        }

        @Override
        public void close() throws NoSuchElementException{
            currentContext.pop();
        }
    }

    /**
     * Represents description of the bean to be managed by this connector.
     * <p>You should not implement this interface directly.</p>
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    private interface ManagedBeanDescriptor<T>{
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
        private final BeanInfo metadata;

        private SelfDescriptor(final ManagedResourceConnectorBean instance) throws IntrospectionException {
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
     * Describes management notification type supported by this connector.
     * @param <T> Well-known type of the user data to be associated with each notification.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected interface ManagementNotificationType<T> extends Descriptive{
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

    private enum EmptyManagementNotificationType implements ManagementNotificationType<Void>{
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
    protected interface ManagementAttributeFormatter<T>{
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
        Object fromJmxValue(final T jmxValue);
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
    protected @interface ManagementAttribute {
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
    protected interface BeanIntrospector{
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

    /**
     * Represents factory for the user context associated with attribute.
     * @param <T> Type of the user context.
     * @param <E> Type of the exception that can be produced by initializer.
     */
    protected interface AttributeUserDataFactory<T, E extends Throwable>{
        /**
         * Creates a new user context.
         * @param metadata The metadata of the attribute.
         * @return User context.
         * @throws E Unable to create user context.
         */
        T createUserData(final AttributeDescriptorRead metadata) throws E;
    }

    /**
     * Represents an attribute declared as a Java property in this resource connector.
     * This class cannot be inherited or instantiated directly in your code.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static class JavaBeanAttributeInfo extends MBeanAttributeInfo implements AttributeDescriptorRead{
        private static final long serialVersionUID = -5047097712279607039L;
        private static final ThreadLocalStack<JavaBeanAttributeInfo> currentAttribute = new ThreadLocalStack<>();
        private final MethodHandle getter;
        private final MethodHandle setter;
        private volatile Object userData;

        /**
         * Represents attribute formatter.
         */
        protected final ManagementAttributeFormatter formatter;

        private JavaBeanAttributeInfo(final String attributeName,
                                      final PropertyDescriptor property,
                                      final AttributeDescriptor descriptor,
                                      final Object owner) throws ReflectionException {
            super(attributeName,
                    property.getPropertyType().getName(),
                    getDescription(property, descriptor),
                    canRead(property),
                    canWrite(property),
                    isFlag(property),
                    descriptor);
            final Method getter = property.getReadMethod();
            final Method setter = property.getWriteMethod();
            final ManagementAttribute info = getAdditionalInfo(getter, setter);
            if(info != null)
                try {
                    final Class<? extends ManagementAttributeFormatter> formatterClass =
                            info.formatter();
                    this.formatter = Objects.equals(formatterClass, DefaultManagementAttributeFormatter.class) ?
                            new DefaultManagementAttributeFormatter():
                            formatterClass.newInstance();
                } catch (final ReflectiveOperationException e){
                    throw new ReflectionException(e);
                }
            else formatter = new DefaultManagementAttributeFormatter();
            final MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            try{
                this.setter = setter != null ? lookup.unreflect(setter).bindTo(owner): null;
                this.getter = getter != null ? lookup.unreflect(getter).bindTo(owner): null;
            } catch (final IllegalAccessException e) {
                throw new ReflectionException(e);
            }
        }

        /**
         * Gets currently executing attribute.
         * <p>
         *     This method can be called inside of the property getter or setter only.
         * @return The currently executing attribute.
         */
        public static JavaBeanAttributeInfo current(){
            return currentAttribute.top();
        }

        final void clear(){
            userData = null;
        }

        /**
         * Gets user data associated with this attribute.
         * @return The user data associated with this attribute.
         */
        public final Object getUserData(){
            return userData;
        }

        /**
         * Gets or creates user data associated with this attribute.
         * @param factory The factory used to initialize user data.
         * @param userDataType The type of the user data.
         * @param <T> Type of the user data.
         * @param <E> Type of the exception that may be raised by factory.
         * @return The user data associated with this attribute.
         * @throws E An exception by factory.
         */
        public final <T, E extends Throwable> T getUserData(final AttributeUserDataFactory<T, E> factory,
                                                            final Class<T> userDataType) throws E {
            Object userData = this.userData;
            if (userDataType.isInstance(userData))
                return userDataType.cast(userData);
            else synchronized (this) {
                userData = this.userData;
                if (userDataType.isInstance(userData))
                    return userDataType.cast(userData);
                else {
                    final T ud = factory.createUserData(this);
                    this.userData = ud;
                    return ud;
                }
            }
        }

        /**
         * Returns the descriptor for the feature.  Changing the returned value
         * will have no affect on the original descriptor.
         *
         * @return a descriptor that is either immutable or a copy of the original.
         */
        @Override
        public final AttributeDescriptor getDescriptor() {
            return Utils.safeCast(super.getDescriptor(), AttributeDescriptor.class);
        }

        private void pushThis(){
            currentAttribute.push(this);
        }

        private static void pop(){
            currentAttribute.pop();
        }

        final Object getValue() throws ReflectionException{
            if(getter != null) {
                pushThis();
                try {
                    return formatter.toJmxValue(getter.invoke());
                } catch (final Exception e) {
                    throw new ReflectionException(e);
                } catch (final Error e) {
                    throw e;
                } catch (final Throwable e) {
                    throw new ReflectionException(new UndeclaredThrowableException(e));
                } finally {
                    pop();
                }
            }
            else throw new ReflectionException(new UnsupportedOperationException("Attribute is write-only"));
        }

        @SuppressWarnings("unchecked")
        final void setValue(final Object value) throws ReflectionException, InvalidAttributeValueException {
            if (setter != null) {
                pushThis();
                try {
                    setter.invoke(formatter.fromJmxValue(value));
                } catch (final IllegalArgumentException e) {
                    throw new InvalidAttributeValueException(e.getMessage());
                } catch (final Exception e) {
                    throw new ReflectionException(e);
                } catch (final Error e) {
                    throw e;
                } catch (final Throwable e) {
                    throw new ReflectionException(new UndeclaredThrowableException(e));
                } finally {
                    pop();
                }
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
        private static final long serialVersionUID = -4173983412042130772L;
        private final OpenType<?> openType;

        private JavaBeanOpenAttributeInfo(final String attributeName,
                                          final PropertyDescriptor property,
                                          final AttributeDescriptor descriptor,
                                          final Object owner) throws ReflectionException, OpenDataException {
            super(attributeName, property, descriptor, owner);
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

        private JavaBeanAttributeSupport(final String resourceName,
                                         final ManagedBeanDescriptor<?> beanDesc,
                                         final Logger logger){
            super(resourceName, JavaBeanAttributeInfo.class);
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
                        return new JavaBeanOpenAttributeInfo(attributeID, property, descriptor, bean.getInstance());
                    }
                    catch (final OpenDataException e){
                        //bean property type is not Open Type
                        return new JavaBeanAttributeInfo(attributeID, property, descriptor, bean.getInstance());
                    }
            throw JMExceptionUtils.attributeNotFound(descriptor.getAttributeName());
        }

        @Override
        protected void failedToConnectAttribute(final String attributeID, final String attributeName, final Exception e) {
            failedToConnectAttribute(logger, Level.SEVERE, attributeID, attributeName, e);
        }

        @Override
        protected Object getAttribute(final JavaBeanAttributeInfo metadata) throws ReflectionException {
            return metadata.getValue();
        }

        @Override
        protected void failedToGetAttribute(final String attributeID, final Exception e) {
            failedToGetAttribute(logger, Level.WARNING, attributeID, e);
        }

        @Override
        protected void setAttribute(final JavaBeanAttributeInfo attribute, final Object value) throws ReflectionException, InvalidAttributeValueException {
            attribute.setValue(value);
        }

        @Override
        protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
            failedToSetAttribute(logger, Level.WARNING, attributeID, value, e);
        }

        @Override
        protected boolean disconnectAttribute(final JavaBeanAttributeInfo attributeInfo) {
            attributeInfo.clear();
            return true;
        }
    }

    private static final class JavaBeanNotificationSupport extends AbstractNotificationSupport<CustomNotificationInfo>{
        private final Logger logger;
        private final Set<? extends ManagementNotificationType<?>> notifTypes;
        private final NotificationListenerInvoker listenerInvoker;

        private JavaBeanNotificationSupport(final String resourceName,
                                            final Set<? extends ManagementNotificationType<?>> notifTypes,
                                            final Logger logger){
            super(resourceName, CustomNotificationInfo.class);
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
                return new CustomNotificationInfo(category, description, metadata.setUserDataType(type.getUserDataType()));
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
            try(final JavaBeanNotification ignored = new JavaBeanNotification(category)){
                fire(category.getCategory(), message, userData);
            }
        }
    }

    private final JavaBeanAttributeSupport attributes;
    private final JavaBeanNotificationSupport notifications;

    private ManagedResourceConnectorBean(final String resourceName,
                                         ManagedBeanDescriptor<?> descriptor,
                                         final Set<? extends ManagementNotificationType<?>> notifTypes) throws IntrospectionException {
        if(descriptor == null) descriptor = new SelfDescriptor(this);
        attributes = new JavaBeanAttributeSupport(resourceName, descriptor, getLogger());
        notifications = new JavaBeanNotificationSupport(resourceName, notifTypes, getLogger());
    }

    /**
     * Initializes a new managed resource connector that reflects properties of the specified instance
     * as connector managementAttributes.
     * @param resourceName The name of the managed resource served by this connector.
     * @param beanInstance An instance of JavaBean to reflect. Cannot be {@literal null}.
     * @param introspector An introspector that reflects the specified JavaBean. Cannot be {@literal null}.
     * @throws IntrospectionException Cannot reflect the specified instance.
     * @throws IllegalArgumentException At least one of the specified arguments is {@literal null}.
     */
    protected ManagedResourceConnectorBean( final String resourceName,
                                            final Object beanInstance,
                                               final BeanIntrospector introspector) throws IntrospectionException {
        this(resourceName,
                new BeanDescriptor<>(beanInstance, introspector),
                Collections.<ManagementNotificationType<?>>emptySet());
    }

    /**
     * Initializes a new managed resource connector that reflects itself.
     * @param resourceName The name of the managed resource served by this connector.
     * @throws IntrospectionException Unable to reflect managed resource connector.
     */
    protected ManagedResourceConnectorBean(final String resourceName) throws IntrospectionException {
        this(resourceName, EnumSet.noneOf(EmptyManagementNotificationType.class));
    }

    /**
     * Initializes a new managed resource connector that reflects itself.
     * @param resourceName The name of the managed resource served by this connector.
     * @param notifTypes A set of notifications supported by this connector.
     * @param <N> Type of the notification category provider.
     * @throws IntrospectionException Unable to reflect managed resource connector.
     */
    protected <N extends Enum<N> & ManagementNotificationType<?>> ManagedResourceConnectorBean(final String resourceName,
                                                                                               final EnumSet<N> notifTypes) throws IntrospectionException {
        this(resourceName, null, notifTypes);
    }

    /**
     * Creates SNAMP management connector from the specified Java Bean.
     * @param resourceName The name of the managed resource served by this connector.
     * @param connectorName The name of the managed resource connector.
     * @param beanInstance An instance of the Java Bean to wrap.
     * @param <T> Type of the Java Bean to wrap.
     * @return A new instance of the management connector that wraps the Java Bean.
     * @throws IntrospectionException Cannot reflect the specified instance.
     */
    public static <T> ManagedResourceConnectorBean wrap(final String resourceName,
                                                        final String connectorName,
                                                        final T beanInstance) throws IntrospectionException {
        return new ManagedResourceConnectorBean(resourceName, beanInstance, new StandardBeanIntrospector()){

            @Override
            public Logger getLogger() {
                return getLogger(connectorName);
            }
        };
    }

    /**
     * Adds a new listener for the connector-related events.
     * <p/>
     * The managed resource connector should holds a weak reference to all added event listeners.
     *
     * @param listener An event listener to add.
     */
    @Override
    public final void addResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, attributes, notifications);
    }

    /**
     * Removes connector event listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public final void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, attributes, notifications);
    }

    /**
     * Gets subscription model.
     *
     * @return The subscription model.
     */
    @Override
    public final NotificationSubscriptionModel getSubscriptionModel() {
        return notifications.getSubscriptionModel();
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
    public final void removeNotificationListener(final NotificationListener listener) throws ListenerNotFoundException {
        verifyInitialization();
        notifications.removeNotificationListener(listener);
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
    public final void addNotificationListener(final NotificationListener listener, final NotificationFilter filter, final Object handback) throws IllegalArgumentException {
        verifyInitialization();
        notifications.addNotificationListener(listener, filter, handback);
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
    public final boolean disableNotifications(final String listId) {
        verifyInitialization();
        return notifications.disableNotifications(listId);
    }

    /**
     * Enables event listening for the specified category of events.
     * <p>
     * category can be used for enabling notifications for the same category
     * but with different options.
     * <p>
     * listId parameter
     * is used as a value of {@link javax.management.Notification#getType()}.
     *
     * @param listId   An identifier of the subscription list.
     * @param category The name of the event category to listen.
     * @param options  Event discovery options.
     * @return The metadata of the event to listen; or {@literal null}, if the specified category is not supported.
     */
    public final MBeanNotificationInfo enableNotifications(final String listId,
                                                           final String category,
                                                           final CompositeData options) {
        verifyInitialization();
        return notifications.enableNotifications(listId, category, options);
    }

    /**
     * Removes the attribute from the connector.
     *
     * @param id The unique identifier of the attribute.
     * @return {@literal true}, if the attribute successfully disconnected; otherwise, {@literal false}.
     */
    public final boolean removeAttribute(final String id) {
        return attributes.removeAttribute(id);
    }

    /**
     * Connects to the specified attribute.
     *
     * @param id               A key string that is used to invoke attribute from this connector.
     * @param attributeName    The name of the attribute.
     * @param readWriteTimeout A read/write timeout using for attribute read/write operation.
     * @param options          The attribute discovery options.
     * @return The description of the attribute.
     */
    public final MBeanAttributeInfo addAttribute(final String id,
                                                 final String attributeName,
                                                 final TimeSpan readWriteTimeout,
                                                 final CompositeData options) {
        verifyInitialization();
        return attributes.addAttribute(id, attributeName, readWriteTimeout, options);
    }

    /**
     * Removes all attributes from this connector.
     */
    public final void removeAllAttributes(){
        verifyInitialization();
        attributes.clear(false);
    }

    /**
     * Removes all notifications and listeners from this connector.
     */
    public final void removeAllNotifications(){
        verifyInitialization();
        notifications.clear(true, false);
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

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the aggregated object.
     * @return An instance of the requested object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(final Class<T> objectType) {
        return findObject(objectType, new Function<Class<T>, T>() {
            @Override
            public T apply(final Class<T> objectType) {
                return ManagedResourceConnectorBean.super.queryObject(objectType);
            }
        }, attributes, notifications);
    }
}
