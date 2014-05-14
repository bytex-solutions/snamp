package com.itworks.snamp.connectors;

import com.itworks.snamp.core.FrameworkService;

/**
 * Represents management connector that provides unified access to the management information.
 * @param <TConnectionOptions> The connection options used to initiate connection to the management target.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface ManagementConnector<TConnectionOptions> extends AutoCloseable, FrameworkService, AttributeSupport {
    /**
     * Returns connection options used by this management connector.
     * @return The connection options used by this management connector.
     */
    TConnectionOptions getConnectionOptions();
}
