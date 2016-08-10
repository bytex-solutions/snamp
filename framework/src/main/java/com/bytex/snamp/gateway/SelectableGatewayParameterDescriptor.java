package com.bytex.snamp.gateway;

import com.bytex.snamp.configuration.ConfigurationEntityDescription.ParameterDescription;
import com.bytex.snamp.configuration.GatewayConfiguration;

import java.util.Locale;
import java.util.Map;

/**
 * Represents advanced descriptor of the configuration parameter which value
 * may be selected from the array of suggested values.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public interface SelectableGatewayParameterDescriptor extends ParameterDescription {
    /**
     * Suggests the values of the configuration parameter.
     * @param connectionOptions Configuration parameters associated with the adapter (see {@link GatewayConfiguration#getParameters()}).
     * @param loc Target localization of the suggested values.
     * @return An array of parameter suggested values.
     * @throws java.lang.Exception Unable to suggest values.
     */
    String[] suggestValues(final Map<String, String> connectionOptions, final Locale loc) throws Exception;
}
