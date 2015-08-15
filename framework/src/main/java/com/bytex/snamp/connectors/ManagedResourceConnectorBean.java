package com.bytex.snamp.connectors;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.Descriptive;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.connectors.attributes.*;
import com.bytex.snamp.connectors.discovery.DiscoveryResultBuilder;
import com.bytex.snamp.connectors.discovery.DiscoveryService;
import com.bytex.snamp.connectors.notifications.*;
import com.bytex.snamp.connectors.operations.AbstractOperationSupport;
import com.bytex.snamp.connectors.operations.OperationDescriptor;
import com.bytex.snamp.connectors.operations.OperationSupport;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.CompositeDataUtils;
import com.bytex.snamp.jmx.JMExceptionUtils;
import com.bytex.snamp.jmx.WellKnownType;

import javax.management.*;
import javax.management.openmbean.*;
import java.beans.*;
import java.beans.IntrospectionException;
import java.lang.annotation.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.configuration.SerializableAgentConfiguration.SerializableManagedResourceConfiguration.*;
import static com.bytex.snamp.connectors.ConfigurationEntityRuntimeMetadata.AUTOMATICALLY_ADDED_FIELD;

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
        implements NotificationSupport, AttributeSupport, OperationSupport {
    private static final CompositeData AUTO_PROPS = Utils.interfaceStaticInitialize(new Callable<CompositeData>() {
        @Override
        public CompositeData call() throws OpenDataException {
            return CompositeDataUtils.create(ImmutableMap.of(AUTOMATICALLY_ADDED_FIELD, Boolean.TRUE.toString()), SimpleType.STRING);
        }
    });

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
        private final Reference<ManagedResourceConnectorBean> connectorRef;
        private final BeanInfo metadata;

        private SelfDescriptor(final ManagedResourceConnectorBean instance) throws IntrospectionException {
            connectorRef = new WeakReference<>(instance);
            metadata = reflect(instance.getClass());
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
            return connectorRef.get();
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
     * Represents provider of JMX Open Type.
     * @param <T> Underlying Java native type.
     */
    protected interface OpenTypeProvider<T>{
        /**
         * Gets open type.
         * @return JMX Open Type.
         */
        OpenType<T> getOpenType();
    }

    /**
     * Represents attribute marshaller for custom attribute types.
     * @param <T> JMX-compliant type.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected interface ManagementAttributeMarshaller<T> extends OpenTypeProvider<T>{
        /**
         * Gets JMX-compliant type of the attribute.
         * @return JMX-compliant type of the attribute.
         */
        OpenType<T> getOpenType();

        /**
         * Converts attribute value to the JMX-compliant type.
         * @param attributeValue The value of the attribute.
         * @param metadata The metadata of the bean property.
         * @return JMX-compliant attribute value.
         */
        T toJmxValue(final Object attributeValue, final CustomAttributeInfo metadata);

        /**
         * Converts JMX-compliant attribute value into the native Java object.
         * @param jmxValue The value to convert.
         * @param metadata The metadata of the bean property.
         * @return The converted attribute value.
         */
        Object fromJmxValue(final T jmxValue, final CustomAttributeInfo metadata);
    }

    private static final class DefaultManagementAttributeMarshaller implements ManagementAttributeMarshaller<Object> {
        @Override
        public OpenType<Object> getOpenType() {
            return null;
        }

        @Override
        public Object toJmxValue(final Object attributeValue,
                                 final CustomAttributeInfo descriptor) {
            return attributeValue;
        }

        @Override
        public Object fromJmxValue(final Object jmxValue,
                                   final CustomAttributeInfo descriptor) {
            return jmxValue;
        }
    }

    /**
     * Marks getter or setter as a management attribute.
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
         * Represents attribute marshaller that is used to convert custom Java type to
         * JMX-compliant value and vice versa.
         * @return The attribute formatter.
         */
        Class<? extends ManagementAttributeMarshaller<?>> marshaller() default DefaultManagementAttributeMarshaller.class;
    }

    /**
     * Associates additional information with parameter of the management operation.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    protected @interface OperationParameter{
        /**
         * Gets description of the parameter.
         * @return The description of the parameter.
         */
        String description() default "";

        /**
         * Gets name of the parameter.
         * @return The name of the parameter.
         */
        String name();
    }

    /**
     * Marks method as a management operation. Marked operation should be public non-static.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    protected @interface ManagementOperation{
        /**
         * Gets description of the management operation.
         * @return The description of the management operation.
         */
        String description() default "";

        /**
         * The impact of the method.
         * @return The impact of the method.
         * @see MBeanOperationInfo#UNKNOWN
         * @see MBeanOperationInfo#ACTION
         * @see MBeanOperationInfo#ACTION_INFO
         * @see MBeanOperationInfo#INFO
         */
        int impact() default MBeanOperationInfo.UNKNOWN;
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

    private static final class JavaBeanOperationInfo extends OpenMBeanOperationInfoSupport{
        private final MethodHandle handle;

        private JavaBeanOperationInfo(final String operationName,
                                      final MethodDescriptor method,
                                      final OperationDescriptor descriptor,
                                      final Object owner) throws ReflectionException{
            super(operationName,
                    getDescription(method),
                    getParameters(method),
                    getReturnType(method),
                    getImpact(method),
                    descriptor);
            try {
                handle = MethodHandles.publicLookup().unreflect(method.getMethod()).bindTo(owner);
            } catch (final IllegalAccessException e) {
                throw new ReflectionException(e);
            }
        }

        private static String getDescription(final MethodDescriptor method){
            final ManagementOperation operationInfo = method.getMethod().getAnnotation(ManagementOperation.class);
            return operationInfo != null ? operationInfo.description() : method.getShortDescription();
        }

        private static int getImpact(final MethodDescriptor method){
            final ManagementOperation operationInfo = method.getMethod().getAnnotation(ManagementOperation.class);
            return operationInfo != null ? operationInfo.impact() : MBeanOperationInfo.UNKNOWN;
        }

        private static OpenType<?> getReturnType(final MethodDescriptor method) throws ReflectionException {
            return getType(method.getMethod().getReturnType());
        }

        private static OpenMBeanParameterInfoSupport[] getParameters(final MethodDescriptor method) throws ReflectionException {
            final Class<?>[] parameters = method.getMethod().getParameterTypes();
            final OpenMBeanParameterInfoSupport[] result = new OpenMBeanParameterInfoSupport[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                final OperationParameter metadata = Utils.getParameterAnnotation(method.getMethod(), i, OperationParameter.class);
                final String name;
                final String description;
                if (metadata == null)
                    description = name = Integer.toString(i);
                else {
                    name = metadata.name();
                    description = Strings.isNullOrEmpty(metadata.description()) ?
                            Integer.toString(i) :
                            metadata.description();
                }
                result[i] = new OpenMBeanParameterInfoSupport(name,
                        description,
                        getType(parameters[i]));
            }
            return result;
        }

        private static OpenType<?> getType(final Class<?> type) throws ReflectionException{
            final WellKnownType knownType = WellKnownType.getType(type);
            if(knownType == null || !knownType.isOpenType())
                throw new ReflectionException(new Exception(String.format("Invalid parameter type '%s'", type)));
            else return knownType.getOpenType();
        }
    }

    private static final class JavaBeanOperationSupport extends AbstractOperationSupport<JavaBeanOperationInfo>{
        private final Logger logger;
        private final ManagedBeanDescriptor<?> descriptor;
        private static final Class<JavaBeanOperationInfo> FEATURE_TYPE = JavaBeanOperationInfo.class;

        private JavaBeanOperationSupport(final String resourceName,
                                      final ManagedBeanDescriptor<?> descriptor,
                                      final Logger logger){
            super(resourceName, FEATURE_TYPE);
            this.logger = logger;
            this.descriptor = descriptor;
        }

        @Override
        protected JavaBeanOperationInfo enableOperation(final String userDefinedName,
                                                        final OperationDescriptor descriptor) throws ReflectionException, MBeanException {
            for(final MethodDescriptor method: this.descriptor.getBeanInfo().getMethodDescriptors())
                if(Objects.equals(method.getName(), descriptor.getOperationName()) &&
                        method.getMethod().isAnnotationPresent(ManagementOperation.class)){
                    return new JavaBeanOperationInfo(userDefinedName, method, descriptor, this.descriptor.getInstance());
                }
            throw new MBeanException(new IllegalArgumentException(String.format("Operation '%s' doesn't exist", descriptor.getOperationName())));
        }

        @Override
        public Collection<JavaBeanOperationInfo> expand() {
            final List<JavaBeanOperationInfo> result = new LinkedList<>();
            for(final MethodDescriptor method: this.descriptor.getBeanInfo().getMethodDescriptors())
                if(method.getMethod().isAnnotationPresent(ManagementOperation.class)){
                    final JavaBeanOperationInfo operation = enableOperation(method.getDisplayName(),
                            method.getName(),
                            TIMEOUT_FOR_SMART_MODE,
                            AUTO_PROPS);
                    if(operation != null) result.add(operation);
                }
            return result;
        }

        @Override
        protected void failedToEnableOperation(final String userDefinedName, final String operationName, final Exception e) {
            failedToEnableOperation(logger, Level.SEVERE, userDefinedName, operationName, e);
        }

        @Override
        protected Object invoke(final OperationCallInfo<JavaBeanOperationInfo> callInfo) throws Exception {
            try {
                return callInfo.invoke(callInfo.getMetadata().handle);
            } catch (final Exception | Error e) {
                throw e;
            } catch (final Throwable e) {
                throw new UndeclaredThrowableException(e);
            }
        }

        private static boolean canExpandWith(final Class<? extends MBeanFeatureInfo> featureType) {
            return featureType.isAssignableFrom(FEATURE_TYPE);
        }
    }

    /**
     * Represents an attribute declared as a Java property in this resource connector.
     * This class cannot be inherited or instantiated directly in your code.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    private static class JavaBeanAttributeInfo extends CustomAttributeInfo implements AttributeDescriptorRead{
        private static final long serialVersionUID = -5047097712279607039L;
        private final MethodHandle getter;
        private final MethodHandle setter;

        /**
         * Represents attribute formatter.
         */
        protected final ManagementAttributeMarshaller formatter;

        private JavaBeanAttributeInfo(final String attributeName,
                                      final PropertyDescriptor property,
                                      final AttributeDescriptor descriptor,
                                      final Object owner) throws ReflectionException {
            super(attributeName,
                    property.getPropertyType(),
                    getDescription(property, descriptor),
                    getSpecifier(property),
                    descriptor);
            final Method getter = property.getReadMethod();
            final Method setter = property.getWriteMethod();
            final ManagementAttribute info = getAdditionalInfo(getter, setter);
            if(info != null)
                try {
                    final Class<? extends ManagementAttributeMarshaller> formatterClass =
                            info.marshaller();
                    this.formatter = Objects.equals(formatterClass, DefaultManagementAttributeMarshaller.class) ?
                            new DefaultManagementAttributeMarshaller():
                            formatterClass.newInstance();
                } catch (final ReflectiveOperationException e){
                    throw new ReflectionException(e);
                }
            else formatter = new DefaultManagementAttributeMarshaller();
            final MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            try{
                this.setter = setter != null ? lookup.unreflect(setter).bindTo(owner): null;
                this.getter = getter != null ? lookup.unreflect(getter).bindTo(owner): null;
            } catch (final IllegalAccessException e) {
                throw new ReflectionException(e);
            }
        }

        final Object getValue() throws ReflectionException {
            if (getter != null)
                try {
                    return formatter.toJmxValue(getter.invoke(), this);
                } catch (final Exception e) {
                    throw new ReflectionException(e);
                } catch (final Error e) {
                    throw e;
                } catch (final Throwable e) {
                    throw new ReflectionException(new UndeclaredThrowableException(e));
                }
            else throw new ReflectionException(new UnsupportedOperationException("Attribute is write-only"));
        }

        @SuppressWarnings("unchecked")
        final void setValue(final Object value) throws ReflectionException, InvalidAttributeValueException {
            if (setter != null)
                try {
                    setter.invoke(formatter.fromJmxValue(value, this));
                } catch (final IllegalArgumentException e) {
                    throw new InvalidAttributeValueException(e.getMessage());
                } catch (final Exception e) {
                    throw new ReflectionException(e);
                } catch (final Error e) {
                    throw e;
                } catch (final Throwable e) {
                    throw new ReflectionException(new UndeclaredThrowableException(e));
                }
            else throw new ReflectionException(new UnsupportedOperationException("Attribute is read-only"));
        }

        private static AttributeSpecifier getSpecifier(final PropertyDescriptor descriptor){
            AttributeSpecifier result = AttributeSpecifier.NOT_ACCESSIBLE;
            if(descriptor.getReadMethod() != null){
                result = result.readable(true);
                result = descriptor.getReadMethod().getName().startsWith("is") ?
                        result.flag(true):
                        result;
            }
            result = result.writable(descriptor.getWriteMethod() != null);
            return result;
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
            OpenType<?> type = formatter.getOpenType();
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

    private static final class JavaBeanAttributeSupport extends AbstractAttributeSupport<JavaBeanAttributeInfo> {
        private final Logger logger;
        private final ManagedBeanDescriptor<?> bean;
        private static final Class<JavaBeanAttributeInfo> FEATURE_TYPE = JavaBeanAttributeInfo.class;

        private JavaBeanAttributeSupport(final String resourceName,
                                         final ManagedBeanDescriptor<?> beanDesc,
                                         final Logger logger){
            super(resourceName, FEATURE_TYPE);
            this.logger = Objects.requireNonNull(logger);
            this.bean = Objects.requireNonNull(beanDesc);
        }

        private static boolean canExpandWith(final Class<? extends MBeanFeatureInfo> featureType){
            return featureType.isAssignableFrom(featureType);
        }

        private static JavaBeanAttributeInfo createAttribute(final String attributeID,
                                                             final PropertyDescriptor property,
                                                             final AttributeDescriptor descriptor,
                                                             final Object beanInstance) throws ReflectionException {
            try{
                //try to connect as Open Type attribute
                return new JavaBeanOpenAttributeInfo(attributeID, property, descriptor, beanInstance);
            }
            catch (final OpenDataException e){
                //bean property type is not Open Type
                return new JavaBeanAttributeInfo(attributeID, property, descriptor, beanInstance);
            }
        }

        @Override
        protected JavaBeanAttributeInfo connectAttribute(final String attributeID,
                                                         final AttributeDescriptor descriptor) throws AttributeNotFoundException, ReflectionException {
            final BeanInfo info = bean.getBeanInfo();
            for(final PropertyDescriptor property: info.getPropertyDescriptors())
                if(isReservedProperty(property)) continue;
                else if(Objects.equals(property.getName(), descriptor.getAttributeName()))
                    return createAttribute(attributeID, property, descriptor, bean.getInstance());
            throw JMExceptionUtils.attributeNotFound(descriptor.getAttributeName());
        }

        @Override
        public Collection<JavaBeanAttributeInfo> expand() {
            final List<JavaBeanAttributeInfo> result = new LinkedList<>();
            final BeanInfo info = bean.getBeanInfo();
            for (final PropertyDescriptor property : info.getPropertyDescriptors())
                if (!isReservedProperty(property)) {
                    final JavaBeanAttributeInfo attr =
                            addAttribute(property.getDisplayName(), property.getName(), TIMEOUT_FOR_SMART_MODE, AUTO_PROPS);
                    if (attr != null) result.add(attr);
                }
            return result;
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
    }

    private static final class JavaBeanNotificationSupport extends AbstractNotificationSupport<CustomNotificationInfo> {
        private final Logger logger;
        private final Set<? extends ManagementNotificationType<?>> notifTypes;
        private final NotificationListenerInvoker listenerInvoker;
        private static final Class<CustomNotificationInfo> FEATURE_TYPE = CustomNotificationInfo.class;

        private JavaBeanNotificationSupport(final String resourceName,
                                            final Set<? extends ManagementNotificationType<?>> notifTypes,
                                            final Logger logger) {
            super(resourceName, FEATURE_TYPE);
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
            if (type != null) {
                String description = type.getDescription(Locale.getDefault());
                if (description == null || description.isEmpty()) {
                    description = metadata.getDescription();
                    if (description == null || description.isEmpty())
                        description = type.getCategory();
                }
                return new CustomNotificationInfo(category, description, metadata.setUserDataType(type.getUserDataType()));
            } else
                throw new IllegalArgumentException(String.format("Unsupported notification %s", metadata.getNotificationCategory()));
        }

        @Override
        protected void failedToEnableNotifications(final String listID, final String category, final Exception e) {
            failedToEnableNotifications(logger, Level.WARNING, listID, category, e);
        }

        private void fire(final ManagementNotificationType<?> category, final String message, final Object userData) {
            fire(category.getCategory(), message, userData);
        }
    }

    /**
     * Represents default implementation of {@link DiscoveryService} based on information
     * supplied through reflection of the bean.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static abstract class BeanDiscoveryService extends AbstractAggregator implements DiscoveryService{
        private final Collection<? extends ManagementNotificationType<?>> notifications;
        private final BeanInfo beanMetadata;

        private BeanDiscoveryService(final BeanInfo beanMetadata,
                                     final Collection<? extends ManagementNotificationType<?>> notifications){
            this.beanMetadata = Objects.requireNonNull(beanMetadata);
            this.notifications = Objects.requireNonNull(notifications);
        }

        protected BeanDiscoveryService(final Class<? extends ManagedResourceConnectorBean> connectorType) throws IntrospectionException {
            this(connectorType, EnumSet.noneOf(EmptyManagementNotificationType.class));
        }

        protected <N extends Enum<N> & ManagementNotificationType<?>> BeanDiscoveryService(final Class<? extends ManagedResourceConnectorBean> connectorType,
                                                                                           final EnumSet<N> notifications) throws IntrospectionException {
            this(reflect(connectorType), notifications);
        }

        protected BeanDiscoveryService(final Class<?> beanType,
                                       final BeanIntrospector introspector) throws IntrospectionException {
            this(introspector.getBeanInfo(beanType),
                    EnumSet.noneOf(EmptyManagementNotificationType.class));
        }

        protected <N extends Enum<N> & ManagementNotificationType<?>> BeanDiscoveryService(final Class<?> beanType,
                                                                                           final BeanIntrospector introspector,
                                                                                           final EnumSet<N> notifications) throws IntrospectionException {
            this(introspector.getBeanInfo(beanType), notifications);
        }

        private static Collection<AttributeConfiguration> discoverAttributes(final PropertyDescriptor[] properties) {
            final Collection<AttributeConfiguration> result = Lists.newArrayListWithExpectedSize(properties.length);
            for (final PropertyDescriptor descriptor : properties)
                if (!isReservedProperty(descriptor))
                    result.add(new SerializableAttributeConfiguration(descriptor.getName()));
            return result;
        }

        private static Collection<EventConfiguration> discoverNotifications(final Collection<? extends ManagementNotificationType<?>> notifications){
            final Collection<EventConfiguration> result = Lists.newArrayListWithExpectedSize(notifications.size());
            for(final ManagementNotificationType<?> notifType: notifications)
                result.add(new SerializableEventConfiguration(notifType.getCategory()));
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends FeatureConfiguration> Collection<T> discover(final String connectionString,
                                                                       final Map<String, String> connectionOptions,
                                                                       final Class<T> entityType) {
            if(Objects.equals(entityType, AttributeConfiguration.class))
                return (Collection<T>)discoverAttributes(beanMetadata.getPropertyDescriptors());
            else if(Objects.equals(entityType, EventConfiguration.class))
                return (Collection<T>)discoverNotifications(notifications);
            else return Collections.emptyList();
        }

        @SafeVarargs
        @Override
        public final DiscoveryResult discover(final String connectionString,
                                        final Map<String, String> connectionOptions,
                                        final Class<? extends FeatureConfiguration>... entityTypes) {
            final DiscoveryResultBuilder result = new DiscoveryResultBuilder();
            for(final Class<? extends FeatureConfiguration> type: entityTypes)
                result.importFeatures(this, connectionString, connectionOptions, type);
            return result.get();
        }
    }

    private final JavaBeanAttributeSupport attributes;
    private final JavaBeanNotificationSupport notifications;
    private final JavaBeanOperationSupport operations;

    private ManagedResourceConnectorBean(final String resourceName,
                                         ManagedBeanDescriptor<?> descriptor,
                                         final Set<? extends ManagementNotificationType<?>> notifTypes) throws IntrospectionException {
        if(descriptor == null) descriptor = new SelfDescriptor(this);
        attributes = new JavaBeanAttributeSupport(resourceName, descriptor, getLogger());
        notifications = new JavaBeanNotificationSupport(resourceName, notifTypes, getLogger());
        operations = new JavaBeanOperationSupport(resourceName, descriptor, getLogger());
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
                EnumSet.noneOf(EmptyManagementNotificationType.class));
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
        addResourceEventListener(listener, attributes, notifications, operations);
    }

    /**
     * Removes connector event listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public final void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, attributes, notifications, operations);
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
     * @return Metadata of deleted notification.
     */
    public final MBeanNotificationInfo disableNotifications(final String listId) {
        verifyInitialization();
        return notifications.remove(listId);
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
     * @param attributeID The unique identifier of the attribute.
     * @return The metadata of deleted attribute.
     */
    public final MBeanAttributeInfo removeAttribute(final String attributeID) {
        return attributes.remove(attributeID);
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

    public final MBeanOperationInfo enableOperation(final String userDefinedName,
                                                    final String operationName,
                                                    final TimeSpan invocationTimeout,
                                                    final CompositeData options){
        verifyInitialization();
        return operations.enableOperation(userDefinedName, operationName, invocationTimeout, options);
    }

    public final MBeanOperationInfo disableOperation(final String userDefinedName){
        verifyInitialization();
        return operations.remove(userDefinedName);
    }

    /**
     * Removes all operations from this connector.
     */
    public final void removeAllOperations(){
        verifyInitialization();
        operations.removeAll(false);
    }

    public final Collection<? extends MBeanOperationInfo> disableOperationsExcept(final Set<String> operations){
        verifyInitialization();
        return this.operations.removeAllExcept(operations);
    }

    /**
     * Removes all attributes from this connector.
     */
    public final void removeAllAttributes(){
        verifyInitialization();
        attributes.removeAll(false);
    }

    public final Collection<? extends MBeanAttributeInfo> removeAttributesExcept(final Set<String> attributes){
        verifyInitialization();
        return this.attributes.removeAllExcept(attributes);
    }

    /**
     * Removes all notifications and listeners from this connector.
     */
    public final void removeAllNotifications(){
        verifyInitialization();
        notifications.removeAll(true, false);
    }

    public final Collection<? extends MBeanNotificationInfo> disableNotificationsExcept(final Set<String> events){
        verifyInitialization();
        return this.notifications.removeAllExcept(events);
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

    private static BeanDiscoveryService createDiscoveryService(final BeanInfo beanMetadata,
                                                               final Set<? extends ManagementNotificationType<?>> notifTypes,
                                                               final Logger logger){
        return new BeanDiscoveryService(beanMetadata, notifTypes) {
            @Override
            public Logger getLogger() {
                return logger;
            }
        };
    }

    /**
     * Creates a new instance of the resource metadata discovery service.
     * @return A new instance of the discovery service.
     */
    public DiscoveryService createDiscoveryService(){
        final BeanInfo beanMetadata = attributes.bean.getBeanInfo();
        final Set<? extends ManagementNotificationType<?>> notifTypes = notifications.notifTypes;
        return createDiscoveryService(beanMetadata, notifTypes, getLogger());
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
        }, attributes, notifications, operations);
    }

    protected static BeanInfo reflect(final Class<? extends ManagedResourceConnectorBean> connectorType) throws IntrospectionException {
        return Introspector.getBeanInfo(connectorType, ManagedResourceConnectorBean.class);
    }

    private static boolean isReservedProperty(final PropertyDescriptor property){
        return Objects.equals(property.getName(), "logger");
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    public void close() throws Exception {
        operations.removeAll(true);
        notifications.removeAll(true, true);
        attributes.removeAll(true);
    }

    @Override
    public boolean canExpandWith(final Class<? extends MBeanFeatureInfo> featureType) {
        return JavaBeanAttributeSupport.canExpandWith(featureType) ||
                JavaBeanOperationSupport.canExpandWith(featureType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <F extends MBeanFeatureInfo> Collection<? extends F> expand(final Class<F> featureType) {
        if (attributes.canExpandWith(featureType))
            return (Collection<F>) attributes.expand();
        else if(operations.canExpandWith(featureType))
            return (Collection<F>) operations.expand();
        else return Collections.emptyList();
    }
}
