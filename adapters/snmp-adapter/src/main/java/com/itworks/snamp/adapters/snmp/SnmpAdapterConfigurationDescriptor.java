package com.itworks.snamp.adapters.snmp;


import com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import java.util.Locale;
import java.util.ResourceBundle;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;

/**
 * Represents descriptor of SnmpAgent-specific configuration elements.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpAdapterConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    /**
     * Represents configuration property that provides a set of user groups.
     */
    public static final String SNMPv3_GROUPS_PARAM = "snmpv3-groups";

    /**
     * Represents LDAP server URI.
     */
    public static final String LDAP_URI_PARAM = "ldap-uri";

    /**
     * Represents LDAP DN of the admin user that is used to read security configuration structure.
     */
    public static final String LDAP_ADMINDN_PARAM = "ldap-user";

    /**
     * Represents LDAP admin user password.
     */
    public static final String LDAP_ADMIN_PASSWORD_PARAM = "ldap-password";

    /**
     * Represents type of the LDAP authentication.
     */
    public static final String LDAP_ADMIN_AUTH_TYPE_PARAM = "ldap-auth-protocol";

    /**
     * Represents user search filter template that is used to find users in the group.
     * <p>
     *     $GROUPNAME$ string inside of the filter will be replaced with group name.
     * </p>
     */
    public static final String LDAP_USER_SEARCH_FILTER_PARAM = "ldap-user-search-filter";

    /**
     * Represents semicolon delimiter string of group DNs.
     */
    public static final String LDAP_GROUPS_PARAM = "ldap-groups";

    /**
     * Represents search base DN.
     */
    public static final String LDAP_BASE_DN_PARAM = "ldap-base-dn";

    /**
     * Represents JNDI/LDAP factory name.
     * <p>
     *     By default, this property equals to com.sun.jndi.ldap.LdapCtxFactory.
     * </p>
     */
    public static final String JNDI_LDAP_FACTORY_PARAM = "jndi-ldap-factory";

    /**
     * Represents name of the attribute in directory that holds the user attribute as clear text.
     */
    public static final String LDAP_PASSWORD_HOLDER_PARAM = "ldap-user-password-attribute-name";

    /**
     * Represents configuration property that contains UDP socket timeout, in milliseconds.
     */
    public static final String SOCKET_TIMEOUT_PARAM = "socketTimeout";

    public static final String PORT_PARAM_NAME = "port";

    public static final String HOST_PARAM_NAME = "host";

    public static final String OID_PARAM_NAME = "oidPostfix";

    /**
     * Represents name of the metadata property that specifies unix time display format.
     */
    public static final String DATE_TIME_DISPLAY_FORMAT_PARAM = "displayFormat";

    public static final String TARGET_NOTIF_TIMEOUT_PARAM = "sendingTimeout";
    public static final String TARGET_RETRY_COUNT_PARAM = "retryCount";
    public static final String TARGET_NAME_PARAM = "receiverName";
    public static final String TARGET_ADDRESS_PARAM = "receiverAddress";

    private static final String RESOURCE_NAME = "SnmpAdapterConfig";


    private static final class ResourceAdapterConfigurationInfo extends ResourceBasedConfigurationEntityDescription<ResourceAdapterConfiguration> {
        public ResourceAdapterConfigurationInfo(){
            super(ResourceAdapterConfiguration.class,
                    SNMPv3_GROUPS_PARAM,
                    SOCKET_TIMEOUT_PARAM,
                    PORT_PARAM_NAME,
                    HOST_PARAM_NAME,
                    LDAP_URI_PARAM);
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
        public AttributeConfigurationInfo(){
            super(AttributeConfiguration.class, DATE_TIME_DISPLAY_FORMAT_PARAM);
        }

        @Override
        protected final ResourceBundle getBundle(final Locale loc) {
            return loc != null ? ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc) :
                    ResourceBundle.getBundle(getResourceName(RESOURCE_NAME));
        }
    }

    private static final class EventConfigurationInfo extends ResourceBasedConfigurationEntityDescription<EventConfiguration>{
        public EventConfigurationInfo(){
            super(EventConfiguration.class,
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

    public SnmpAdapterConfigurationDescriptor(){
        super(new ResourceAdapterConfigurationInfo(), new AttributeConfigurationInfo(), new EventConfigurationInfo());
    }
}
