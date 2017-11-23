package com.bytex.snamp.connector;

import com.bytex.snamp.Localizable;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeManager;
import com.bytex.snamp.connector.attributes.AttributeRepository;
import com.bytex.snamp.connector.attributes.reflection.JavaBeanAttributeInfo;
import com.bytex.snamp.connector.attributes.reflection.JavaBeanOpenAttributeInfo;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.connector.notifications.NotificationManager;
import com.bytex.snamp.connector.notifications.NotificationRepository;
import com.bytex.snamp.connector.notifications.SimpleNotificationInfo;
import com.bytex.snamp.connector.operations.OperationDescriptor;
import com.bytex.snamp.connector.operations.OperationManager;
import com.bytex.snamp.connector.operations.OperationRepository;
import com.bytex.snamp.connector.operations.reflection.JavaBeanOperationInfo;
import com.bytex.snamp.core.SharedCounter;
import com.bytex.snamp.jmx.JMExceptionUtils;
import com.google.common.collect.ImmutableSet;

import javax.management.*;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import java.beans.*;
import java.beans.IntrospectionException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;


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
 *     <br/>
 *     Please note that the derived class should be public
 * @author Roman Sakno
 * @since 1.0
 * @version 2.1
 */
public abstract class ManagedResourceConnectorBean extends AbstractManagedResourceConnector implements AttributeManager,
        AttributeRepository.AttributeReader<JavaBeanAttributeInfo>,
        AttributeRepository.AttributeWriter<JavaBeanAttributeInfo>,
        OperationManager,
        OperationRepository.OperationInvoker<JavaBeanOperationInfo>,
        NotificationManager,
        NotificationBroadcaster{


    /**
     * Describes management notification type supported by this connector.
     * @param <T> Well-known type of the user data to be associated with each notification.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.1
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

    private final AttributeRepository<JavaBeanAttributeInfo> attributes;
    private final NotificationRepository<SimpleNotificationInfo> notifications;
    private final ImmutableSet<? extends ManagementNotificationType<?>> notifTypes;
    private final OperationRepository<JavaBeanOperationInfo> operations;
    private final SharedCounter sequenceNumberGenerator;

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
        super(resourceName);
        attributes = new AttributeRepository<>();
        operations = new OperationRepository<>();
        notifications = new NotificationRepository<>();
        sequenceNumberGenerator = getClusterMembership().getCounters().getSharedObject("notifications-".concat(resourceName));
        this.notifTypes = ImmutableSet.copyOf(notifTypes);
    }

    public static JavaBeanAttributeInfo createAttribute(final BeanInfo metadata,
                                                        final String attributeName,
                                                        final AttributeDescriptor descriptor) throws ReflectionException, AttributeNotFoundException {
        for (final PropertyDescriptor property : metadata.getPropertyDescriptors())
            if (Objects.equals(property.getName(), descriptor.getAlternativeName().orElse(attributeName)) && JavaBeanAttributeInfo.isValidDescriptor(property))
                try {
                    //try to connect as Open Type attribute
                    return new JavaBeanOpenAttributeInfo(attributeName, property, descriptor);
                } catch (final OpenDataException e) {
                    //bean property type is not Open Type
                    return new JavaBeanAttributeInfo(attributeName, property, descriptor);
                }
        throw JMExceptionUtils.attributeNotFound(descriptor.getAlternativeName().orElse(attributeName));
    }

    private JavaBeanAttributeInfo createAttribute(final String attributeName,
                                                   final AttributeDescriptor descriptor) throws JMException, IntrospectionException {
        return createAttribute(reflectBeanInfo(), attributeName, descriptor);
    }

    @Override
    public final Object getAttributeValue(final JavaBeanAttributeInfo attribute) throws ReflectionException {
        return attribute.getValue(this);
    }

    @Override
    public final void setAttributeValue(final JavaBeanAttributeInfo attribute, final Object value) throws ReflectionException, InvalidAttributeValueException {
        attribute.setValue(this, value);
    }

    @Override
    public final void addAttribute(final String attributeName, final AttributeDescriptor descriptor) throws JMException {
        addFeature(attributes, attributeName, descriptor, this::createAttribute);
    }

    @Override
    public final boolean removeAttribute(final String attributeName) {
        return removeFeature(attributes, attributeName);
    }

    @Override
    public final void retainAttributes(final Set<String> attributes) {
        retainFeatures(this.attributes, attributes);
    }

    @Override
    public final Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException, ReflectionException {
        return attributes.getAttribute(attributeName, this);
    }

    @Override
    public final void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        attributes.setAttribute(attribute, this);
    }

    @Override
    public final JavaBeanAttributeInfo[] getAttributeInfo() {
        return getFeatureInfo(attributes, JavaBeanAttributeInfo.class);
    }

    @Override
    public AttributeList getAttributes() throws MBeanException, ReflectionException {
        return attributes.getAttributes(this);
    }

    @Override
    public final Map<String, AttributeDescriptor> discoverAttributes() {
        final BeanInfo metadata;
        try {
            metadata = reflectBeanInfo();
        } catch (final IntrospectionException e) {
            logger.log(Level.SEVERE, "Failed to discover attributes", e);
            return Collections.emptyMap();
        }
        final Map<String, AttributeDescriptor> result = new HashMap<>();
        for (final PropertyDescriptor property : metadata.getPropertyDescriptors())
            if (JavaBeanAttributeInfo.isValidDescriptor(property))
                result.put(property.getName(), AttributeDescriptor.EMPTY_DESCRIPTOR);
        return result;
    }

    @Override
    public final JavaBeanOperationInfo[] getOperationInfo() {
        return getFeatureInfo(operations, JavaBeanOperationInfo.class);
    }

    public static JavaBeanOperationInfo createOperation(final BeanInfo metadata,
                                                        final String operationName,
                                                  final OperationDescriptor descriptor) throws ReflectionException {
        for (final MethodDescriptor method : metadata.getMethodDescriptors())
            if (Objects.equals(method.getName(), descriptor.getAlternativeName().orElse(operationName)) && JavaBeanOperationInfo.isValidDescriptor(method)) {
                return new JavaBeanOperationInfo(operationName, method, descriptor);
            }
        throw new IllegalArgumentException(String.format("Operation '%s' doesn't exist", descriptor.getAlternativeName().orElse(operationName)));
    }

    private JavaBeanOperationInfo createOperation(final String operationName,
                                                  final OperationDescriptor descriptor) throws JMException, IntrospectionException{
        return createOperation(reflectBeanInfo(), operationName, descriptor);
    }

    /**
     * Enables management operation.
     *
     * @param operationName The name of operation to be executed on managed resource.
     * @param descriptor    Operation invocation options. Cannot be {@literal null}.
     * @throws JMException Unable to create operation.
     * @since 2.0
     */
    @Override
    public final void enableOperation(final String operationName, final OperationDescriptor descriptor) throws JMException {
        addFeature(operations, operationName, descriptor, this::createOperation);
    }

    @Override
    public final boolean disableOperation(final String operationName) {
        return removeFeature(operations, operationName);
    }

    @Override
    public final void retainOperations(final Set<String> operations) {
        retainFeatures(this.operations, operations);
    }

    @Override
    public final Object invokeOperation(final OperationRepository.OperationCallInfo<JavaBeanOperationInfo> callInfo) throws Exception {
        return callInfo.getOperation().invoke(this, callInfo.toArray());
    }

    @Override
    public final Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
        return operations.invoke(actionName, params, signature, this);
    }

    /**
     * Discover operations.
     *
     * @return A map of discovered operations that can be enabled using method {@link #enableOperation(String, OperationDescriptor)}.
     * @since 2.0
     */
    @Override
    public final Map<String, OperationDescriptor> discoverOperations() {
        final BeanInfo metadata;
        try {
            metadata = reflectBeanInfo();
        } catch (final IntrospectionException e) {
            logger.log(Level.SEVERE, "Failed to discover operations", e);
            return Collections.emptyMap();
        }
        final Map<String, OperationDescriptor> result = new HashMap<>();
        for (final MethodDescriptor method : metadata.getMethodDescriptors())
            if (JavaBeanOperationInfo.isValidDescriptor(method))
                result.put(method.getName(), OperationDescriptor.EMPTY_DESCRIPTOR);
        return result;
    }

    private SimpleNotificationInfo createNotification(final String category, final NotificationDescriptor descriptor) {
        //find the suitable notification type
        final ManagementNotificationType<?> type = notifTypes.stream()
                .filter(t -> descriptor.getAlternativeName().orElse(category).equals(t.getCategory()))
                .findFirst()
                .orElseGet(null);
        if (type != null) {
            String description = type.toString(Locale.getDefault());
            if (isNullOrEmpty(description)) {
                description = descriptor.getDescription();
                if (isNullOrEmpty(description))
                    description = type.getCategory();
            }
            return new SimpleNotificationInfo(category, description, descriptor.setUserDataType(type.getUserDataType()));
        } else
            throw new IllegalArgumentException(String.format("Unsupported notification %s", descriptor.getAlternativeName().orElse(category)));
    }

    @Override
    public final void enableNotifications(final String category, final NotificationDescriptor descriptor) throws JMException {
        addFeature(notifications, category, descriptor, this::createNotification);
    }

    @Override
    public final boolean disableNotifications(final String category) {
        return removeFeature(notifications, category);
    }

    @Override
    public final void retainNotifications(final Set<String> events) {
        retainFeatures(notifications, events);
    }

    @Override
    public final void addNotificationListener(final NotificationListener listener, final NotificationFilter filter, final Object handback) {
        notifications.addNotificationListener(listener, filter, handback);
    }

    @Override
    public final void removeNotificationListener(final NotificationListener listener) throws ListenerNotFoundException {
        notifications.removeNotificationListener(listener);
    }

    @Override
    public final Map<String, NotificationDescriptor> discoverNotifications() {
        return notifTypes.stream()
                .collect(Collectors.toMap(ManagementNotificationType::getCategory, v -> NotificationDescriptor.EMPTY_DESCRIPTOR));
    }

    @Override
    public final SimpleNotificationInfo[] getNotificationInfo() {
        return getFeatureInfo(notifications, SimpleNotificationInfo.class);
    }

    @Override
    protected final MetricsSupport createMetricsReader() {
        return assembleMetricsReader(attributes.metrics, notifications.metrics, operations.metrics);
    }

    /**
     * Reflects this instance of the managed resource connector.
     * @return Reflection metadata.
     * @throws IntrospectionException Unable to reflect managed resource connector.
     */
    protected BeanInfo reflectBeanInfo() throws IntrospectionException {
        return Introspector.getBeanInfo(getClass(), ManagedResourceConnectorBean.class);
    }

    private Stream<Notification> createNotifications(final MBeanNotificationInfo metadata,
                                                            final String message,
                                                            final Object userData) {
        return Arrays.stream(metadata.getNotifTypes())
                .map(type -> {
                    final Notification n = new Notification(type, this, sequenceNumberGenerator.getAsLong(), message);
                    n.setUserData(userData);
                    return n;
                });
    }

    private void emitNotificationImpl(final ManagementNotificationType<?> category,
                                      final String message,
                                      final Object userData) {
        notifications.emitStream(category.getCategory(),
                this,
                (connector, metadata) -> connector.createNotifications(metadata, message, userData));
    }

    /**
     * Emits notification from this Bean.
     * @param category Category of the notification to emit.
     * @param message Human-readable message associated with emitted notification.
     */
    protected final void emitNotification(final ManagementNotificationType<?> category,
                                          final String message) {
        emitNotificationImpl(category, message, null);
    }

    /**
     * Emits notification from this Bean.
     * @param category Category of the notification to emit.
     * @param message Human-readable message associated with emitted notification.
     * @param userData An object to be attached.
     */
    protected final <T> void emitNotification(final ManagementNotificationType<T> category,
                                              final String message,
                                              final T userData) {
        emitNotificationImpl(category, message, userData);
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    public void close() throws Exception {
        removeFeatures(attributes);
        removeFeatures(notifications);
        removeFeatures(operations);
        notifications.removeNotificationListeners();
        Introspector.flushFromCaches(getClass());
        super.close();
    }
}
