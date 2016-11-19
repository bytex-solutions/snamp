package com.bytex.snamp.testing.connector.http;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.instrumentation.IntegerMeasurement;
import com.bytex.snamp.instrumentation.StandardMeasurements;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import org.junit.Test;

import javax.management.JMException;
import java.io.IOException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class HttpConnectorTest extends AbstractHttpConnectorTest {

    private static final String INSTANCE_NAME = "testInstance";

    public HttpConnectorTest() {
        super(INSTANCE_NAME);
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Test
    public void testLastValueExtraction2() throws IOException, JMException {
        final IntegerMeasurement measurement1 = StandardMeasurements.freeRam(41L);
        measurement1.setInstanceName(INSTANCE_NAME);
        measurement1.setComponentName(COMPONENT_NAME);
        final IntegerMeasurement measurement2 = StandardMeasurements.freeRam(46L);
        measurement2.setInstanceName(INSTANCE_NAME);
        measurement2.setComponentName(COMPONENT_NAME);
        sendMeasurements(measurement1, measurement2);
        testAttribute("min", TypeToken.of(Long.class), 41L, true);
        testAttribute("max", TypeToken.of(Long.class), 46L, true);
    }

    @Test
    public void testLastValueExtraction() throws IOException, JMException {
        final IntegerMeasurement measurement = StandardMeasurements.freeRam(42L);
        measurement.setInstanceName(INSTANCE_NAME);
        measurement.setComponentName(COMPONENT_NAME);
        sendMeasurement(measurement);
        testAttribute("longValue", TypeToken.of(Long.class), 42L, true);
    }

    @Test
    public void configurationTest(){
        testConfigurationDescriptor(ManagedResourceConfiguration.class, ImmutableSet.of(
                "componentInstance",
                "componentName",
                "synchronizationPeriod"
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
            attribute.getParameters().put("gauge", "gauge64");
            attribute.setAlternativeName(StandardMeasurements.FREE_RAM.getMeasurementName());
        });
        attributes.addAndConsume("longValue", attribute -> attribute.getParameters().put("gauge", "get lastValue from gauge64 attribute1"));
        attributes.addAndConsume("min", attribute -> attribute.getParameters().put("gauge", "get minValue from gauge64 attribute1"));
        attributes.addAndConsume("max", attribute -> attribute.getParameters().put("gauge", "get maxValue from gauge64 attribute1"));
    }
}
