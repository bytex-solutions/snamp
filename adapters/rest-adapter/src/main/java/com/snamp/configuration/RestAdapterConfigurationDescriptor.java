package com.snamp.configuration;

import java.util.Locale;
import java.util.ResourceBundle;
import static com.snamp.adapters.Adapter.*;

import static com.snamp.configuration.AgentConfiguration.HostingConfiguration;

/**
 * Represents descriptor of REST adapter configuration scheme.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class RestAdapterConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {

    public static final String DATE_FORMAT_PARAM_NAME = "dateFormat";
    public static final String WEB_SOCKET_TIMEOUT_PARAM_NAME = "webSocketIdleTimeout";

    private static final class HostingConfigurationInfo extends ResourceBasedConfigurationEntityDescription<HostingConfiguration>{
        private static final String RESOURCE_NAME = "RestAdapterConfig";

        public HostingConfigurationInfo(){
            super(HostingConfiguration.class, ADDRESS_PARAM_NAME, PORT_PARAM_NAME);
        }

        @Override
        protected final ResourceBundle getBundle(final Locale loc) {
            return loc != null ? ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc) :
                    ResourceBundle.getBundle(getResourceName(RESOURCE_NAME));
        }
    }

    public RestAdapterConfigurationDescriptor(){
        super(new HostingConfigurationInfo());
    }
}
