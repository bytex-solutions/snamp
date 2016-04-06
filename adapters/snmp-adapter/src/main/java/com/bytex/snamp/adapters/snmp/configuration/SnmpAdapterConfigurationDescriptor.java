package com.bytex.snamp.adapters.snmp.configuration;


import com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.bytex.snamp.jmx.DescriptorUtils.getField;
import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration.THREAD_POOL_KEY;

/**
 * Represents descriptor of SnmpAgent-specific configuration elements.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class SnmpAdapterConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    /**
     * Represents authoritative engine ID
     */
    static final String ENGINE_ID_PARAM = "engineID";

    /**
     * Represents configuration property that provides a set of user groups.
     */
    static final String SNMPv3_GROUPS_PARAM = SecurityConfiguration.SNMPv3_GROUPS_PARAM;

    /**
     * Represents LDAP server URI.
     */
    static final String LDAP_URI_PARAM = SecurityConfiguration.LDAP_URI_PARAM;

    /**
     * Represents semicolon delimiter string of group DNs.
     */
    static final String LDAP_GROUPS_PARAM = SecurityConfiguration.LDAP_GROUPS_PARAM;

    private static final String LDAP_ADMINDN_PARAM = SecurityConfiguration.LDAP_ADMINDN_PARAM;
    private static final String LDAP_ADMIN_PASSWORD_PARAM = SecurityConfiguration.LDAP_ADMIN_PASSWORD_PARAM;
    private static final String LDAP_ADMIN_AUTH_TYPE_PARAM = SecurityConfiguration.LDAP_ADMIN_AUTH_TYPE_PARAM;
    private static final String LDAP_BASE_DN_PARAM = SecurityConfiguration.LDAP_BASE_DN_PARAM;
    private static final String LDAP_USER_SEARCH_FILTER_PARAM = SecurityConfiguration.LDAP_USER_SEARCH_FILTER_PARAM;

    /**
     * Represents configuration property that contains UDP socket timeout, in milliseconds.
     */
    static final String SOCKET_TIMEOUT_PARAM = "socketTimeout";

    static final String PORT_PARAM_NAME = "port";

    static final String HOST_PARAM_NAME = "host";

    static final String OID_PARAM_NAME = "oid";

    static final String CONTEXT_PARAM_NAME = "context";

    static final String RESTART_TIMEOUT_PARAM = "restartTimeout";

    /**
     * Represents name of the metadata property that specifies unix time display format.
     */
    static final String DATE_TIME_DISPLAY_FORMAT_PARAM = "displayFormat";

    static final String TARGET_NOTIF_TIMEOUT_PARAM = "sendingTimeout";
    static final String TARGET_RETRY_COUNT_PARAM = "retryCount";
    static final String TARGET_NAME_PARAM = "receiverName";
    static final String TARGET_ADDRESS_PARAM = "receiverAddress";

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

    public SnmpAdapterConfigurationDescriptor(){
        super(new ResourceAdapterConfigurationInfo(), new AttributeConfigurationInfo(), new EventConfigurationInfo());
    }


}
