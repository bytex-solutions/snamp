package com.bytex.snamp.connector;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.Localizable;
import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.FeatureConfiguration;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.attributes.reflection.JavaBeanAttributeInfo;
import com.bytex.snamp.connector.attributes.reflection.JavaBeanAttributeRepository;
import com.bytex.snamp.connector.discovery.DiscoveryResultBuilder;
import com.bytex.snamp.connector.discovery.DiscoveryService;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.connector.notifications.*;
import com.bytex.snamp.connector.operations.OperationSupport;
import com.bytex.snamp.connector.operations.reflection.JavaBeanOperationRepository;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.core.SharedCounter;
import org.osgi.framework.BundleContext;

import javax.management.openmbean.OpenType;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.bytex.snamp.configuration.ConfigurationManager.createEntityConfiguration;
import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;


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

    private static final class JavaBeanNotificationRepository extends AbstractNotificationRepository<AbstractNotificationInfo> {
        private final Logger logger;
        private final Set<? extends ManagementNotificationType<?>> notifTypes;
        private final NotificationListenerInvoker listenerInvoker;
        private final SharedCounter sequenceNumberGenerator;

        private JavaBeanNotificationRepository(final String resourceName,
                                               final Set<? extends ManagementNotificationType<?>> notifTypes,
                                               final BundleContext context,
                                               final Logger logger) {
            super(resourceName, AbstractNotificationInfo.class, false);
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
        protected AbstractNotificationInfo connectNotifications(final String category,
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
                return new AbstractNotificationInfo(category, description, metadata.setUserDataType(type.getUserDataType()));
            } else
                throw new IllegalArgumentException(String.format("Unsupported notification %s", metadata.getName(category)));
        }

        @Override
        protected void failedToEnableNotifications(final String category, final Exception e) {
            failedToEnableNotifications(logger, Level.WARNING, category, e);
        }

        private boolean fire(final ManagementNotificationType<?> category, final String message, final Object userData) {
            return fire(category.getCategory(), message, sequenceNumberGenerator, userData);
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
                    .filter(JavaBeanAttributeInfo::isValidDescriptor)
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
        attributes = JavaBeanAttributeRepository.create(resourceName, this, beanInfo);
        notifications = new JavaBeanNotificationRepository(resourceName,
                notifTypes,
                getBundleContextOfObject(this),
                getLogger());
        operations = JavaBeanOperationRepository.create(resourceName, this, beanInfo);
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
        return notifications.fire(category, message, userData);
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
