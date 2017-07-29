package com.bytex.snamp.testing.connector.jmx;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@SnampDependencies({SnampFeature.JMX_CONNECTOR, SnampFeature.STANDARD_TOOLS})
public final class AddRemoveConnectorTest extends AbstractSnampIntegrationTest {

    @Test
    public void addRemoveConnectorTest() throws IOException, InterruptedException {
        final String RESOURCE_NAME = "dummyResource";
        processConfiguration(config -> {
            final ManagedResourceConfiguration resource = config.getResources().getOrAdd("dummyResource");
            resource.setConnectionString(AbstractJmxConnectorTest.getConnectionString());
            resource.setType(AbstractJmxConnectorTest.CONNECTOR_NAME);
            resource.putAll(AbstractJmxConnectorTest.DEFAULT_PARAMS);
            return true;
        });
        Thread.sleep(1000);
        processConfiguration(config -> {
            final ManagedResourceConfiguration resource = config.getResources().getOrAdd("dummyResource");
            resource.put("param", "value");
            return true;
        });
        Thread.sleep(1000);
        processConfiguration(config -> {
            config.getResources().remove(RESOURCE_NAME);
            return true;
        });
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
