package com.itworks.snamp.testing.adapters.snmp;

import com.itworks.snamp.SimpleTable;
import com.itworks.snamp.SynchronizationEvent;
import com.itworks.snamp.Table;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.AbstractResourceAdapterActivator;
import com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.itworks.snamp.connectors.notifications.Severity;
import com.itworks.snamp.testing.SnampArtifact;
import com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.TestManagementBean;
import org.apache.commons.collections4.Factory;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.itworks.snamp.testing.connectors.jmx.TestManagementBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@ExamReactorStrategy(PerMethod.class)
public final class JmxToSnmpV3PasswordTest extends AbstractJmxConnectorTest<TestManagementBean> {
    private static final String ADAPTER_NAME = "snmp";
    private static final String SNMP_PORT = "3222";
    private static final String SNMP_HOST = "127.0.0.1";
    private static final String USER_NAME = "testuser";
    private static final String PASSWORD = "1-2-3-4-5-password";
    private static final SnmpClient client = SnmpClientFactory.createSnmpV3("udp:" + SNMP_HOST + "/" + SNMP_PORT, USER_NAME, SecurityLevel.authPriv);

    public JmxToSnmpV3PasswordTest() throws MalformedObjectNameException {
        super(new TestManagementBean(), new ObjectName(BEAN_NAME),
                SnampArtifact.SNMP4J.getReference(),
                SnampArtifact.SNMP_ADAPTER.getReference());
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        super.afterStartTest(context);
        AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        AbstractResourceAdapterActivator.startResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
    }

    @Test
    public final void testForStringProperty() throws IOException, BundleException {
        try {
            final String valueToCheck = "SETTED VALUE";
            final OID attributeId = new OID("1.1.1.0");
            client.writeAttribute(attributeId, valueToCheck, String.class);
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, attributeId, String.class));
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GETBULK, attributeId, String.class));
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public final void testForFloatProperty() throws IOException, BundleException {
        try {
            final float valueToCheck = 31.337F;
            final OID oid = new OID("1.1.8.0");
            client.writeAttribute(oid, valueToCheck, Float.class);
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, oid, Float.class), 0.000001);
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GETBULK, oid, Float.class), 0.000001);
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public final void testForDatePropertyCustomDisplayFormat() throws IOException, BundleException {
        try {
            final Calendar cal = Calendar.getInstance();
            cal.set(1994, Calendar.APRIL, 5); // Kurt Donald Cobain, good night, sweet prince
            final String valueToCheck = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(cal.getTime());
            final OID oid = new OID("1.1.9.0");
            client.writeAttribute(oid, valueToCheck, String.class);
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, oid, String.class));
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GETBULK, oid, String.class));
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public final void testForDatePropertyRFC1903HumanReadable() throws IOException, BundleException {
        try {
            final Calendar cal = Calendar.getInstance();
            cal.set(1994, Calendar.APRIL, 5); // Kurt Donald Cobain, good night, sweet prince
            final SnmpHelpers.DateTimeFormatter formatter = SnmpHelpers.createDateTimeFormatter("rfc1903-human-readable");
            final String valueToCheck = new String(formatter.convert(cal.getTime()));
            final OID oid = new OID("1.1.10.0");
            client.writeAttribute(oid, valueToCheck, String.class);
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, oid, String.class));
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GETBULK, oid, String.class));
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public final void testForDatePropertyRFC1903() throws IOException, BundleException {
        try {
            final Calendar cal = Calendar.getInstance();
            cal.set(1994, Calendar.APRIL, 5); // Kurt Donald Cobain, good night, sweet prince
            final SnmpHelpers.DateTimeFormatter formatter = SnmpHelpers.createDateTimeFormatter("rfc1903");
            final byte[] byteString = formatter.convert(cal.getTime());
            final OID oid = new OID("1.1.11.0");
            client.writeAttribute(oid, byteString, byte[].class);
            assertArrayEquals(byteString, client.readAttribute(ReadMethod.GET, oid, byte[].class));
            assertArrayEquals(byteString, client.readAttribute(ReadMethod.GETBULK, oid, byte[].class));
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public final void testForBooleanProperty() throws IOException, BundleException {
        try {
            final boolean valueToCheck = true;
            final OID oid = new OID("1.1.2.0");
            client.writeAttribute(oid, valueToCheck, Boolean.class);
            assertTrue(client.readAttribute(ReadMethod.GET, oid, Boolean.class));
            assertTrue(client.readAttribute(ReadMethod.GETBULK, oid, Boolean.class));
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public final void testForInt32Property() throws IOException, BundleException {
        try {
            final int valueToCheck = 42;
            final OID oid = new OID("1.1.3.0");
            client.writeAttribute(oid, valueToCheck, Integer.class);
            assertEquals(valueToCheck, (int) client.readAttribute(ReadMethod.GET, oid, Integer.class));
            assertEquals(valueToCheck, (int) client.readAttribute(ReadMethod.GETBULK, oid, Integer.class));
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public final void testForBigIntProperty() throws IOException, BundleException {
        try {
            final BigInteger valueToCheck = new BigInteger("100500");
            final OID oid = new OID("1.1.4.0");
            client.writeAttribute(oid, valueToCheck, BigInteger.class);
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, oid, BigInteger.class));
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GETBULK, oid, BigInteger.class));
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public final void testForTableProperty() throws Exception{
        try {
            Table<Integer> table = new SimpleTable<>(new HashMap<Integer, Class<?>>() {{
                put(2, Variable.class);//bool
                put(3, Variable.class);//int
                put(4, Variable.class);//str
            }});
            table.addRow(new HashMap<Integer, Object>() {{
                put(2, new Integer32(0));//false
                put(3, new Integer32(4230));
                put(4, new OctetString("Row #1"));
            }});
            table.addRow(new HashMap<Integer, Object>() {{
                put(2, new Integer32(1));//true
                put(3, new Integer32(4231));
                put(4, new OctetString("Row #2"));
            }});
            table.addRow(new HashMap<Integer, Object>() {{
                put(2, new Integer32(1));//true
                put(3, new Integer32(4232));
                put(4, new OctetString("Row #3"));
            }});
            table.addRow(new HashMap<Integer, Object>() {{
                put(2, new Integer32(1));//true
                put(3, new Integer32(4233));
                put(4, new OctetString("Row #4"));
            }});
            client.writeTable("1.1.7.1", table);
            table = client.readTable(ReadMethod.GETBULK, new OID("1.1.7.1"), new HashMap<Integer, Class<?>>() {{
                put(2, Boolean.class);//bool
                put(3, Integer.class);//int
                put(4, String.class);//str
            }});
            assertEquals(4, table.getRowCount());
            assertEquals(3, table.getColumns().size());

            assertEquals(false, table.getCell(2, 0));
            assertEquals(4230, table.getCell(3, 0));
            assertEquals("Row #1", table.getCell(4, 0));

            assertEquals(true, table.getCell(2, 3));
            assertEquals(4233, table.getCell(3, 3));
            assertEquals("Row #4", table.getCell(4, 3));
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public final void testForArrayProperty() throws Exception{
        try {
            Table<Integer> array = new SimpleTable<>(new HashMap<Integer, Class<?>>(1) {{
                put(2, Variable.class);
            }});
            array.addRow(new HashMap<Integer, Object>(2) {{
                put(2, new Integer32(20));
            }});
            array.addRow(new HashMap<Integer, Object>(2) {{
                put(2, new Integer32(30));
            }});
            client.writeTable("1.1.5.1", array);
            array = client.readTable(ReadMethod.GETBULK, new OID("1.1.5.1"), new HashMap<Integer, Class<?>>() {{
                put(2, Integer.class);
            }});
            assertEquals(2, array.getRowCount());
            assertEquals(1, array.getColumns().size());
            assertEquals(20, array.getCell(2, 0));
            assertEquals(30, array.getCell(2, 1));
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public final void testForDictionaryProperty() throws Exception{
        try {
            Table<Integer> dict = new SimpleTable<>(new HashMap<Integer, Class<?>>() {{
                put(2, Variable.class);
                put(3, Variable.class);
                put(4, Variable.class);
            }});
            dict.addRow(new HashMap<Integer, Object>() {{
                put(2, new Integer32(0));//false
                put(3, new Integer32(4230));
                put(4, new OctetString("Test for dictionary property"));
            }});
            client.writeTable("1.1.6.1", dict);
            dict = client.readTable(ReadMethod.GETBULK, new OID("1.1.6.1"), new HashMap<Integer, Class<?>>() {{
                put(2, Boolean.class);
                put(3, Integer.class);
                put(4, String.class);
            }});
            assertEquals(3, dict.getColumns().size());
            assertEquals(1, dict.getRowCount());
            assertEquals(false, dict.getCell(2, 0));
            assertEquals(4230, dict.getCell(3, 0));
            assertEquals("Test for dictionary property", dict.getCell(4, 0));
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public final void notificationTest() throws IOException, TimeoutException, InterruptedException, BundleException {
        try {
            final SynchronizationEvent.Awaitor<SnmpNotification> awaitor1 = client.addNotificationListener(new OID("1.1.19.1"));
            final SynchronizationEvent.Awaitor<SnmpNotification> awaitor2 = client.addNotificationListener(new OID("1.1.20.1"));
            client.writeAttribute(new OID("1.1.1.0"), "NOTIFICATION TEST", String.class);
            final SnmpNotification p1 = awaitor1.await(new TimeSpan(4, TimeUnit.MINUTES));
            final SnmpNotification p2 = awaitor2.await(new TimeSpan(4, TimeUnit.MINUTES));
            assertNotNull(p1);
            assertNotNull(p2);
            assertEquals(Severity.NOTICE, p1.getSeverity());
            assertEquals(Severity.PANIC, p2.getSeverity());
            assertEquals(0L, p1.getSequenceNumber());
            assertEquals("Property string is changed", p1.getMessage());
            assertEquals("Property changed", p2.getMessage());
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Override
    protected void fillAdapters(final Map<String, ResourceAdapterConfiguration> adapters, final Factory<ResourceAdapterConfiguration> adapterFactory) {
        final ResourceAdapterConfiguration snmpAdapter = adapterFactory.create();
        snmpAdapter.setAdapterName(ADAPTER_NAME);
        snmpAdapter.getHostingParams().put("port", SNMP_PORT);
        snmpAdapter.getHostingParams().put("host", SNMP_HOST);
        snmpAdapter.getHostingParams().put("socketTimeout", "5000");
        adapters.put("test-snmp", snmpAdapter);
        snmpAdapter.getHostingParams().put("snmpv3-groups", "group1; group2");
        //group1 setup
        snmpAdapter.getHostingParams().put("group1-security-level", "authPriv");
        snmpAdapter.getHostingParams().put("group1-access-rights", "read; write; notify");
        snmpAdapter.getHostingParams().put("group1-users", USER_NAME);
        snmpAdapter.getHostingParams().put(USER_NAME + "-password", PASSWORD);
        snmpAdapter.getHostingParams().put(USER_NAME + "-auth-protocol", "sha");
        snmpAdapter.getHostingParams().put(USER_NAME + "-privacy-key", "6-7-8-9-0-passphrase");
        snmpAdapter.getHostingParams().put(USER_NAME + "-privacy-protocol", "AES256");
        //group2 setup
        snmpAdapter.getHostingParams().put("group2-security-level", "authNoPriv");
        snmpAdapter.getHostingParams().put("group2-access-rights", "read");
        snmpAdapter.getHostingParams().put("group2-users", "testuser2");
        snmpAdapter.getHostingParams().put("testuser2-password", "1-2-3-4-5-password");
        snmpAdapter.getHostingParams().put("testuser2-auth-protocol", "sha");
    }

    @Override
    protected void fillEvents(final Map<String, EventConfiguration> events, final Factory<EventConfiguration> eventFactory) {
        EventConfiguration event = eventFactory.create();
        event.setCategory(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);
        event.getParameters().put("receiverAddress", SNMP_HOST + "/" + client.getClientPort());
        event.getParameters().put("receiverName", "test-receiver-1");
        event.getParameters().put("oid", "1.1.19.1");
        events.put("19.1", event);

        event = eventFactory.create();
        event.setCategory("com.itworks.snamp.connectors.tests.impl.testnotif");
        event.getParameters().put("severity", "panic");
        event.getParameters().put("objectName", BEAN_NAME);
        event.getParameters().put("receiverAddress", SNMP_HOST + "/" + client.getClientPort());
        event.getParameters().put("receiverName", "test-receiver-2");
        event.getParameters().put("oid", "1.1.20.1");
        events.put("20.1", event);
    }

    @Override
    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Factory<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attribute = attributeFactory.create();
        attribute.setAttributeName("string");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.1.0");
        attributes.put("1.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("boolean");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.2.0");
        attributes.put("2.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("int32");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.3.0");
        attributes.put("3.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("bigint");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.4.0");
        attributes.put("4.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("array");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.5.1");
        attributes.put("5.1", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("dictionary");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.6.1");
        attributes.put("6.1", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("table");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.7.1");
        attributes.put("7.1", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("float");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.8.0");
        attributes.put("8.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("displayFormat", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        attribute.getParameters().put("oid", "1.1.9.0");
        attributes.put("9.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("displayFormat", "rfc1903-human-readable");
        attribute.getParameters().put("oid", "1.1.10.0");
        attributes.put("10.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("displayFormat", "rfc1903");
        attribute.getParameters().put("oid", "1.1.11.0");
        attributes.put("11.0", attribute);
    }
}
