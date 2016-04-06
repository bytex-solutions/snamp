package com.bytex.snamp.adapters.nrdp.configuration;

import ch.shamu.jsendnrdp.NRDPServerConnectionSettings;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.adapters.ResourceAdapterConfigurationParser;
import com.google.common.base.Strings;

import javax.management.Descriptor;
import java.util.Map;

import static com.bytex.snamp.jmx.DescriptorUtils.getField;
import static com.bytex.snamp.jmx.DescriptorUtils.getUOM;
import static com.bytex.snamp.jmx.DescriptorUtils.hasField;
import static com.bytex.snamp.adapters.nrdp.configuration.NRDPAdapterConfigurationDescriptor.*;

/**
 * Represents parser of NRDP adapter configuration.
 */
public final class NRDPAdapterConfigurationParser extends ResourceAdapterConfigurationParser {

    public NRDPServerConnectionSettings parseSettings(final Map<String, String> parameters) throws AbsentNRDPConfigurationParameterException {
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
