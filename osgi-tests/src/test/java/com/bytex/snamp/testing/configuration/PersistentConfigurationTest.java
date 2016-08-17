package com.bytex.snamp.testing.configuration;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@ExamReactorStrategy(PerMethod.class)
public class PersistentConfigurationTest extends AbstractSnampIntegrationTest {

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Test
    public void groupConfigurationTest() throws Exception {
        final ServiceHolder<ConfigurationManager> admin = ServiceHolder.tryCreate(getTestBundleContext(), ConfigurationManager.class);
        assertNotNull(admin);
        try {
            admin.get().processConfiguration(currentConfig -> {
                //resource without group
                ManagedResourceConfiguration resource = currentConfig.getEntities(ManagedResourceConfiguration.class).getOrAdd("resource1");
                resource.getParameters().put("key1", "value1");
                //resource with group
                resource = currentConfig.getEntities(ManagedResourceConfiguration.class).getOrAdd("resource2");
                resource.getParameters().put("key1", "value1");
                resource.setGroupName("group1");
                //group
                ManagedResourceGroupConfiguration group = currentConfig.getEntities(ManagedResourceGroupConfiguration.class).getOrAdd("group1");
                group.getParameters().put("key1", "valueFromGroup");
                group.getParameters().put("key2", "value2");
                //attribute in group
                assertTrue(group.getFeatures(AttributeConfiguration.class).addAndConsume("attribute1", attr -> {
                    attr.setAlternativeName("altName");
                    attr.getParameters().put("param1", "value1");
                }));
                return true;
            });
            //verify first and second resources
            admin.get().readConfiguration(currentConfig -> {
                //verify resource without group
                ManagedResourceConfiguration resource = currentConfig.getEntities(ManagedResourceConfiguration.class).get("resource1");
                assertNotNull(resource);
                assertEquals("value1", resource.getParameters().get("key1"));
                assertEquals(0, resource.getFeatures(AttributeConfiguration.class).size());
                //verify resource with group
                resource = currentConfig.getEntities(ManagedResourceConfiguration.class).get("resource2");
                assertNotNull(resource);
                assertEquals("valueFromGroup", resource.getParameters().get("key1"));
                assertEquals("value2", resource.getParameters().get("key2"));
                //verify attribute in resources
                assertFalse(resource.getFeatures(AttributeConfiguration.class).addAndConsume("attribute1", attr -> {
                    assertEquals("altName", attr.getAlternativeName());
                    assertEquals("value1", attr.getParameters().get("param1"));
                }));
            });
        } finally {
            admin.release(getTestBundleContext());
        }
    }

    @Test
    public void configurationTest() throws Exception {
        final ServiceHolder<ConfigurationManager> admin = ServiceHolder.tryCreate(getTestBundleContext(), ConfigurationManager.class);
        assertNotNull(admin);
        try{
            admin.get().processConfiguration(currentConfig -> {
                assertNotNull(currentConfig);
                assertEquals(0, currentConfig.getEntities(GatewayConfiguration.class).size());
                assertEquals(0, currentConfig.getEntities(ManagedResourceConfiguration.class).size());
                return false;
            });
            //save gateway
            admin.get().processConfiguration(currentConfig -> {
                final GatewayConfiguration gatewayInstanceConfig =
                        currentConfig.getEntities(GatewayConfiguration.class).getOrAdd("gateway1");
                assertNotNull(gatewayInstanceConfig);
                gatewayInstanceConfig.setType("snmp");
                gatewayInstanceConfig.getParameters().put("param1", "value");
                return true;
            });
            //save connector
            admin.get().processConfiguration(currentConfig -> {
                final ManagedResourceConfiguration resource = currentConfig.getEntities(ManagedResourceConfiguration.class).getOrAdd("resource1");
                resource.setConnectionString("connection string");
                resource.setType("jmx");
                resource.getParameters().put("param2", "value2");
                resource.getFeatures(AttributeConfiguration.class).getOrAdd("attr1");
                return true;
            });
            //verify configuration
            admin.get().processConfiguration(currentConfig -> {
                assertEquals(1, currentConfig.getEntities(GatewayConfiguration.class).size());
                assertEquals(1, currentConfig.getEntities(ManagedResourceConfiguration.class).size());
                assertTrue(currentConfig.getEntities(GatewayConfiguration.class).containsKey("gateway1"));
                assertTrue(currentConfig.getEntities(ManagedResourceConfiguration.class).containsKey("resource1"));
                assertEquals("value", currentConfig.getEntities(GatewayConfiguration.class).get("gateway1").getParameters().get("param1"));
                assertEquals("value2", currentConfig.getEntities(ManagedResourceConfiguration.class).get("resource1").getParameters().get("param2"));
                assertNotNull(currentConfig.getEntities(ManagedResourceConfiguration.class).get("resource1")
                        .getFeatures(AttributeConfiguration.class).get("attr1"));

                return false;
            });
            //delete managed resource
            admin.get().processConfiguration(currentConfig -> {
                currentConfig.getEntities(ManagedResourceConfiguration.class).remove("resource1");
                assertEquals(0, currentConfig.getEntities(ManagedResourceConfiguration.class).size());
                return true;
            });
            admin.get().processConfiguration(currentConfig -> {
                assertEquals(1, currentConfig.getEntities(GatewayConfiguration.class).size());
                assertEquals(0, currentConfig.getEntities(ManagedResourceConfiguration.class).size());
                assertTrue(currentConfig.getEntities(GatewayConfiguration.class).containsKey("gateway1"));
                assertEquals("value", currentConfig.getEntities(GatewayConfiguration.class).get("gateway1").getParameters().get("param1"));
                return false;
            });
        }
        finally {
            admin.release(getTestBundleContext());
        }
    }

    /**
     * Creates a new configuration for running this test.
     *
     * @param config The configuration to set.
     */
    @Override
    protected void setupTestConfiguration(final AgentConfiguration config) {

    }
}
