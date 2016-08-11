package com.bytex.snamp.testing.configuration;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import org.junit.Test;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class PersistentConfigurationTest extends AbstractSnampIntegrationTest {

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
                resource.getFeatures(ManagedResourceConfiguration.AttributeConfiguration.class).getOrAdd("attr1");
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
                        .getFeatures(ManagedResourceConfiguration.AttributeConfiguration.class).get("attr1"));

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
