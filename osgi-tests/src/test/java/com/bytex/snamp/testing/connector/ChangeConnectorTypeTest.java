package com.bytex.snamp.testing.connector;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.instrumentation.measurements.IntegerMeasurement;
import com.bytex.snamp.instrumentation.measurements.StandardMeasurements;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.groovy.AbstractGroovyConnectorTest;
import com.bytex.snamp.testing.connector.http.AbstractHttpConnectorTest;
import org.junit.Test;

import javax.management.JMException;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@SnampDependencies({SnampFeature.GROOVY_CONNECTOR, SnampFeature.HTTP_ACCEPTOR})
public final class ChangeConnectorTypeTest extends AbstractSnampIntegrationTest {
    @Test
    public void changeTypeTest() throws IOException, TimeoutException, InterruptedException, JMException {
        final String RESOURCE_NAME = "connector";
        //Configure Groovy connector
        processConfiguration(config -> {
            ManagedResourceConfiguration connector = config.getResources().getOrAdd(RESOURCE_NAME);
            connector.setType(AbstractGroovyConnectorTest.CONNECTOR_TYPE);
            connector.setConnectionString(AbstractGroovyConnectorTest.getConnectionString());
            connector.getAttributes().getOrAdd("DummyAttribute").put("configParam", "value");
            return true;
        });

        try (final ManagedResourceConnectorClient client = ManagedResourceConnectorClient.tryCreate(getTestBundleContext(), RESOURCE_NAME, Duration.ofSeconds(2))
                .orElseThrow(AssertionError::new)) {
            final Object attribute = client.getAttribute("DummyAttribute");
            assertTrue(attribute instanceof Integer);
        }
        //let's change type of the connector
        final String COMPONENT_NAME = "javaApp";
        processConfiguration(config -> {
            ManagedResourceConfiguration connector = config.getResources().getOrAdd(RESOURCE_NAME);
            assertEquals(AbstractGroovyConnectorTest.CONNECTOR_TYPE, connector.getType());
            connector.setType(AbstractHttpConnectorTest.CONNECTOR_TYPE);
            connector.setConnectionString("");
            connector.setGroupName(COMPONENT_NAME);
            connector.getAttributes().clear();
            connector.getAttributes()
                    .addAndConsume("longValue", attribute -> attribute.put("gauge", "get lastValue from gauge64 attribute1"));
            connector.getAttributes()
                    .addAndConsume("attribute1", attribute -> {
                        attribute.put("gauge", "gauge64");
                        attribute.setAlternativeName(StandardMeasurements.FREE_RAM);
                    });
            return true;
        });
        try (final ManagedResourceConnectorClient client = ManagedResourceConnectorClient.tryCreate(getTestBundleContext(), RESOURCE_NAME, Duration.ofSeconds(2))
                .orElseThrow(AssertionError::new)) {
            final IntegerMeasurement measurement = StandardMeasurements.freeRam(42L);
            measurement.setInstanceName(RESOURCE_NAME);
            measurement.setComponentName(COMPONENT_NAME);
            AbstractHttpConnectorTest.sendMeasurement(measurement);
            Thread.sleep(2_000);
            final Object attribute = client.getAttribute("longValue");
            assertEquals(42L, attribute);
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
