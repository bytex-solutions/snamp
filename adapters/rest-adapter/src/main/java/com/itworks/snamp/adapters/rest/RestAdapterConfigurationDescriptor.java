package com.itworks.snamp.adapters.rest;

import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import java.util.Locale;
import java.util.ResourceBundle;

import static com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;

/**
 * Represents descriptor of REST adapter configuration scheme.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class RestAdapterConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {

    public static final String DATE_FORMAT_PARAM_NAME = "dateFormat";
    public static final String WEB_SOCKET_TIMEOUT_PARAM_NAME = "webSocketIdleTimeout";
    public static final String PORT_PARAM_NAME = "port";
    public static final String HOST_PARAM_NAME = "host";
    public static final String LOGIN_MODULE_NAME = "loginModule";

    private static final class AdapterConfigurationInfo extends ResourceBasedConfigurationEntityDescription<ResourceAdapterConfiguration> {
        private static final String RESOURCE_NAME = "RestAdapterConfig";

        public AdapterConfigurationInfo(){
            super(ResourceAdapterConfiguration.class,
                    HOST_PARAM_NAME,
                    PORT_PARAM_NAME,
                    DATE_FORMAT_PARAM_NAME,
                    WEB_SOCKET_TIMEOUT_PARAM_NAME,
                    LOGIN_MODULE_NAME);
        }

        @Override
        protected final ResourceBundle getBundle(final Locale loc) {
            return loc != null ? ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc) :
                    ResourceBundle.getBundle(getResourceName(RESOURCE_NAME));
        }
    }

    public RestAdapterConfigurationDescriptor(){
        super(new AdapterConfigurationInfo());
    }
}
