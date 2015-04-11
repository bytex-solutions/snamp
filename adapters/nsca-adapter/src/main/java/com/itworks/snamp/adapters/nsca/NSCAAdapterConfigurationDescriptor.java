package com.itworks.snamp.adapters.nsca;

import com.googlecode.jsendnsca.core.Encryption;
import com.googlecode.jsendnsca.core.NagiosSettings;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import javax.management.Descriptor;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.*;
import static com.itworks.snamp.jmx.DescriptorUtils.getField;
import static com.itworks.snamp.jmx.DescriptorUtils.hasField;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class NSCAAdapterConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    private static final String NAGIOS_HOST_PARAM = "nagiosHost";
    private static final String NAGIOS_PORT_PARAM = "nagiosPort";
    private static final String CONNECTION_TIMEOUT_PARAM = "connectionTimeout";
    private static final String PASSWORD_PARAM = "password";
    private static final String ENCRYPTION_PARAM = "encryption";
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
                    NAGIOS_HOST_PARAM,
                    NAGIOS_PORT_PARAM,
                    CONNECTION_TIMEOUT_PARAM,
                    PASSWORD_PARAM,
                    ENCRYPTION_PARAM,
                    PASSIVE_CHECK_SEND_PERIOD_PARAM);
        }

        @Override
        protected final ResourceBundle getBundle(final Locale loc) {
            return loc != null ? ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc) :
                    ResourceBundle.getBundle(getResourceName(RESOURCE_NAME));
        }
    }

    NSCAAdapterConfigurationDescriptor() {
        super(new ResourceAdapterConfigurationInfo(),
                new AttributeConfigurationInfo(),
                new EventConfigurationInfo());
    }

    static NagiosSettings parseSettings(final Map<String, String> parameters) throws AbsentNSCAConfigurationParameterException {
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

    static TimeSpan getPassiveCheckSendPeriod(final Map<String, String> parameters){
        if(parameters.containsKey(PASSIVE_CHECK_SEND_PERIOD_PARAM))
            return new TimeSpan(Long.parseLong(parameters.get(PASSIVE_CHECK_SEND_PERIOD_PARAM)));
        else return TimeSpan.fromSeconds(1L);
    }
}
