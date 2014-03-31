package com.snamp.connectors;

import com.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.snamp.configuration.JmxConnectorConfigurationDescriptor;
import com.snamp.licensing.JmxConnectorLimitations;
import com.snamp.licensing.LicensedPlatformPlugin;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.meta.Author;

import javax.management.remote.JMXServiceURL;
import java.util.*;
import java.util.logging.*;

/**
 * Represents JMX connector factory.
 * @author Roman Sakno
 */
@PluginImplementation
@Author(name = "Roman Sakno")
final class JmxConnectorFactory extends AbstractManagementConnectorFactory<JmxConnector> implements LicensedPlatformPlugin<JmxConnectorLimitations> {

    /**
     * Represents JMX connector name.
     */
    public static final String connectorName = "jmx";

    private JmxConnectorConfigurationDescriptor configDescr;

    /**
     * Initializes a new JMX connector factory.
     */
    public JmxConnectorFactory(){
        super(connectorName);
        JmxConnectorLimitations.current().verifyPluginVersion(getClass());
        configDescr = null;
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

    /**
     * Determines whether the specified feature is supported.
     *
     * @param feature Type of the feature to check, such as {@link com.snamp.connectors.NotificationSupport}.
     * @return {@literal true}, if the specified management connector feature is supported; otherwise, {@literal false}.
     * @see com.snamp.connectors.AttributeSupport
     * @see com.snamp.connectors.NotificationSupport
     */
    @Override
    public final boolean supportsFeature(final Class<?> feature) {
        return feature == null ? false : feature.isAssignableFrom(JmxConnector.class);
    }

    @Aggregation
    public final ConfigurationEntityDescriptionProvider getConfigurationDescriptor(){
        if(configDescr == null)
            configDescr = new JmxConnectorConfigurationDescriptor();
        return configDescr;
    }

    /**
     * Returns license limitations associated with this plugin.
     *
     * @return The license limitations applied to this plugin.
     */
    @Override
    public final JmxConnectorLimitations getLimitations() {
        return JmxConnectorLimitations.current();
    }
}
