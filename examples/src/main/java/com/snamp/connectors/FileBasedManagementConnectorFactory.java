package com.snamp.connectors;

import java.util.Map;

/**
 * Represents factory for the {@link FileBasedManagementConnector} connector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class FileBasedManagementConnectorFactory extends AbstractManagementConnectorFactory<FileBasedManagementConnector> {

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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
