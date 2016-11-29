package com.bytex.snamp.gateway.groovy.impl;

import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.google.common.base.Splitter;

import java.net.URL;
import java.util.Map;

import static com.bytex.snamp.internal.Utils.callUnchecked;

/**
 * Represents configuration descriptor of Groovy Gateway.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class GroovyGatewayConfigurationProvider extends ConfigurationEntityDescriptionProviderImpl {
    private static final Splitter PATH_SPLITTER = Splitter.on(';').trimResults().omitEmptyStrings();
    private static final String SCRIPT_FILE_PARAM = "scriptFile";
    private static final String SCRIPT_PATH_PARAM = "scriptPath";

    private static final class GatewayConfigurationInfo extends ResourceBasedConfigurationEntityDescription<GatewayConfiguration>{
        private static final String NAME = "GatewayConfig";

        private GatewayConfigurationInfo(){
            super(NAME, GatewayConfiguration.class,
                    SCRIPT_FILE_PARAM,
                    SCRIPT_PATH_PARAM);
        }
    }

    GroovyGatewayConfigurationProvider(){
        super(new GatewayConfigurationInfo());
    }

    private static String getParameter(final String paramName,
                                       final Map<String, String> params) throws GroovyAbsentParameterConfigurationException {
        if (params.containsKey(paramName))
            return params.get(paramName);
        else throw new GroovyAbsentParameterConfigurationException(paramName);
    }

    static String getScriptFile(final Map<String, String> params) throws GroovyAbsentParameterConfigurationException {
        return getParameter(SCRIPT_FILE_PARAM, params);
    }

    static URL[] getScriptPath(final Map<String, String> params) throws GroovyAbsentParameterConfigurationException {
        final String path = getParameter(SCRIPT_PATH_PARAM, params);
        return PATH_SPLITTER.splitToList(path).stream().map(p -> callUnchecked(() -> new URL(p))).toArray(URL[]::new);
    }
}