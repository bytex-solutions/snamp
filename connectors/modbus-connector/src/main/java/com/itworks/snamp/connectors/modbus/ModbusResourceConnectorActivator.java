package com.itworks.snamp.connectors.modbus;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.connectors.ManagedResourceActivator;
import com.itworks.snamp.core.AbstractBundleActivator;

import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

/**
 * Represents
 */
public final class ModbusResourceConnectorActivator extends ManagedResourceActivator<ModbusResourceConnector> {
    private static final class ModbusConnectorFactory extends ManagedResourceConnectorModeler<ModbusResourceConnector>{

        @Override
        protected boolean addAttribute(final ModbusResourceConnector connector, final String attributeID, final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options) {
            return connector.addAttribute(attributeID, attributeName, readWriteTimeout, options);
        }

        @Override
        protected void removeAttributesExcept(final ModbusResourceConnector connector, final Set<String> attributes) {
            connector.removeAttributesExcept(attributes);
        }

        @Override
        protected boolean enableNotifications(final ModbusResourceConnector connector, final String listId, final String category, final CompositeData options) {
            return false;
        }

        @Override
        protected void disableNotificationsExcept(final ModbusResourceConnector connector, final Set<String> events) {

        }

        @Override
        protected boolean enableOperation(final ModbusResourceConnector connector, final String operationID, final String operationName, final TimeSpan invocationTimeout, final CompositeData options) {
            //not supported
            return false;
        }

        @Override
        protected void disableOperationsExcept(final ModbusResourceConnector connector, final Set<String> operations) {
            //not supported
        }

        @Override
        public ModbusResourceConnector createConnector(final String resourceName, final String connectionString, final Map<String, String> connectionParameters, final RequiredService<?>... dependencies) throws URISyntaxException, IOException {
            final ModbusResourceConnector connector = new ModbusResourceConnector(resourceName, new URI(connectionString));
            connector.connect();
            return connector;
        }
    }

    public ModbusResourceConnectorActivator(){
        super(new ModbusConnectorFactory());
    }
}
