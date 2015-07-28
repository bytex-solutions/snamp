package com.bytex.snamp.testing.adapters.nagios;

import com.google.common.base.Supplier;
import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.ExceptionalCallable;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.adapters.ResourceAdapter;
import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.adapters.ResourceAdapterClient;
import com.bytex.snamp.configuration.ConfigurationEntityDescription;
import com.bytex.snamp.connectors.ManagedResourceConnector;
import com.bytex.snamp.internal.RecordReader;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connectors.jmx.TestOpenMBean;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import static com.bytex.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.NAGIOS_ADAPTER)
public final class NagiosAdapterTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String ADAPTER_NAME = "nagios";
    private static final String INSTANCE_NAME = "test-nagios";

    public NagiosAdapterTest() throws MalformedObjectNameException {
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
        syncWithAdapterStartedEvent(ADAPTER_NAME, new ExceptionalCallable<Void, BundleException>() {
            @Override
            public Void call() throws BundleException {
                ResourceAdapterActivator.startResourceAdapter(context, ADAPTER_NAME);
                return null;
            }
        }, TimeSpan.fromMinutes(4));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        ResourceAdapterActivator.stopResourceAdapter(context, ADAPTER_NAME);
        stopResourceConnector(context);
    }

    private static String getNagiosCheckResult(final String attributeID) throws IOException {
        final URL attributeQuery = new URL(String.format("http://localhost:8181/snamp/adapters/nagios/%s/attributes/%s/%s", INSTANCE_NAME, TEST_RESOURCE_NAME, attributeID));
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
        final ConfigurationEntityDescription desc = ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, AttributeConfiguration.class);
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
    public void attributeBindingTest() throws TimeoutException, InterruptedException {
        final ResourceAdapterClient client = new ResourceAdapterClient(getTestBundleContext(), INSTANCE_NAME, TimeSpan.fromSeconds(2));
        try {
            assertTrue(client.forEachFeature(MBeanAttributeInfo.class, new RecordReader<String, ResourceAdapter.FeatureBindingInfo<MBeanAttributeInfo>, ExceptionPlaceholder>() {
                @Override
                public boolean read(final String resourceName, final ResourceAdapter.FeatureBindingInfo<MBeanAttributeInfo> bindingInfo) {
                    return bindingInfo.getProperty("path") instanceof String &&
                            bindingInfo.getProperty(ResourceAdapter.FeatureBindingInfo.MAPPED_TYPE) instanceof String;
                }
            }));
        } finally {
            client.release(getTestBundleContext());
        }
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Override
    protected void fillAdapters(final Map<String, ResourceAdapterConfiguration> adapters, final Supplier<ResourceAdapterConfiguration> adapterFactory) {
        final ResourceAdapterConfiguration nagiosAdapter = adapterFactory.get();
        nagiosAdapter.setAdapterName(ADAPTER_NAME);
        adapters.put(INSTANCE_NAME, nagiosAdapter);
    }

    @Override
    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Supplier<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attribute = attributeFactory.get();
        attribute.setAttributeName("string");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("serviceName", "stringService");
        attributes.put("1.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("boolean");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("2.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("int32");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("serviceName", "memory");
        attribute.getParameters().put(DescriptorUtils.MAX_VALUE_FIELD, "100");
        attribute.getParameters().put(DescriptorUtils.MIN_VALUE_FIELD, "0");
        attribute.getParameters().put("criticalThreshold", "80");
        attribute.getParameters().put("warningThreshold", "60");
        attribute.getParameters().put(DescriptorUtils.UNIT_OF_MEASUREMENT_FIELD, "MB");
        attributes.put("3.0", attribute);
    }
}
