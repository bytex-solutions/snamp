package com.itworks.snamp.testing.adapters.jmx;

import com.google.common.base.Supplier;
import com.itworks.snamp.ExceptionalCallable;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.itworks.snamp.testing.SnampArtifact;
import com.itworks.snamp.testing.connectors.rshell.AbstractRShellConnectorTest;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class RShellToJmxTest extends AbstractRShellConnectorTest {
    private static final String ROOT_OBJECT_NAME = "com.itworks.snamp.testing:type=TestOpenMBean";
    private static final String USER_NAME = "Dummy";
    private static final String PASSWORD = "Password";
    private static final int PORT = 22000;
    private static final String FINGERPRINT = "e8:0d:af:84:bb:ec:05:03:b9:7c:f3:75:19:5a:2a:63";
    private static final String CERTIFICATE_FILE = "hostkey.ser";
    private static final String ADAPTER_NAME = "jmx";

    public RShellToJmxTest() {
        super(USER_NAME,
                PASSWORD,
                PORT,
                CERTIFICATE_FILE,
                FINGERPRINT,
                SnampArtifact.JMX_ADAPTER.getReference());
    }

    private static ObjectName createObjectName() throws MalformedObjectNameException {
        final ObjectName root = new ObjectName(ROOT_OBJECT_NAME);
        final Hashtable<String, String> params = new Hashtable<>(root.getKeyPropertyList());
        params.put("resource", TEST_RESOURCE_NAME);
        return new ObjectName(root.getDomain(), params);
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
        }, TimeSpan.fromSeconds(20));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        ResourceAdapterActivator.stopResourceAdapter(context, ADAPTER_NAME);
        stopResourceConnector(context);
    }

    @Override
    protected void fillAdapters(final Map<String, ResourceAdapterConfiguration> adapters, final Supplier<ResourceAdapterConfiguration> adapterFactory) {
        final ResourceAdapterConfiguration restAdapter = adapterFactory.get();
        restAdapter.setAdapterName(ADAPTER_NAME);
        restAdapter.getParameters().put("objectName", ROOT_OBJECT_NAME);
        restAdapter.getParameters().put("usePlatformMBean", "true");
        restAdapter.getParameters().put("dbgUsePureSerialization", "true");
        adapters.put("test-jmx", restAdapter);
    }

    @Override
    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Supplier<AttributeConfiguration> attributeFactory) {
        final AttributeConfiguration attr = attributeFactory.get();
        attr.setAttributeName("memStatus");
        attr.getParameters().put("commandProfileLocation", "freemem-tool-profile.xml");
        attr.getParameters().put("format", "-m");
        attributes.put("ms", attr);
    }

    private Object readAttribute(final String attributeName) throws IOException, JMException {
        final String jmxPort =
                System.getProperty("com.sun.management.jmxremote.port", "9010");
        final String connectionString = String.format("service:jmx:rmi:///jndi/rmi://localhost:%s/jmxrmi", jmxPort);
        try (final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(connectionString))) {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName resourceObjectName = createObjectName();
            assertNotNull(connection.getMBeanInfo(resourceObjectName));
            assertNotNull(connection.getMBeanInfo(resourceObjectName).getAttributes().length > 0);
            return connection.getAttribute(resourceObjectName, attributeName);
        }
    }

    @Test
    public void readMemStatusTest() throws BundleException, IOException, JMException {
        final Object memStatus = readAttribute("ms");
        assertNotNull(memStatus);
        assertTrue(memStatus instanceof CompositeData);
        assertTrue(((CompositeData) memStatus).get("total") instanceof Long);
        assertTrue(((CompositeData) memStatus).get("used") instanceof Long);
        assertTrue(((CompositeData) memStatus).get("free") instanceof Long);
    }
}