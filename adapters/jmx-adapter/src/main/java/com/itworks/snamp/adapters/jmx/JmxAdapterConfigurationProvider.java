package com.itworks.snamp.adapters.jmx;

import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxAdapterConfigurationProvider extends ConfigurationEntityDescriptionProviderImpl {

    private static final String OBJECT_NAME_PARAM = "objectName";

    private static final String USE_PLATFORM_MBEAN_PARAM = "usePlatformMBean";

    private static final String SEVERITY_PARAM = "severity";

    private static final class EventConfigSchema extends ResourceBasedConfigurationEntityDescription<EventConfiguration>{
        private static final String RESOURCE_NAME = "JmxEventSettings";

        public EventConfigSchema(){
            super(RESOURCE_NAME, EventConfiguration.class, SEVERITY_PARAM);
        }
    }

    private static final class AdapterConfigSchema extends ResourceBasedConfigurationEntityDescription<ResourceAdapterConfiguration>{
        private static final String RESOURCE_NAME = "JmxAdapterSettings";

        public AdapterConfigSchema() {
            super(RESOURCE_NAME,
                    ResourceAdapterConfiguration.class, OBJECT_NAME_PARAM, USE_PLATFORM_MBEAN_PARAM);
        }
    }

    JmxAdapterConfigurationProvider(){
        super(new AdapterConfigSchema(), new EventConfigSchema());
    }

    static boolean usePlatformMBean(final Map<String, String> parameters){
        if(parameters.containsKey(USE_PLATFORM_MBEAN_PARAM))
            switch (parameters.get(USE_PLATFORM_MBEAN_PARAM)){
                case "true":
                case "yes": return true;
                default: return false;
            }
        else return false;
    }

    static ObjectName parseRootObjectName(final Map<String, String> parameters) throws MalformedObjectNameException {
        if(parameters.containsKey(OBJECT_NAME_PARAM))
                return new ObjectName(parameters.get(OBJECT_NAME_PARAM));
        throw new MalformedObjectNameException("Root object name of MBean is not specified in the resource adapter configuration");
    }
}
