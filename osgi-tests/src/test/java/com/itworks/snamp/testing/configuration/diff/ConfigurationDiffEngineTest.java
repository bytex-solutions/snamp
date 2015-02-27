package com.itworks.snamp.testing.configuration.diff;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.configuration.AbstractAgentConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.SerializableAgentConfiguration;
import com.itworks.snamp.configuration.diff.ConfigurationDiffEngine;
import com.itworks.snamp.testing.AbstractUnitTest;
import org.junit.Test;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ConfigurationDiffEngineTest extends AbstractUnitTest<ConfigurationDiffEngine> {
    @Test
    public void diffTest(){
        final AgentConfiguration baseline = new SerializableAgentConfiguration();
        final ManagedResourceConfiguration resource = baseline.newConfigurationEntity(ManagedResourceConfiguration.class);
        final ManagedResourceConfiguration.AttributeConfiguration attr = resource.newElement(ManagedResourceConfiguration.AttributeConfiguration.class);
        attr.setAttributeName("attribute");
        attr.setReadWriteTimeout(TimeSpan.fromSeconds(1));
        resource.getElements(ManagedResourceConfiguration.AttributeConfiguration.class).put("attr", attr);
        resource.setConnectionString("connection-string");
        resource.setConnectionType("jmx");
        resource.getParameters().put("param", "value");
        baseline.getManagedResources().put("resource1", resource);
        final AgentConfiguration target = baseline.clone();
        target.clear();
        final ManagedResourceConfiguration resource2 = target.newConfigurationEntity(ManagedResourceConfiguration.class);
        AbstractAgentConfiguration.copy(resource, resource2);
        resource2.setConnectionString("connection-string-2");
        resource2.setConnectionType("snmp");
        target.getManagedResources().put("resource2", resource2);
        final ManagedResourceConfiguration resource3 = target.newConfigurationEntity(ManagedResourceConfiguration.class);
        AbstractAgentConfiguration.copy(resource, resource3);
        resource3.setConnectionString("connection-string-3");
        target.getManagedResources().put("resource1", resource3);
        assertEquals(2, ConfigurationDiffEngine.merge(target, baseline));
        assertEquals(0, baseline.getResourceAdapters().size());
        assertEquals(2, baseline.getManagedResources().size());
    }
}