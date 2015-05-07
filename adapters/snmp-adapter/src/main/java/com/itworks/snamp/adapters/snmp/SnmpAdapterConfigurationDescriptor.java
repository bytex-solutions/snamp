package com.itworks.snamp.adapters.snmp;


import com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.itworks.snamp.configuration.ThreadPoolConfigurationDescriptor;
import org.snmp4j.mp.MPv3;
import org.snmp4j.smi.OctetString;

import javax.management.DescriptorRead;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.itworks.snamp.jmx.DescriptorUtils.getField;
import static com.itworks.snamp.jmx.DescriptorUtils.hasField;

/**
 * Represents descriptor of SnmpAgent-specific configuration elements.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpAdapterConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    /**
     * Represents authoritative engine ID
     */
    private static final String ENGINE_ID_PARAM = "engineID";

    /**
     * Represents configuration property that provides a set of user groups.
     */
    private static final String SNMPv3_GROUPS_PARAM = SecurityConfiguration.SNMPv3_GROUPS_PARAM;

    /**
     * Represents LDAP server URI.
     */
    private static final String LDAP_URI_PARAM = SecurityConfiguration.LDAP_URI_PARAM;

    /**
     * Represents semicolon delimiter string of group DNs.
     */
    private static final String LDAP_GROUPS_PARAM = SecurityConfiguration.LDAP_GROUPS_PARAM;

    /**
     * Represents configuration property that contains UDP socket timeout, in milliseconds.
     */
    private static final String SOCKET_TIMEOUT_PARAM = "socketTimeout";

    private static final String PORT_PARAM_NAME = "port";

    private static final String HOST_PARAM_NAME = "host";

    private static final String OID_PARAM_NAME = "oid";

    private static final String CONTEXT_PARAM_NAME = "context";

    private static String RESTART_TIMEOUT_PARAM = "restartTimeout";

    /**
     * Represents name of the metadata property that specifies unix time display format.
     */
    private static final String DATE_TIME_DISPLAY_FORMAT_PARAM = "displayFormat";

    private static final String TARGET_NOTIF_TIMEOUT_PARAM = "sendingTimeout";
    private static final String TARGET_RETRY_COUNT_PARAM = "retryCount";
    private static final String TARGET_NAME_PARAM = "receiverName";
    private static final String TARGET_ADDRESS_PARAM = "receiverAddress";

    private static final class ResourceAdapterConfigurationInfo extends ResourceBasedConfigurationEntityDescription<ResourceAdapterConfiguration> implements ThreadPoolConfigurationDescriptor<ResourceAdapterConfiguration> {
        private static final String RESOURCE_NAME = "SnmpAdapterConfig";
        private ResourceAdapterConfigurationInfo(){
            super(ResourceAdapterConfiguration.class,
                    CONTEXT_PARAM_NAME,
                    ENGINE_ID_PARAM,
                    SNMPv3_GROUPS_PARAM,
                    SOCKET_TIMEOUT_PARAM,
                    PORT_PARAM_NAME,
                    HOST_PARAM_NAME,
                    LDAP_URI_PARAM,
                    MIN_POOL_SIZE_PROPERTY,
                    MAX_POOL_SIZE_PROPERTY,
                    QUEUE_SIZE_PROPERTY,
                    KEEP_ALIVE_TIME_PROPERTY,
                    PRIORITY_PROPERTY,
                    RESTART_TIMEOUT_PARAM);
        }

        /**
         * Retrieves resource accessor for the specified locale.
         *
         * @param loc The requested localization of the resource. May be {@literal null}.
         * @return The resource accessor.
         */
        @Override
        protected final ResourceBundle getBundle(final Locale loc) {
            return loc != null ? ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc) :
                    ResourceBundle.getBundle(getResourceName(RESOURCE_NAME));
        }
    }

    private static final class AttributeConfigurationInfo extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration>{
        private static final String RESOURCE_NAME = "SnmpAttributeConfig";

        private AttributeConfigurationInfo(){
            super(AttributeConfiguration.class,
                    OID_PARAM_NAME,
                    DATE_TIME_DISPLAY_FORMAT_PARAM);
        }

        @Override
        protected final ResourceBundle getBundle(final Locale loc) {
            return loc != null ? ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc) :
                    ResourceBundle.getBundle(getResourceName(RESOURCE_NAME));
        }
    }

    private static final class EventConfigurationInfo extends ResourceBasedConfigurationEntityDescription<EventConfiguration>{
        private static final String RESOURCE_NAME = "SnmpEventConfig";

        private EventConfigurationInfo(){
            super(EventConfiguration.class,
                    OID_PARAM_NAME,
                    DATE_TIME_DISPLAY_FORMAT_PARAM,
                    TARGET_ADDRESS_PARAM,
                    TARGET_NAME_PARAM,
                    TARGET_NOTIF_TIMEOUT_PARAM,
                    TARGET_RETRY_COUNT_PARAM);
        }

        @Override
        protected final ResourceBundle getBundle(final Locale loc) {
            return loc != null ? ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc) :
                    ResourceBundle.getBundle(getResourceName(RESOURCE_NAME));
        }
    }

    SnmpAdapterConfigurationDescriptor(){
        super(new ResourceAdapterConfigurationInfo(), new AttributeConfigurationInfo(), new EventConfigurationInfo());
    }

    static String parseContext(final Map<String, String> parameters) throws SnmpAdapterAbsentParameterException{
        if(parameters.containsKey(CONTEXT_PARAM_NAME))
            return parameters.get(CONTEXT_PARAM_NAME);
        else throw new SnmpAdapterAbsentParameterException(CONTEXT_PARAM_NAME);
    }

    static String parseOID(final DescriptorRead info){
        return getField(info.getDescriptor(), OID_PARAM_NAME, String.class);
    }

    static String parseDateTimeDisplayFormat(final DescriptorRead info){
        return getField(info.getDescriptor(), DATE_TIME_DISPLAY_FORMAT_PARAM, String.class);
    }

    static OctetString parseEngineID(final Map<String, String> parameters){
        if(parameters.containsKey(ENGINE_ID_PARAM))
            return OctetString.fromHexString(parameters.get(ENGINE_ID_PARAM));
        else return new OctetString(MPv3.createLocalEngineID());
    }

    static int parsePort(final Map<String, String> parameters) {
        return parameters.containsKey(PORT_PARAM_NAME) ?
                Integer.parseInt(parameters.get(PORT_PARAM_NAME)) :
                161;
    }

    static String parseAddress(final Map<String, String> parameters){
        return parameters.containsKey(HOST_PARAM_NAME) ?
                parameters.get(HOST_PARAM_NAME) :
                "127.0.0.1";
    }

    static int parseSocketTimeout(final Map<String, String> parameters) {
        return parameters.containsKey(SOCKET_TIMEOUT_PARAM) ?
                Integer.parseInt(parameters.get(SOCKET_TIMEOUT_PARAM)) :
                5000;
    }

    static SecurityConfiguration parseSecurityConfiguration(final Map<String, String> parameters,
                                                            final DirContextFactory contextFactory){
        if(parameters.containsKey(SNMPv3_GROUPS_PARAM) || parameters.containsKey(LDAP_GROUPS_PARAM)){
            final OctetString engineID = parseEngineID(parameters);
            final SecurityConfiguration result = new SecurityConfiguration(engineID.getValue(), contextFactory);
            result.read(parameters);
            return result;
        }
        else return null;
    }

    static boolean isValidNotification(final DescriptorRead metadata){
        return hasField(metadata.getDescriptor(), TARGET_ADDRESS_PARAM) &&
                hasField(metadata.getDescriptor(), TARGET_NAME_PARAM) &&
                hasField(metadata.getDescriptor(), OID_PARAM_NAME);
    }

    static String parseTargetAddress(final DescriptorRead metadata){
        return getField(metadata.getDescriptor(), TARGET_ADDRESS_PARAM, String.class);
    }

    static String parseTargetName(final DescriptorRead metadata){
        return getField(metadata.getDescriptor(), TARGET_NAME_PARAM, String.class);
    }

    static int parseNotificationTimeout(final DescriptorRead metadata){
        return hasField(metadata.getDescriptor(), TARGET_NOTIF_TIMEOUT_PARAM) ?
                Integer.parseInt(getField(metadata.getDescriptor(), TARGET_NOTIF_TIMEOUT_PARAM, String.class)) :
                0;
    }

    static int parseRetryCount(final DescriptorRead metadata){
        return hasField(metadata.getDescriptor(), TARGET_RETRY_COUNT_PARAM) ?
                Integer.parseInt(getField(metadata.getDescriptor(), TARGET_RETRY_COUNT_PARAM, String.class)) :
                3;
    }

    static long parseRestartTimeout(final Map<String, String> parameters){
        return parameters.containsKey(RESTART_TIMEOUT_PARAM) ?
                Long.parseLong(parameters.get(RESTART_TIMEOUT_PARAM)) :
                10000L;
    }
}
