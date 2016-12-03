package com.bytex.snamp.testing.connector;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
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
            ManagedResourceConfiguration connector = config.getEntities(ManagedResourceConfiguration.class).getOrAdd(RESOURCE_NAME);
            connector.setType(AbstractGroovyConnectorTest.CONNECTOR_TYPE);
            connector.setConnectionString(AbstractGroovyConnectorTest.getConnectionString());
            connector.getFeatures(AttributeConfiguration.class).getOrAdd("DummyAttribute").getParameters().put("configParam", "value");
            return true;
        });
        ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(getTestBundleContext(), RESOURCE_NAME, Duration.ofSeconds(2));
        try{
            final Object attribute = client.getAttribute("DummyAttribute");
            assertTrue(attribute instanceof Integer);
        } finally {
            client.release(getTestBundleContext());
        }
        //let's change type of the connector
        processConfiguration(config -> {
            ManagedResourceConfiguration connector = config.getEntities(ManagedResourceConfiguration.class).getOrAdd(RESOURCE_NAME);
            assertEquals(AbstractGroovyConnectorTest.CONNECTOR_TYPE, connector.getType());
            connector.setType(AbstractHttpConnectorTest.CONNECTOR_TYPE);
            connector.setConnectionString("myComponent");
            connector.getFeatures(AttributeConfiguration.class).clear();
            connector.getFeatures(AttributeConfiguration.class)
                    .addAndConsume("strValue", attribute -> attribute.getParameters().put("gauge", "get lastValue from stringGauge attribute2"));
            connector.getFeatures(AttributeConfiguration.class)
                    .addAndConsume("attribute2", attribute -> {
                        attribute.getParameters().put("gauge", "stringGauge");
                        attribute.setAlternativeName("customStrings");
                    });
            return true;
        });
        client = new ManagedResourceConnectorClient(getTestBundleContext(), RESOURCE_NAME, Duration.ofSeconds(2));
        try{
            AbstractHttpConnectorTest.sendText("DS");
            final Object attribute = client.getAttribute("strValue");
            assertEquals("DS", attribute);
        } finally {
            client.release(getTestBundleContext());
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
