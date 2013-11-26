package com.snamp.connectors;

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
public class IbmMqConnectorFactory extends AbstractManagementConnectorFactory<IbmMqConnector> {

    /**
     * Initializes a new connector factory.
     */
    public IbmMqConnectorFactory() {
        super(IbmMqConnector.NAME);
    }

    @Override
    public IbmMqConnector newInstance(String connectionString, Map<String, String> connectionProperties) {
        try {
            return new IbmMqConnector(connectionString, connectionProperties, new WellKnownTypeSystem<>(EntityTypeInfoBuilder.AttributeTypeConverter.class));
        } catch (IntrospectionException e) {
            getLogger().log(Level.SEVERE, "Unable to create IBM MQ connector", e);
            return null;
        }
    }
}
