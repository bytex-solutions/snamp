package com.bytex.snamp.gateway.nrdp;

import ch.shamu.jsendnrdp.NRDPServerConnectionSettings;
import com.bytex.snamp.gateway.GatewayDescriptionProvider;
import com.bytex.snamp.concurrent.LazyValueFactory;
import com.bytex.snamp.concurrent.LazyValue;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.bytex.snamp.jmx.DescriptorUtils;

import javax.management.Descriptor;
import java.time.Duration;
import java.util.Map;

import static com.bytex.snamp.configuration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.EventConfiguration;
import static com.bytex.snamp.configuration.GatewayConfiguration.THREAD_POOL_KEY;
import static com.bytex.snamp.jmx.DescriptorUtils.*;
import static com.google.common.base.Strings.nullToEmpty;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class NRDPGatewayConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl implements GatewayDescriptionProvider {
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

    private static final class GatewayConfigurationInfo extends ResourceBasedConfigurationEntityDescription<GatewayConfiguration>{
        private static final String RESOURCE_NAME = "GatewayParameters";

        private GatewayConfigurationInfo(){
            super(RESOURCE_NAME,
                    GatewayConfiguration.class,
                    NRDP_SERVER_URL_PARAM,
                    CONNECTION_TIMEOUT_PARAM,
                    THREAD_POOL_KEY,
                    TOKEN_PARAM,
                    PASSIVE_CHECK_SEND_PERIOD_PARAM);
        }
    }

    private static final LazyValue<NRDPGatewayConfigurationDescriptor> INSTANCE = LazyValueFactory.THREAD_SAFE.of(NRDPGatewayConfigurationDescriptor::new);

    private NRDPGatewayConfigurationDescriptor() {
        super(new GatewayConfigurationInfo(),
                new AttributeConfigurationInfo(),
                new EventConfigurationInfo());
    }

    static NRDPGatewayConfigurationDescriptor getInstance(){
        return INSTANCE.get();
    }

    NRDPServerConnectionSettings parseSettings(final Map<String, String> parameters) throws AbsentNRDPConfigurationParameterException {
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

    Duration getPassiveCheckSendPeriod(final Map<String, String> parameters){
        if(parameters.containsKey(PASSIVE_CHECK_SEND_PERIOD_PARAM))
            return Duration.ofMillis(Long.parseLong(parameters.get(PASSIVE_CHECK_SEND_PERIOD_PARAM)));
        else return Duration.ofSeconds(1L);
    }

    static String getUnitOfMeasurement(final Descriptor descr){
        return nullToEmpty(getUOM(descr));
    }
}
