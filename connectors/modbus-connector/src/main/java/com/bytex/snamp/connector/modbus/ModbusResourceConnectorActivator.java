package com.bytex.snamp.connector.modbus;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.ManagedResourceActivator;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static com.bytex.snamp.connector.modbus.ModbusResourceConnectorConfigurationDescriptor.parseConnectionTimeout;
import static com.bytex.snamp.connector.modbus.ModbusResourceConnectorConfigurationDescriptor.parseRetryCount;

/**
 * Represents
 */
public final class ModbusResourceConnectorActivator extends ManagedResourceActivator<ModbusResourceConnector> {

    @SpecialUse
    public ModbusResourceConnectorActivator() {
        super(ModbusResourceConnectorActivator::createConnector, configurationDescriptor(ModbusResourceConnectorConfigurationDescriptor::new));
    }

    private static ModbusResourceConnector createConnector(final String resourceName, final String connectionString, final Map<String, String> connectionParameters, final RequiredService<?>... dependencies) throws URISyntaxException, IOException {
        final ModbusResourceConnector connector = new ModbusResourceConnector(resourceName, new URI(connectionString));
        connector.connect(parseConnectionTimeout(connectionParameters), parseRetryCount(connectionParameters));
        return connector;
    }
}
