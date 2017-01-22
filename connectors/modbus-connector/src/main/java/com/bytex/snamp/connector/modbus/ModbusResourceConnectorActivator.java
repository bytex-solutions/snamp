package com.bytex.snamp.connector.modbus;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.ManagedResourceActivator;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Represents
 */
public final class ModbusResourceConnectorActivator extends ManagedResourceActivator<ModbusResourceConnector> {

    @SpecialUse
    public ModbusResourceConnectorActivator() {
        super(ModbusResourceConnectorActivator::createConnector, configurationDescriptor(ModbusResourceConnectorConfigurationDescriptor::new));
    }

    private static ModbusResourceConnector createConnector(final String resourceName,
                                                           final ManagedResourceInfo configuration,
                                                           final DependencyManager dependencies) throws URISyntaxException, IOException {
        final ModbusResourceConnector connector = new ModbusResourceConnector(resourceName, new URI(configuration.getConnectionString()));
        connector.connect(configuration);
        return connector;
    }
}
