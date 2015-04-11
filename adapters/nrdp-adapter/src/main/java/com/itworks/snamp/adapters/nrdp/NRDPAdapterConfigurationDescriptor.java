package com.itworks.snamp.adapters.nrdp;

import ch.shamu.jsendnrdp.NRDPServerConnectionSettings;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import javax.management.Descriptor;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.itworks.snamp.jmx.DescriptorUtils.getField;
import static com.itworks.snamp.jmx.DescriptorUtils.hasField;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class NRDPAdapterConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    private static final String NRDP_SERVER_URL_PARAM = "serverURL";
    private static final String CONNECTION_TIMEOUT_PARAM = "connectionTimeout";
    private static final String TOKEN_PARAM = "token";
    private static final String SERVICE_NAME_PARAM = "serviceName";
    private static final String PASSIVE_CHECK_SEND_PERIOD_PARAM = "passiveCheckSendPeriod";

    private static final class EventConfigurationInfo extends ResourceBasedConfigurationEntityDescription<EventConfiguration>{
        private static final String RESOURCE_NAME = "EventParameters";

        private EventConfigurationInfo(){
            super(EventConfiguration.class, SERVICE_NAME_PARAM);
        }

        @Override
        protected ResourceBundle getBundle(final Locale loc) {
            return loc != null ? ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc) :
                    ResourceBundle.getBundle(getResourceName(RESOURCE_NAME));
        }
    }

    private static final class AttributeConfigurationInfo extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration>{
        private static final String RESOURCE_NAME = "AttributeParameters";

        private AttributeConfigurationInfo(){
            super(AttributeConfiguration.class,
                    SERVICE_NAME_PARAM);
        }

        @Override
        protected ResourceBundle getBundle(final Locale loc) {
            return loc != null ? ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc) :
                    ResourceBundle.getBundle(getResourceName(RESOURCE_NAME));
        }
    }

    private static final class ResourceAdapterConfigurationInfo extends ResourceBasedConfigurationEntityDescription<ResourceAdapterConfiguration>{
        private static final String RESOURCE_NAME = "AdapterParameters";

        private ResourceAdapterConfigurationInfo(){
            super(ResourceAdapterConfiguration.class,
                    NRDP_SERVER_URL_PARAM,
                    CONNECTION_TIMEOUT_PARAM,
                    TOKEN_PARAM,
                    PASSIVE_CHECK_SEND_PERIOD_PARAM);
        }

        @Override
        protected final ResourceBundle getBundle(final Locale loc) {
            return loc != null ? ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc) :
                    ResourceBundle.getBundle(getResourceName(RESOURCE_NAME));
        }
    }

    NRDPAdapterConfigurationDescriptor() {
        super(new ResourceAdapterConfigurationInfo(),
                new AttributeConfigurationInfo(),
                new EventConfigurationInfo());
    }

    static NRDPServerConnectionSettings parseSettings(final Map<String, String> parameters) throws AbsentNRDPConfigurationParameterException {
        final String serverURL;
        final int connectionTimeout;
        final String token;
        if(parameters.containsKey(NRDP_SERVER_URL_PARAM))
            serverURL = parameters.get(NRDP_SERVER_URL_PARAM);
        else throw new AbsentNRDPConfigurationParameterException(NRDP_SERVER_URL_PARAM);
        if(parameters.containsKey(CONNECTION_TIMEOUT_PARAM))
            connectionTimeout = Integer.parseInt(parameters.get(CONNECTION_TIMEOUT_PARAM));
        else connectionTimeout = 4000;
        if(parameters.containsKey(TOKEN_PARAM))
            token = parameters.get(TOKEN_PARAM);
        else throw new AbsentNRDPConfigurationParameterException(TOKEN_PARAM);
        return new NRDPServerConnectionSettings(serverURL, token, connectionTimeout);
    }

    static String getServiceName(final Descriptor descriptor, final String defaultService){
        return hasField(descriptor, SERVICE_NAME_PARAM) ?
                getField(descriptor, SERVICE_NAME_PARAM, String.class):
                defaultService;
    }

    static TimeSpan getPassiveCheckSendPeriod(final Map<String, String> parameters){
        if(parameters.containsKey(PASSIVE_CHECK_SEND_PERIOD_PARAM))
            return new TimeSpan(Long.parseLong(parameters.get(PASSIVE_CHECK_SEND_PERIOD_PARAM)));
        else return TimeSpan.fromSeconds(1L);
    }
}
