package com.itworks.snamp.testing.configuration;

import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.testing.AbstractSnampIntegrationTest;
import org.junit.Test;

import java.io.IOException;

/**
 * Represents test for{@link com.itworks.snamp.configuration.impl.XmlConfigurationManager} bundle.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class XmlConfigurationTest extends AbstractSnampIntegrationTest {

    @Test
    public final void configManagerTest() throws IOException {
        final AgentConfiguration currentConfig = readSnampConfiguration();
        assertNotNull(currentConfig);
        assertEquals(1, currentConfig.getResourceAdapters().size());
        assertTrue(currentConfig.getResourceAdapters().containsKey("impl-adapter"));
        final AgentConfiguration.ResourceAdapterConfiguration adapter = currentConfig.getResourceAdapters().get("impl-adapter");
        assertEquals("TEST ADAPTER", adapter.getAdapterName());
        assertEquals("value1", adapter.getParameters().get("param1"));
        assertEquals(0, currentConfig.getManagedResources().size());
    }

    /**
     * Creates a new configuration for running this test.
     */
    @Override
    protected void setupTestConfiguration(final AgentConfiguration config) {
        final AgentConfiguration.ResourceAdapterConfiguration adapter =
                config.newConfigurationEntity(AgentConfiguration.ResourceAdapterConfiguration.class);
        adapter.setAdapterName("TEST ADAPTER");
        adapter.getParameters().put("param1", "value1");
        config.getResourceAdapters().put("impl-adapter", adapter);
    }
}
