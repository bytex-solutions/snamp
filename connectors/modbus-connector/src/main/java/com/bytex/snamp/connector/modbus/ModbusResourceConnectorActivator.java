package com.bytex.snamp.connector.modbus;

import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.SpecialUse;

import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static com.bytex.snamp.connector.modbus.ModbusResourceConnectorConfigurationDescriptor.parseRetryCount;
import static com.bytex.snamp.connector.modbus.ModbusResourceConnectorConfigurationDescriptor.parseConnectionTimeout;

/**
 * Represents
 */
public final class ModbusResourceConnectorActivator extends ManagedResourceActivator<ModbusResourceConnector> {
    private static final class ModbusConnectorFactory extends ManagedResourceConnectorModeler<ModbusResourceConnector>{

        @Override
        protected boolean addAttribute(final ModbusResourceConnector connector, final String attributeName, final Duration readWriteTimeout, final CompositeData options) {
            return connector.addAttribute(attributeName, readWriteTimeout, options);
        }

        @Override
        protected void retainAttributes(final ModbusResourceConnector connector, final Set<String> attributes) {
            connector.removeAttributesExcept(attributes);
        }

        @Override
        protected boolean enableNotifications(final ModbusResourceConnector connector, final String category, final CompositeData options) {
            //not supported
            return false;
        }

        @Override
        protected void retainNotifications(final ModbusResourceConnector connector, final Set<String> events) {
            //not supported
        }

        @Override
        protected boolean enableOperation(final ModbusResourceConnector connector, final String operationName, final Duration invocationTimeout, final CompositeData options) {
            //not supported
            return false;
        }

        @Override
        protected void retainOperations(final ModbusResourceConnector connector, final Set<String> operations) {
            //not supported
        }

        @Override
        public ModbusResourceConnector createConnector(final String resourceName, final String connectionString, final Map<String, String> connectionParameters, final RequiredService<?>... dependencies) throws URISyntaxException, IOException {
            final ModbusResourceConnector connector = new ModbusResourceConnector(resourceName, new URI(connectionString));
            connector.connect(parseConnectionTimeout(connectionParameters), parseRetryCount(connectionParameters));
            return connector;
        }
    }

    @SpecialUse
    public ModbusResourceConnectorActivator() {
        super(new ModbusConnectorFactory(), configurationDescriptor(ModbusResourceConnectorConfigurationDescriptor::new));
    }
}
