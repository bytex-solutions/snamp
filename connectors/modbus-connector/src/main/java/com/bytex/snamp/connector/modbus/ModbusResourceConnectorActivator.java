package com.bytex.snamp.connector.modbus;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.ManagedResourceActivator;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Represents activator of {@link ModbusResourceConnector}.
 */
public final class ModbusResourceConnectorActivator extends ManagedResourceActivator<ModbusResourceConnector> {

    @SpecialUse(SpecialUse.Case.OSGi)
    public ModbusResourceConnectorActivator() {
        super(ModbusResourceConnectorActivator::createConnector, configurationDescriptor(ModbusResourceConnectorConfigurationDescriptor::new));
    }

    @Nonnull
    private static ModbusResourceConnector createConnector(final String resourceName,
                                                           final ManagedResourceInfo configuration,
                                                           final DependencyManager dependencies) throws URISyntaxException, IOException {
        final ModbusResourceConnector connector = new ModbusResourceConnector(resourceName, new URI(configuration.getConnectionString()));
        connector.connect(configuration);
        return connector;
    }
}
