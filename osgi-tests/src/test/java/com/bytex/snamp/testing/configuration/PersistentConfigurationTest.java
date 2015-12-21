package com.bytex.snamp.testing.configuration;

import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.PersistentConfigurationManager;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import org.junit.Test;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class PersistentConfigurationTest extends AbstractSnampIntegrationTest {

    @Test
    public void configurationTest() throws Exception {
        final ServiceHolder<ConfigurationAdmin> admin = new ServiceHolder<>(getTestBundleContext(), ConfigurationAdmin.class);
        try{
            final PersistentConfigurationManager manager = new PersistentConfigurationManager(admin);
            manager.load();
            AgentConfiguration currentConfig = manager.getCurrentConfiguration();
            assertNotNull(currentConfig);
            assertEquals(0, currentConfig.getResourceAdapters().size());
            assertEquals(0, currentConfig.getManagedResources().size());
            //save adapter
            final AgentConfiguration.ResourceAdapterConfiguration adapter =
                    currentConfig.getResourceAdapters().getOrAdd("adapter1");
            assertNotNull(adapter);
            adapter.setAdapterName("snmp");
            adapter.getParameters().put("param1", "value");
            //save connector
            final AgentConfiguration.ManagedResourceConfiguration resource = currentConfig.getManagedResources().getOrAdd("resource1");
            resource.setConnectionString("connection string");
            resource.setConnectionType("jmx");
            resource.getParameters().put("param2", "value2");
            resource.getFeatures(AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration.class).getOrAdd("attr1");
            //save and reload again
            manager.save();
            manager.load();
            currentConfig = manager.getCurrentConfiguration();
            assertEquals(1, currentConfig.getResourceAdapters().size());
            assertEquals(1, currentConfig.getManagedResources().size());
            assertTrue(currentConfig.getResourceAdapters().containsKey("adapter1"));
            assertTrue(currentConfig.getManagedResources().containsKey("resource1"));
            assertEquals("value", currentConfig.getResourceAdapters().get("adapter1").getParameters().get("param1"));
            assertEquals("value2", currentConfig.getManagedResources().get("resource1").getParameters().get("param2"));
            assertNotNull(currentConfig.getManagedResources().get("resource1")
                    .getFeatures(AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration.class).get("attr1"));
            //delete managed resource
            currentConfig.getManagedResources().remove("resource1");
            assertEquals(0, currentConfig.getManagedResources().size());
            manager.save();
            manager.load();
            currentConfig = manager.getCurrentConfiguration();
            assertEquals(1, currentConfig.getResourceAdapters().size());
            assertEquals(0, currentConfig.getManagedResources().size());
            assertTrue(currentConfig.getResourceAdapters().containsKey("adapter1"));
            assertEquals("value", currentConfig.getResourceAdapters().get("adapter1").getParameters().get("param1"));
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
