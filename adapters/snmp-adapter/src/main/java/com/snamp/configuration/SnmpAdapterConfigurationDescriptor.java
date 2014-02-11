package com.snamp.configuration;


import java.util.Locale;
import java.util.ResourceBundle;

import static com.snamp.configuration.AgentConfiguration.HostingConfiguration;
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
     * Represents configuration property that contains UDP socket timeout, in milliseconds.
     */
    public static final String SOCKET_TIMEOUT_PARAM = "socketTimeout";

    private static final class ConfigurationAttributesInfo extends ResourceBasedConfigurationEntityDescription<HostingConfiguration>{
        private static final String RESOURCE_NAME = "SnmpAdapterConfig.properties";

        public ConfigurationAttributesInfo(){
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

    public SnmpAdapterConfigurationDescriptor(){
        super(new ConfigurationAttributesInfo());
    }
}
