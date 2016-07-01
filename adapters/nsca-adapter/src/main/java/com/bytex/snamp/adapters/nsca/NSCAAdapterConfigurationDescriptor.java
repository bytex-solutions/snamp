package com.bytex.snamp.adapters.nsca;

import com.bytex.snamp.adapters.ResourceAdapterDescriptionProvider;
import com.bytex.snamp.concurrent.LazyContainers;
import com.bytex.snamp.concurrent.LazyValue;
import com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.googlecode.jsendnsca.core.Encryption;
import com.googlecode.jsendnsca.core.NagiosSettings;

import javax.management.Descriptor;
import java.time.Duration;
import java.util.Map;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration.THREAD_POOL_KEY;
import static com.bytex.snamp.jmx.DescriptorUtils.*;
import static com.google.common.base.Strings.nullToEmpty;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class NSCAAdapterConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl implements ResourceAdapterDescriptionProvider {
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

    private static final class ResourceAdapterConfigurationInfo extends ResourceBasedConfigurationEntityDescription<ResourceAdapterConfiguration>{
        private static final String RESOURCE_NAME = "AdapterParameters";

        private ResourceAdapterConfigurationInfo(){
            super(RESOURCE_NAME,
                    ResourceAdapterConfiguration.class,
                    NAGIOS_HOST_PARAM,
                    NAGIOS_PORT_PARAM,
                    CONNECTION_TIMEOUT_PARAM,
                    PASSWORD_PARAM,
                    ENCRYPTION_PARAM,
                    THREAD_POOL_KEY,
                    PASSIVE_CHECK_SEND_PERIOD_PARAM);
        }
    }
    private static final LazyValue<NSCAAdapterConfigurationDescriptor> INSTANCE = LazyContainers.THREAD_SAFE.of(NSCAAdapterConfigurationDescriptor::new);

    private NSCAAdapterConfigurationDescriptor() {
        super(new ResourceAdapterConfigurationInfo(),
                new AttributeConfigurationInfo(),
                new EventConfigurationInfo());
    }

    static NSCAAdapterConfigurationDescriptor getInstance(){
        return INSTANCE.get();
    }

    NagiosSettings parseSettings(final Map<String, String> parameters) throws AbsentNSCAConfigurationParameterException {
        final NagiosSettings result = new NagiosSettings();
        if(parameters.containsKey(NAGIOS_HOST_PARAM))
            result.setNagiosHost(parameters.get(NAGIOS_HOST_PARAM));
        else throw new AbsentNSCAConfigurationParameterException(NAGIOS_HOST_PARAM);
        if(parameters.containsKey(NAGIOS_PORT_PARAM))
            result.setPort(Integer.parseInt(parameters.get(NAGIOS_PORT_PARAM)));
        else throw new AbsentNSCAConfigurationParameterException(NAGIOS_PORT_PARAM);
        if(parameters.containsKey(CONNECTION_TIMEOUT_PARAM))
            result.setConnectTimeout(Integer.parseInt(parameters.get(CONNECTION_TIMEOUT_PARAM)));
        if(parameters.containsKey(PASSWORD_PARAM))
            result.setPassword(parameters.get(PASSWORD_PARAM));
        if(parameters.containsKey(ENCRYPTION_PARAM))
            switch (parameters.get(ENCRYPTION_PARAM)){
                case "XOR":
                case "xor": result.setEncryptionMethod(Encryption.XOR_ENCRYPTION); break;
                case "3DES":
                case "3des": result.setEncryptionMethod(Encryption.TRIPLE_DES_ENCRYPTION); break;
                default: result.setEncryptionMethod(Encryption.NO_ENCRYPTION); break;
            }
        return result;
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
