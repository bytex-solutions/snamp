package com.bytex.snamp.connector;

import com.bytex.snamp.Localizable;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.attributes.reflection.JavaBeanAttributeRepository;
import com.bytex.snamp.connector.health.HealthCheckSupport;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.OkStatus;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.connector.notifications.AbstractNotificationInfo;
import com.bytex.snamp.connector.notifications.AbstractNotificationRepository;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.connector.operations.OperationSupport;
import com.bytex.snamp.connector.operations.reflection.JavaBeanOperationRepository;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.SharedCounter;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.management.openmbean.OpenType;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.*;

import static com.bytex.snamp.core.SharedObjectType.COUNTER;
import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;
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
 * @version 2.0
 */
public abstract class ManagedResourceConnectorBean extends AbstractManagedResourceConnector implements HealthCheckSupport {

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
        private final Set<? extends ManagementNotificationType<?>> notifTypes;
        private final SharedCounter sequenceNumberGenerator;

        private JavaBeanNotificationRepository(final String resourceName,
                                               final Set<? extends ManagementNotificationType<?>> notifTypes,
                                               final BundleContext context) {
            super(resourceName, AbstractNotificationInfo.class);
            this.notifTypes = Objects.requireNonNull(notifTypes);
            this.sequenceNumberGenerator = ClusterMember.get(context).getService("notifications-".concat(resourceName), COUNTER)
                    .orElseThrow(AssertionError::new);
        }

        @Override
        public Map<String, NotificationDescriptor> discoverNotifications() {
            final Map<String, NotificationDescriptor> result = new HashMap<>();
            for (final ManagementNotificationType<?> notificationType : notifTypes)
                result.put(notificationType.getCategory(), createDescriptor());
            return result;
        }

        @Override
        protected AbstractNotificationInfo connectNotifications(final String category,
                                                                final NotificationDescriptor metadata) throws IllegalArgumentException {
            //find the suitable notification type
            final ManagementNotificationType<?> type = notifTypes.stream()
                    .filter(type1 -> Objects.equals(type1.getCategory(), metadata.getAlternativeName().orElse(category)))
                    .findFirst()
                    .orElseGet(null);
            if (type != null) {
                String description = type.toString(Locale.getDefault());
                if (isNullOrEmpty(description)) {
                    description = metadata.getDescription();
                    if (isNullOrEmpty(description))
                        description = type.getCategory();
                }
                return new AbstractNotificationInfo(category, description, metadata.setUserDataType(type.getUserDataType()));
            } else
                throw new IllegalArgumentException(String.format("Unsupported notification %s", metadata.getAlternativeName().orElse(category)));
        }

        private boolean fire(final ManagementNotificationType<?> category, final String message, final Object userData) {
            return fire(category.getCategory(), message, sequenceNumberGenerator, userData);
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
        final BeanInfo beanInfo = reflectBeanInfo();
        attributes = JavaBeanAttributeRepository.create(resourceName, this, beanInfo);
        notifications = new JavaBeanNotificationRepository(resourceName,
                notifTypes,
                getBundleContextOfObject(this));
        notifications.setSource(this);
        operations = JavaBeanOperationRepository.create(resourceName, this, beanInfo);
    }

    @Override
    protected final MetricsSupport createMetricsReader(){
        return assembleMetricsReader(attributes, notifications, operations);
    }

    /**
     * Reflects this instance of the managed resource connector.
     * @return Reflection metadata.
     * @throws IntrospectionException Unable to reflect managed resource connector.
     */
    protected BeanInfo reflectBeanInfo() throws IntrospectionException {
        return Introspector.getBeanInfo(getClass(), ManagedResourceConnectorBean.class);
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

    /**
     * Determines whether the connected managed resource is alive.
     *
     * @return Status of the remove managed resource.
     */
    @Override
    @Nonnull
    public HealthStatus getStatus() {
        return new OkStatus();
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
        super.close();
    }
}
