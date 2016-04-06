package com.bytex.snamp.adapters.nrdp;

import ch.shamu.jsendnrdp.NRDPServerConnectionSettings;
import com.google.common.base.Strings;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.bytex.snamp.jmx.DescriptorUtils;

import javax.management.Descriptor;
import java.util.Map;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration.THREAD_POOL_KEY;
import static com.bytex.snamp.jmx.DescriptorUtils.*;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class NRDPAdapterConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    private static final String NRDP_SERVER_URL_PARAM = "serverURL";
    private static final String CONNECTION_TIMEOUT_PARAM = "connectionTimeout";
    private static final String TOKEN_PARAM = "token";
    private static final String SERVICE_NAME_PARAM = "serviceName";
    private static final String PASSIVE_CHECK_SEND_PERIOD_PARAM = "passiveCheckSendPeriod";
    private static final String MAX_VALUE_PARAM = DescriptorUtils.MAX_VALUE_FIELD;
    private static final String MIN_VALUE_PARAM = DescriptorUtils.MIN_VALUE_FIELD;
    private static final String UNIT_OF_MEASUREMENT_PARAM = DescriptorUtils.UNIT_OF_MEASUREMENT_FIELD;

    private static final class EventConfigurationInfo extends ResourceBasedConfigurationEntityDescription<EventConfiguration>{
        private static final String RESOURCE_NAME = "EventParameters";

        private EventConfigurationInfo(){
            super(RESOURCE_NAME,
                    EventConfiguration.class,
                    SERVICE_NAME_PARAM);
        }
    }

    private static final class AttributeConfigurationInfo extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration>{
        private static final String RESOURCE_NAME = "AttributeParameters";

        private AttributeConfigurationInfo(){
            super(RESOURCE_NAME,
                    AttributeConfiguration.class,
                    SERVICE_NAME_PARAM,
                    MAX_VALUE_PARAM,
                    MIN_VALUE_PARAM,
                    UNIT_OF_MEASUREMENT_PARAM);
        }
    }

    private static final class ResourceAdapterConfigurationInfo extends ResourceBasedConfigurationEntityDescription<ResourceAdapterConfiguration>{
        private static final String RESOURCE_NAME = "AdapterParameters";

        private ResourceAdapterConfigurationInfo(){
            super(RESOURCE_NAME,
                    ResourceAdapterConfiguration.class,
                    NRDP_SERVER_URL_PARAM,
                    CONNECTION_TIMEOUT_PARAM,
                    THREAD_POOL_KEY,
                    TOKEN_PARAM,
                    PASSIVE_CHECK_SEND_PERIOD_PARAM);
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
            return TimeSpan.ofMillis(parameters.get(PASSIVE_CHECK_SEND_PERIOD_PARAM));
        else return TimeSpan.ofSeconds(1L);
    }

    static String getUnitOfMeasurement(final Descriptor descr){
        return Strings.nullToEmpty(getUOM(descr));
    }
}
