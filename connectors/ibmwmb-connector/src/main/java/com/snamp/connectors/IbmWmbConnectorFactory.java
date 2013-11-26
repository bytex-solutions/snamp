package com.snamp.connectors;

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
     */
    protected IbmWmbConnectorFactory()
    {
        super(IbmWmbConnector.connectorName);
    }

    @Override
    /**
     * Compounds a new instance of IbmWmbConnector
     * @param connectionString String of following format: "host;port;qmgr_name"
     *
     */
    public IbmWmbConnector newInstance(String connectionString, Map<String, String> env)
    {
        try {
            return new IbmWmbConnector(connectionString, env, new IbmWmbTypeSystem());
        }
        catch (Exception e) {
            getLogger().log(Level.SEVERE, "Unable to create IBM WMB connector", e);
        return null;
        }
    }
}
