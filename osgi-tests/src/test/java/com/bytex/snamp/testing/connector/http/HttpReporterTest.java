package com.bytex.snamp.testing.connector.http;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.instrumentation.ApplicationInfo;
import com.bytex.snamp.instrumentation.IntegerMeasurementReporter;
import com.bytex.snamp.instrumentation.MetricRegistry;
import com.bytex.snamp.instrumentation.measurements.IntegerMeasurement;
import com.bytex.snamp.instrumentation.measurements.StandardMeasurements;
import com.bytex.snamp.instrumentation.reporters.http.HttpReporter;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.google.common.reflect.TypeToken;
import org.junit.Test;

import javax.management.JMException;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by Роман on 06.12.2016.
 */
@SnampDependencies({SnampFeature.WRAPPED_LIBS})
public class HttpReporterTest extends AbstractHttpConnectorTest {
    private static final String INSTANCE_NAME = "testApplication";
    private final IntegerMeasurementReporter freeRAM;

    public HttpReporterTest() throws URISyntaxException {
        super(INSTANCE_NAME);
        ApplicationInfo.setInstance(INSTANCE_NAME);
        final MetricRegistry registry = new MetricRegistry(new HttpReporter("http://localhost:8181", null));
        freeRAM = registry.integer(StandardMeasurements.FREE_RAM);
    }

    @Test
    public void testLastValueExtraction() throws IOException, JMException, InterruptedException {
        freeRAM.report(154L);
        Thread.sleep(300);//reporting is asynchronous
        testAttribute("longValue", TypeToken.of(Long.class), 42L, true);
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        attributes.addAndConsume("attribute1", attribute -> {
            attribute.getParameters().put("gauge", "gauge64");
            attribute.setAlternativeName(StandardMeasurements.FREE_RAM);
        });
        attributes.addAndConsume("longValue", attribute -> attribute.getParameters().put("gauge", "get lastValue from gauge64 attribute1"));
        attributes.addAndConsume("min", attribute -> attribute.getParameters().put("gauge", "get minValue from gauge64 attribute1"));
        attributes.addAndConsume("max", attribute -> attribute.getParameters().put("gauge", "get maxValue from gauge64 attribute1"));
    }

}
