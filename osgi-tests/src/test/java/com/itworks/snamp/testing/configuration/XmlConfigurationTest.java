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
        assertEquals("TEST ADAPTER", currentConfig.getAgentHostingConfig().getAdapterName());
        assertEquals("value1", currentConfig.getAgentHostingConfig().getHostingParams().get("param1"));
        assertEquals(0, currentConfig.getManagedResources().size());
    }

    /**
     * Creates a new configuration for running this test.
     *
     * @return A new SNAMP configuration used for executing SNAMP bundles.
     */
    @Override
    protected void setupTestConfiguration(final AgentConfiguration config) {
        config.getAgentHostingConfig().setAdapterName("TEST ADAPTER");
        config.getAgentHostingConfig().getHostingParams().put("param1", "value1");
    }
}
