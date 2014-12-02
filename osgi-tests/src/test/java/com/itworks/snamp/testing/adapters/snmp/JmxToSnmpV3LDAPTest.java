package com.itworks.snamp.testing.adapters.snmp;

import com.google.common.base.Supplier;
import com.itworks.snamp.SynchronizationEvent;
import com.itworks.snamp.mapping.Table;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.AbstractResourceAdapterActivator;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.connectors.notifications.Severity;
import com.itworks.snamp.testing.SnampArtifact;
import com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.TestOpenMBean;
import org.junit.Test;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.mapping.TableFactory.INTEGER_TABLE_FACTORY;
import static com.itworks.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;
import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class JmxToSnmpV3LDAPTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String ADAPTER_NAME = "snmp";
    private static final String SNMP_PORT = "3222";
    private static final String SNMP_HOST = "127.0.0.1";
    private static final String LDAP_ADMIN_USER = "uid=admin,ou=system";
    private static final String LDAP_ADMIN_PASSWORD = "1-2-3-4-5-password";
    private static final String LDAP_USER = "cn=Roman";
    private static EmbeddedADSVerTrunk ads;
    private static File workDir;
    private static final SnmpClient client = SnmpClientFactory.createSnmpV3("udp:" + SNMP_HOST + "/" + SNMP_PORT, LDAP_USER, SecurityLevel.authPriv);

    //ldapsearch -h 127.0.0.1 -p 10389 -w 1-2-3-4-5-password -D uid=admin,ou=system -b dc=ad,dc=microsoft,dc=com
    public JmxToSnmpV3LDAPTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME),
                SnampArtifact.SNMP4J.getReference(),
                SnampArtifact.SNMP_ADAPTER.getReference(),
                mavenBundle("org.apache.aries.jndi", "org.apache.aries.jndi", "1.0.0"),
                mavenBundle("org.apache.aries.jndi", "org.apache.aries.jndi.core", "1.0.0"),
                mavenBundle("org.apache.aries.jndi", "org.apache.aries.jndi.url", "1.0.0"),
                mavenBundle("org.apache.aries.jndi", "org.apache.aries.jndi.api", "1.0.0"),
                mavenBundle("org.apache.aries.jndi", "org.apache.aries.jndi.rmi", "1.0.0"),
                mavenBundle("org.apache.aries", "org.apache.aries.util", "1.0.0"),
                mavenBundle("org.apache.aries.proxy", "org.apache.aries.proxy.api", "1.0.0"),
                mavenBundle("net.engio", "mbassador", "1.1.10"),
                wrappedBundle(maven("org.apache.directory.server", "apacheds-all", "2.0.0-M16")).exports("org.apache.directory.*; version=2.0.0.16"));
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
            cal.set(Calendar.MILLISECOND, 0);
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
            cal.set(Calendar.MILLISECOND, 0);
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
            cal.set(Calendar.MILLISECOND, 0);
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
            Table<Integer> table = INTEGER_TABLE_FACTORY.create(new HashMap<Integer, Class<?>>() {{
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
            Table<Integer> array = INTEGER_TABLE_FACTORY.create(new HashMap<Integer, Class<?>>(1) {{
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
            Table<Integer> dict = INTEGER_TABLE_FACTORY.create(new HashMap<Integer, Class<?>>() {{
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

    private static void delete(final File f) throws IOException {
        if (f.isDirectory())
            for (final File c : f.listFiles())
                delete(c);
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        workDir = new File( System.getProperty( "java.io.tmpdir" ) + "/server-work" );

        if (workDir.exists()) delete(workDir);
        workDir.mkdirs();
        // Create the server
        ads = new EmbeddedADSVerTrunk( workDir );

        // optionally we can start a server too
        ads.startServer();
        super.beforeStartTest(context);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        super.afterStartTest(context);
        AbstractResourceAdapterActivator.startResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        stopResourceConnector(context);
        super.afterCleanupTest(context);
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        ads.stopServer();
        workDir.delete();
    }

    @Override
    protected void fillAdapters(final Map<String, AgentConfiguration.ResourceAdapterConfiguration> adapters, final Supplier<AgentConfiguration.ResourceAdapterConfiguration> adapterFactory) {
        final AgentConfiguration.ResourceAdapterConfiguration snmpAdapter = adapterFactory.get();
        snmpAdapter.setAdapterName(ADAPTER_NAME);
        snmpAdapter.getParameters().put("port", SNMP_PORT);
        snmpAdapter.getParameters().put("host", SNMP_HOST);
        snmpAdapter.getParameters().put("socketTimeout", "5000");
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
        event.setCategory("com.itworks.snamp.connectors.tests.impl.testnotif");
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
