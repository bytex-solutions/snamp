package com.bytex.snamp.connectors;

import com.bytex.snamp.ThreadSafe;
import com.bytex.snamp.core.FrameworkService;
import com.bytex.snamp.internal.Utils;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleRevision;

import javax.management.DynamicMBean;
import javax.management.MBeanFeatureInfo;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents management connector that provides unified access to the management information.
 * <p>
 *     By default, managed resource connector doesn't expose default management mechanisms.
 *     The class that implements this interface must implement one or more following interfaces
 *     to provide management mechanisms:
 *     <ul>
 *         <li>{@link com.bytex.snamp.connectors.attributes.AttributeSupport} to provide management
 *         via resource attributes.</li>
 *         <li>{@link com.bytex.snamp.connectors.notifications.NotificationSupport} to receiver
 *         management notifications.</li>
 *     </ul>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.2
 */
public interface ManagedResourceConnector extends AutoCloseable, FrameworkService, DynamicMBean {
    /**
     * This namespace must be defined in Provide-Capability manifest header inside of the bundle containing implementation
     * of Managed Resource Connector.
     * <p>
     *     Example: Provide-Capability: com.bytex.snamp.connectors; type=jmx
     */
    String CAPABILITY_NAMESPACE = "com.bytex.snamp.connectors";

    /**
     * This property must be defined in Provide-Capability manifest header and specify type of Managed Resource Connector.
     * @see #CAPABILITY_NAMESPACE
     */
    String TYPE_CAPABILITY_ATTRIBUTE = "type";

    /**
     * Represents an exception indicating that the resource connector cannot be updated
     * without it recreation. This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.2
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
    void update(final String connectionString,
                final Map<String, String> connectionParameters) throws Exception;

    /**
     * Adds a new listener for the connector-related events.
     * <p>
     *     The managed resource connector should holds a weak reference to all added event listeners.
     * @param listener An event listener to add.
     */
    @ThreadSafe
    void addResourceEventListener(final ResourceEventListener listener);

    /**
     * Removes connector event listener.
     * @param listener The listener to remove.
     */
    @ThreadSafe
    void removeResourceEventListener(final ResourceEventListener listener);

    /**
     * Determines whether the connector may automatically expanded with features without predefined configuration.
     * @param featureType Type of the feature. Cannot be {@literal null}.
     * @return {@literal true}, if this connector supports automatic registration of its features; otherwise, {@literal false}.
     */
    boolean canExpandWith(final Class<? extends MBeanFeatureInfo> featureType);

    /**
     * Expands this connector with features of the specified type.
     * @param featureType The type of the feature that this connector may automatically registers.
     * @param <F> Type of the feature class.
     * @return A collection of registered features; or empty collection if the specified feature type is not supported.
     */
    <F extends MBeanFeatureInfo> Collection<? extends F> expand(final Class<F> featureType);

    static String getResourceConnectorType(final Bundle bnd){
        final BundleRevision revision = bnd.adapt(BundleRevision.class);
        assert revision != null;
        return revision.getCapabilities(CAPABILITY_NAMESPACE)
                .stream()
                .map(capability -> capability.getAttributes().get(TYPE_CAPABILITY_ATTRIBUTE))
                .map(name -> Objects.toString(name, ""))
                .findFirst()
                .orElseGet(() -> "");
    }

    static boolean isResourceConnectorBundle(final Bundle bnd) {
        return bnd != null && !isNullOrEmpty(getResourceConnectorType(bnd));
    }

    static boolean isResourceConnector(final ServiceReference<?> ref){
        return Utils.isInstanceOf(ref, ManagedResourceConnector.class) && isResourceConnectorBundle(ref.getBundle());
    }
}
