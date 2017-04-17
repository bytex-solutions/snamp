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
public final class PersistentConfigurationTest extends AbstractSnampIntegrationTest {

    @Test
    public void groupConfigurationTest() throws Exception {
        final ServiceHolder<ConfigurationManager> admin = ServiceHolder.tryCreate(getTestBundleContext(), ConfigurationManager.class)
                .orElseThrow(AssertionError::new);
        try {
            admin.get().processConfiguration(currentConfig -> {
                //resource without group
                ManagedResourceConfiguration resource = currentConfig.getResources().getOrAdd("resource1");
                resource.put("key1", "value1");
                //resource with group
                resource = currentConfig.getResources().getOrAdd("resource2");
                resource.put("key1", "value1");
                resource.setGroupName("group1");
                //group
                ManagedResourceGroupConfiguration group = currentConfig.getResourceGroups().getOrAdd("group1");
                group.put("key1", "valueFromGroup");
                group.put("key2", "value2");
                //attribute in group
                assertTrue(group.getAttributes().addAndConsume("attribute1", attr -> {
                    attr.setAlternativeName("altName");
                    attr.put("param1", "value1");
                }));
                return true;
            });
            //verify first and second resources
            admin.get().readConfiguration(currentConfig -> {
                //verify resource without group
                ManagedResourceConfiguration resource = currentConfig.getResources().get("resource1");
                assertNotNull(resource);
                assertEquals("value1", resource.get("key1"));
                assertEquals(0, resource.getAttributes().size());
                //verify resource with group
                resource = currentConfig.getResources().get("resource2");
                assertNotNull(resource);
                assertEquals("valueFromGroup", resource.get("key1"));
                assertEquals("value2", resource.get("key2"));
                //verify attribute in resources
                assertFalse(resource.getAttributes().addAndConsume("attribute1", attr -> {
                    assertEquals("altName", attr.getAlternativeName());
                    assertEquals("value1", attr.get("param1"));
                }));
            });
        } finally {
            admin.release(getTestBundleContext());
        }
    }

    @Test
    public void configurationTest() throws Exception {
        final ServiceHolder<ConfigurationManager> admin = ServiceHolder.tryCreate(getTestBundleContext(), ConfigurationManager.class)
                .orElseThrow(AssertionError::new);
        try{
            admin.get().processConfiguration(currentConfig -> {
                assertNotNull(currentConfig);
                assertEquals(0, currentConfig.getGateways().size());
                assertEquals(0, currentConfig.getResources().size());
                return false;
            });
            //save gateway
            admin.get().processConfiguration(currentConfig -> {
                final GatewayConfiguration gatewayInstanceConfig =
                        currentConfig.getGateways().getOrAdd("gateway1");
                assertNotNull(gatewayInstanceConfig);
                gatewayInstanceConfig.setType("snmp");
                gatewayInstanceConfig.put("param1", "value");
                return true;
            });
            //save connector
            admin.get().processConfiguration(currentConfig -> {
                final ManagedResourceConfiguration resource = currentConfig.getResources().getOrAdd("resource1");
                resource.setConnectionString("connection string");
                resource.setType("jmx");
                resource.put("param2", "value2");
                resource.getAttributes().getOrAdd("attr1");
                return true;
            });
            //verify configuration
            admin.get().processConfiguration(currentConfig -> {
                assertEquals(1, currentConfig.getGateways().size());
                assertEquals(1, currentConfig.getResources().size());
                assertTrue(currentConfig.getGateways().containsKey("gateway1"));
                assertTrue(currentConfig.getResources().containsKey("resource1"));
                assertEquals("value", currentConfig.getGateways().get("gateway1").get("param1"));
                assertEquals("value2", currentConfig.getResources().get("resource1").get("param2"));
                assertNotNull(currentConfig.getResources().get("resource1").getAttributes().get("attr1"));

                return false;
            });
            //delete managed resource
            admin.get().processConfiguration(currentConfig -> {
                currentConfig.getResources().remove("resource1");
                assertEquals(0, currentConfig.getResources().size());
                return true;
            });
            admin.get().processConfiguration(currentConfig -> {
                assertEquals(1, currentConfig.getGateways().size());
                assertEquals(0, currentConfig.getResources().size());
                assertTrue(currentConfig.getGateways().containsKey("gateway1"));
                assertEquals("value", currentConfig.getGateways().get("gateway1").get("param1"));
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
