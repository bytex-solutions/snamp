package com.bytex.snamp.connector;

import com.bytex.snamp.Aggregator;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.connector.operations.OperationSupport;
import com.bytex.snamp.core.FrameworkService;
import com.bytex.snamp.internal.Utils;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleRevision;

import javax.annotation.Nonnull;
import javax.management.*;
import java.util.*;
import java.util.function.Supplier;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents management connector that provides unified access to the management information.
 * <p>
 *     By default, managed resource connector doesn't expose default management mechanisms.
 *     The class that implements this interface must implement one or more following interfaces
 *     to provide management mechanisms:
 *     <ul>
 *         <li>{@link com.bytex.snamp.connector.attributes.AttributeSupport} to provide management
 *         via resource attributes.</li>
 *         <li>{@link com.bytex.snamp.connector.notifications.NotificationSupport} to receiver
 *         management notifications.</li>
 *         <li>{@link com.bytex.snamp.connector.operations.OperationSupport} to provide management
 *         via operations.</li>
 *     </ul>
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public interface ManagedResourceConnector extends AutoCloseable, FrameworkService, DynamicMBean {
    /**
     * This namespace must be defined in Provide-Capability manifest header inside of the bundle containing implementation
     * of Managed Resource Connector.
     * <p>
     *     Example: Provide-Capability: com.bytex.snamp.connector; type=jmx
     */
    String CAPABILITY_NAMESPACE = "com.bytex.snamp.connector";

    /**
     * This property must be defined in Provide-Capability manifest header and specify type of Managed Resource Connector.
     * @see #CAPABILITY_NAMESPACE
     */
    String TYPE_CAPABILITY_ATTRIBUTE = "type";

    /**
     * Represents property that contains unique name of the managed resource instance.
     */
    String NAME_PROPERTY = "resourceName";

    /**
     * Represents property that contains connection string associated with the managed resource instance.
     */
    String CONNECTION_STRING_PROPERTY = "connectionString";

    /**
     * Represents an exception indicating that the resource connector cannot be updated
     * without it recreation. This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    final class UnsupportedUpdateOperationException extends UnsupportedOperationException{
        private static final long serialVersionUID = 8128304831615736668L;

        /**
         * Initializes a new exception.
         * @param message A human-readable explanation.
         * @param args Formatting arguments.
         */
        UnsupportedUpdateOperationException(final String message, final Object... args){
            super(String.format(message, args));
        }
    }

    /**
     * Updates resource connector with a new connection options.
     * @param connectionString A new connection string.
     * @param connectionParameters A new connection parameters.
     * @throws Exception Unable to update managed resource connector.
     * @throws UnsupportedUpdateOperationException This operation is not supported
     *  by this resource connector.
     */
    default void update(final String connectionString,
                final Map<String, String> connectionParameters) throws Exception{
        throw new UnsupportedUpdateOperationException("Update operation is not supported");
    }

    /**
     * Adds a new listener for the connector-related events.
     * <p>
     *     The managed resource connector should holds a weak reference to all added event listeners.
     * @param listener An event listener to add.
     */
    void addResourceEventListener(final ResourceEventListener listener);

    /**
     * Removes connector event listener.
     * @param listener The listener to remove.
     */
    void removeResourceEventListener(final ResourceEventListener listener);

    /**
     * Gets mutable set of characteristics of this managed resource connector.
     * @return Characteristics of this managed resource connector.
     */
    @Override
    @Nonnull
    Map<String, String> getRuntimeConfiguration();

    static String getConnectorType(final Bundle bnd) {
        final BundleRevision revision = bnd.adapt(BundleRevision.class);
        assert revision != null;
        return revision.getCapabilities(CAPABILITY_NAMESPACE)
                .stream()
                .map(capability -> capability.getAttributes().get(TYPE_CAPABILITY_ATTRIBUTE))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst()
                .orElse("");
    }

    /**
     * Returns system name of the connector using its implementation class.
     * @param connectorImpl A class that represents implementation of resource connector.
     * @return System name of the connector.
     */
    static String getConnectorType(final Class<? extends ManagedResourceConnector> connectorImpl){
        return getConnectorType(FrameworkUtil.getBundle(connectorImpl));
    }

    static boolean isResourceConnectorBundle(final Bundle bnd) {
        return bnd != null && !isNullOrEmpty(getConnectorType(bnd));
    }

    static boolean isResourceConnector(final ServiceReference<?> ref){
        return Utils.isInstanceOf(ref, ManagedResourceConnector.class) && isResourceConnectorBundle(ref.getBundle());
    }

    /**
     * Determines whether the Smart-mode is supported by the specified connector.
     * @param connector An instance of the connector. Cannot be {@literal null}.
     * @return {@literal true}, if Smart-mode is supported; otherwise, {@literal false}.
     */
    static boolean isSmartModeSupported(final ManagedResourceConnector connector) {
        return connector.canExpand(MBeanAttributeInfo.class) ||
                connector.canExpand(MBeanNotificationInfo.class) ||
                connector.canExpand(MBeanOperationInfo.class);
    }

    default boolean canExpand(final Class<? extends MBeanFeatureInfo> featureType) {
        final Supplier<Boolean> FALLBACK = () -> Boolean.FALSE;
        if (featureType.equals(MBeanAttributeInfo.class))
            return Aggregator.queryAndApply(this, AttributeSupport.class, AttributeSupport::canExpandAttributes, FALLBACK);
        else if (featureType.equals(MBeanNotificationInfo.class))
            return Aggregator.queryAndApply(this, NotificationSupport.class, NotificationSupport::canExpandNotifications, FALLBACK);
        else if (featureType.equals(MBeanOperationInfo.class))
            return Aggregator.queryAndApply(this, OperationSupport.class, OperationSupport::canExpandOperations, FALLBACK);
        else
            return FALLBACK.get();
    }

    default Collection<? extends MBeanFeatureInfo> expandAll() {
        final List<MBeanFeatureInfo> result = new LinkedList<>();
        result.addAll(Aggregator.queryAndApply(this,
                AttributeSupport.class,
                attributeSupport -> attributeSupport.canExpandAttributes() ? attributeSupport.expandAttributes() : Collections.emptyList(),
                Collections::emptyList));
        result.addAll(Aggregator.queryAndApply(this,
                NotificationSupport.class,
                notificationSupport -> notificationSupport.canExpandNotifications() ? notificationSupport.expandNotifications() : Collections.emptyList(),
                Collections::emptyList));
        result.addAll(Aggregator.queryAndApply(this,
                OperationSupport.class,
                operationSupport -> operationSupport.canExpandOperations() ? operationSupport.expandOperations() : Collections.emptyList(),
                Collections::emptyList));
        return result;
    }
}
