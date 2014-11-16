package com.itworks.snamp.adapters.jmx;

import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxAdapterConfigurationProvider extends ConfigurationEntityDescriptionProviderImpl {

    public static final String OBJECT_NAME_PARAM = "objectName";

    public static final String USE_PLATFORM_MBEAN_PARAM = "usePlatformMBean";

    public static final String DEBUG_USE_PURE_SERIALIZATION_PARAM = "dbgUsePureSerialization";

    public static final String NOTIF_TYPE_PARAM = "jmxNotificationType";

    private static final class EventConfigSchema extends ResourceBasedConfigurationEntityDescription<EventConfiguration>{
        private static final String RESOURCE_NAME = "JmxEventSettings";

        public EventConfigSchema(){
            super(EventConfiguration.class, NOTIF_TYPE_PARAM);
        }

        @Override
        protected ResourceBundle getBundle(final Locale loc) {
            return loc != null ? ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc) :
                    ResourceBundle.getBundle(getResourceName(RESOURCE_NAME));
        }
    }

    private static final class AdapterConfigSchema extends ResourceBasedConfigurationEntityDescription<ResourceAdapterConfiguration>{
        private static final String RESOURCE_NAME = "JmxAdapterSettings";

        public AdapterConfigSchema() {
            super(ResourceAdapterConfiguration.class, OBJECT_NAME_PARAM, USE_PLATFORM_MBEAN_PARAM);
        }

        @Override
        protected ResourceBundle getBundle(final Locale loc) {
            return loc != null ? ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc) :
                    ResourceBundle.getBundle(getResourceName(RESOURCE_NAME));
        }
    }

    public JmxAdapterConfigurationProvider(){
        super(new AdapterConfigSchema(), new EventConfigSchema());
    }
}
