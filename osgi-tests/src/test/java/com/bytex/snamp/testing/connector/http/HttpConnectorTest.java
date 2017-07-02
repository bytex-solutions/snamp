package com.bytex.snamp.testing.connector.http;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.instrumentation.measurements.IntegerMeasurement;
import com.bytex.snamp.instrumentation.measurements.StandardMeasurements;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import org.junit.Test;

import javax.management.JMException;
import java.io.File;
import java.io.IOException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class HttpConnectorTest extends AbstractHttpConnectorTest {

    public HttpConnectorTest() {
        super("file:" + getPathToFileInProjectRoot("sample-groovy-scripts") + File.separator,
                "HttpAcceptorParser.groovy");
    }

    @Test
    public void testLastValueExtraction2() throws IOException, JMException, InterruptedException {
        final IntegerMeasurement measurement1 = StandardMeasurements.freeRam(41L);
        measurement1.setInstanceName(TEST_RESOURCE_NAME);
        measurement1.setComponentName(COMPONENT_NAME);
        final IntegerMeasurement measurement2 = StandardMeasurements.freeRam(46L);
        measurement2.setInstanceName(TEST_RESOURCE_NAME);
        measurement2.setComponentName(COMPONENT_NAME);
        sendMeasurements(measurement1, measurement2);
        Thread.sleep(100);
        testAttribute("min", TypeToken.of(Long.class), 41L, true);
        testAttribute("max", TypeToken.of(Long.class), 46L, true);
    }

    @Test
    public void testLastValueExtraction() throws IOException, JMException {
        final IntegerMeasurement measurement = StandardMeasurements.freeRam(42L);
        measurement.setInstanceName(TEST_RESOURCE_NAME);
        measurement.setComponentName(COMPONENT_NAME);
        sendMeasurement(measurement);
        testAttribute("longValue", TypeToken.of(Long.class), 42L, true);
    }

    @Test
    public void testCustomTextParser() throws IOException, JMException {
        sendText("Hello, world");
        testAttribute("strValue", TypeToken.of(String.class), "Hello, world", true);
    }

    @Test
    public void testCustomJsonParser() throws IOException, JMException {
        sendJson("{\"content\": \"Frank Underwood\"}");
        testAttribute("strValue", TypeToken.of(String.class), "Frank Underwood", true);
    }

    @Test
    public void testCustomXmlParser() throws IOException, JMException {
        sendXml("<root><content>Barry Burton</content></root>");
        testAttribute("strValue", TypeToken.of(String.class), "Barry Burton", true);
    }

    @Test
    public void configurationTest(){
        testConfigurationDescriptor(ManagedResourceConfiguration.class, ImmutableSet.of(
                "synchronizationPeriod",
                "parserScriptPath",
                "parserScript"
        ));
        testConfigurationDescriptor(AttributeConfiguration.class, ImmutableSet.of(
                "from",
                "to",
                "filter",
                "gauge"
        ));
        testConfigurationDescriptor(EventConfiguration.class, ImmutableSet.of(
                "filter"
        ));
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        attributes.addAndConsume("attribute1", attribute -> {
            attribute.put("gauge", "gauge64");
            attribute.setAlternativeName(StandardMeasurements.FREE_RAM);
        });
        attributes.addAndConsume("attribute2", attribute -> {
            attribute.put("gauge", "stringGauge");
            attribute.setAlternativeName("customStrings");
        });
        attributes.addAndConsume("longValue", attribute -> attribute.put("gauge", "get lastValue from gauge64 attribute1"));
        attributes.addAndConsume("strValue", attribute -> attribute.put("gauge", "get lastValue from stringGauge attribute2"));
        attributes.addAndConsume("min", attribute -> attribute.put("gauge", "get minValue from gauge64 attribute1"));
        attributes.addAndConsume("max", attribute -> attribute.put("gauge", "get maxValue from gauge64 attribute1"));
    }
}
