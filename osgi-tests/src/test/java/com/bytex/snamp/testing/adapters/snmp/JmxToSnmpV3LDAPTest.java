package com.bytex.snamp.testing.adapters.snmp;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.ExceptionalCallable;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.concurrent.SynchronizationEvent;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.connectors.notifications.Severity;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.SnmpTable;
import com.bytex.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connectors.jmx.TestOpenMBean;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

import javax.management.AttributeChangeNotification;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies({SnampFeature.SNMP_ADAPTER, SnampFeature.WRAPPED_LIBS})
public final class JmxToSnmpV3LDAPTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String ADAPTER_NAME = "snmp";
    private static final String SNMP_PORT = "3222";
    private static final String SNMP_HOST = "127.0.0.1";
    private static final String LDAP_ADMIN_USER = "uid=admin,ou=system";
    private static final String LDAP_ADMIN_PASSWORD = "1-2-3-4-5-password";
    private static final String LDAP_USER = "cn=Roman";
    private static final String ENGINE_ID = "80:00:13:70:01:7f:00:01:01:be:1e:8b:35";
    private EmbeddedADSVerTrunk ads;
    private File workDir;
    private final SnmpClient client;

    //ldapsearch -h 127.0.0.1 -p 10389 -w 1-2-3-4-5-password -D uid=admin,ou=system -b dc=ad,dc=microsoft,dc=com
    public JmxToSnmpV3LDAPTest() throws MalformedObjectNameException, IOException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME));
        client = SnmpClientFactory.createSnmpV3(ENGINE_ID,
                "udp:" + SNMP_HOST + "/" + SNMP_PORT, LDAP_USER,
                SecurityLevel.authPriv,
                EmbeddedADSVerTrunk.PASSWORD,
                AuthSHA.ID,
                EmbeddedADSVerTrunk.PRIVACY_KEY,
                PrivAES128.ID);
    }
    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Test
    public final void testForStringProperty() throws IOException, BundleException {
        try {
            final String valueToCheck = "SETTED VALUE";
            final OID attributeId = new OID("1.1.1.0");
            client.writeAttribute(attributeId, valueToCheck, String.class);
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, attributeId, String.class));
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET_BULK, attributeId, String.class));
        }
        finally {
            ResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public final void testForFloatProperty() throws IOException, BundleException {
        try {
            final float valueToCheck = 31.337F;
            final OID oid = new OID("1.1.8.0");
            client.writeAttribute(oid, valueToCheck, Float.class);
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, oid, Float.class), 0.000001);
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET_BULK, oid, Float.class), 0.000001);
        }
        finally {
            ResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public final void testForDatePropertyCustomDisplayFormat() throws IOException, BundleException {
        try {
            final Calendar cal = Calendar.getInstance();
            cal.set(1994, Calendar.APRIL, 5); // Kurt Donald Cobain, good night, sweet prince
            cal.set(Calendar.MILLISECOND, 0);
            final String valueToCheck = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(cal.getTime());
            final OID oid = new OID("1.1.9.0");
            client.writeAttribute(oid, valueToCheck, String.class);
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, oid, String.class));
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET_BULK, oid, String.class));
        }
        finally {
            ResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public final void testForDatePropertyRFC1903HumanReadable() throws IOException, BundleException {
        try {
            final Calendar cal = Calendar.getInstance();
            cal.set(1994, Calendar.APRIL, 5); // Kurt Donald Cobain, good night, sweet prince
            cal.set(Calendar.MILLISECOND, 0);
            final SnmpHelpers.DateTimeFormatter formatter = SnmpHelpers.createDateTimeFormatter("rfc1903-human-readable");
            final String valueToCheck = new String(formatter.convert(cal.getTime()));
            final OID oid = new OID("1.1.10.0");
            client.writeAttribute(oid, valueToCheck, String.class);
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, oid, String.class));
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET_BULK, oid, String.class));
        }
        finally {
            ResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public final void testForDatePropertyRFC1903() throws IOException, BundleException {
        try {
            final Calendar cal = Calendar.getInstance();
            cal.set(1994, Calendar.APRIL, 5); // Kurt Donald Cobain, good night, sweet prince
            cal.set(Calendar.MILLISECOND, 0);
            final SnmpHelpers.DateTimeFormatter formatter = SnmpHelpers.createDateTimeFormatter("rfc1903");
            final byte[] byteString = formatter.convert(cal.getTime());
            final OID oid = new OID("1.1.11.0");
            client.writeAttribute(oid, byteString, byte[].class);
            assertArrayEquals(byteString, client.readAttribute(ReadMethod.GET, oid, byte[].class));
            assertArrayEquals(byteString, client.readAttribute(ReadMethod.GET_BULK, oid, byte[].class));
        }
        finally {
            ResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public final void testForBooleanProperty() throws IOException, BundleException {
        try {
            final boolean valueToCheck = true;
            final OID oid = new OID("1.1.2.0");
            client.writeAttribute(oid, valueToCheck, Boolean.class);
            assertTrue(client.readAttribute(ReadMethod.GET, oid, Boolean.class));
            assertTrue(client.readAttribute(ReadMethod.GET_BULK, oid, Boolean.class));
        }
        finally {
            ResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public final void testForInt32Property() throws IOException, BundleException {
        try {
            final int valueToCheck = 42;
            final OID oid = new OID("1.1.3.0");
            client.writeAttribute(oid, valueToCheck, Integer.class);
            assertEquals(valueToCheck, (int) client.readAttribute(ReadMethod.GET, oid, Integer.class));
            assertEquals(valueToCheck, (int) client.readAttribute(ReadMethod.GET_BULK, oid, Integer.class));
        }
        finally {
            ResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public final void testForBigIntProperty() throws IOException, BundleException {
        try {
            final BigInteger valueToCheck = new BigInteger("100500");
            final OID oid = new OID("1.1.4.0");
            client.writeAttribute(oid, valueToCheck, BigInteger.class);
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, oid, BigInteger.class));
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET_BULK, oid, BigInteger.class));
        }
        finally {
            ResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public final void testForTableProperty() throws Exception {
        final SnmpTable table = new AbstractSnmpTable(Boolean.class, Integer.class, String.class) {
            private final ImmutableList<Variable[]> rows = ImmutableList.of(
                    new Variable[]{new Integer32(0), new Integer32(4230), new OctetString("Row #1")},
                    new Variable[]{new Integer32(1), new Integer32(4231), new OctetString("Row #2")},
                    new Variable[]{new Integer32(1), new Integer32(4232), new OctetString("Row #3")},
                    new Variable[]{new Integer32(1), new Integer32(4233), new OctetString("Row #4")}
            );

            @Override
            public int getRowCount() {
                return rows.size();
            }

            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public Variable getRawCell(final int columnIndex, final int rowIndex) {
                return rows.get(rowIndex)[columnIndex];
            }
        };
        client.writeTable("1.1.7.1", table);
        final SnmpTable result = client.readTable(ReadMethod.GET_BULK, new OID("1.1.7.1"),
                Boolean.class,
                Integer.class,
                String.class
        );
        assertEquals(4, result.getRowCount());
        assertEquals(false, result.getCell(0, 0));
        assertEquals(4230, result.getCell(1, 0));
        assertEquals("Row #1", result.getCell(2, 0));

        assertEquals(true, result.getCell(0, 3));
        assertEquals(4233, result.getCell(1, 3));
        assertEquals("Row #4", result.getCell(2, 3));
    }

    @Test
    public final void testForArrayProperty() throws Exception {
        SnmpTable array = new AbstractSnmpTable() {
            private final ImmutableList<Variable[]> rows = ImmutableList.of(
                    new Variable[]{new Integer32(20)},
                    new Variable[]{new Integer32(30)}
            );

            @Override
            public int getRowCount() {
                return rows.size();
            }

            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public Variable getRawCell(final int columnIndex, final int rowIndex) {
                return rows.get(rowIndex)[columnIndex];
            }
        };
        client.writeTable("1.1.5.1", array);
        array = client.readTable(ReadMethod.GET_BULK, new OID("1.1.5.1"), Integer.class);
        assertEquals(2, array.getRowCount());
        assertEquals(20, array.getCell(0, 0));
        assertEquals(30, array.getCell(0, 1));
    }

    @Test
    public final void testForDictionaryProperty() throws Exception {
        SnmpTable dict = new AbstractSnmpTable() {
            private final Variable[] row = {
                    new Integer32(0),
                    new Integer32(4230),
                    new OctetString("Test for dictionary property")
            };

            @Override
            public int getRowCount() {
                return 1;
            }

            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public Variable getRawCell(final int columnIndex, final int rowIndex) {
                return row[columnIndex];
            }
        };
        client.writeTable("1.1.6.1", dict);
        dict = client.readTable(ReadMethod.GET_BULK, new OID("1.1.6.1"),
                Boolean.class,
                Integer.class,
                String.class
        );
        assertEquals(1, dict.getRowCount());
        assertEquals(false, dict.getCell(0, 0));
        assertEquals(4230, dict.getCell(1, 0));
        assertEquals("Test for dictionary property", dict.getCell(2, 0));
    }

    @Test
    public final void notificationTest() throws IOException, TimeoutException, InterruptedException, BundleException {
        try {
            final SynchronizationEvent.EventAwaitor<SnmpNotification> awaitor1 = client.addNotificationListener(new OID("1.1.19.1"));
            final SynchronizationEvent.EventAwaitor<SnmpNotification> awaitor2 = client.addNotificationListener(new OID("1.1.20.1"));
            client.writeAttribute(new OID("1.1.1.0"), "NOTIFICATION TEST", String.class);
            final SnmpNotification p1 = awaitor1.await(TimeSpan.ofSeconds(4));
            final SnmpNotification p2 = awaitor2.await(TimeSpan.ofSeconds(4));
            assertNotNull(p1);
            assertNotNull(p2);
            assertEquals(Severity.NOTICE, p1.getSeverity());
            assertEquals(Severity.PANIC, p2.getSeverity());
            assertEquals(0L, p1.getSequenceNumber());
            assertEquals("Property string is changed", p1.getMessage());
            assertEquals("Property changed", p2.getMessage());
        }
        finally {
            ResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    private static void delete(final File f) throws IOException {
        if (f.isDirectory())
            for (final File c : ArrayUtils.emptyIfNull(f.listFiles(), File.class))
                delete(c);
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        workDir = new File( System.getProperty( "java.io.tmpdir" ) + "/server-work" );

        if (workDir.exists()) delete(workDir);
        //noinspection ResultOfMethodCallIgnored
        workDir.mkdirs();
        // Create the server
        ads = new EmbeddedADSVerTrunk( workDir );

        // optionally we can start a server too
        ads.startServer();
        super.beforeStartTest(context);
        beforeCleanupTest(context);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws BundleException, TimeoutException, InterruptedException {
        startResourceConnector(context);
        syncWithAdapterStartedEvent(ADAPTER_NAME, new ExceptionalCallable<Void, BundleException>() {
            @Override
            public Void call() throws BundleException {
                ResourceAdapterActivator.startResourceAdapter(context, ADAPTER_NAME);
                return null;
            }
        }, TimeSpan.ofSeconds(4));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        ResourceAdapterActivator.stopResourceAdapter(context, ADAPTER_NAME);
        stopResourceConnector(context);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        super.afterCleanupTest(context);
        ads.stopServer();
        workDir.delete();
        ads = null;
        workDir = null;
    }

    @Override
    protected void fillAdapters(final Map<String, AgentConfiguration.ResourceAdapterConfiguration> adapters, final Supplier<AgentConfiguration.ResourceAdapterConfiguration> adapterFactory) {
        final AgentConfiguration.ResourceAdapterConfiguration snmpAdapter = adapterFactory.get();
        snmpAdapter.setAdapterName(ADAPTER_NAME);
        snmpAdapter.getParameters().put("port", SNMP_PORT);
        snmpAdapter.getParameters().put("host", SNMP_HOST);
        snmpAdapter.getParameters().put("socketTimeout", "5000");
        snmpAdapter.getParameters().put("engineID", ENGINE_ID);
        snmpAdapter.getParameters().put("context", "1.1");
        adapters.put("test-snmp", snmpAdapter);
        snmpAdapter.getParameters().put("ldap-uri", "ldap://127.0.0.1:" + EmbeddedADSVerTrunk.SERVER_PORT);
        snmpAdapter.getParameters().put("ldap-user", LDAP_ADMIN_USER);
        snmpAdapter.getParameters().put("ldap-password", LDAP_ADMIN_PASSWORD);
        snmpAdapter.getParameters().put("ldap-auth-protocol", "simple");
        snmpAdapter.getParameters().put("ldap-base-dn", "dc=ad,dc=microsoft,dc=com");
        snmpAdapter.getParameters().put("ldap-user-search-filter", String.format("(%s)", LDAP_USER));
        snmpAdapter.getParameters().put("ldap-groups", "(&(objectclass=domain)(objectclass=top))");
    }

    @Override
    protected void fillEvents(final Map<String, AgentConfiguration.ManagedResourceConfiguration.EventConfiguration> events, final Supplier<AgentConfiguration.ManagedResourceConfiguration.EventConfiguration> eventFactory) {
        AgentConfiguration.ManagedResourceConfiguration.EventConfiguration event = eventFactory.get();
        event.setCategory(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);
        event.getParameters().put("receiverAddress", SNMP_HOST + "/" + client.getClientPort());
        event.getParameters().put("receiverName", "test-receiver-1");
        event.getParameters().put("oid", "1.1.19.1");
        events.put("19.1", event);

        event = eventFactory.get();
        event.setCategory("com.bytex.snamp.connectors.tests.impl.testnotif");
        event.getParameters().put("severity", "panic");
        event.getParameters().put("objectName", BEAN_NAME);
        event.getParameters().put("receiverAddress", SNMP_HOST + "/" + client.getClientPort());
        event.getParameters().put("receiverName", "test-receiver-2");
        event.getParameters().put("oid", "1.1.20.1");
        events.put("20.1", event);
    }

    @Override
    protected void fillAttributes(final Map<String, AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration> attributes, final Supplier<AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration> attributeFactory) {
        AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration attribute = attributeFactory.get();
        attribute.setAttributeName("string");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.1.0");
        attributes.put("1.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("boolean");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.2.0");
        attributes.put("2.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("int32");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.3.0");
        attributes.put("3.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("bigint");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.4.0");
        attributes.put("4.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("array");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.5.1");
        attributes.put("5.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("dictionary");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.6.1");
        attributes.put("6.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("table");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.7.1");
        attributes.put("7.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("float");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.8.0");
        attributes.put("8.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("displayFormat", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        attribute.getParameters().put("oid", "1.1.9.0");
        attributes.put("9.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("displayFormat", "rfc1903-human-readable");
        attribute.getParameters().put("oid", "1.1.10.0");
        attributes.put("10.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("displayFormat", "rfc1903");
        attribute.getParameters().put("oid", "1.1.11.0");
        attributes.put("11.0", attribute);
    }
}
