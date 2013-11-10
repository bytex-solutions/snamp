package com.snamp.connectors;

import com.snamp.*;

import java.util.Map;

/**
 * Represents management connector factory.
 * <p>
 *     The implementer class should have parameterless constructor and annotated with {@link net.xeoh.plugins.base.annotations.PluginImplementation}.
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Lifecycle(InstanceLifecycle.SINGLE_PER_PROCESS)
public interface ManagementConnectorFactory extends PlatformPlugin {
    /**
     * Creates a new instance of the connector.
     * @param connectionString The protocol-specific connection string.
     * @param connectionProperties The connection properties such as credentials.
     * @return A new instance of the management connector.
     */
    public ManagementConnector newInstance(final String connectionString, final Map<String, String> connectionProperties);
}
