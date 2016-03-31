package com.bytex.snamp.connectors.rshell;

import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.connectors.ManagedResourceActivator;
import com.bytex.snamp.MethodStub;
import com.bytex.snamp.SpecialUse;

import javax.management.openmbean.CompositeData;
import java.util.Map;
import java.util.Set;

/**
 * Represents an activator of the rshell resource connector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class RShellResourceConnectorActivator extends ManagedResourceActivator<RShellResourceConnector> {
    private static final class RShellConnectorFactory extends ManagedResourceConnectorModeler<RShellResourceConnector> {

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
        protected boolean addAttribute(final RShellResourceConnector connector, final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options) {
            return connector.addAttribute(attributeName, readWriteTimeout, options);
        }
        @Override
        protected void retainAttributes(final RShellResourceConnector connector, final Set<String> attributes) {
            connector.removeAttributesExcept(attributes);
        }

        @MethodStub
        @Override
        protected boolean enableNotifications(final RShellResourceConnector connector, final String category, final CompositeData options) {
            //not supported
            return false;
        }

        @Override
        protected void retainNotifications(final RShellResourceConnector connector, final Set<String> events) {
            //not supported
        }

        @Override
        protected boolean enableOperation(final RShellResourceConnector connector, final String operationName, final TimeSpan timeout, final CompositeData options) {
            //not supported
            return false;
        }

        @Override
        protected void retainOperations(final RShellResourceConnector connector, final Set<String> operations) {
            //not supported
        }
    }

    /**
     * Initializes a new instance of the connector activator.
     */
    @SpecialUse
    public RShellResourceConnectorActivator() {
        super(new RShellConnectorFactory());
    }
}
