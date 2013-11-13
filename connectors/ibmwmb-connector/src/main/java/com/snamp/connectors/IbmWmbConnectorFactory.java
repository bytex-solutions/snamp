package com.snamp.connectors;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * User: adonai
 * Date: 13.11.13
 * Time: 15:20
 */
public class IbmWmbConnectorFactory extends AbstractManagementConnectorFactory<IbmWmbConnector>
{
    /**
     * Initializes a new connector factory.
     *
     * @param connectorName The name of the connector.
     * @throws IllegalArgumentException
     *          connectorName is null.
     */
    protected IbmWmbConnector(String connectorName)
    {
        super(connectorName);
    }

    @Override
    public IbmWmbConnector newInstance(String connectionString, Map<String, String> env)
    {
        try {
            return new IbmWmbConnector(env);
        }
        catch (Exception e) {
            getLogger().log(Level.SEVERE, "Unable to create IBM MQ connector", e);
        return null;
    }
    }
}
