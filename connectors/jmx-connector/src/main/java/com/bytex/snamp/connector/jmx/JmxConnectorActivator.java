package com.bytex.snamp.connector.jmx;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.connector.ManagedResourceActivator;

import javax.annotation.Nonnull;
import javax.management.JMException;
import java.io.IOException;


/**
 * Represents bundle activator for JMX connector.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class JmxConnectorActivator extends ManagedResourceActivator {
    private static final class JmxConnectorFactory extends ManagedResourceLifecycleManager<JmxConnector>{

        @Nonnull
        @Override
        protected JmxConnector createConnector(final String resourceName, final ManagedResourceConfiguration configuration) throws IOException, JMException {
            final JmxConnector connector = new JmxConnector(resourceName, new JmxConnectionOptions(configuration.getConnectionString(), configuration));
            connector.init();
            return connector;
        }
    }

    /**
     * Initializes a new instance of the JMX connector bundle activator.
     */
    @SpecialUse(SpecialUse.Case.OSGi)
    public JmxConnectorActivator() {
        super(new JmxConnectorFactory(),
                configurationDescriptor(JmxConnectorDescriptionProvider::getInstance));
    }
}
