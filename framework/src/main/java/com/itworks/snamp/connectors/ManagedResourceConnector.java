package com.itworks.snamp.connectors;

import com.itworks.snamp.core.FrameworkService;

/**
 * Represents management connector that provides unified access to the management information.
 * <p>
 *     By default, managed resource connector doesn't expose default management mechanisms.
 *     The class that implements this interface must implement one or more following interfaces
 *     to provide management mechanisms:
 *     <ul>
 *         <li>{@link com.itworks.snamp.connectors.attributes.AttributeSupport} to provide management
 *         via resource properties.</li>
 *         <li>{@link com.itworks.snamp.connectors.notifications.NotificationSupport} to receiver
 *         management notifications.</li>
 *         <li>{@link com.itworks.snamp.connectors.operations.OperationSupport} to operate
 *         with managed resource in service-like manner.</li>
 *     </ul>
 * </p>
 * @param <TConnectionOptions> The connection options used to initiate connection to the management target.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface ManagedResourceConnector<TConnectionOptions> extends AutoCloseable, FrameworkService {
    /**
     * Returns connection options used by this management connector.
     * @return The connection options used by this management connector.
     */
    TConnectionOptions getConnectionOptions();
}
