package com.bytex.snamp.connectors;

import com.bytex.snamp.configuration.ConfigurationEntityDescription.ParameterDescription;

import java.util.Locale;
import java.util.Map;

/**
 * Represents advanced descriptor of the configuration parameter which value
 * may be selected from the array of suggested values.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface SelectableConnectorParameterDescriptor extends ParameterDescription {

    /**
     * Suggests the values of the configuration parameter.
     * @param connectionString The connection string that identifies the resource (see {@link com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration#getConnectionString()}).
     * @param connectionOptions Additional connection options (see {@link com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration#getConnectionString()}).
     * @param loc Target localization of the suggested values.
     * @return An array of parameter suggested values.
     * @throws java.lang.Exception Unable to suggest parameter values.
     */
    String[] suggestValues(final String connectionString,
                           final Map<String, String> connectionOptions,
                           final Locale loc) throws Exception;
}
