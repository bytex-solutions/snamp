package com.itworks.snamp.connectors;

import com.itworks.snamp.core.FrameworkService;

import javax.management.DynamicMBean;
import java.util.Map;

/**
 * Represents management connector that provides unified access to the management information.
 * <p>
 *     By default, managed resource connector doesn't expose default management mechanisms.
 *     The class that implements this interface must implement one or more following interfaces
 *     to provide management mechanisms:
 *     <ul>
 *         <li>{@link com.itworks.snamp.connectors.attributes.AttributeSupport} to provide management
 *         via resource attributes.</li>
 *         <li>{@link com.itworks.snamp.connectors.notifications.NotificationSupport} to receiver
 *         management notifications.</li>
 *     </ul>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface ManagedResourceConnector extends AutoCloseable, FrameworkService, DynamicMBean {
    /**
     * Represents an exception indicating that the resource connector cannot be updated
     * without it recreation. This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static final class UnsupportedUpdateOperationException extends UnsupportedOperationException{
        private static final long serialVersionUID = 8128304831615736668L;

        /**
         * Initializes a new exception.
         * @param message A human-readable explanation.
         * @param args Formatting arguments.
         */
        public UnsupportedUpdateOperationException(final String message, final Object... args){
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
}
