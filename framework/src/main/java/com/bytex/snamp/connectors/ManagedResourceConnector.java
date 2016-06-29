package com.bytex.snamp.connectors;

import com.bytex.snamp.ThreadSafe;
import com.bytex.snamp.core.FrameworkService;

import javax.management.DynamicMBean;
import javax.management.MBeanFeatureInfo;
import java.util.Collection;
import java.util.Map;

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
     * Represents name of the manifest header which contains the name of the management connector.
     * <p>
     *     The following example demonstrates how to set the name of the management connector
     *     in the connector's bundle manifest:
     *     <pre><tt>
     *          SNAMP-Resource-Connector: impl
     *     </tt></pre>
     */
    String CONNECTOR_NAME_MANIFEST_HEADER = "SNAMP-Resource-Connector";

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
}
