package com.snamp.connectors;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.beans.IntrospectionException;
import java.net.*;
import java.rmi.*;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@PluginImplementation
final class RmiConnectorFactory extends AbstractManagementConnectorFactory<RmiConnector> {

    /**
     * Initializes a new RMI factory.
     */
    public RmiConnectorFactory(){
        super(RmiConnector.NAME);
    }

    public final RmiConnector newInstance(final URI connectionString){
        try {
            return new RmiConnector(connectionString.getHost(), connectionString.getPort() <= 0 ? Registry.REGISTRY_PORT : connectionString.getPort(), connectionString.getPath().substring(1), getLogger());
        }
        catch (final RemoteException | IntrospectionException | NotBoundException e) {
            getLogger().log(Level.SEVERE, e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Creates a new instance of the connector.
     *
     * @param connectionString     The protocol-specific connection string.
     * @param connectionProperties The connection properties such as credentials.
     * @return A new instance of the management connector.
     */
    @Override
    public final RmiConnector newInstance(final String connectionString, final Map<String, String> connectionProperties) {
        try {
            return newInstance(new URI(connectionString));
        }
        catch (final URISyntaxException e) {
            getLogger().log(Level.SEVERE, "Invalid RMI connection string. Expected rmi://host[:port]/remoteObject", e);
            return null;
        }
    }
}
