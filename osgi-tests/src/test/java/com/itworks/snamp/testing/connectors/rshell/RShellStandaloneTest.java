package com.itworks.snamp.testing.connectors.rshell;

import com.google.common.base.Supplier;
import com.itworks.snamp.concurrent.FutureThread;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.itworks.snamp.connectors.ManagedResourceConnector;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.CompositeDataUtils;
import org.junit.Assume;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.JMException;
import javax.management.openmbean.CompositeData;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * @author Roman Sakno
 * @version 1.0
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
    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Supplier<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attribute = attributeFactory.get();
        attribute.setAttributeName("memStatus");
        attribute.getParameters().put("commandProfileLocation", getPathToFileInProjectRoot("freemem-tool-profile.xml"));
        attribute.getParameters().put("format", "-m");
        attributes.put("ms", attribute);
    }

    @Test()
    public void loadTest() throws InterruptedException, ExecutionException, JMException {
        Assume.assumeTrue(Utils.IS_OS_LINUX);
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
        Assume.assumeTrue(Utils.IS_OS_LINUX);
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