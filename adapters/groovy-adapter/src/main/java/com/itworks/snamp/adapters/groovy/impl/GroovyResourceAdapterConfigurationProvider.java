package com.itworks.snamp.adapters.groovy.impl;

import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.io.IOUtils;

import java.util.Map;

/**
 * Represents configuration descriptor of Groovy Resource Adapter.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class GroovyResourceAdapterConfigurationProvider extends ConfigurationEntityDescriptionProviderImpl {
    private static final String SCRIPT_FILE_PARAM = "scriptFile";
    private static final String SCRIPT_PATH_PARAM = "scriptPath";

    private static String getParameter(final String paramName,
                                       final Map<String, String> params) throws GroovyAbsentParameterConfigurationException {
        if (params.containsKey(paramName))
            return params.get(paramName);
        else throw new GroovyAbsentParameterConfigurationException(paramName);
    }

    static String getScriptFile(final Map<String, String> params) throws GroovyAbsentParameterConfigurationException {
        return getParameter(SCRIPT_FILE_PARAM, params);
    }

    static String[] getScriptPath(final Map<String, String> params) throws GroovyAbsentParameterConfigurationException {
        final String path = getParameter(SCRIPT_PATH_PARAM, params);
        return IOUtils.splitPath(path);
    }
}