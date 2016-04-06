package com.bytex.snamp.adapters.snmp.configuration;

import com.bytex.snamp.adapters.ResourceAdapterConfigurationParser;
import com.google.common.base.Supplier;
import org.snmp4j.SNMP4JSettings;
import org.snmp4j.mp.MPv3;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;

import javax.management.DescriptorRead;
import javax.naming.NamingException;
import java.text.ParseException;
import java.util.Map;

import static com.bytex.snamp.jmx.DescriptorUtils.getField;
import static com.bytex.snamp.jmx.DescriptorUtils.hasField;
import static com.bytex.snamp.adapters.snmp.configuration.SnmpAdapterConfigurationDescriptor.*;

/**
 * Provides parser of SNMP Adapter configuration properties.
 */
public class SnmpAdapterConfigurationParser extends ResourceAdapterConfigurationParser {
    public String parseContext(final Map<String, String> parameters) throws SnmpAdapterAbsentParameterException {
        if(parameters.containsKey(CONTEXT_PARAM_NAME))
            return parameters.get(CONTEXT_PARAM_NAME);
        else throw new SnmpAdapterAbsentParameterException(CONTEXT_PARAM_NAME);
    }

    public static OID parseOID(final DescriptorRead info, final Supplier<OID> oidGenerator) throws ParseException {
        if (hasField(info.getDescriptor(), OID_PARAM_NAME))
            return new OID(SNMP4JSettings.getOIDTextFormat().parse(getField(info.getDescriptor(), OID_PARAM_NAME, String.class)));
        else return oidGenerator.get();
    }

    public static String parseDateTimeDisplayFormat(final DescriptorRead info){
        return getField(info.getDescriptor(), DATE_TIME_DISPLAY_FORMAT_PARAM, String.class);
    }

    public OctetString parseEngineID(final Map<String, String> parameters){
        if(parameters.containsKey(ENGINE_ID_PARAM))
            return OctetString.fromHexString(parameters.get(ENGINE_ID_PARAM));
        else return new OctetString(MPv3.createLocalEngineID());
    }

    public int parsePort(final Map<String, String> parameters) {
        return parameters.containsKey(PORT_PARAM_NAME) ?
                Integer.parseInt(parameters.get(PORT_PARAM_NAME)) :
                161;
    }

    public String parseAddress(final Map<String, String> parameters){
        return parameters.containsKey(HOST_PARAM_NAME) ?
                parameters.get(HOST_PARAM_NAME) :
                "127.0.0.1";
    }

    public int parseSocketTimeout(final Map<String, String> parameters) {
        return parameters.containsKey(SOCKET_TIMEOUT_PARAM) ?
                Integer.parseInt(parameters.get(SOCKET_TIMEOUT_PARAM)) :
                5000;
    }

    public SecurityConfiguration parseSecurityConfiguration(final Map<String, String> parameters,
                                                            final DirContextFactory contextFactory) throws NamingException{
        if(parameters.containsKey(SNMPv3_GROUPS_PARAM) || parameters.containsKey(LDAP_GROUPS_PARAM)){
            final OctetString engineID = parseEngineID(parameters);
            final SecurityConfiguration result = new SecurityConfiguration(engineID.getValue(), contextFactory);
            result.read(parameters);
            return result;
        }
        else return null;
    }

    public static boolean isValidNotification(final DescriptorRead metadata){
        return hasField(metadata.getDescriptor(), TARGET_ADDRESS_PARAM) &&
                hasField(metadata.getDescriptor(), TARGET_NAME_PARAM) &&
                hasField(metadata.getDescriptor(), OID_PARAM_NAME);
    }

    public static String parseTargetAddress(final DescriptorRead metadata){
        return getField(metadata.getDescriptor(), TARGET_ADDRESS_PARAM, String.class);
    }

    public static String parseTargetName(final DescriptorRead metadata){
        return getField(metadata.getDescriptor(), TARGET_NAME_PARAM, String.class);
    }

    public static int parseNotificationTimeout(final DescriptorRead metadata){
        return hasField(metadata.getDescriptor(), TARGET_NOTIF_TIMEOUT_PARAM) ?
                Integer.parseInt(getField(metadata.getDescriptor(), TARGET_NOTIF_TIMEOUT_PARAM, String.class)) :
                2000;
    }

    public static int parseRetryCount(final DescriptorRead metadata){
        return hasField(metadata.getDescriptor(), TARGET_RETRY_COUNT_PARAM) ?
                Integer.parseInt(getField(metadata.getDescriptor(), TARGET_RETRY_COUNT_PARAM, String.class)) :
                3;
    }

    public long parseRestartTimeout(final Map<String, String> parameters){
        return parameters.containsKey(RESTART_TIMEOUT_PARAM) ?
                Long.parseLong(parameters.get(RESTART_TIMEOUT_PARAM)) :
                10000L;
    }
}
