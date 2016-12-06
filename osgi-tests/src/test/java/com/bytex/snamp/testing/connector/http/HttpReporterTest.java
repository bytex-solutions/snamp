package com.bytex.snamp.testing.connector.http;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.instrumentation.ApplicationInfo;
import com.bytex.snamp.instrumentation.MetricRegistry;
import com.bytex.snamp.instrumentation.measurements.StandardMeasurements;
import com.bytex.snamp.instrumentation.reporters.http.HttpReporter;
import com.bytex.snamp.testing.MavenArtifact;
import com.bytex.snamp.testing.MavenDependencies;
import com.google.common.reflect.TypeToken;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.JMException;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Represents tests for SNAMP instrumentation based on HTTP reporter.
 */
@MavenDependencies(bundles = {
        @MavenArtifact(groupId = "com.bytex.snamp.instrumentation", artifactId = "http-reporter", version = "1.0.0")
})
public class HttpReporterTest extends AbstractHttpConnectorTest {
    private static final String INSTANCE_NAME = "testApplication";
    private MetricRegistry registry;

    public HttpReporterTest() throws URISyntaxException {
        super(INSTANCE_NAME);
        ApplicationInfo.setInstance(INSTANCE_NAME);
        ApplicationInfo.setName(COMPONENT_NAME);
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws URISyntaxException {
        registry = new MetricRegistry(new HttpReporter("http://localhost:8181", null));
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws IOException {
        registry.close();
        registry = null;
    }

    @Test
    public void testLastValueExtraction() throws IOException, JMException, InterruptedException {
        registry.integer(StandardMeasurements.FREE_RAM).report(154L);
        Thread.sleep(300);//reporting is asynchronous
        testAttribute("longValue", TypeToken.of(Long.class), 154L, true);
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
