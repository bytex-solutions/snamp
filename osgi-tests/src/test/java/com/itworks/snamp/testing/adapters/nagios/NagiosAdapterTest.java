package com.itworks.snamp.testing.adapters.nagios;

import com.google.common.base.Supplier;
import com.itworks.snamp.ExceptionalCallable;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.adapters.ResourceAdapterClient;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.connectors.ManagedResourceConnector;
import com.itworks.snamp.io.IOUtils;
import com.itworks.snamp.jmx.DescriptorUtils;
import com.itworks.snamp.testing.SnampDependencies;
import com.itworks.snamp.testing.SnampFeature;
import com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.TestOpenMBean;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.Attribute;
import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import static com.itworks.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies({SnampFeature.NAGIOS_ADAPTER})
public final class NagiosAdapterTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String ADAPTER_NAME = "nagios";
    private static final String ADAPTER_INSTANCE = "test-nagios";

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
        final URL attributeQuery = new URL(String.format("http://localhost:8181/snamp/adapters/nagios/%s/attributes/%s/%s", ADAPTER_INSTANCE, TEST_RESOURCE_NAME, attributeID));
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
        assertNotNull(desc);
        final ConfigurationEntityDescription.ParameterDescription param = desc.getParameterDescriptor("warningThreshold");
        assertNotNull(param);
        assertFalse(param.getDescription(null).isEmpty());
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Override
    protected void fillAdapters(final Map<String, ResourceAdapterConfiguration> adapters, final Supplier<ResourceAdapterConfiguration> adapterFactory) {
        final ResourceAdapterConfiguration nagiosAdapter = adapterFactory.get();
        nagiosAdapter.setAdapterName(ADAPTER_NAME);
        adapters.put(ADAPTER_INSTANCE, nagiosAdapter);
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
