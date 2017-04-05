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
        configuration.getResources().addAndConsume("resource1", resource -> {
            resource.setType("http");
            resource.setGroupName("cluster");
            resource.setConnectionString("http://localhost");
            resource.getEvents().addAndConsume("event1", event -> {
                event.put("eventParam", "value");
            });
        });
        configuration.getResourceGroups().addAndConsume("cluster", group -> {
            group.setType("http");
            group.put("groupParam", "value");
        });
        configuration.getGateways().addAndConsume("gateway1", gateway -> {
            gateway.setType("snmp");
            gateway.put("gatewayParam", "value");
        });
        final String json = mapper.writeValueAsString(new AgentDataObject(configuration));
        assertNotNull(json);
        configuration.clear();
        assertTrue(configuration.isEmpty());
        mapper.readValue(json, AgentDataObject.class).exportTo(configuration);
        assertEquals("value", configuration.get("param"));
        assertNotNull(configuration.getResources().get("resource1"));
        assertNotNull(configuration.getResourceGroups().get("cluster"));
        assertNotNull(configuration.getGateways().get("gateway1"));
    }
}
