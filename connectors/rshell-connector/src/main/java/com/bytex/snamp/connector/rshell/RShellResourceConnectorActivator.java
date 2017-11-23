package com.bytex.snamp.connector.rshell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.ManagedResourceActivator;

import java.util.Map;

/**
 * Represents an activator of the rshell resource connector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class RShellResourceConnectorActivator extends ManagedResourceActivator {
    private static final class RShellConnectorLifecycleManager extends DefaultManagedResourceLifecycleManager<RShellResourceConnector>{

        @Override
        protected RShellResourceConnector createConnector(final String resourceName, final String connectionString, final Map<String, String> configuration) throws Exception {
            return new RShellResourceConnector(resourceName, new RShellConnectionOptions(connectionString, configuration));
        }
    }

    /**
     * Initializes a new instance of the connector activator.
     */
    @SpecialUse(SpecialUse.Case.OSGi)
    public RShellResourceConnectorActivator() {
        super(new RShellConnectorLifecycleManager());
    }
}
