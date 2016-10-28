package com.bytex.snamp.gateway.nrdp;

import ch.shamu.jsendnrdp.NRDPServerConnectionSettings;
import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.gateway.GatewayDescriptionProvider;
import com.bytex.snamp.jmx.DescriptorUtils;

import javax.management.Descriptor;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.bytex.snamp.MapUtils.*;
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

    private static final LazySoftReference<NRDPGatewayConfigurationDescriptor> INSTANCE = new LazySoftReference<>();

    private NRDPGatewayConfigurationDescriptor() {
        super(new GatewayConfigurationInfo(),
                new AttributeConfigurationInfo(),
                new EventConfigurationInfo());
    }

    static NRDPGatewayConfigurationDescriptor getInstance(){
        return INSTANCE.lazyGet(NRDPGatewayConfigurationDescriptor::new);
    }

    NRDPServerConnectionSettings parseSettings(final Map<String, String> parameters) throws AbsentNRDPConfigurationParameterException {
        final String serverURL = getIfPresent(parameters, NRDP_SERVER_URL_PARAM, Function.identity(), AbsentNRDPConfigurationParameterException::new);
        final int connectionTimeout = getValueAsInt(parameters, CONNECTION_TIMEOUT_PARAM, Integer::parseInt, () -> 4000);
        final String token = getIfPresent(parameters, TOKEN_PARAM, Function.identity(), AbsentNRDPConfigurationParameterException::new);
        return new NRDPServerConnectionSettings(serverURL, token, connectionTimeout);
    }

    static String getServiceName(final Descriptor descriptor, final String defaultService){
        return getField(descriptor, SERVICE_NAME_PARAM, Objects::toString, () -> defaultService);
    }

    Duration getPassiveCheckSendPeriod(final Map<String, String> parameters){
        final long period = getValueAsLong(parameters, PASSIVE_CHECK_SEND_PERIOD_PARAM, Long::parseLong, () -> 1000L);
        return Duration.ofMillis(period);
    }

    static String getUnitOfMeasurement(final Descriptor descr){
        return nullToEmpty(getUOM(descr));
    }
}
