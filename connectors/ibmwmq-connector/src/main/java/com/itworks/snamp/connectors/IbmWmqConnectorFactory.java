package com.itworks.snamp.connectors;

import com.ibm.mq.MQException;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.beans.IntrospectionException;
import java.util.Map;
import java.util.logging.Level;

/**
 * Factory for instantiating IBM MQ Connectors
 * @author  Chernovsky Oleg
 * @since 1.1.0
 */
@PluginImplementation
public final class IbmWmqConnectorFactory extends AbstractManagementConnectorFactory<IbmWmqConnector> {

    /**
     * Initializes a new connector factory.
     */
    public IbmWmqConnectorFactory() {
        super(IbmWmqConnector.NAME);
    }

    @Override
    public IbmWmqConnector newInstance(String connectionString, Map<String, String> connectionProperties) {
        try {
            return new IbmWmqConnector(connectionString, connectionProperties);
        } catch (final IntrospectionException | MQException e) {
            getLogger().log(Level.SEVERE, "Unable to create IBM MQ connector", e);
            return null;
        }
    }
}
