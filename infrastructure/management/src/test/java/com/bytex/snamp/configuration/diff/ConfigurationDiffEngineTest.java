package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.configuration.impl.SerializableAgentConfiguration;
import org.junit.Assert;
import org.junit.Test;

import java.time.temporal.ChronoUnit;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class ConfigurationDiffEngineTest extends Assert {
    @Test
    public void renameResourceTest() throws CloneNotSupportedException {
        final AgentConfiguration baseline = new SerializableAgentConfiguration();
        final ManagedResourceConfiguration resource = baseline.getEntities(ManagedResourceConfiguration.class).getOrAdd("resource1");
        final AttributeConfiguration attr = resource
                .getFeatures(AttributeConfiguration.class)
                .getOrAdd("attr");
        attr.setReadWriteTimeout(1, ChronoUnit.SECONDS);
        resource.setConnectionString("connection-string");
        resource.setType("jmx");
        resource.getParameters().put("param", "value");

        final AgentConfiguration target = baseline.clone();
        //remove old resource
        target.getEntities(ManagedResourceConfiguration.class).remove("resource1");
        //add the same resource but with different name
        AbstractAgentConfiguration.copy(resource, target.getEntities(ManagedResourceConfiguration.class).getOrAdd("resource2"));

        //merge and verify
        ConfigurationDiffEngine.merge(target, baseline);
        assertEquals(1, baseline.getEntities(ManagedResourceConfiguration.class).size());
        assertNull(baseline.getEntities(ManagedResourceConfiguration.class).get("resource1"));
        assertNotNull(baseline.getEntities(ManagedResourceConfiguration.class).get("resource2"));
    }

    @Test
    public void attributeAddRemoveTest() throws CloneNotSupportedException {
        final AgentConfiguration baseline = new SerializableAgentConfiguration();
        ManagedResourceConfiguration resource = baseline.getEntities(ManagedResourceConfiguration.class).getOrAdd("resource1");
        AttributeConfiguration attr = resource
                .getFeatures(AttributeConfiguration.class)
                .getOrAdd("attr");
        attr.setReadWriteTimeout(1, ChronoUnit.SECONDS);
        resource.setConnectionString("connection-string");
        resource.setType("jmx");
        resource.getParameters().put("param", "value");

        //add attribute
        AgentConfiguration target = baseline.clone();
        resource = target.getEntities(ManagedResourceConfiguration.class).get("resource1");
        attr = resource
                .getFeatures(AttributeConfiguration.class)
                .getOrAdd("attr2");
        attr.getParameters().put("param2", "value2");

        ConfigurationDiffEngine.merge(target, baseline);

        assertEquals("value2", baseline.getEntities(ManagedResourceConfiguration.class)
                .get("resource1")
                .getFeatures(AttributeConfiguration.class)
                .get("attr2")
                .getParameters()
                .get("param2"));

        //remove attribute
        target = baseline.clone();
        assertNotNull(target.getEntities(ManagedResourceConfiguration.class)
                .get("resource1")
                .getFeatures(AttributeConfiguration.class)
                .remove("attr2"));

        ConfigurationDiffEngine.merge(target, baseline);

        assertNull(baseline.getEntities(ManagedResourceConfiguration.class)
                .get("resource1")
                .getFeatures(AttributeConfiguration.class)
                .get("attr2"));
    }

    @Test
    public void diffTest() throws CloneNotSupportedException {
        final AgentConfiguration baseline = new SerializableAgentConfiguration();
        final ManagedResourceConfiguration resource = baseline.getEntities(ManagedResourceConfiguration.class).getOrAdd("resource1");
        final AttributeConfiguration attr = resource
                .getFeatures(AttributeConfiguration.class)
                .getOrAdd("attr");
        attr.setReadWriteTimeout(1, ChronoUnit.SECONDS);
        resource.setConnectionString("connection-string");
        resource.setType("jmx");
        resource.getParameters().put("param", "value");
        final AgentConfiguration target = baseline.clone();
        target.clear();
        final ManagedResourceConfiguration resource2 = target.getEntities(ManagedResourceConfiguration.class).getOrAdd("resource2");
        AbstractAgentConfiguration.copy(resource, resource2);
        resource2.setConnectionString("connection-string-2");
        resource2.setType("snmp");
        final ManagedResourceConfiguration resource3 = target.getEntities(ManagedResourceConfiguration.class).getOrAdd("resource3");
        AbstractAgentConfiguration.copy(resource, resource3);
        resource3.setConnectionString("connection-string-3");
        ConfigurationDiffEngine.merge(target, baseline);
        Assert.assertEquals(0, baseline.getEntities(GatewayConfiguration.class).size());
        Assert.assertEquals(2, baseline.getEntities(ManagedResourceConfiguration.class).size());
    }
}
