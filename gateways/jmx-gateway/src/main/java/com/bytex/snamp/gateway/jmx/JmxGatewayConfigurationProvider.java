package com.bytex.snamp.gateway.jmx;

import com.bytex.snamp.configuration.ManagedResourceConfiguration.EventConfiguration;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class JmxGatewayConfigurationProvider extends ConfigurationEntityDescriptionProviderImpl {

    private static final String OBJECT_NAME_PARAM = "objectName";

    private static final String USE_PLATFORM_MBEAN_PARAM = "usePlatformMBean";

    private static final String SEVERITY_PARAM = "severity";

    private static final class EventConfigSchema extends ResourceBasedConfigurationEntityDescription<EventConfiguration>{
        private static final String RESOURCE_NAME = "JmxEventSettings";

        public EventConfigSchema(){
            super(RESOURCE_NAME, EventConfiguration.class, SEVERITY_PARAM);
        }
    }

    private static final class GatewayConfigSchema extends ResourceBasedConfigurationEntityDescription<GatewayConfiguration>{
        private static final String RESOURCE_NAME = "JmxGatewaySettings";

        public GatewayConfigSchema() {
            super(RESOURCE_NAME,
                    GatewayConfiguration.class, OBJECT_NAME_PARAM, USE_PLATFORM_MBEAN_PARAM);
        }
    }

    JmxGatewayConfigurationProvider(){
        super(new GatewayConfigSchema(), new EventConfigSchema());
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
        throw new MalformedObjectNameException("Root object name of MBean is not specified in the gateway configuration");
    }
}
