package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.configuration.AbstractAgentConfiguration;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.impl.SerializableAgentConfiguration;
import org.junit.Assert;
import org.junit.Test;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class ConfigurationDiffEngineTest extends Assert {
    @Test
    public void renameResourceTest(){
        final AgentConfiguration baseline = new SerializableAgentConfiguration();
        final ManagedResourceConfiguration resource = baseline.getEntities(ManagedResourceConfiguration.class).getOrAdd("resource1");
        final ManagedResourceConfiguration.AttributeConfiguration attr = resource
                .getFeatures(ManagedResourceConfiguration.AttributeConfiguration.class)
                .getOrAdd("attr");
        attr.setReadWriteTimeout(TimeSpan.ofSeconds(1));
        resource.setConnectionString("connection-string");
        resource.setConnectionType("jmx");
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
    public void attributeAddRemoveTest(){
        final AgentConfiguration baseline = new SerializableAgentConfiguration();
        ManagedResourceConfiguration resource = baseline.getEntities(ManagedResourceConfiguration.class).getOrAdd("resource1");
        ManagedResourceConfiguration.AttributeConfiguration attr = resource
                .getFeatures(ManagedResourceConfiguration.AttributeConfiguration.class)
                .getOrAdd("attr");
        attr.setReadWriteTimeout(TimeSpan.ofSeconds(1));
        resource.setConnectionString("connection-string");
        resource.setConnectionType("jmx");
        resource.getParameters().put("param", "value");

        //add attribute
        AgentConfiguration target = baseline.clone();
        resource = target.getEntities(ManagedResourceConfiguration.class).get("resource1");
        attr = resource
                .getFeatures(ManagedResourceConfiguration.AttributeConfiguration.class)
                .getOrAdd("attr2");
        attr.getParameters().put("param2", "value2");

        ConfigurationDiffEngine.merge(target, baseline);

        assertEquals("value2", baseline.getEntities(ManagedResourceConfiguration.class)
                .get("resource1")
                .getFeatures(ManagedResourceConfiguration.AttributeConfiguration.class)
                .get("attr2")
                .getParameters()
                .get("param2"));

        //remove attribute
        target = baseline.clone();
        assertNotNull(target.getEntities(ManagedResourceConfiguration.class)
                .get("resource1")
                .getFeatures(ManagedResourceConfiguration.AttributeConfiguration.class)
                .remove("attr2"));

        ConfigurationDiffEngine.merge(target, baseline);

        assertNull(baseline.getEntities(ManagedResourceConfiguration.class)
                .get("resource1")
                .getFeatures(ManagedResourceConfiguration.AttributeConfiguration.class)
                .get("attr2"));
    }

    @Test
    public void diffTest(){
        final AgentConfiguration baseline = new SerializableAgentConfiguration();
        final ManagedResourceConfiguration resource = baseline.getEntities(ManagedResourceConfiguration.class).getOrAdd("resource1");
        final ManagedResourceConfiguration.AttributeConfiguration attr = resource
                .getFeatures(ManagedResourceConfiguration.AttributeConfiguration.class)
                .getOrAdd("attr");
        attr.setReadWriteTimeout(TimeSpan.ofSeconds(1));
        resource.setConnectionString("connection-string");
        resource.setConnectionType("jmx");
        resource.getParameters().put("param", "value");
        final AgentConfiguration target = baseline.clone();
        target.clear();
        final ManagedResourceConfiguration resource2 = target.getEntities(ManagedResourceConfiguration.class).getOrAdd("resource2");
        AbstractAgentConfiguration.copy(resource, resource2);
        resource2.setConnectionString("connection-string-2");
        resource2.setConnectionType("snmp");
        final ManagedResourceConfiguration resource3 = target.getEntities(ManagedResourceConfiguration.class).getOrAdd("resource3");
        AbstractAgentConfiguration.copy(resource, resource3);
        resource3.setConnectionString("connection-string-3");
        ConfigurationDiffEngine.merge(target, baseline);
        Assert.assertEquals(0, baseline.getEntities(ManagedResourceConfiguration.class).size());
        Assert.assertEquals(2, baseline.getEntities(ManagedResourceConfiguration.class).size());
    }
}
