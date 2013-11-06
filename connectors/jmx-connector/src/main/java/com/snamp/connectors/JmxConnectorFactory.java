package com.snamp.connectors;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import javax.management.remote.JMXServiceURL;
import java.util.*;
import java.util.logging.*;

/**
 * Represents JMX connector factory.
 * @author Roman Sakno
 */
@PluginImplementation
final class JmxConnectorFactory extends AbstractManagementConnectorFactory<JmxConnector> {

    /**
     * Represents JMX connector name.
     */
    public static final String connectorName = "jmx";

    /**
     * Initializes a new JMX connector factory.
     */
    public JmxConnectorFactory(){
        super(connectorName);
        //TODO: Package.getImplementationVersion() returns null, I don't know why...
        //JmxConnectorLimitations.current().verifyPluginVersion(getClass());
    }

    /**
     * Creates a new instance of the connector.
     * @param serviceURL JMX service URL.
     * @param props JMX connection properties.
     * @return A new instance of the JMX connector.
     */
    public final JmxConnector newInstance(final JMXServiceURL serviceURL, final Map<String, String> props){
        final Map<String, Object> jmxConnectionProperties = new HashMap<>();
        //parse credentials
        if(props.containsKey("login") && props.containsKey("password"))
            jmxConnectionProperties.put(javax.management.remote.JMXConnector.CREDENTIALS, new String[]{
                    String.valueOf(props.get("login")),
                    String.valueOf(props.get("password"))
            });
        return new JmxConnector(serviceURL, jmxConnectionProperties);
    }

    /**
     * Creates a new instance of the connector.
     * @param connectionString     The protocol-specific connection string.
     * @param connectionProperties The connection properties such as credentials.
     * @return A new instance of the management connector.
     */
    @Override
    public final JmxConnector newInstance(final String connectionString, final Map<String, String> connectionProperties) {
        try {
            return newInstance(new JMXServiceURL(connectionString), connectionProperties);
        }
        catch (Exception e) {
            getLogger().log(Level.SEVERE, "Unable to create JMX connector", e);
            return null;
        }
    }
}
