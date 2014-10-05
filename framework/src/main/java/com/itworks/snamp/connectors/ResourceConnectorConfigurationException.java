package com.itworks.snamp.connectors;

import com.itworks.snamp.configuration.ConfigurationException;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;

/**
 * Represents an exception occurred when management connector is not configured correctly.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class ResourceConnectorConfigurationException extends ConfigurationException {

    public ResourceConnectorConfigurationException(final String message, final ManagedResourceConfiguration config, final Throwable cause){
        super(message, config, cause);
    }

    public ResourceConnectorConfigurationException(final String message, final ManagedResourceConfiguration config){
        this(message, config, null);
    }
}
