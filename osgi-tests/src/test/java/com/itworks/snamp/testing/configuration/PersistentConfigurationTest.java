package com.itworks.snamp.testing.configuration;

import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.PersistentConfigurationManager;
import com.itworks.snamp.testing.AbstractIntegrationTest;
import com.itworks.snamp.testing.SnampFeature;
import org.junit.Test;
import org.osgi.service.cm.ConfigurationAdmin;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class PersistentConfigurationTest extends AbstractIntegrationTest {
    public PersistentConfigurationTest(){
        super(mavenBundle("org.apache.felix", "org.apache.felix.configadmin", "1.8.0"),
                mavenBundle("org.apache.felix", "org.apache.felix.log", "1.0.1"),
                mavenBundle("org.apache.felix", "org.apache.felix.eventadmin", "1.4.2"),
                mavenBundle("com.google.guava", "guava", "18.0"),
                SnampFeature.CORLIB.getReference());
    }

    @Test
    public void configurationTest() throws Exception {
        final ServiceReferenceHolder<ConfigurationAdmin> admin = new ServiceReferenceHolder<>(getTestBundleContext(), ConfigurationAdmin.class);
        try{
            final PersistentConfigurationManager manager = new PersistentConfigurationManager(admin);
            manager.load();
            AgentConfiguration currentConfig = manager.getCurrentConfiguration();
            assertNotNull(currentConfig);
            assertEquals(0, currentConfig.getResourceAdapters().size());
            assertEquals(0, currentConfig.getManagedResources().size());
            //save adapter
            final AgentConfiguration.ResourceAdapterConfiguration adapter =
                    currentConfig.newConfigurationEntity(AgentConfiguration.ResourceAdapterConfiguration.class);
            assertNotNull(adapter);
            adapter.setAdapterName("snmp");
            adapter.getParameters().put("param1", "value");
            currentConfig.getResourceAdapters().put("adapter1", adapter);
            //save connector
            final AgentConfiguration.ManagedResourceConfiguration resource = currentConfig.newConfigurationEntity(AgentConfiguration.ManagedResourceConfiguration.class);
            resource.setConnectionString("connection string");
            resource.setConnectionType("jmx");
            resource.getParameters().put("param2", "value2");
            final AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration attr = resource.newElement(AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration.class);
            resource.getElements(AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration.class).put("attr1", attr);
            currentConfig.getManagedResources().put("resource1", resource);
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
            assertNotNull(currentConfig.getManagedResources().get("resource1").getElements(AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration.class).get("attr1"));
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
}
