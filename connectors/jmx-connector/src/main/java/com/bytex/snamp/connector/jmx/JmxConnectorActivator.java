package com.bytex.snamp.connector.jmx;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.ManagedResourceActivator;

import javax.management.MalformedObjectNameException;
import java.net.MalformedURLException;
import java.util.Map;


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
    @SpecialUse
    public JmxConnectorActivator() {
        super(JmxConnectorActivator::createConnector,
                configurationDescriptor(JmxConnectorDescriptionProvider::getInstance),
                discoveryService(JmxConnectorActivator::newDiscoveryService));
    }

    private static JmxConnector createConnector(final String resourceName,
                                        final String connectionString,
                                        final Map<String, String> connectionOptions,
                                        final RequiredService<?>... dependencies) throws MalformedURLException, MalformedObjectNameException {
        return new JmxConnector(resourceName, connectionString, connectionOptions);
    }

    private static JmxDiscoveryService newDiscoveryService(final RequiredService<?>... dependencies){
        return new JmxDiscoveryService();
    }
}
