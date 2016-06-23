package com.bytex.snamp.adapters.nrdp.configuration;

import com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.bytex.snamp.jmx.DescriptorUtils;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration.THREAD_POOL_KEY;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class NRDPAdapterConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    static final String NRDP_SERVER_URL_PARAM = "serverURL";
    static final String CONNECTION_TIMEOUT_PARAM = "connectionTimeout";
    static final String TOKEN_PARAM = "token";
    static final String SERVICE_NAME_PARAM = "serviceName";
    static final String PASSIVE_CHECK_SEND_PERIOD_PARAM = "passiveCheckSendPeriod";
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

    public NRDPAdapterConfigurationDescriptor() {
        super(new ResourceAdapterConfigurationInfo(),
                new AttributeConfigurationInfo(),
                new EventConfigurationInfo());
    }


}
