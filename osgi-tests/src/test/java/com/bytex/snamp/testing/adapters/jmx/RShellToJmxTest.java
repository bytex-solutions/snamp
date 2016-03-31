package com.bytex.snamp.testing.adapters.jmx;

import com.bytex.snamp.ExceptionalCallable;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.configuration.AgentConfiguration.EntityMap;
import com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.bytex.snamp.internal.OperatingSystem;
import com.bytex.snamp.jmx.CompositeDataUtils;
import com.bytex.snamp.scripting.OSGiScriptEngineManager;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connectors.rshell.AbstractRShellConnectorTest;
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
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;

import static com.bytex.snamp.testing.connectors.jmx.AbstractJmxConnectorTest.*;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@SnampDependencies(SnampFeature.JMX_ADAPTER)
public final class RShellToJmxTest extends AbstractRShellConnectorTest {
    private static final String ROOT_OBJECT_NAME = "com.bytex.snamp.testing:type=TestOpenMBean";
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
        syncWithAdapterStartedEvent(ADAPTER_NAME, new ExceptionalCallable<Void, BundleException>() {
            @Override
            public Void call() throws BundleException {
                ResourceAdapterActivator.startResourceAdapter(context, ADAPTER_NAME);
                return null;
            }
        }, TimeSpan.ofSeconds(20));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        ResourceAdapterActivator.stopResourceAdapter(context, ADAPTER_NAME);
        stopResourceConnector(context);
    }

    @Override
    protected void fillAdapters(final EntityMap<? extends ResourceAdapterConfiguration> adapters) {
        final ResourceAdapterConfiguration restAdapter = adapters.getOrAdd("test-jmx");
        restAdapter.setAdapterName(ADAPTER_NAME);
        restAdapter.getParameters().put("objectName", ROOT_OBJECT_NAME);
        restAdapter.getParameters().put("usePlatformMBean", Boolean.toString(isInTestContainer()));
        restAdapter.getParameters().put("dbgUsePureSerialization", "true");
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
    public void systemScriptEngineTest() throws IOException, ClassNotFoundException {
        final Set<String> factories = OSGiScriptEngineManager.getSystemScriptEngineFactories();
        assertTrue(factories.size() > 0);
        for (final String className : factories) {
            final Class<?> cls = Class.forName(className, true, getClass().getClassLoader());
            assertNotNull(cls);
            assertTrue(ScriptEngineFactory.class.isAssignableFrom(cls));
        }
    }

    @Test
    public void javaScriptEngineTest() throws IOException, ReflectiveOperationException, ScriptException {
        final OSGiScriptEngineManager engineManager = new OSGiScriptEngineManager(getTestBundleContext());
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
