package com.itworks.snamp.connectors.rshell;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.ManagedResourceActivator;
import com.itworks.snamp.internal.annotations.MethodStub;
import com.itworks.snamp.internal.annotations.SpecialUse;

import javax.management.openmbean.CompositeData;
import java.util.Map;

/**
 * Represents an activator of the rshell resource connector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class RShellResourceConnectorActivator extends ManagedResourceActivator<RShellResourceConnector> {
    private static final class RShellConnectorFactory extends ManagedResourceConnectorSimpleModeler<RShellResourceConnector> {

        @Override
        public RShellResourceConnector createConnector(final String resourceName,
                                                       final String connectionString,
                                                       final Map<String, String> connectionOptions,
                                                       final RequiredService<?>... dependencies) throws Exception {
            return new RShellResourceConnector(resourceName,
                    connectionString,
                    connectionOptions);
        }

        @Override
        protected void removeAllAttributes(final RShellResourceConnector connector) {
            connector.removeAllAttributes();
        }

        @Override
        protected void addAttribute(final RShellResourceConnector connector, final String attributeID, final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options) {
            connector.addAttribute(attributeID, attributeName, readWriteTimeout, options);
        }

        @Override
        @MethodStub
        protected void removeAllNotifications(final RShellResourceConnector connector) {
            //not supported
        }

        @MethodStub
        @Override
        protected void enableNotifications(final RShellResourceConnector connector, final String listId, final String category, final CompositeData options) {
            //not supported
        }
    }

    /**
     * Initializes a new instance of the connector activator.
     */
    @SpecialUse
    public RShellResourceConnectorActivator() {
        super(RShellResourceConnector.NAME,
                new RShellConnectorFactory(),
                new ConfigurationEntityDescriptionManager<RShellConnectorConfigurationDescriptor>() {
                    @Override
                    protected RShellConnectorConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
                        return new RShellConnectorConfigurationDescriptor();
                    }
                });
    }
}
