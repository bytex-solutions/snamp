package com.bytex.snamp.adapters.nsca.configuration;

import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.adapters.ResourceAdapterDescriptionProvider;
import com.google.common.base.Strings;
import com.googlecode.jsendnsca.core.Encryption;
import com.googlecode.jsendnsca.core.NagiosSettings;

import javax.management.Descriptor;
import java.util.Map;

import static com.bytex.snamp.jmx.DescriptorUtils.getField;
import static com.bytex.snamp.jmx.DescriptorUtils.getUOM;
import static com.bytex.snamp.jmx.DescriptorUtils.hasField;
import static com.bytex.snamp.adapters.nsca.configuration.NSCAAdapterConfigurationDescriptor.*;

/**
 * Represents parser for NSCA adapter configuration parameters.
 */
public final class NSCAAdapterConfigurationParser extends ResourceAdapterDescriptionProvider {
    public NagiosSettings parseSettings(final Map<String, String> parameters) throws AbsentNSCAConfigurationParameterException {
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

    public static String getServiceName(final Descriptor descriptor, final String defaultService){
        return hasField(descriptor, SERVICE_NAME_PARAM) ?
                getField(descriptor, SERVICE_NAME_PARAM, String.class):
                defaultService;
    }

    public TimeSpan getPassiveCheckSendPeriod(final Map<String, String> parameters){
        if(parameters.containsKey(PASSIVE_CHECK_SEND_PERIOD_PARAM))
            return TimeSpan.ofMillis(parameters.get(PASSIVE_CHECK_SEND_PERIOD_PARAM));
        else return TimeSpan.ofSeconds(1L);
    }

    public static String getUnitOfMeasurement(final Descriptor descr){
        return Strings.nullToEmpty(getUOM(descr));
    }
}
