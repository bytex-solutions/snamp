package com.itworks.snamp.adapters.syslog;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.MessageFormat;
import com.cloudbees.syslog.sender.SyslogMessageSender;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.itworks.snamp.connectors.notifications.NotificationDescriptor;

import javax.management.Descriptor;
import java.util.Map;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.itworks.snamp.jmx.DescriptorUtils.getField;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SysLogConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    private static final String CONNECTION_TIMEOUT_PARAM = "connectionTimeout";
    private static final String APPLICATION_NAME_PARAM = "applicationName";
    private static final String FACILITY_PARAM = "facility";
    private static final String PORT_PARAM = "port";
    private static final String ADDRESS_PARAM = "address";
    private static final String USE_SSL_PARAM = "ssl";
    private static final String MESSAGE_FORMAT_PARAM = "messageFormat";
    private static final String PROTOCOL_PARAM = "protocol";
    private static final String PASSIVE_CHECK_SEND_PERIOD_PARAM = "passiveCheckSendPeriod";
    private static final String SEVERITY_PARAM = NotificationDescriptor.SEVERITY_PARAM;

    private static final class AdapterConfigurationInfo extends ResourceBasedConfigurationEntityDescription<ResourceAdapterConfiguration>{
        private static final String RESOURCE_NAME = "AdapterParameters";

        private AdapterConfigurationInfo(){
            super(RESOURCE_NAME,
                    ResourceAdapterConfiguration.class,
                    PORT_PARAM,
                    ADDRESS_PARAM,
                    USE_SSL_PARAM,
                    MESSAGE_FORMAT_PARAM,
                    PROTOCOL_PARAM,
                    PASSIVE_CHECK_SEND_PERIOD_PARAM,
                    CONNECTION_TIMEOUT_PARAM);
        }
    }

    private static final class AttributeConfigurationInfo extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration>{
        private static final String RESOURCE_NAME = "AttributeParameters";

        private AttributeConfigurationInfo(){
            super(RESOURCE_NAME,
                    AttributeConfiguration.class,
                    APPLICATION_NAME_PARAM,
                    FACILITY_PARAM);
        }
    }

    private static final class EventConfigurationInfo extends ResourceBasedConfigurationEntityDescription<EventConfiguration>{
        private static final String RESOURCE_NAME = "EventParameters";

        private EventConfigurationInfo(){
            super(RESOURCE_NAME,
                    EventConfiguration.class,
                    APPLICATION_NAME_PARAM,
                    FACILITY_PARAM,
                    SEVERITY_PARAM);
        }
    }

    SysLogConfigurationDescriptor(){
        super(new AdapterConfigurationInfo(),
                new AttributeConfigurationInfo(),
                new EventConfigurationInfo());
    }

    static String getApplicationName(final Descriptor descr, final String defaultValue){
        return getField(descr, APPLICATION_NAME_PARAM, String.class, defaultValue);
    }

    static Facility getFacility(final Descriptor descr, final Facility defaultValue){
        final Object value = descr.getFieldValue(FACILITY_PARAM);
        if(value instanceof String)
            return Facility.fromLabel((String)value);
        else if(value instanceof Integer)
            return Facility.fromNumericalCode((int)value);
        else return defaultValue;
    }

    static SyslogMessageSender createSender(final Map<String, String> parameters) throws AbsentSysLogConfigurationParameterException{
        if(!parameters.containsKey(PORT_PARAM))
            throw new AbsentSysLogConfigurationParameterException(PORT_PARAM);
        if(!parameters.containsKey(ADDRESS_PARAM))
            throw new AbsentSysLogConfigurationParameterException(ADDRESS_PARAM);
        if(!parameters.containsKey(PROTOCOL_PARAM))
            throw new AbsentSysLogConfigurationParameterException(PROTOCOL_PARAM);
        final int port = Integer.parseInt(parameters.get(PORT_PARAM));
        final String address = parameters.get(ADDRESS_PARAM);
        final boolean ssl;
        final int connectionTimeout = parameters.containsKey(CONNECTION_TIMEOUT_PARAM) ?
                Integer.parseInt(parameters.get(CONNECTION_TIMEOUT_PARAM)):
                2000;
        if(parameters.containsKey(USE_SSL_PARAM))
            switch (parameters.get(USE_SSL_PARAM)){
                case "yes":
                case "true":
                case "TRUE":
                case "YES": ssl = true; break;
                default: ssl = false; break;
            }
        else ssl = false;
        final MessageFormat format;
        if(parameters.containsKey(MESSAGE_FORMAT_PARAM))
            switch (parameters.get(MESSAGE_FORMAT_PARAM)){
                case "RFC-3164":
                case "rfc-3164":
                case "BSD":
                case "bsd": format = MessageFormat.RFC_3164;break;
                default: format = MessageFormat.RFC_5424; break;
            }
        else format = MessageFormat.RFC_5424;
        final SyslogMessageSenderFactory factory;
        switch (parameters.get(PROTOCOL_PARAM)){
            case "tcp":
            case "TCP": factory = SyslogMessageSenderFactory.TCP; break;
            default: factory = SyslogMessageSenderFactory.UDP; break;
        }
        return factory.create(address, port, format, ssl, connectionTimeout);
    }

    static TimeSpan getPassiveCheckSendPeriod(final Map<String, String> parameters){
        if(parameters.containsKey(PASSIVE_CHECK_SEND_PERIOD_PARAM))
            return new TimeSpan(Long.parseLong(parameters.get(PASSIVE_CHECK_SEND_PERIOD_PARAM)));
        else return TimeSpan.fromSeconds(1L);
    }
}
