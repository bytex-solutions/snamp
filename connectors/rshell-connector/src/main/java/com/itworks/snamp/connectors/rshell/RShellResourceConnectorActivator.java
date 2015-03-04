package com.itworks.snamp.connectors.rshell;

import com.itworks.snamp.connectors.ManagedResourceActivator;
import com.itworks.snamp.internal.annotations.SpecialUse;

import java.util.Map;

/**
 * Represents an activator of the rshell resource connector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class RShellResourceConnectorActivator extends ManagedResourceActivator<RShellResourceConnector> {
    private static final class RShellConnectorFactory extends ManagedResourceConnectorFactory<RShellResourceConnector> {

        @Override
        public RShellResourceConnector createConnector(final String resourceName,
                                                       final String connectionString,
                                                       final Map<String, String> connectionOptions,
                                                       final RequiredService<?>... dependencies) throws Exception {
            return new RShellResourceConnector(connectionString,
                    connectionOptions);
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
