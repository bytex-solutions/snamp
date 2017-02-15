package com.bytex.snamp.management.http.model;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.configuration.impl.SerializableAgentConfiguration;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ConfigurationModelSerializationTest extends Assert {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void agentConfigurationSerialization() throws IOException {
        final AgentConfiguration configuration = new SerializableAgentConfiguration();
        configuration.put("param", "value");
        configuration.getEntities(ManagedResourceConfiguration.class).addAndConsume("resource1", resource -> {
            resource.setType("http");
            resource.setGroupName("cluster");
            resource.setConnectionString("http://localhost");
            resource.getFeatures(EventConfiguration.class).addAndConsume("event1", event -> {
                event.put("eventParam", "value");
            });
        });
        configuration.getEntities(ManagedResourceGroupConfiguration.class).addAndConsume("cluster", group -> {
            group.setType("http");
            group.put("groupParam", "value");
        });
        configuration.getEntities(GatewayConfiguration.class).addAndConsume("gateway1", gateway -> {
            gateway.setType("snmp");
            gateway.put("gatewayParam", "value");
        });
        final String json = mapper.writeValueAsString(new AgentDataObject(configuration));
        assertNotNull(json);
        configuration.clear();
        assertTrue(configuration.isEmpty());
        mapper.readValue(json, AgentDataObject.class).exportTo(configuration);
        assertEquals("value", configuration.get("param"));
        assertNotNull(configuration.getEntities(ManagedResourceConfiguration.class).get("resource1"));
        assertNotNull(configuration.getEntities(ManagedResourceGroupConfiguration.class).get("cluster"));
        assertNotNull(configuration.getEntities(GatewayConfiguration.class).get("gateway1"));
    }
}
