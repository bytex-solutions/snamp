package com.bytex.snamp.gateway.syslog;

import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.gateway.GatewayDescriptionProvider;
import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.MessageFormat;
import com.cloudbees.syslog.sender.SyslogMessageSender;

import javax.management.Descriptor;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.bytex.snamp.MapUtils.*;
import static com.bytex.snamp.jmx.DescriptorUtils.getField;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class SysLogConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl implements GatewayDescriptionProvider {
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

    private static final class GatewayConfigurationInfo extends ResourceBasedConfigurationEntityDescription<GatewayConfiguration>{
        private static final String RESOURCE_NAME = "GatewayParameters";

        private GatewayConfigurationInfo(){
            super(RESOURCE_NAME,
                    GatewayConfiguration.class,
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

    private static final LazySoftReference<SysLogConfigurationDescriptor> INSTANCE = new LazySoftReference<>();

    private SysLogConfigurationDescriptor(){
        super(new GatewayConfigurationInfo(),
                new AttributeConfigurationInfo(),
                new EventConfigurationInfo());
    }

    static SysLogConfigurationDescriptor getInstance(){
        return INSTANCE.lazyGet(SysLogConfigurationDescriptor::new);
    }

    static String getApplicationName(final Descriptor descr, final String defaultValue){
        return getField(descr, APPLICATION_NAME_PARAM, Objects::toString, () -> defaultValue);
    }

    static Facility getFacility(final Descriptor descr, final Facility defaultValue){
        final Object value = descr.getFieldValue(FACILITY_PARAM);
        if(value instanceof String)
            return Facility.fromLabel((String)value);
        else if(value instanceof Integer)
            return Facility.fromNumericalCode((int)value);
        else return defaultValue;
    }

    SyslogMessageSender createSender(final Map<String, String> parameters) throws AbsentSysLogConfigurationParameterException {
        final int port = getIfPresent(parameters, PORT_PARAM, Integer::parseInt, AbsentSysLogConfigurationParameterException::new);
        final String address = getIfPresent(parameters, ADDRESS_PARAM, Function.identity(), AbsentSysLogConfigurationParameterException::new);
        final int connectionTimeout = getValueAsInt(parameters, CONNECTION_TIMEOUT_PARAM, Integer::parseInt, () -> 2000);
        final boolean ssl = getValue(parameters, USE_SSL_PARAM, useSSL -> {
            switch (useSSL) {
                case "yes":
                case "true":
                case "TRUE":
                case "YES":
                    return true;
                default:
                    return false;
            }
        }, () -> false);
        final MessageFormat format = getValue(parameters, MESSAGE_FORMAT_PARAM, formatName -> {
            switch (formatName) {
                case "RFC-3164":
                case "rfc-3164":
                case "BSD":
                case "bsd":
                    return MessageFormat.RFC_3164;
                default:
                    return MessageFormat.RFC_5424;
            }
        }, () -> MessageFormat.RFC_5424);
        final SyslogMessageSenderFactory factory = getIfPresent(parameters, PROTOCOL_PARAM, protocol -> {
            switch (protocol) {
                case "tcp":
                case "TCP":
                    return SyslogMessageSenderFactory.TCP;
                default:
                    return SyslogMessageSenderFactory.UDP;
            }
        }, AbsentSysLogConfigurationParameterException::new);
        return factory.create(address, port, format, ssl, connectionTimeout);
    }

    Duration getPassiveCheckSendPeriod(final Map<String, String> parameters){
        final long period = getValueAsLong(parameters, PASSIVE_CHECK_SEND_PERIOD_PARAM, Long::parseLong, () -> 1000L);
        return Duration.ofMillis(period);
    }
}
