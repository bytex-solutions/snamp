package com.itworks.snamp.testing.configuration;

import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.testing.AbstractSnampIntegrationTest;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import java.io.IOException;

/**
 * Represents test for{@link com.itworks.snamp.configuration.impl.XmlConfigurationManager} bundle.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@ExamReactorStrategy(PerMethod.class)
public final class XmlConfigurationTest extends AbstractSnampIntegrationTest {

    @Test
    public final void configManagerTest() throws IOException {
        final AgentConfiguration currentConfig = readSnampConfiguration();
        assertNotNull(currentConfig);
        assertEquals(1, currentConfig.getResourceAdapters().size());
        assertTrue(currentConfig.getResourceAdapters().containsKey("jmx-adapter"));
        final AgentConfiguration.ResourceAdapterConfiguration adapter = currentConfig.getResourceAdapters().get("jmx-adapter");
        assertEquals("TEST ADAPTER", adapter.getAdapterName());
        assertEquals("value1", adapter.getHostingParams().get("param1"));
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
        adapter.getHostingParams().put("param1", "value1");
        config.getResourceAdapters().put("jmx-adapter", adapter);
    }
}
