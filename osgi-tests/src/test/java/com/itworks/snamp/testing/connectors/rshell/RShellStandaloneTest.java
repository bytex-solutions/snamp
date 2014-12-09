package com.itworks.snamp.testing.connectors.rshell;

import com.itworks.snamp.FutureThread;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.ManagedResourceConnector;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.attributes.AttributeSupportException;
import com.itworks.snamp.connectors.attributes.UnknownAttributeException;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.mapping.RecordSetUtils;
import com.itworks.snamp.mapping.TypeLiterals;
import org.junit.Assume;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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
                CERTIFICATE_FILE,
                FINGERPRINT);
    }

    @Test()
    public void loadTest() throws InterruptedException, ExecutionException, AttributeSupportException {
        Assume.assumeTrue(Utils.IS_OS_LINUX);
        final ManagedResourceConnector<?> connector = getManagementConnector();
        assertNotNull(connector);
        final AttributeSupport attributes = connector.queryObject(AttributeSupport.class);
        assertNotNull(attributes);
        assertNotNull(attributes.connectAttribute("ms", "memStatus", new HashMap<String, String>(1) {{
            put("commandProfileLocation", "freemem-tool-profile.xml");
            put("format", "-m");
        }}));
        @SuppressWarnings("unchecked")
        final FutureThread<Object>[] tables = new FutureThread[10];
        for (int i = 0; i < tables.length; i++)
            tables[i] = FutureThread.start(new Callable<Object>() {
                @Override
                public Object call() throws TimeoutException, AttributeSupportException, UnknownAttributeException {
                    return attributes.getAttribute("ms", TimeSpan.INFINITE);
                }
            });
        for (final FutureThread<Object> thread : tables) {
            final Object table = thread.get();
            assertNotNull(table);
            assertTrue(TypeLiterals.isInstance(table, TypeLiterals.NAMED_RECORD_SET));
            final Map<String, ?> map = RecordSetUtils.toMap(TypeLiterals.cast(table, TypeLiterals.NAMED_RECORD_SET));
            assertTrue(map.get("total") instanceof Long);
            assertTrue(map.get("used") instanceof Long);
            assertTrue(map.get("free") instanceof Long);
        }
    }

    @Test
    public void readMemStatusAttribute() throws TimeoutException, AttributeSupportException, UnknownAttributeException {
        Assume.assumeTrue(Utils.IS_OS_LINUX);
        final ManagedResourceConnector<?> connector = getManagementConnector();
        assertNotNull(connector);
        final AttributeSupport attributes = connector.queryObject(AttributeSupport.class);
        assertNotNull(attributes);
        assertNotNull(attributes.connectAttribute("ms", "memStatus", new HashMap<String, String>(1) {{
            put("commandProfileLocation", "freemem-tool-profile.xml");
            put("format", "-m");
        }}));
        final Object dict = attributes.getAttribute("ms", TimeSpan.INFINITE);
        assertNotNull(dict);
        assertTrue(TypeLiterals.isInstance(dict, TypeLiterals.NAMED_RECORD_SET));
        final Map<String, ?> m = RecordSetUtils.toMap(TypeLiterals.cast(dict, TypeLiterals.NAMED_RECORD_SET));
        assertTrue(m.get("total") instanceof Long);
        assertTrue(m.get("used") instanceof Long);
        assertTrue(m.get("free") instanceof Long);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        stopResourceConnector(context);
        super.afterCleanupTest(context);
    }
}