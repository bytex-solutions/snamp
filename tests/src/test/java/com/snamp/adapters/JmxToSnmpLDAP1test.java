package com.snamp.adapters;

/**
 * @author Evgeniy Kirichenko
 */

import com.snamp.SimpleTable;
import com.snamp.SynchronizationEvent;
import com.snamp.Table;
import com.snamp.TimeSpan;
import com.snamp.configuration.EmbeddedAgentConfiguration;
import com.snamp.connectors.JmxConnectorTest;
import com.snamp.connectors.NotificationSupport;
import com.snamp.hosting.Agent;
import org.junit.Test;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

import javax.management.AttributeChangeNotification;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class JmxToSnmpLDAP1test extends JmxConnectorTest<TestManagementBean> {
    private static final String portForSNMP = "3222";
    private static final String addressForSNMP = "127.0.0.1";
    private static final String prefix = "1.1";
    private static final String username = "testuser";
    private static final String password = "1-2-3-4-5-password";
    private static final SnmpClient client = SnmpClientFactory.createSnmpV3("udp:" + addressForSNMP + "/" + portForSNMP, username, SecurityLevel.authPriv);
    private static EmbeddedADSVerTrunk ads;
    private static File workDir;

    private static final Map<String, String> snmpAdapterSettings = new HashMap<String, String>(2){{
        put(Adapter.PORT_PARAM_NAME, portForSNMP);
        put(Adapter.ADDRESS_PARAM_NAME, addressForSNMP);
        put("socketTimeout", "5000");
        put("snmpv3-groups", "group1, group2");
        put("ldap-uri", "ldap://127.0.0.1:" + EmbeddedADSVerTrunk.SERVER_PORT);
        //group1 setup
        put("group1-security-level", "authPriv");
        put("group1-access-rights", "read, write, notify");
        put("group1-users", username);
        put(username + "-password", password);
        put(username + "-auth-protocol", "snmp = sha, ldap = simple");
        put(username + "-privacy-key", "6-7-8-9-0-passphrase");
        put(username + "-privacy-protocol", "AES256");
        //group2 setup
        put("group2-security-level", "authNoPriv");
        put("group2-access-rights", "read");
        put("group2-users", "testuser2");
        put("testuser2-password", "1-2-3-4-5-password");
        put("testuser2-auth-protocol", "sha");
    }};
    private static final String BEAN_NAME = "com.snampy.jmx:type=com.snamp.adapters.TestManagementBean";

    public JmxToSnmpLDAP1test() throws MalformedObjectNameException {
        super("snmp", snmpAdapterSettings, new TestManagementBean(), new ObjectName(BEAN_NAME));
    }

    @Override
    protected String getAttributesNamespace() {
        return prefix;
    }

    @Override
    protected void afterAgentStart(final Agent agent) throws Exception{
        workDir = new File( System.getProperty( "java.io.tmpdir" ) + "/server-work" );
        if (workDir.exists()) workDir.delete();
        workDir.mkdirs();

        // Create the server
        ads = new EmbeddedADSVerTrunk( workDir );

        // optionally we can start a server too
        ads.startServer();

    }

    @Override
    protected void beforeAgentStop(final Agent agent) throws Exception{
        ads.stopServer();
        workDir.delete();
    }


    @Test
    public final void testForStringProperty() throws IOException, InterruptedException {
        final String valueToCheck = "SETTED VALUE";
        final OID oid = new OID(prefix + "." + "1.0");
        client.writeAttribute(oid, valueToCheck, String.class);
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, oid, String.class));
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GETBULK, oid, String.class));
    }


    @Test
    public final void testForArrayProperty() throws Exception{
        final String POSTFIX = "5.1";
        Table<Integer> array = new SimpleTable<>(new HashMap<Integer, Class<?>>(1){{
            put(2, Variable.class);
        }});
        array.addRow(new HashMap<Integer, Object>(2){{
            put(2, new Integer32(20));
        }});
        array.addRow(new HashMap<Integer, Object>(2){{
            put(2, new Integer32(30));
        }});
        client.writeTable(prefix + "." + POSTFIX, array);
        array = client.readTable(ReadMethod.GETBULK, new OID(prefix + "." + POSTFIX), new HashMap<Integer, Class<?>>(){{
            put(2, Integer.class);
        }});
        assertEquals(2, array.getRowCount());
        assertEquals(1, array.getColumns().size());
        assertEquals(20, array.getCell(2, 0));
        assertEquals(30, array.getCell(2, 1));
    }

    @Test
    public final void testForDictionaryProperty() throws Exception{
        final String POSTFIX = "6.1";
        Table<Integer> dict = new SimpleTable<>(new HashMap<Integer, Class<?>>(){{
            put(2, Variable.class);
            put(3, Variable.class);
            put(4, Variable.class);
        }});
        dict.addRow(new HashMap<Integer, Object>(){{
            put(2, new Integer32(0));//false
            put(3, new Integer32(4230));
            put(4, new OctetString("Test for dictionary property"));
        }});
        client.writeTable(prefix + "." + POSTFIX, dict);
        dict = client.readTable(ReadMethod.GETBULK, new OID(prefix + "." +POSTFIX), new HashMap<Integer, Class<?>>(){{
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

    @Test
    public final void notificationTest() throws IOException, TimeoutException, InterruptedException {
        final SynchronizationEvent.Awaitor<SnmpWrappedNotification> awaitor1 = client.addNotificationListener(new OID(prefix + ".19.1"));
        final SynchronizationEvent.Awaitor<SnmpWrappedNotification> awaitor2 = client.addNotificationListener(new OID(prefix + ".20.1"));
        client.writeAttribute(new OID(prefix + "." + "1.0"), "NOTIFICATION TEST", String.class);
        final SnmpWrappedNotification p1 = awaitor1.await(new TimeSpan(4, TimeUnit.MINUTES));
        final SnmpWrappedNotification p2 = awaitor2.await(new TimeSpan(4, TimeUnit.MINUTES));
        assertNotNull(p1);
        assertNotNull(p2);
        assertEquals(NotificationSupport.Notification.Severity.NOTICE, p1.getSeverity());
        assertEquals(NotificationSupport.Notification.Severity.PANIC, p2.getSeverity());
        assertEquals(0L, p1.getSequenceNumber());
        assertEquals("Property string is changed", p1.getMessage());
        assertEquals("Property changed", p2.getMessage());
    }

    @Test
    public final void testForTableProperty() throws Exception{
        final String POSTFIX = "7.1";
        Table<Integer> table = new SimpleTable<>(new HashMap<Integer, Class<?>>(){{
            put(2, Variable.class);//bool
            put(3, Variable.class);//int
            put(4, Variable.class);//str
        }});
        table.addRow(new HashMap<Integer, Object>(){{
            put(2, new Integer32(0));//false
            put(3, new Integer32(4230));
            put(4, new OctetString("Row #1"));
        }});
        table.addRow(new HashMap<Integer, Object>(){{
            put(2, new Integer32(1));//true
            put(3, new Integer32(4231));
            put(4, new OctetString("Row #2"));
        }});
        table.addRow(new HashMap<Integer, Object>(){{
            put(2, new Integer32(1));//true
            put(3, new Integer32(4232));
            put(4, new OctetString("Row #3"));
        }});
        table.addRow(new HashMap<Integer, Object>(){{
            put(2, new Integer32(1));//true
            put(3, new Integer32(4233));
            put(4, new OctetString("Row #4"));
        }});
        client.writeTable(prefix + "." + POSTFIX, table);
        table = client.readTable(ReadMethod.GETBULK, new OID(prefix + "." + POSTFIX), new HashMap<Integer, Class<?>>() {{
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

    @Test
    public final void testForBooleanProperty() throws IOException{
        final boolean valueToCheck = true;
        final OID oid = new OID(prefix + "." + "2.0");
        client.writeAttribute(oid, valueToCheck, Boolean.class);
        assertTrue(client.readAttribute(ReadMethod.GET, oid, Boolean.class));
        assertTrue(client.readAttribute(ReadMethod.GETBULK, oid, Boolean.class));
    }

    @Test
    public final void testForInt32Property() throws IOException{
        final int valueToCheck = 42;
        final OID oid = new OID(prefix + "." + "3.0");
        client.writeAttribute(oid, valueToCheck, Integer.class);
        assertEquals(valueToCheck, (int) client.readAttribute(ReadMethod.GET, oid, Integer.class));
        assertEquals(valueToCheck, (int) client.readAttribute(ReadMethod.GETBULK, oid, Integer.class));
    }

    @Test
    public final void testForBigIntProperty() throws IOException{
        final BigInteger valueToCheck = new BigInteger("100500");
        final OID oid = new OID(prefix + "." + "4.0");
        client.writeAttribute(oid, valueToCheck, BigInteger.class);
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, oid, BigInteger.class));
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GETBULK, oid, BigInteger.class));
    }

    @Test
    public final void testForFloatProperty() throws IOException{
        final float valueToCheck = 31.337F;
        final OID oid = new OID(prefix + "." + "8.0");
        client.writeAttribute(oid, valueToCheck, Float.class);
        assertEquals(valueToCheck, (float) client.readAttribute(ReadMethod.GET, oid, Float.class), 0.000001);
        assertEquals(valueToCheck, (float) client.readAttribute(ReadMethod.GETBULK, oid, Float.class), 0.000001);
    }

    @Test
    public final void testForDatePropertyCustomDisplayFormat() throws IOException{
        final Calendar cal = Calendar.getInstance(); cal.set(1994, 3, 5); // Kurt Donald Cobain, good night, sweet prince
        final String valueToCheck = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(cal.getTime());
        final OID oid = new OID(prefix + "." + "9.0");
        client.writeAttribute(oid, valueToCheck, String.class);
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, oid, String.class));
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GETBULK, oid, String.class));
    }

    @Test
    public final void testForDatePropertyRFC1903HumanReadable() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Calendar cal = Calendar.getInstance(); cal.set(1994, 3, 5); // Kurt Donald Cobain, good night, sweet prince
        final SnmpHelpers.DateTimeFormatter formatter = SnmpHelpers.createDateTimeFormatter("rfc1903-human-readable");
        final String valueToCheck = new String(formatter.convert(cal.getTime()));
        final OID oid = new OID(prefix + "." + "10.0");
        client.writeAttribute(oid, valueToCheck, String.class);
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, oid, String.class));
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GETBULK, oid, String.class));
    }

    @Test
    public final void testForDatePropertyRFC1903() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Calendar cal = Calendar.getInstance(); cal.set(1994, 3, 5); // Kurt Donald Cobain, good night, sweet prince
        final SnmpHelpers.DateTimeFormatter formatter = SnmpHelpers.createDateTimeFormatter("rfc1903");
        final byte[] byteString = formatter.convert(cal.getTime());
        final OID oid = new OID(prefix + "." + "11.0");
        client.writeAttribute(oid, byteString, byte[].class);
        assertArrayEquals(byteString, client.readAttribute(ReadMethod.GET, oid, byte[].class));
        assertArrayEquals(byteString, client.readAttribute(ReadMethod.GETBULK, oid, byte[].class));
    }


    @Override
    protected final void fillAttributes(final Map<String, ManagementTargetConfiguration.AttributeConfiguration> attributes) {
        EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration attribute = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("string");
        attribute.getAdditionalElements().put("objectName", BEAN_NAME);
        attributes.put("1.0", attribute);

        attribute = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("boolean");
        attribute.getAdditionalElements().put("objectName", BEAN_NAME);
        attributes.put("2.0", attribute);

        attribute = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("int32");
        attribute.getAdditionalElements().put("objectName", BEAN_NAME);
        attributes.put("3.0", attribute);

        attribute = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("bigint");
        attribute.getAdditionalElements().put("objectName", BEAN_NAME);
        attributes.put("4.0", attribute);

        attribute = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("array");
        attribute.getAdditionalElements().put("objectName", BEAN_NAME);
        attributes.put("5.1", attribute);

        attribute = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("dictionary");
        attribute.getAdditionalElements().put("objectName", BEAN_NAME);
        attributes.put("6.1", attribute);

        attribute = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("table");
        attribute.getAdditionalElements().put("objectName", BEAN_NAME);
        attributes.put("7.1", attribute);

        attribute = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("float");
        attribute.getAdditionalElements().put("objectName", BEAN_NAME);
        attributes.put("8.0", attribute);

        attribute = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("date");
        attribute.getAdditionalElements().put("objectName", BEAN_NAME);
        attribute.getAdditionalElements().put("displayFormat", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        attributes.put("9.0", attribute);

        attribute = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("date");
        attribute.getAdditionalElements().put("objectName", BEAN_NAME);
        attribute.getAdditionalElements().put("displayFormat", "rfc1903-human-readable");
        attributes.put("10.0", attribute);

        attribute = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("date");
        attribute.getAdditionalElements().put("objectName", BEAN_NAME);
        attribute.getAdditionalElements().put("displayFormat", "rfc1903");
        attributes.put("11.0", attribute);
    }

    @Override
    protected final void fillEvents(Map<String, ManagementTargetConfiguration.EventConfiguration> events) {
        EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedEventConfiguration event = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedEventConfiguration();
        event.setCategory(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getAdditionalElements().put("severity", "notice");
        event.getAdditionalElements().put("objectName", BEAN_NAME);
        event.getAdditionalElements().put("receiverAddress", addressForSNMP+"/"+client.getClientPort());
        event.getAdditionalElements().put("receiverName","test-receiver");
        events.put("19.1", event);

        event = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedEventConfiguration();
        event.setCategory("com.snamp.connectors.jmx.testnotif");
        event.getAdditionalElements().put("severity", "panic");
        event.getAdditionalElements().put("objectName", BEAN_NAME);
        event.getAdditionalElements().put("receiverAddress", addressForSNMP+"/"+client.getClientPort());
        event.getAdditionalElements().put("receiverName","test-receiver-2");
        events.put("20.1", event);
    }
}
