package com.itworks.snamp.connectors.groovy.impl;

import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class GroovyResourceConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    private static final String INIT_SCRIPT = "initScript";

    static String getInitScriptFile(final Map<String, String> parameters){
        return parameters.get(INIT_SCRIPT);
    }
}
