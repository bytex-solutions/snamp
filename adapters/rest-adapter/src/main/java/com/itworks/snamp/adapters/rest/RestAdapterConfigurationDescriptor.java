package com.itworks.snamp.adapters.rest;

import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.itworks.snamp.configuration.ThreadPoolConfigurationDescriptor;

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

    static final String DATE_FORMAT_PARAM_NAME = "dateFormat";
    static final String WEB_SOCKET_TIMEOUT_PARAM_NAME = "webSocketIdleTimeout";
    static final String PORT_PARAM_NAME = "port";
    static final String HOST_PARAM_NAME = "host";
    static final String LOGIN_MODULE_NAME = "loginModule";
    static final int DEFAULT_PORT = 3456;
    static final String DEFAULT_HOST = "127.0.0.1";
    static final int DEFAULT_TIMEOUT = 5000;

    private static final class AdapterConfigurationInfo extends ResourceBasedConfigurationEntityDescription<ResourceAdapterConfiguration> implements ThreadPoolConfigurationDescriptor<ResourceAdapterConfiguration> {
        private static final String RESOURCE_NAME = "RestAdapterConfig";

        public AdapterConfigurationInfo(){
            super(ResourceAdapterConfiguration.class,
                    HOST_PARAM_NAME,
                    PORT_PARAM_NAME,
                    DATE_FORMAT_PARAM_NAME,
                    WEB_SOCKET_TIMEOUT_PARAM_NAME,
                    LOGIN_MODULE_NAME,
                    MIN_POOL_SIZE_PROPERTY,
                    MAX_POOL_SIZE_PROPERTY,
                    PRIORITY_PROPERTY,
                    QUEUE_SIZE_PROPERTY,
                    KEEP_ALIVE_TIME_PROPERTY);
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
