package com.bytex.snamp.adapters.snmp;


import com.bytex.snamp.adapters.ResourceAdapterDescriptionProvider;
import com.bytex.snamp.concurrent.LazyValueFactory;
import com.bytex.snamp.concurrent.LazyValue;
import com.bytex.snamp.configuration.ResourceAdapterConfiguration;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import org.snmp4j.SNMP4JSettings;
import org.snmp4j.mp.MPv3;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;

import javax.management.DescriptorRead;
import javax.naming.NamingException;
import java.text.ParseException;
import java.util.Map;
import java.util.function.Supplier;

import static com.bytex.snamp.configuration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.EventConfiguration;
import static com.bytex.snamp.configuration.ResourceAdapterConfiguration.THREAD_POOL_KEY;
import static com.bytex.snamp.jmx.DescriptorUtils.getField;
import static com.bytex.snamp.jmx.DescriptorUtils.hasField;

/**
 * Represents descriptor of SnmpAgent-specific configuration elements.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class SnmpAdapterDescriptionProvider extends ConfigurationEntityDescriptionProviderImpl implements ResourceAdapterDescriptionProvider {
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

    private static final String LDAP_ADMINDN_PARAM = SecurityConfiguration.LDAP_ADMINDN_PARAM;
    private static final String LDAP_ADMIN_PASSWORD_PARAM = SecurityConfiguration.LDAP_ADMIN_PASSWORD_PARAM;
    private static final String LDAP_ADMIN_AUTH_TYPE_PARAM = SecurityConfiguration.LDAP_ADMIN_AUTH_TYPE_PARAM;
    private static final String LDAP_BASE_DN_PARAM = SecurityConfiguration.LDAP_BASE_DN_PARAM;
    private static final String LDAP_USER_SEARCH_FILTER_PARAM = SecurityConfiguration.LDAP_USER_SEARCH_FILTER_PARAM;

    /**
     * Represents configuration property that contains UDP socket timeout, in milliseconds.
     */
    private static final String SOCKET_TIMEOUT_PARAM = "socketTimeout";

    private static final String PORT_PARAM_NAME = "port";

    private static final String HOST_PARAM_NAME = "host";

    private static final String OID_PARAM_NAME = "oid";

    private static final String CONTEXT_PARAM_NAME = "context";

    private static final String RESTART_TIMEOUT_PARAM = "restartTimeout";

    /**
     * Represents name of the metadata property that specifies unix time display format.
     */
    private static final String DATE_TIME_DISPLAY_FORMAT_PARAM = "displayFormat";

    private static final String TARGET_NOTIF_TIMEOUT_PARAM = "sendingTimeout";
    private static final String TARGET_RETRY_COUNT_PARAM = "retryCount";
    private static final String TARGET_NAME_PARAM = "receiverName";
    private static final String TARGET_ADDRESS_PARAM = "receiverAddress";

    private static final class ResourceAdapterConfigurationInfo extends ResourceBasedConfigurationEntityDescription<ResourceAdapterConfiguration> {
        private static final String RESOURCE_NAME = "SnmpAdapterConfig";
        private ResourceAdapterConfigurationInfo(){
            super(RESOURCE_NAME,
                    ResourceAdapterConfiguration.class,
                    CONTEXT_PARAM_NAME,
                    ENGINE_ID_PARAM,
                    SNMPv3_GROUPS_PARAM,
                    SOCKET_TIMEOUT_PARAM,
                    PORT_PARAM_NAME,
                    HOST_PARAM_NAME,
                    LDAP_URI_PARAM,
                    THREAD_POOL_KEY,
                    RESTART_TIMEOUT_PARAM,
                    LDAP_ADMINDN_PARAM,
                    LDAP_ADMIN_PASSWORD_PARAM,
                    LDAP_ADMIN_AUTH_TYPE_PARAM,
                    LDAP_BASE_DN_PARAM,
                    LDAP_USER_SEARCH_FILTER_PARAM);
        }
    }

    private static final class AttributeConfigurationInfo extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration>{
        private static final String RESOURCE_NAME = "SnmpAttributeConfig";

        private AttributeConfigurationInfo(){
            super(RESOURCE_NAME,
                    AttributeConfiguration.class,
                    OID_PARAM_NAME,
                    DATE_TIME_DISPLAY_FORMAT_PARAM);
        }
    }

    private static final class EventConfigurationInfo extends ResourceBasedConfigurationEntityDescription<EventConfiguration>{
        private static final String RESOURCE_NAME = "SnmpEventConfig";

        private EventConfigurationInfo(){
            super(RESOURCE_NAME,
                    EventConfiguration.class,
                    OID_PARAM_NAME,
                    DATE_TIME_DISPLAY_FORMAT_PARAM,
                    TARGET_ADDRESS_PARAM,
                    TARGET_NAME_PARAM,
                    TARGET_NOTIF_TIMEOUT_PARAM,
                    TARGET_RETRY_COUNT_PARAM);
        }
    }

    private static final LazyValue<SnmpAdapterDescriptionProvider> INSTANCE = LazyValueFactory.THREAD_SAFE.of(SnmpAdapterDescriptionProvider::new);

    private SnmpAdapterDescriptionProvider(){
        super(new ResourceAdapterConfigurationInfo(), new AttributeConfigurationInfo(), new EventConfigurationInfo());
    }

    public static SnmpAdapterDescriptionProvider getInstance(){
        return INSTANCE.get();
    }

    OID parseContext(final Map<String, String> parameters) throws SnmpAdapterAbsentParameterException {
        if(parameters.containsKey(CONTEXT_PARAM_NAME))
            return new OID(parameters.get(CONTEXT_PARAM_NAME));
        else throw new SnmpAdapterAbsentParameterException(CONTEXT_PARAM_NAME);
    }

    static OID parseOID(final DescriptorRead info, final Supplier<OID> oidGenerator) throws ParseException {
        if (hasField(info.getDescriptor(), OID_PARAM_NAME))
            return new OID(SNMP4JSettings.getOIDTextFormat().parse(getField(info.getDescriptor(), OID_PARAM_NAME, String.class)));
        else return oidGenerator.get();
    }

    static String parseDateTimeDisplayFormat(final DescriptorRead info){
        return getField(info.getDescriptor(), DATE_TIME_DISPLAY_FORMAT_PARAM, String.class);
    }

    OctetString parseEngineID(final Map<String, String> parameters){
        if(parameters.containsKey(ENGINE_ID_PARAM))
            return OctetString.fromHexString(parameters.get(ENGINE_ID_PARAM));
        else return new OctetString(MPv3.createLocalEngineID());
    }

    int parsePort(final Map<String, String> parameters) {
        return parameters.containsKey(PORT_PARAM_NAME) ?
                Integer.parseInt(parameters.get(PORT_PARAM_NAME)) :
                161;
    }

    String parseAddress(final Map<String, String> parameters){
        return parameters.containsKey(HOST_PARAM_NAME) ?
                parameters.get(HOST_PARAM_NAME) :
                "127.0.0.1";
    }

    int parseSocketTimeout(final Map<String, String> parameters) {
        return parameters.containsKey(SOCKET_TIMEOUT_PARAM) ?
                Integer.parseInt(parameters.get(SOCKET_TIMEOUT_PARAM)) :
                5000;
    }

    SecurityConfiguration parseSecurityConfiguration(final Map<String, String> parameters,
                                                     final DirContextFactory contextFactory) throws NamingException {
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
                2000;
    }

    static int parseRetryCount(final DescriptorRead metadata){
        return hasField(metadata.getDescriptor(), TARGET_RETRY_COUNT_PARAM) ?
                Integer.parseInt(getField(metadata.getDescriptor(), TARGET_RETRY_COUNT_PARAM, String.class)) :
                3;
    }

    long parseRestartTimeout(final Map<String, String> parameters){
        return parameters.containsKey(RESTART_TIMEOUT_PARAM) ?
                Long.parseLong(parameters.get(RESTART_TIMEOUT_PARAM)) :
                10000L;
    }
}
