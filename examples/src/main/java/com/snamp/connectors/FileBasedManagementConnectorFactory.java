package com.snamp.connectors;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.io.File;
import java.util.Map;

/**
 * Represents factory for the {@link FileBasedManagementConnector} connector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@PluginImplementation
final class FileBasedManagementConnectorFactory extends AbstractManagementConnectorFactory<FileBasedManagementConnector> {

    /**
     * Initializes a new file-based management connector factory.
     */
    public FileBasedManagementConnectorFactory(){
        super(FileBasedManagementConnector.NAME);
    }

    /**
     * Creates a new instance of the connector.
     *
     * @param connectionString     The protocol-specific connection string.
     * @param connectionProperties The connection properties such as credentials.
     * @return A new instance of the management connector.
     */
    @Override
    public FileBasedManagementConnector newInstance(final String connectionString, final Map<String, String> connectionProperties) {
        return new FileBasedManagementConnector(new File(connectionString));
    }
}
