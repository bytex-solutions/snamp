package com.bytex.snamp.connector.rshell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.ManagedResourceActivator;

/**
 * Represents an activator of the rshell resource connector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class RShellResourceConnectorActivator extends ManagedResourceActivator<RShellResourceConnector> {
    /**
     * Initializes a new instance of the connector activator.
     */
    @SpecialUse(SpecialUse.Case.OSGi)
    public RShellResourceConnectorActivator() {
        super(RShellResourceConnectorActivator::createConnector);
    }

    private static RShellResourceConnector createConnector(final String resourceName,
                                                           final ManagedResourceInfo configuration,
                                                   final DependencyManager dependencies) throws Exception {
        return new RShellResourceConnector(resourceName, configuration);
    }
}
