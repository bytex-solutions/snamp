package com.bytex.snamp.connector.jmx;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.ManagedResourceActivator;

import javax.annotation.Nonnull;
import javax.management.MalformedObjectNameException;
import java.io.IOException;


/**
 * Represents bundle activator for JMX connector.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class JmxConnectorActivator extends ManagedResourceActivator<JmxConnector> {

    /**
     * Initializes a new instance of the JMX connector bundle activator.
     */
    @SpecialUse(SpecialUse.Case.OSGi)
    public JmxConnectorActivator() {
        super(JmxConnectorActivator::createConnector,
                configurationDescriptor(JmxConnectorDescriptionProvider::getInstance));
    }

    @Nonnull
    private static JmxConnector createConnector(final String resourceName,
                                                final ManagedResourceInfo configuration,
                                        final DependencyManager dependencies) throws IOException, MalformedObjectNameException {
        final JmxConnector connector = new JmxConnector(resourceName, configuration);
        connector.init();
        return connector;
    }
}
