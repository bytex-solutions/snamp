package com.bytex.snamp.testing.connectors.rshell;

import com.bytex.snamp.concurrent.FutureThread;
import com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.bytex.snamp.connectors.ManagedResourceConnector;
import com.bytex.snamp.connectors.attributes.AttributeSupport;
import com.bytex.snamp.internal.OperatingSystem;
import com.bytex.snamp.jmx.CompositeDataUtils;
import org.junit.Assume;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.JMException;
import javax.management.openmbean.CompositeData;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static com.bytex.snamp.configuration.AgentConfiguration.EntityMap;

/**
 * @author Roman Sakno
 * @version 1.2
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
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("ms");
        setFeatureName(attribute, getPathToFileInProjectRoot("freemem-tool-profile.xml"));
        attribute.getParameters().put("format", "-m");
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
                tables[i] = FutureThread.start(new Callable<Object>() {
                    @Override
                    public Object call() throws JMException {
                        return attributes.getAttribute("ms");
                    }
                });
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

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        stopResourceConnector(context);
        super.afterCleanupTest(context);
    }
}