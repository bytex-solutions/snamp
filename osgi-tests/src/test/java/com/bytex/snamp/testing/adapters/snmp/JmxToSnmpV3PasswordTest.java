package com.bytex.snamp.testing.adapters.snmp;

import com.bytex.snamp.ExceptionalCallable;
import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.configuration.AgentConfiguration.EntityMap;
import com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.bytex.snamp.connectors.notifications.Severity;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.SnmpTable;
import com.bytex.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connectors.jmx.TestOpenMBean;
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
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.bytex.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@SnampDependencies(SnampFeature.SNMP_ADAPTER)
public final class JmxToSnmpV3PasswordTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String ADAPTER_NAME = "snmp";
    private static final String SNMP_PORT = "3222";
    private static final String SNMP_HOST = "127.0.0.1";
    private static final String USER_NAME = "testuser";
    private static final String PASSWORD = "1-2-3-4-5-password";
    private static final String PRIVACY_KEY = "6-7-8-9-0-passphrase";
    private static final String ENGINE_ID = "80:00:13:70:01:7f:00:01:01:be:1e:8b:35";
    private final SnmpClient client;

    public JmxToSnmpV3PasswordTest() throws MalformedObjectNameException, IOException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME));
        client = SnmpClientFactory.createSnmpV3(ENGINE_ID,
                "udp:" + SNMP_HOST + "/" + SNMP_PORT, USER_NAME,
                SecurityLevel.authPriv,
                PASSWORD,
                AuthSHA.ID,
                // Oracle JRE does not support 256 by default, so for test compatibility the key in use has been changed to 128.
                // FYI: it DOES work with any other JRE, and either works with necessary Oracle JRE modules
                PRIVACY_KEY,
                PrivAES128.ID);
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        super.beforeStartTest(context);
        beforeCleanupTest(context);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception  {
        startResourceConnector(context);
        syncWithAdapterStartedEvent(ADAPTER_NAME, new ExceptionalCallable<Void, BundleException>() {
            @Override
            public Void call() throws BundleException {
                ResourceAdapterActivator.startResourceAdapter(context, ADAPTER_NAME);
                return null;
            }
        }, Duration.ofSeconds(4));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        ResourceAdapterActivator.stopResourceAdapter(context, ADAPTER_NAME);
        stopResourceConnector(context);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        try{
            client.close();
        }
        finally {
            super.afterCleanupTest(context);
        }
    }

    @Test
    public void testForStringProperty() throws IOException, InterruptedException {
        final String valueToCheck = "SETTED VALUE";
        final OID attributeId = new OID("1.1.1.0");
        client.writeAttribute(attributeId, valueToCheck, String.class);
        Thread.sleep(100);
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, attributeId, String.class));
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET_BULK, attributeId, String.class));
    }

    @Test
    public void testForFloatProperty() throws IOException, InterruptedException {
        final float valueToCheck = 31.337F;
        final OID oid = new OID("1.1.8.0");
        client.writeAttribute(oid, valueToCheck, Float.class);
        Thread.sleep(100);
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, oid, Float.class), 0.000001);
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET_BULK, oid, Float.class), 0.000001);
    }

    @Test
    public void testForDatePropertyCustomDisplayFormat() throws IOException, InterruptedException {
        final Calendar cal = Calendar.getInstance();
        cal.set(1994, Calendar.APRIL, 5); // Kurt Donald Cobain, good night, sweet prince
        cal.set(Calendar.MILLISECOND, 0);
        final String valueToCheck = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(cal.getTime());
        final OID oid = new OID("1.1.9.0");
        client.writeAttribute(oid, valueToCheck, String.class);
        Thread.sleep(100);
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, oid, String.class));
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET_BULK, oid, String.class));
    }

    @Test
    public void testForDatePropertyRFC1903HumanReadable() throws IOException, InterruptedException {
        final Calendar cal = Calendar.getInstance();
        cal.set(1994, Calendar.APRIL, 5); // Kurt Donald Cobain, good night, sweet prince
        cal.set(Calendar.MILLISECOND, 0);
        final SnmpHelpers.DateTimeFormatter formatter = SnmpHelpers.createDateTimeFormatter("rfc1903-human-readable");
        final String valueToCheck = new String(formatter.convert(cal.getTime()));
        final OID oid = new OID("1.1.10.0");
        client.writeAttribute(oid, valueToCheck, String.class);
        Thread.sleep(100);
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, oid, String.class));
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET_BULK, oid, String.class));
    }

    @Test
    public void testForDatePropertyRFC1903() throws IOException, InterruptedException {
        final Calendar cal = Calendar.getInstance();
        cal.set(1994, Calendar.APRIL, 5); // Kurt Donald Cobain, good night, sweet prince
        cal.set(Calendar.MILLISECOND, 0);
        final SnmpHelpers.DateTimeFormatter formatter = SnmpHelpers.createDateTimeFormatter("rfc1903");
        final byte[] byteString = formatter.convert(cal.getTime());
        final OID oid = new OID("1.1.11.0");
        client.writeAttribute(oid, byteString, byte[].class);
        Thread.sleep(100);
        assertArrayEquals(byteString, client.readAttribute(ReadMethod.GET, oid, byte[].class));
        assertArrayEquals(byteString, client.readAttribute(ReadMethod.GET_BULK, oid, byte[].class));
    }

    @Test
    public void testForBooleanProperty() throws IOException, InterruptedException {
        final boolean valueToCheck = true;
        final OID oid = new OID("1.1.2.0");
        client.writeAttribute(oid, valueToCheck, Boolean.class);
        Thread.sleep(100);
        assertTrue(client.readAttribute(ReadMethod.GET, oid, Boolean.class));
        assertTrue(client.readAttribute(ReadMethod.GET_BULK, oid, Boolean.class));
    }

    @Test
    public void testForInt32Property() throws IOException, InterruptedException {
        final int valueToCheck = 42;
        final OID oid = new OID("1.1.3.0");
        client.writeAttribute(oid, valueToCheck, Integer.class);
        Thread.sleep(100);
        assertEquals(valueToCheck, (int) client.readAttribute(ReadMethod.GET, oid, Integer.class));
        assertEquals(valueToCheck, (int) client.readAttribute(ReadMethod.GET_BULK, oid, Integer.class));
    }

    @Test
    public void testForBigIntProperty() throws IOException, InterruptedException {
        final BigInteger valueToCheck = new BigInteger("100500");
        final OID oid = new OID("1.1.4.0");
        client.writeAttribute(oid, valueToCheck, BigInteger.class);
        Thread.sleep(100);
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, oid, BigInteger.class));
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET_BULK, oid, BigInteger.class));
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Test
    public void testForTableProperty() throws Exception {
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
        Thread.sleep(100);
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
    public void testForArrayProperty() throws Exception {
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
        Thread.sleep(100);
        array = client.readTable(ReadMethod.GET_BULK, new OID("1.1.5.1"), Integer.class);
        assertEquals(2, array.getRowCount());
        assertEquals(20, array.getCell(0, 0));
        assertEquals(30, array.getCell(0, 1));
    }

    @Test
    public void testForDictionaryProperty() throws Exception {
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
        Thread.sleep(100);
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
    public void notificationTest() throws IOException, TimeoutException, InterruptedException, BundleException, ExecutionException {
        final Future<SnmpNotification> awaitor1 = client.addNotificationListener(new OID("1.1.19.1"));
        final Future<SnmpNotification> awaitor2 = client.addNotificationListener(new OID("1.1.20.1"));
        client.writeAttribute(new OID("1.1.1.0"), "NOTIFICATION TEST", String.class);
        final SnmpNotification p1 = awaitor1.get(10, TimeUnit.SECONDS);
        final SnmpNotification p2 = awaitor2.get(10, TimeUnit.SECONDS);
        assertNotNull(p1);
        assertNotNull(p2);
        assertEquals(Severity.NOTICE, p1.getSeverity());
        assertEquals(Severity.PANIC, p2.getSeverity());
        assertEquals(0L, p1.getSequenceNumber());
        assertEquals("Property string is changed", p1.getMessage());
        assertEquals("Property changed", p2.getMessage());
    }

    @Override
    protected void fillAdapters(final EntityMap<? extends ResourceAdapterConfiguration> adapters) {
        final ResourceAdapterConfiguration snmpAdapter = adapters.getOrAdd("test-snmp");
        snmpAdapter.setAdapterName(ADAPTER_NAME);
        snmpAdapter.getParameters().put("port", SNMP_PORT);
        snmpAdapter.getParameters().put("host", SNMP_HOST);
        snmpAdapter.getParameters().put("socketTimeout", "5000");
        snmpAdapter.getParameters().put("engineID", ENGINE_ID);
        snmpAdapter.getParameters().put("context", "1.1");
        snmpAdapter.getParameters().put("snmpv3-groups", "group1; group2");
        //group1 setup
        snmpAdapter.getParameters().put("group1-security-level", "authPriv");
        snmpAdapter.getParameters().put("group1-access-rights", "read; write; notify");
        snmpAdapter.getParameters().put("group1-users", USER_NAME);
        snmpAdapter.getParameters().put(USER_NAME + "-password", PASSWORD);
        snmpAdapter.getParameters().put(USER_NAME + "-auth-protocol", "sha");
        snmpAdapter.getParameters().put(USER_NAME + "-privacy-key", PRIVACY_KEY);
        // Oracle JRE does not support 256 by default, so for test compatibility the key in use has been changed to 128.
        // FYI: it DOES work with any other JRE, and either works with necessary Oracle JRE modules
        snmpAdapter.getParameters().put(USER_NAME + "-privacy-protocol", "AES128");
        //group2 setup
        snmpAdapter.getParameters().put("group2-security-level", "authNoPriv");
        snmpAdapter.getParameters().put("group2-access-rights", "read");
        snmpAdapter.getParameters().put("group2-users", "testuser2");
        snmpAdapter.getParameters().put("testuser2-password", PASSWORD);
        snmpAdapter.getParameters().put("testuser2-auth-protocol", "sha");
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd("19.1");
        setFeatureName(event, AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);
        event.getParameters().put("receiverAddress", SNMP_HOST + "/" + client.getClientPort());
        event.getParameters().put("receiverName", "test-receiver-1");
        event.getParameters().put("oid", "1.1.19.1");

        event = events.getOrAdd("20.1");
        setFeatureName(event, "com.bytex.snamp.connectors.tests.impl.testnotif");
        event.getParameters().put("severity", "panic");
        event.getParameters().put("objectName", BEAN_NAME);
        event.getParameters().put("receiverAddress", SNMP_HOST + "/" + client.getClientPort());
        event.getParameters().put("receiverName", "test-receiver-2");
        event.getParameters().put("oid", "1.1.20.1");
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("1.0");
        setFeatureName(attribute, "string");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.1.0");

        attribute = attributes.getOrAdd("2.0");
        setFeatureName(attribute, "boolean");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.2.0");

        attribute = attributes.getOrAdd("3.0");
        setFeatureName(attribute, "int32");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.3.0");

        attribute = attributes.getOrAdd("4.0");
        setFeatureName(attribute, "bigint");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.4.0");

        attribute = attributes.getOrAdd("5.1");
        setFeatureName(attribute, "array");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.5.1");

        attribute = attributes.getOrAdd("6.1");
        setFeatureName(attribute, "dictionary");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.6.1");

        attribute = attributes.getOrAdd("7.1");
        setFeatureName(attribute, "table");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.7.1");

        attribute = attributes.getOrAdd("8.0");
        setFeatureName(attribute, "float");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.8.0");

        attribute = attributes.getOrAdd("9.0");
        setFeatureName(attribute, "date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("displayFormat", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        attribute.getParameters().put("oid", "1.1.9.0");

        attribute = attributes.getOrAdd("10.0");
        setFeatureName(attribute, "date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("displayFormat", "rfc1903-human-readable");
        attribute.getParameters().put("oid", "1.1.10.0");

        attribute = attributes.getOrAdd("11.0");
        setFeatureName(attribute, "date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("displayFormat", "rfc1903");
        attribute.getParameters().put("oid", "1.1.11.0");
    }
}