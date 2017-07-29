package com.bytex.snamp.gateway;

import com.bytex.snamp.configuration.ConfigurationEntityDescription.ParameterDescription;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/**
 * Represents advanced descriptor of the configuration parameter which value
 * may be selected from the array of suggested values.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.1
 */
public interface SelectableGatewayParameterDescriptor extends ParameterDescription {
    /**
     * Suggests the values of the configuration parameter.
     * @param connectionOptions Configuration parameters associated with the gateway.
     * @param loc Target localization of the suggested values.
     * @return A collection of suggested values.
     * @throws java.lang.Exception Unable to suggest values.
     */
    Collection<String> suggestValues(final Map<String, String> connectionOptions, final Locale loc) throws Exception;
}
