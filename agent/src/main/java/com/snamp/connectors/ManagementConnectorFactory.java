package com.snamp.connectors;

import com.snamp.PlatformPlugin;

import java.util.Map;

/**
 * Represents management connector factory.
 * @author roman
 */
public interface ManagementConnectorFactory extends PlatformPlugin {
    /**
     * Creates a new instance of the connector.
     * @param connectionString The protocol-specific connection string.
     * @param connectionProperties The connection properties such as credentials.
     * @return A new instance of the management connector.
     */
    public ManagementConnector newInstance(final String connectionString, final Map<String, String> connectionProperties);
}
