package com.snamp.connectors.jmx;

import com.snamp.connectors.*;
import com.sun.jmx.defaults.JmxProperties;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import javax.management.remote.JMXServiceURL;
import java.net.MalformedURLException;
import java.util.*;
import java.util.logging.*;

/**
 * Represents JMX connector factory.
 * @author roman
 */
@PluginImplementation
public final class JmxConnectorFactory extends ManagementConnectorFactoryBase<JmxConnector> {

    private static final Logger log = Logger.getLogger("snamp.log");
    /**
     * Represents JMX connector name.
     */
    public static final String connectorName = "jmx";

    /**
     * Initializes a new JMX connector factory.
     */
    public JmxConnectorFactory(){
        super(connectorName);
    }

    /**
     * Creates a new instance of the connector.
     * @param serviceURL JMX service URL.
     * @param props JMX connection properties.
     * @return A new instance of the JMX connector.
     */
    public final JmxConnector newInstance(final JMXServiceURL serviceURL, final Properties props){
        final Map<String, Object> connectionProperties = new HashMap<>();
        //parse credentials
        if(props.containsKey("login") && props.containsKey("password"))
            connectionProperties.put(javax.management.remote.JMXConnector.CREDENTIALS, new String[]{
                    String.valueOf(props.get("login")),
                    String.valueOf(props.get("password"))
            });
        return new JmxConnector(serviceURL, connectionProperties);
    }

    /**
     * Creates a new instance of the connector.
     * @param connectionString     The protocol-specific connection string.
     * @param connectionProperties The connection properties such as credentials.
     * @return A new instance of the management connector.
     */
    @Override
    public final JmxConnector newInstance(final String connectionString, final Properties connectionProperties) {
        try {
            return newInstance(new JMXServiceURL(connectionString), connectionProperties);
        }
        catch (Exception e) {
            log.log(Level.WARNING, "Unable to create JMX connector", e);
            return null;
        }
    }
}
