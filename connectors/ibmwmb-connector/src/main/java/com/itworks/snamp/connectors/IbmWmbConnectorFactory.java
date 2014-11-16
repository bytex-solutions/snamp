package com.itworks.snamp.connectors;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.util.Map;
import java.util.logging.Level;

/**
 * User: adonai
 * Date: 13.11.13
 * Time: 15:20
 */
@PluginImplementation
public class IbmWmbConnectorFactory extends AbstractManagementConnectorFactory<IbmWmbConnector>
{
    /**
     * Initializes a new connector factory.
     */
    public IbmWmbConnectorFactory()
    {
        super(IbmWmbConnector.NAME);
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
            return new IbmWmbConnector(connectionString, env);
        }
        catch (Exception e) {
            getLogger().log(Level.SEVERE, "Unable to create IBM WMB connector", e);
            return null;
        }
    }
}
