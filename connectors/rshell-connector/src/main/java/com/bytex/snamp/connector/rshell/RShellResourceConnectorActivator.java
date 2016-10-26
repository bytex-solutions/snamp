package com.bytex.snamp.connector.rshell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.ManagedResourceActivator;

import java.util.Map;

/**
 * Represents an activator of the rshell resource connector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class RShellResourceConnectorActivator extends ManagedResourceActivator<RShellResourceConnector> {
    /**
     * Initializes a new instance of the connector activator.
     */
    @SpecialUse
    public RShellResourceConnectorActivator() {
        super(RShellResourceConnectorActivator::createConnector);
    }

    private static RShellResourceConnector createConnector(final String resourceName,
                                                   final String connectionString,
                                                   final Map<String, String> connectionOptions,
                                                   final RequiredService<?>... dependencies) throws Exception {
        return new RShellResourceConnector(resourceName,
                connectionString,
                connectionOptions);
    }
}
