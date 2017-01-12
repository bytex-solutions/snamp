package com.bytex.snamp.testing.gateway.nagios;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.ConfigurationEntityDescription;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.gateway.Gateway;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.gateway.GatewayClient;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connector.jmx.TestOpenMBean;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.testing.connector.jmx.TestOpenMBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.NAGIOS_GATEWAY)
public final class NagiosGatewayTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String GATEWAY_NAME = "nagios";
    private static final String INSTANCE_NAME = "test-nagios";

    public NagiosGatewayTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME));
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        super.beforeStartTest(context);
        beforeCleanupTest(context);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        startResourceConnector(context);
        syncWithGatewayStartedEvent(GATEWAY_NAME, () -> {
                GatewayActivator.enableGateway(context, GATEWAY_NAME);
                return null;
        }, Duration.ofMinutes(4));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        GatewayActivator.disableGateway(context, GATEWAY_NAME);
        stopResourceConnector(context);
    }

    private static String getNagiosCheckResult(final String attributeID) throws IOException {
        final URL attributeQuery = new URL(String.format("http://localhost:8181/snamp/gateway/nagios/%s/attributes/%s/%s", INSTANCE_NAME, TEST_RESOURCE_NAME, attributeID));
        final HttpURLConnection connection = (HttpURLConnection)attributeQuery.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        try{
            assertEquals(200, connection.getResponseCode());
            final String attributeValue = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            assertNotNull(attributeValue);
            assertFalse(attributeValue.isEmpty());
            return attributeValue;
        }
        finally {
            connection.disconnect();
        }
    }

    @Test
    public void testStringAttribute() throws IOException, JMException {
        final ManagedResourceConnector connector = getManagementConnector();
        try {
            connector.setAttribute(new Attribute("1.0", "STRVAL"));
        }
        finally {
            releaseManagementConnector();
        }
        final String result = getNagiosCheckResult("1.0");
        assertEquals("stringService OK: STRVAL", result);
    }

    @Test
    public void testBooleanAttribute() throws IOException, JMException {
        final ManagedResourceConnector connector = getManagementConnector();
        try {
            connector.setAttribute(new Attribute("2.0", true));
        }
        finally {
            releaseManagementConnector();
        }
        final String result = getNagiosCheckResult("2.0");
        assertEquals("boolean OK: true", result);
    }

    @Test
    public void testIntAttribute() throws IOException, JMException {
        ManagedResourceConnector connector = getManagementConnector();
        try {
            connector.setAttribute(new Attribute("3.0", 20));
            String result = getNagiosCheckResult("3.0");
            assertEquals("memory OK: 20MB | 3.0=20MB;60;80;0;100", result);
            connector.setAttribute(new Attribute("3.0", 65));
            result = getNagiosCheckResult("3.0");
            assertEquals("memory WARNING: 65MB | 3.0=65MB;60;80;0;100", result);
            connector.setAttribute(new Attribute("3.0", 85));
            result = getNagiosCheckResult("3.0");
            assertEquals("memory CRITICAL: 85MB | 3.0=85MB;60;80;0;100", result);
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void configurationDescriptorTest() throws BundleException {
        final ConfigurationEntityDescription desc = GatewayClient.getConfigurationEntityDescriptor(getTestBundleContext(), GATEWAY_NAME, AttributeConfiguration.class);
        testConfigurationDescriptor(desc,
                "serviceName",
                "label",
                "criticalThreshold",
                "warningThreshold",
                "units",
                "maxValue",
                "minValue"
        );
    }

    @Test
    public void attributeBindingTest() throws TimeoutException, InterruptedException, ExecutionException {
        final GatewayClient client = new GatewayClient(getTestBundleContext(), INSTANCE_NAME, Duration.ofSeconds(2));
        try {
            assertTrue(client.forEachFeature(MBeanAttributeInfo.class, (resourceName, bindingInfo) -> bindingInfo.getProperty("path") instanceof String &&
                    bindingInfo.getProperty(Gateway.FeatureBindingInfo.MAPPED_TYPE) instanceof String));
        } finally {
            client.release(getTestBundleContext());
        }
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Override
    protected void fillGateways(final EntityMap<? extends GatewayConfiguration> gateways) {
        final GatewayConfiguration nagiosGateway = gateways.getOrAdd(INSTANCE_NAME);
        nagiosGateway.setType(GATEWAY_NAME);
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("1.0");
        attribute.setAlternativeName("string");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("serviceName", "stringService");

        attribute = attributes.getOrAdd("2.0");
        attribute.setAlternativeName("boolean");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("3.0");
        attribute.setAlternativeName("int32");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("serviceName", "memory");
        attribute.getParameters().put(DescriptorUtils.MAX_VALUE_FIELD, "100");
        attribute.getParameters().put(DescriptorUtils.MIN_VALUE_FIELD, "0");
        attribute.getParameters().put("criticalThreshold", "80");
        attribute.getParameters().put("warningThreshold", "60");
        attribute.getParameters().put(DescriptorUtils.UNIT_OF_MEASUREMENT_FIELD, "MB");
    }
}
