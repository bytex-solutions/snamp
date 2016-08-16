package com.bytex.snamp.testing.gateway.jmx;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.internal.OperatingSystem;
import com.bytex.snamp.jmx.CompositeDataUtils;
import com.bytex.snamp.scripting.OSGiScriptEngineManager;
import com.bytex.snamp.testing.BundleExceptionCallable;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.rshell.AbstractRShellConnectorTest;
import com.google.common.collect.ImmutableMap;
import org.junit.Assume;
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
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.time.Duration;
import java.util.Hashtable;

import static com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest.*;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.JMX_GATEWAY)
public final class RShellToJmxTest extends AbstractRShellConnectorTest {
    private static final String ROOT_OBJECT_NAME = "com.bytex.snamp.testing:type=TestOpenMBean";
    private static final String USER_NAME = "Dummy";
    private static final String PASSWORD = "Password";
    private static final int PORT = 22000;
    private static final String FINGERPRINT = "e8:0d:af:84:bb:ec:05:03:b9:7c:f3:75:19:5a:2a:63";
    private static final String CERTIFICATE_FILE = "hostkey.ser";
    private static final String GATEWAY_NAME = "jmx";

    public RShellToJmxTest() {
        super(USER_NAME,
                PASSWORD,
                PORT,
                getPathToFileInProjectRoot(CERTIFICATE_FILE),
                FINGERPRINT);
    }

    private static ObjectName createObjectName() throws MalformedObjectNameException {
        final ObjectName root = new ObjectName(ROOT_OBJECT_NAME);
        final Hashtable<String, String> params = new Hashtable<>(root.getKeyPropertyList());
        params.put("resource", TEST_RESOURCE_NAME);
        return new ObjectName(root.getDomain(), params);
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        super.beforeStartTest(context);
        beforeCleanupTest(context);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        startResourceConnector(context);
        syncWithGatewayStartedEvent(GATEWAY_NAME, (BundleExceptionCallable) () -> {
                GatewayActivator.enableGateway(context, GATEWAY_NAME);
                return null;
        }, Duration.ofSeconds(20));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        GatewayActivator.disableGateway(context, GATEWAY_NAME);
        stopResourceConnector(context);
    }

    @Override
    protected void fillGateways(final EntityMap<? extends GatewayConfiguration> gateways) {
        final GatewayConfiguration httpGateway = gateways.getOrAdd("test-jmx");
        httpGateway.setType(GATEWAY_NAME);
        httpGateway.getParameters().put("objectName", ROOT_OBJECT_NAME);
        httpGateway.getParameters().put("usePlatformMBean", Boolean.toString(isInTestContainer()));
        httpGateway.getParameters().put("dbgUsePureSerialization", "true");
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        final AttributeConfiguration attr = attributes.getOrAdd("ms");
        setFeatureName(attr, getPathToFileInProjectRoot("freemem-tool-profile.xml"));
        attr.getParameters().put("format", "-m");
    }

    private Object readAttribute(final String attributeName) throws IOException, JMException {
        final String connectionString = String.format("service:jmx:rmi:///jndi/rmi://localhost:%s/karaf-root", JMX_KARAF_PORT);
        try (final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(connectionString), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))) {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName resourceObjectName = createObjectName();
            assertNotNull(connection.getMBeanInfo(resourceObjectName));
            assertNotNull(connection.getMBeanInfo(resourceObjectName).getAttributes().length > 0);
            return connection.getAttribute(resourceObjectName, attributeName);
        }
    }

    @Test
    public void javaScriptEngineTest() throws IOException, ReflectiveOperationException, ScriptException {
        final ScriptEngineManager engineManager = new OSGiScriptEngineManager(getTestBundleContext());
        final ScriptEngine javaScript = engineManager.getEngineByName("JavaScript");
        assertNotNull(javaScript);
        final Object result = javaScript.eval("function sayHelloWorld(){return 'Hello, world!';}; sayHelloWorld();");
        assertNotNull(result);
        assertEquals("Hello, world!", result.toString());
    }

    @Test
    public void readMemStatusTest() throws BundleException, IOException, JMException {
        Assume.assumeTrue(OperatingSystem.isLinux());
        final Object memStatus = readAttribute("ms");
        assertNotNull(memStatus);
        assertTrue(memStatus instanceof CompositeData);
        assertTrue(CompositeDataUtils.getLong(((CompositeData) memStatus), "total", 0L) > 0L);
        assertTrue(CompositeDataUtils.getLong(((CompositeData) memStatus), "used", 0L) > 0L);
        assertTrue(CompositeDataUtils.getLong(((CompositeData) memStatus), "free", 0L) > 0L);
    }
}
