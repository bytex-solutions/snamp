package com.bytex.snamp.connector;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.Localizable;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.attributes.*;
import com.bytex.snamp.connector.discovery.DiscoveryResultBuilder;
import com.bytex.snamp.connector.discovery.DiscoveryService;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.connector.notifications.*;
import com.bytex.snamp.connector.operations.AbstractOperationRepository;
import com.bytex.snamp.connector.operations.OperationDescriptor;
import com.bytex.snamp.connector.operations.OperationSupport;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.core.LongCounter;
import static com.bytex.snamp.internal.Utils.*;
import com.bytex.snamp.jmx.JMExceptionUtils;
import com.bytex.snamp.jmx.WellKnownType;
import static com.google.common.base.Strings.isNullOrEmpty;
import com.google.common.collect.ImmutableList;
import org.osgi.framework.BundleContext;
import static com.bytex.snamp.configuration.ConfigurationManager.createEntityConfiguration;

import javax.management.*;
import javax.management.openmbean.*;
import java.beans.*;
import java.beans.IntrospectionException;
import java.lang.annotation.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


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
 *           super("resourceName");
 *           prop1 = "Hello, world!";
 *       }
 *
 *       @ManagementAttribute
 *       public String getProperty1(){
 *         return prop1;
 *       }
 *
 *       @ManagementAttribute
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
 * @version 2.0
 */
public abstract class ManagedResourceConnectorBean extends AbstractManagedResourceConnector {

    /**
     * Describes management notification type supported by this connector.
     * @param <T> Well-known type of the user data to be associated with each notification.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    protected interface ManagementNotificationType<T> extends Localizable {
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
            return "";
        }

        @Override
        public String toString(final Locale locale) {
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
     * @version 2.0
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
     * @version 2.0
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

    private static final class JavaBeanOperationInfo extends OpenMBeanOperationInfoSupport{
        private static final long serialVersionUID = 5144309275413329193L;
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

        private static <A extends Annotation> A getParameterAnnotation(final Method method,
                                                                      final int parameterIndex,
                                                                      final Class<A> annotationType) {
            final Annotation[][] annotations = method.getParameterAnnotations();
            if(annotations.length >= parameterIndex)
                return null;
            for(final Annotation candidate: annotations[parameterIndex])
                if(annotationType.isInstance(candidate))
                    return annotationType.cast(candidate);
            return null;
        }

        private static OpenMBeanParameterInfoSupport[] getParameters(final MethodDescriptor method) throws ReflectionException {
            final Class<?>[] parameters = method.getMethod().getParameterTypes();
            final OpenMBeanParameterInfoSupport[] result = new OpenMBeanParameterInfoSupport[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                final OperationParameter metadata = getParameterAnnotation(method.getMethod(), i, OperationParameter.class);
                final String name;
                final String description;
                if (metadata == null)
                    description = name = Integer.toString(i);
                else {
                    name = metadata.name();
                    description = isNullOrEmpty(metadata.description()) ?
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

    private static final class JavaBeanOperationRepository extends AbstractOperationRepository<JavaBeanOperationInfo> {
        private final Logger logger;
        private final WeakReference<? extends ManagedResourceConnector> connectorRef;
        private final ImmutableList<MethodDescriptor> operations;

        private <C extends ManagedResourceConnector> JavaBeanOperationRepository(final String resourceName,
                                            final C connectorRef,
                                            final MethodDescriptor[] operations,
                                            final Logger logger){
            super(resourceName, JavaBeanOperationInfo.class, true);
            this.logger = logger;
            this.connectorRef = new WeakReference<>(connectorRef);
            this.operations = ImmutableList.copyOf(operations);
        }

        @Override
        protected JavaBeanOperationInfo connectOperation(final String operationName,
                                                         final OperationDescriptor descriptor) throws ReflectionException, MBeanException {
            for(final MethodDescriptor method: operations)
                if(Objects.equals(method.getName(), descriptor.getName(operationName)) &&
                        method.getMethod().isAnnotationPresent(ManagementOperation.class)){
                    return new JavaBeanOperationInfo(operationName, method, descriptor, connectorRef.get());
                }
            throw new MBeanException(new IllegalArgumentException(String.format("Operation '%s' doesn't exist", descriptor.getName(operationName))));
        }

        private ClassLoader getConnectorClassLoader(){
            return connectorRef.get().getClass().getClassLoader();
        }

        @Override
        public Collection<JavaBeanOperationInfo> expandOperations() {
            return operations.stream()
                    .filter(method -> method.getMethod().isAnnotationPresent(ManagementOperation.class))
                    .map(method -> {
                        final OperationConfiguration config = createEntityConfiguration(getConnectorClassLoader(), OperationConfiguration.class);
                        assert config != null;
                        config.setAlternativeName(method.getName());
                        config.setAutomaticallyAdded(true);
                        config.setInvocationTimeout(OperationConfiguration.TIMEOUT_FOR_SMART_MODE);
                        return enableOperation(method.getName(), new OperationDescriptor(config));
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        @Override
        protected void failedToEnableOperation(final String operationName, final Exception e) {
            failedToEnableOperation(logger, Level.SEVERE, operationName, e);
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
    }

    /**
     * Represents an attribute declared as a Java property in this resource connector.
     * This class cannot be inherited or instantiated directly in your code.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    private static class JavaBeanAttributeInfo extends CustomAttributeInfo implements AttributeDescriptorRead{
        private static final long serialVersionUID = -5047097712279607039L;
        private final Supplier<?> getter;
        private final Consumer setter;

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
            final MethodHandles.Lookup lookup = MethodHandles.lookup();
            try{
                this.setter = setter != null ? reflectSetter(lookup, owner, setter): null;
                this.getter = getter != null ? reflectGetter(lookup, owner, getter): null;
            } catch (final ReflectiveOperationException e) {
                throw new ReflectionException(e);
            }
        }

        final Object getValue() throws ReflectionException {
            if (getter != null)
                return formatter.toJmxValue(getter.get(), this);
            else throw new ReflectionException(new UnsupportedOperationException("Attribute is write-only"));
        }

        @SuppressWarnings("unchecked")
        final void setValue(final Object value) throws ReflectionException, InvalidAttributeValueException {
            if (setter != null)
                setter.accept(formatter.fromJmxValue(value, this));
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

    private static final class JavaBeanAttributeRepository extends AbstractAttributeRepository<JavaBeanAttributeInfo> {
        private final Logger logger;
        private final ImmutableList<PropertyDescriptor> properties;
        private final WeakReference<? extends ManagedResourceConnector> connectorRef;

        private <C extends ManagedResourceConnector> JavaBeanAttributeRepository(final String resourceName,
                                            final C connector,
                                            final PropertyDescriptor[] properties,
                                            final Logger logger){
            super(resourceName, JavaBeanAttributeInfo.class, true);
            this.logger = Objects.requireNonNull(logger);
            this.properties = ImmutableList.copyOf(properties);
            this.connectorRef = new WeakReference<>(connector);
        }

        private static JavaBeanAttributeInfo createAttribute(final String attributeName,
                                                             final PropertyDescriptor property,
                                                             final AttributeDescriptor descriptor,
                                                             final Object beanInstance) throws ReflectionException {
            try{
                //try to connect as Open Type attribute
                return new JavaBeanOpenAttributeInfo(attributeName, property, descriptor, beanInstance);
            }
            catch (final OpenDataException e){
                //bean property type is not Open Type
                return new JavaBeanAttributeInfo(attributeName, property, descriptor, beanInstance);
            }
        }

        @Override
        protected JavaBeanAttributeInfo connectAttribute(final String attributeName,
                                                         final AttributeDescriptor descriptor) throws AttributeNotFoundException, ReflectionException {
            for(final PropertyDescriptor property: properties)
                if(isReservedProperty(property)) continue;
                else if(Objects.equals(property.getName(), descriptor.getName(attributeName)))
                    return createAttribute(attributeName, property, descriptor, connectorRef.get());
            throw JMExceptionUtils.attributeNotFound(descriptor.getName(attributeName));
        }

        private ClassLoader getConnectorClassLoader(){
            return connectorRef.get().getClass().getClassLoader();
        }

        @Override
        public Collection<JavaBeanAttributeInfo> expandAttributes() {
            return properties.stream()
                    .filter(property -> !isReservedProperty(property))
                    .map(property -> {
                        final AttributeConfiguration config = createEntityConfiguration(getConnectorClassLoader(), AttributeConfiguration.class);
                        assert config != null;
                        config.setAlternativeName(property.getName());
                        config.setAutomaticallyAdded(true);
                        config.setReadWriteTimeout(AttributeConfiguration.TIMEOUT_FOR_SMART_MODE);
                        return addAttribute(property.getName(), new AttributeDescriptor(config));
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        @Override
        protected void failedToConnectAttribute(final String attributeName, final Exception e) {
            failedToConnectAttribute(logger, Level.SEVERE, attributeName, e);
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

    private static final class JavaBeanNotificationRepository extends AbstractNotificationRepository<CustomNotificationInfo> {
        private final Logger logger;
        private final Set<? extends ManagementNotificationType<?>> notifTypes;
        private final NotificationListenerInvoker listenerInvoker;
        private final LongCounter sequenceNumberGenerator;

        private JavaBeanNotificationRepository(final String resourceName,
                                               final Set<? extends ManagementNotificationType<?>> notifTypes,
                                               final BundleContext context,
                                               final Logger logger) {
            super(resourceName, CustomNotificationInfo.class, false);
            this.logger = Objects.requireNonNull(logger);
            this.notifTypes = Objects.requireNonNull(notifTypes);
            this.listenerInvoker = NotificationListenerInvokerFactory.createSequentialInvoker();
            this.sequenceNumberGenerator = context == null ?  //may be null when executing through unit tests
                    DistributedServices.getProcessLocalCounterGenerator("notifications-".concat(resourceName)) :
                    DistributedServices.getDistributedCounter(context, "notifications-".concat(resourceName));
        }

        @Override
        protected NotificationListenerInvoker getListenerInvoker() {
            return listenerInvoker;
        }

        @Override
        protected CustomNotificationInfo connectNotifications(final String category,
                                                             final NotificationDescriptor metadata) throws IllegalArgumentException {
            //find the suitable notification type
            final ManagementNotificationType<?> type = notifTypes.stream()
                    .filter(type1 -> Objects.equals(type1.getCategory(), metadata.getName(category)))
                    .findFirst()
                    .orElseGet(() -> null);
            if (type != null) {
                String description = type.toString(Locale.getDefault());
                if (description == null || description.isEmpty()) {
                    description = metadata.getDescription();
                    if (description == null || description.isEmpty())
                        description = type.getCategory();
                }
                return new CustomNotificationInfo(category, description, metadata.setUserDataType(type.getUserDataType()));
            } else
                throw new IllegalArgumentException(String.format("Unsupported notification %s", metadata.getName(category)));
        }

        @Override
        protected void failedToEnableNotifications(final String category, final Exception e) {
            failedToEnableNotifications(logger, Level.WARNING, category, e);
        }

        private void fire(final ManagementNotificationType<?> category, final String message, final Object userData) {
            fire(category.getCategory(), message, sequenceNumberGenerator, userData);
        }
    }

    /**
     * Represents default implementation of {@link DiscoveryService} based on information
     * supplied through reflection of the bean.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    public static abstract class BeanDiscoveryService extends AbstractAggregator implements DiscoveryService{
        private final Collection<? extends ManagementNotificationType<?>> notifications;
        private final BeanInfo beanMetadata;

        private BeanDiscoveryService(final BeanInfo beanMetadata,
                                     final Collection<? extends ManagementNotificationType<?>> notifications){
            this.beanMetadata = Objects.requireNonNull(beanMetadata);
            this.notifications = Objects.requireNonNull(notifications);
        }

        private ClassLoader getConnectorClassLoader(){
            return beanMetadata.getBeanDescriptor().getBeanClass().getClassLoader();
        }

        protected BeanDiscoveryService(final Class<? extends ManagedResourceConnectorBean> connectorType) throws IntrospectionException {
            this(connectorType, EnumSet.noneOf(EmptyManagementNotificationType.class));
        }

        protected <N extends Enum<N> & ManagementNotificationType<?>> BeanDiscoveryService(final Class<? extends ManagedResourceConnectorBean> connectorType,
                                                                                           final EnumSet<N> notifications) throws IntrospectionException {
            this(getBeanInfo(connectorType), notifications);
        }

        private Collection<AttributeConfiguration> discoverAttributes(final PropertyDescriptor[] properties) {
            return Arrays.stream(properties)
                    .filter(descriptor -> !isReservedProperty(descriptor))
                    .map(descriptor -> {
                        final AttributeConfiguration attribute = ConfigurationManager.createEntityConfiguration(getConnectorClassLoader(), AttributeConfiguration.class);
                        assert attribute != null;
                        attribute.setAlternativeName(descriptor.getName());
                        return attribute;
                    })
                    .collect(Collectors.toList());
        }

        private Collection<EventConfiguration> discoverNotifications(final Collection<? extends ManagementNotificationType<?>> notifications) {
            return notifications.stream()
                    .map(notificationType -> {
                        final EventConfiguration event = createEntityConfiguration(getConnectorClassLoader(), EventConfiguration.class);
                        assert event != null;
                        event.setAlternativeName(notificationType.getCategory());
                        return event;
                    })
                    .collect(Collectors.toList());
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
            Arrays.stream(entityTypes).forEach(type -> result.importFeatures(this, connectionString, connectionOptions, type));
            return result.get();
        }
    }

    @Aggregation(cached = true)
    private final JavaBeanAttributeRepository attributes;
    @Aggregation(cached = true)
    private final JavaBeanNotificationRepository notifications;
    @Aggregation(cached = true)
    private final JavaBeanOperationRepository operations;

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
        final BeanInfo beanInfo = getBeanInfo(getClass());
        attributes = new<ManagedResourceConnectorBean> JavaBeanAttributeRepository(resourceName,
                this,
                beanInfo.getPropertyDescriptors(),
                getLogger());
        notifications = new JavaBeanNotificationRepository(resourceName,
                notifTypes,
                getBundleContextOfObject(this),
                getLogger());
        operations = new<ManagedResourceConnectorBean> JavaBeanOperationRepository(resourceName,
                this,
                beanInfo.getMethodDescriptors(),
                getLogger());
    }

    @Override
    protected final MetricsSupport createMetricsReader(){
        return assembleMetricsReader(attributes, notifications, operations);
    }

    private static BeanInfo getBeanInfo(final Class<? extends ManagedResourceConnectorBean> beanType) throws IntrospectionException {
        return Introspector.getBeanInfo(beanType, ManagedResourceConnectorBean.class);
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

    final AttributeSupport getAttributeSupport(){
        return attributes;
    }

    final NotificationSupport getNotificationSupport(){
        return notifications;
    }

    final OperationSupport getOperationSupport(){
        return operations;
    }

    private boolean emitNotificationImpl(final ManagementNotificationType<?> category,
                                      final String message,
                                      final Object userData){
        if(notifications.isSuspended()) return false;
        else {
            notifications.fire(category, message, userData);
            return true;
        }
    }

    /**
     * Emits notification from this Bean.
     * @param category Category of the notification to emit.
     * @param message Human-readable message associated with emitted notification.
     * @return {@literal true}, if notifications are not suspended for this bean; otherwise, {@literal false}.
     */
    protected final boolean emitNotification(final ManagementNotificationType<?> category,
                                          final String message){
        return emitNotificationImpl(category, message, null);
    }

    /**
     * Emits notification from this Bean.
     * @param category Category of the notification to emit.
     * @param message Human-readable message associated with emitted notification.
     * @param userData An object to be attached.
     * @return {@literal true}, if notifications are not suspended for this bean; otherwise, {@literal false}.
     */
    protected final <T> boolean emitNotification(final ManagementNotificationType<T> category,
                                              final String message,
                                              final T userData){
        return emitNotificationImpl(category, message, userData);
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
     * @throws IntrospectionException Unable to reflect this bean.
     */
    public DiscoveryService createDiscoveryService() throws IntrospectionException{
        final BeanInfo beanMetadata = getBeanInfo(getClass());
        final Set<? extends ManagementNotificationType<?>> notifTypes = notifications.notifTypes;
        return createDiscoveryService(beanMetadata, notifTypes, getLogger());
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
        operations.close();
        notifications.close();
        attributes.close();
    }
}
