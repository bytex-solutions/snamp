package com.bytex.snamp.testing.connector.rshell;

import com.bytex.snamp.concurrent.FutureThread;
import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.OperationConfiguration;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.metrics.AttributeMetric;
import com.bytex.snamp.connector.metrics.MetricsInterval;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.connector.operations.OperationSupport;
import com.bytex.snamp.internal.OperatingSystem;
import com.bytex.snamp.jmx.CompositeDataUtils;
import com.bytex.snamp.scripting.OSGiScriptEngineManager;
import com.google.common.collect.ImmutableList;
import org.junit.Assume;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.JMException;
import javax.management.openmbean.CompositeData;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.concurrent.ExecutionException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class RShellStandaloneTest extends AbstractRShellConnectorTest {
    private static final String USER_NAME = "Dummy";
    private static final String PASSWORD = "Password";
    private static final int PORT = 22000;
    private static final String FINGERPRINT = "e8:0d:af:84:bb:ec:05:03:b9:7c:f3:75:19:5a:2a:63";
    private static final String CERTIFICATE_FILE = "hostkey.ser";

    public RShellStandaloneTest() {
        super(USER_NAME,
                PASSWORD,
                PORT,
                getPathToFileInProjectRoot(CERTIFICATE_FILE),
                FINGERPRINT);
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        // Linux operation
        attributes.getOrAdd("ms").setAlternativeName(getPathToFileInProjectRoot("freemem-tool-profile.xml"));
        attributes.getOrAdd("ms").getParameters().put("format", "-m");
        // Windows operation
        attributes.getOrAdd("ms_win").setAlternativeName(getPathToFileInProjectRoot("virtual-mem-windows.xml"));
    }


    @Override
    protected void fillOperations(EntityMap<? extends OperationConfiguration> operations) {
        operations.getOrAdd("space_on_disk").setAlternativeName(getPathToFileInProjectRoot("browse-folder-windows.xml"));
    }

    @Test()
    public void loadTest() throws InterruptedException, ExecutionException, JMException {
        Assume.assumeTrue(OperatingSystem.isLinux());
        final ManagedResourceConnector connector = getManagementConnector();
        assertNotNull(connector);
        try {
            final AttributeSupport attributes = connector.queryObject(AttributeSupport.class);
            assertNotNull(attributes);
            @SuppressWarnings("unchecked")
            final FutureThread<Object>[] tables = new FutureThread[10];
            for (int i = 0; i < tables.length; i++)
                tables[i] = FutureThread.start(() -> attributes.getAttribute("ms"));
            for (final FutureThread<Object> thread : tables) {
                final Object table = thread.get();
                assertNotNull(table);
                assertTrue(table instanceof CompositeData);
                assertTrue(CompositeDataUtils.getLong((CompositeData) table, "total", 0L) > 0L);
                assertTrue(CompositeDataUtils.getLong((CompositeData) table, "used", 0L) > 0L);
                assertTrue(CompositeDataUtils.getLong((CompositeData) table, "free", 0L) > 0L);
            }
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void testForMetrics() throws Exception {
        final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(getTestBundleContext(), TEST_RESOURCE_NAME);
        try{
            final MetricsSupport metrics = client.queryObject(MetricsSupport.class);
            assertNotNull(metrics);
            assertTrue(metrics.getMetrics(AttributeMetric.class).iterator().hasNext());
            //read and write attributes
            readMemStatusAttribute();
            //verify metrics
            final AttributeMetric attrMetrics = metrics.getMetrics(AttributeMetric.class).iterator().next();
            assertTrue(attrMetrics.reads().getLastRate(MetricsInterval.HOUR) > 0);
            assertTrue(attrMetrics.reads().getTotalRate() > 0);
        } finally {
            client.release(getTestBundleContext());
        }
    }

    @Test
    public void readMemStatusAttribute() throws JMException {
        Assume.assumeTrue(OperatingSystem.isLinux());
        final ManagedResourceConnector connector = getManagementConnector();
        assertNotNull(connector);
        try {
            final AttributeSupport attributes = connector.queryObject(AttributeSupport.class);
            assertNotNull(attributes);
            final Object dict = attributes.getAttribute("ms");
            assertNotNull(dict);
            assertTrue(dict instanceof CompositeData);
            assertTrue(CompositeDataUtils.getLong((CompositeData) dict, "total", 0L) > 0L);
            assertTrue(CompositeDataUtils.getLong((CompositeData) dict, "used", 0L) > 0L);
            assertTrue(CompositeDataUtils.getLong((CompositeData) dict, "free", 0L) > 0L);
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void readMemStatusAttributeWindows() throws JMException, ScriptException {
        Assume.assumeTrue(OperatingSystem.isWindows());
        final ManagedResourceConnector connector = getManagementConnector();
        assertNotNull(connector);
        try {
            final AttributeSupport attributes = connector.queryObject(AttributeSupport.class);
            assertNotNull(attributes);
            final Object dict = attributes.getAttribute("ms_win");
            assertNotNull(dict);
            assertTrue(dict instanceof CompositeData);
            assertTrue(CompositeDataUtils.getLong((CompositeData) dict, "total", 0L) > 0L);
            assertTrue(CompositeDataUtils.getLong((CompositeData) dict, "used", 0L) > 0L);
            assertTrue(CompositeDataUtils.getLong((CompositeData) dict, "free", 0L) > 0L);
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void browseSomeDirectoryWindows() throws JMException, ScriptException {
        Assume.assumeTrue(OperatingSystem.isWindows());
        final ManagedResourceConnector connector = getManagementConnector();
        assertNotNull(connector);
        try {
            final OperationSupport operationSupport = connector.queryObject(OperationSupport.class);
            assertNotNull(operationSupport);
            Object operationResult = operationSupport.invoke("space_on_disk",
                    ImmutableList.of("c:").toArray(),
                    ImmutableList.of(String.class.getName()).toArray(new String[1]));
            assertNotNull(operationResult);
            assertTrue(operationResult instanceof CompositeData);
            assertTrue(CompositeDataUtils.getLong((CompositeData) operationResult, "free", 0L) > 0L);
            assertTrue(CompositeDataUtils.getLong((CompositeData) operationResult, "total", 0L) > 0L);
            assertTrue(CompositeDataUtils.getLong((CompositeData) operationResult, "available", 0L) > 0L);

             operationResult = operationSupport.invoke("space_on_disk",
                    ImmutableList.of("d:").toArray(),
                    ImmutableList.of(String.class.getName()).toArray(new String[1]));
            assertNotNull(operationResult);
            assertTrue(operationResult instanceof CompositeData);
            assertTrue(CompositeDataUtils.getLong((CompositeData) operationResult, "free", 0L) > 0L);
            assertTrue(CompositeDataUtils.getLong((CompositeData) operationResult, "total", 0L) > 0L);
            assertTrue(CompositeDataUtils.getLong((CompositeData) operationResult, "available", 0L) > 0L);
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        stopResourceConnector(context);
        super.afterCleanupTest(context);
    }
}