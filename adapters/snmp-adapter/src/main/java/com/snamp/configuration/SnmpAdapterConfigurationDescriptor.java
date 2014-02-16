package com.snamp.configuration;


import java.util.Locale;
import java.util.ResourceBundle;

import static com.snamp.configuration.AgentConfiguration.HostingConfiguration;
import static com.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;
import static com.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.EventConfiguration;
import static com.snamp.adapters.Adapter.*;

/**
 * Represents descriptor of SnmpAdapter-specific configuration elements.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnmpAdapterConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    /**
     * Represents configuration property that provides a set of user groups.
     */
    public static final String SNMPv3_GROUPS_PROPERTY = "snmpv3-groups";

    /**
     * Represents LDAP server URI.
     */
    public static final String LDAP_URI_PROPERTY = "ldap-uri";

    /**
     * Represents configuration property that contains UDP socket timeout, in milliseconds.
     */
    public static final String SOCKET_TIMEOUT_PARAM = "socketTimeout";

    /**
     * Represents name of the metadata property that specifies unix time display format.
     */
    public static final String DATE_TIME_DISPLAY_FORMAT_PARAM = "displayFormat";

    public static final String TARGET_NOTIF_TIMEOUT_PARAM = "sendingTimeout";
    public static final String TARGET_RETRY_COUNT_PARAM = "retryCount";
    public static final String TARGET_NAME_PARAM = "receiverName";
    public static final String TARGET_ADDRESS_PARAM = "receiverAddress";

    private static final String RESOURCE_NAME = "SnmpAdapterConfig";


    private static final class HostingConfigurationInfo extends ResourceBasedConfigurationEntityDescription<HostingConfiguration>{
        public HostingConfigurationInfo(){
            super(HostingConfiguration.class,
                    SNMPv3_GROUPS_PROPERTY,
                    SOCKET_TIMEOUT_PARAM,
                    PORT_PARAM_NAME,
                    ADDRESS_PARAM_NAME);
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
        super(new HostingConfigurationInfo(), new AttributeConfigurationInfo(), new EventConfigurationInfo());
    }
}
