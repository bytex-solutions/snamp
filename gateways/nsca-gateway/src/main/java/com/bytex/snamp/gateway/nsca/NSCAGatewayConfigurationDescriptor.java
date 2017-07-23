package com.bytex.snamp.gateway.nsca;

import com.bytex.snamp.concurrent.LazyReference;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.gateway.GatewayDescriptionProvider;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.googlecode.jsendnsca.core.Encryption;
import com.googlecode.jsendnsca.core.NagiosSettings;

import javax.management.Descriptor;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.bytex.snamp.MapUtils.*;
import static com.bytex.snamp.configuration.GatewayConfiguration.THREAD_POOL_KEY;
import static com.bytex.snamp.jmx.DescriptorUtils.getField;
import static com.bytex.snamp.jmx.DescriptorUtils.getUOM;
import static com.google.common.base.Strings.nullToEmpty;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class NSCAGatewayConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl implements GatewayDescriptionProvider {
    private static final String NAGIOS_HOST_PARAM = "nagiosHost";
    private static final String NAGIOS_PORT_PARAM = "nagiosPort";
    private static final String CONNECTION_TIMEOUT_PARAM = "connectionTimeout";
    private static final String PASSWORD_PARAM = "password";
    private static final String ENCRYPTION_PARAM = "encryption";
    private static final String SERVICE_NAME_PARAM = "serviceName";
    private static final String PASSIVE_CHECK_SEND_PERIOD_PARAM = "passiveCheckSendPeriod";
    private static final String MAX_VALUE_PARAM = DescriptorUtils.MAX_VALUE_FIELD;
    private static final String MIN_VALUE_PARAM = DescriptorUtils.MIN_VALUE_FIELD;
    private static final String UNIT_OF_MEASUREMENT_PARAM = DescriptorUtils.UNIT_OF_MEASUREMENT_FIELD;

    private static final class EventConfigurationInfo extends ResourceBasedConfigurationEntityDescription<EventConfiguration>{
        private static final String RESOURCE_NAME = "EventParameters";

        private EventConfigurationInfo(){
            super(RESOURCE_NAME, EventConfiguration.class, SERVICE_NAME_PARAM);
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
                    NAGIOS_HOST_PARAM,
                    NAGIOS_PORT_PARAM,
                    CONNECTION_TIMEOUT_PARAM,
                    PASSWORD_PARAM,
                    ENCRYPTION_PARAM,
                    THREAD_POOL_KEY,
                    PASSIVE_CHECK_SEND_PERIOD_PARAM);
        }
    }
    private static final LazyReference<NSCAGatewayConfigurationDescriptor> INSTANCE = LazyReference.soft();

    private NSCAGatewayConfigurationDescriptor() {
        super(new GatewayConfigurationInfo(),
                new AttributeConfigurationInfo(),
                new EventConfigurationInfo());
    }

    static NSCAGatewayConfigurationDescriptor getInstance(){
        return INSTANCE.lazyGet(NSCAGatewayConfigurationDescriptor::new);
    }

    NagiosSettings parseSettings(final Map<String, String> parameters) throws AbsentNSCAConfigurationParameterException {
        final NagiosSettings result = new NagiosSettings();
        if(!acceptIfPresent(parameters, NAGIOS_HOST_PARAM, Function.identity(), result::setNagiosHost))
            throw new AbsentNSCAConfigurationParameterException(NAGIOS_HOST_PARAM);
        if(!acceptIntIfPresent(parameters, NAGIOS_PORT_PARAM, Integer::parseInt, result::setPort))
            throw  new AbsentNSCAConfigurationParameterException(NAGIOS_PORT_PARAM);
        acceptIntIfPresent(parameters, CONNECTION_TIMEOUT_PARAM, Integer::parseInt, result::setConnectTimeout);
        acceptIfPresent(parameters, PASSWORD_PARAM, Function.identity(), result::setPassword);
        result.setEncryptionMethod(Encryption.NO_ENCRYPTION);
        acceptIfPresent(parameters, ENCRYPTION_PARAM, Function.identity(), encryption -> {
            switch (encryption){
                case "XOR":
                case "xor": result.setEncryptionMethod(Encryption.XOR_ENCRYPTION); break;
                case "3DES":
                case "3des": result.setEncryptionMethod(Encryption.TRIPLE_DES_ENCRYPTION);
            }
        });
        return result;
    }

    static String getServiceName(final Descriptor descriptor, final String defaultService){
        return getField(descriptor, SERVICE_NAME_PARAM, Objects::toString).orElse(defaultService);
    }

    Duration getPassiveCheckSendPeriod(final Map<String, String> parameters){
        final long period = getValueAsLong(parameters, PASSIVE_CHECK_SEND_PERIOD_PARAM, Long::parseLong).orElse(1000L);
        return Duration.ofMillis(period);
    }

    static String getUnitOfMeasurement(final Descriptor descr){
        return nullToEmpty(getUOM(descr));
    }
}
