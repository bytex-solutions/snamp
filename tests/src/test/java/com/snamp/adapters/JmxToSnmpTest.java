package com.snamp.adapters;

/**
 * @author Evgeniy Kirichenko
 */

import com.snamp.SimpleTable;
import com.snamp.SynchronizationEvent;
import com.snamp.Table;
import com.snamp.TimeSpan;
import com.snamp.connectors.JmxConnectorTest;
import com.snamp.connectors.NotificationSupport;
import com.snamp.hosting.EmbeddedAgentConfiguration;
import org.junit.Test;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;

import javax.management.AttributeChangeNotification;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JmxToSnmpTest extends JmxConnectorTest<TestManagementBean> {
    private static final String portForSNMP = "3222";
    private static final String addressForSNMP = "127.0.0.1";
    private static final String prefix = "1.1";
    private static final SNMPManager client =  new SNMPManager("udp:"+addressForSNMP+"/"+portForSNMP);


    private static final Map<String, String> snmpAdapterSettings = new HashMap<String, String>(2){{
        put(Adapter.PORT_PARAM_NAME, portForSNMP);
        put(Adapter.ADDRESS_PARAM_NAME, addressForSNMP);
    }};
    private static final String BEAN_NAME = "com.snampy.jmx:type=com.snamp.adapters.TestManagementBean";

    public JmxToSnmpTest() throws MalformedObjectNameException {
        super("snmp", snmpAdapterSettings, new TestManagementBean(), new ObjectName(BEAN_NAME));
    }

    @Override
    protected String getAttributesNamespace() {
        return prefix;
    }

    private static <T> T deserialize(final Variable var, final Class<T> className){
        final Object result;
        if (var instanceof UnsignedInteger32 || var instanceof Integer32)
            result = (className == Boolean.class)?(var.toInt() == 1):var.toInt();
        else if (var instanceof OctetString)
        {
            if (className == BigInteger.class)
                result = new BigInteger(var.toString());
            else if (className == Float.class)
                result = Float.valueOf(var.toString());
            else if (className == byte[].class)
                result = ((OctetString) var).toByteArray();
            else
                result = var.toString();
        }
        else if (var instanceof IpAddress)
            result = var.toString();
        else if (var instanceof Counter64)
            result = var.toLong();
        else result = null;
        return className.cast(result);
    }

    private <T>T readAttribute(final SNMPManager.ReadMethod method, final String postfix, final Class<T> className) throws IOException {
        final ResponseEvent value = client.get(method, new OID[]{new OID(prefix + "." + postfix)});
        assertNotNull(value);
        return deserialize(value.getResponse().getVariable(new OID(prefix + "." + postfix)), className);

    }

    private Table<Integer> readTable(final SNMPManager.ReadMethod method, final String postfix, final Map<Integer, Class<?>> columns) throws Exception {
        final Table<Integer> table = new SimpleTable<>(columns);
        final Collection<Variable[]> rows = client.getTable(method, new OID(prefix + "." + postfix), columns.size());
        for(final Variable[] row: rows)
            table.addRow(new HashMap<Integer, Object>(){{
                for(int i = 0; i < row.length; i++){
                    final Integer column = new Integer(i + 2);
                    put(column, deserialize(row[i], columns.get(column)));
                }
            }});
        return table;
    }

    private void writeTable(final String postfix, final Table<Integer> table) throws IOException {
        final PDU pdu = new PDU();
        pdu.setType(PDU.SET);
        //add rows
        for(int i = 0; i < table.getRowCount(); i++){
            //iterate through each column
            final Integer rowIndex = i;
            for(final Integer column: table.getColumns()){
                final OID rowId = new OID(prefix + "." + postfix + "." + column + "." + (rowIndex + 1));
                pdu.add(new VariableBinding(rowId, (Variable)table.getCell(column, rowIndex)));
            }
        }
        final PDU response = client.set(pdu).getResponse();
        assertEquals(response.getErrorStatusText(), SnmpConstants.SNMP_ERROR_SUCCESS, response.getErrorStatus());
    }

    private <T> void writeAttribute(final String postfix, final T value, final Class<T> valueType) throws IOException{
        final PDU pdu = new PDU();
        // Setting the Oid and Value for sysContact variable
        final OID oid = new OID(prefix + "." +postfix);
        final Variable var;

        if (valueType == int.class || valueType == Integer.class || valueType == short.class)
        {
             var = new Integer32(Integer.class.cast(value));
        }
        else if (valueType == long.class || valueType == Long.class)
        {
             var = new Counter64(Long.class.cast(value));
        }
        else if (valueType == Boolean.class || valueType == boolean.class)
        {
            var = new Integer32((Boolean.class.cast(value) == Boolean.TRUE)?1:0);
        }
        else if (valueType == byte[].class)
        {
            var = new OctetString((byte[])value);
        }
        else
        {
            var = new OctetString(value.toString());
        }

        final VariableBinding varBind = new VariableBinding(oid,var);

        pdu.add(varBind);
        pdu.setType(PDU.SET);

        final ResponseListener listener = new ResponseListener() {
            public void onResponse(ResponseEvent event) {
                final PDU strResponse;
                final String result;
                ((Snmp)event.getSource()).cancel(event.getRequest(), this);
                strResponse = event.getResponse();
                if (strResponse!= null) {
                    result = strResponse.getErrorStatusText();
                    System.out.println("Set Status is: "+result);
                }
                assertNotNull(strResponse);
            }};

        client.set(pdu, listener);
    }


    /**
     * To check the attribute you need use following commands:
     *
     * snmpset -c public -v 2c 127.0.0.1:3222 iso.1.1.0 s ddd
     * snmpwalk -Os -c public -v 2c 127.0.0.1:3222 1
     *
     *
     * To check it via JMX Bean you can use jConsole:
     *
     * service:jmx:rmi:///jndi/rmi://localhost:3334/jmxrmi

     * @throws IOException
     */
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
        writeTable(POSTFIX, array);
        array = readTable(SNMPManager.ReadMethod.GETBULK, POSTFIX, new HashMap<Integer, Class<?>>(){{
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
        writeTable(POSTFIX, dict);
        dict = readTable(SNMPManager.ReadMethod.GETBULK, POSTFIX, new HashMap<Integer, Class<?>>(){{
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
    public final void testForStringProperty() throws IOException {
        final String valueToCheck = "SETTED VALUE";
        writeAttribute("1.0", valueToCheck, String.class);
        assertEquals(valueToCheck, readAttribute(SNMPManager.ReadMethod.GET, "1.0", String.class));
        assertEquals(valueToCheck, readAttribute(SNMPManager.ReadMethod.GETBULK, "1.0", String.class));
    }

    @Test
    public final void notificationTest() throws IOException, TimeoutException, InterruptedException {
        final SynchronizationEvent.Awaitor<SnmpWrappedNotification> awaitor1 = client.addNotificationListener(new OID(prefix + ".19.1"));
        //final SynchronizationEvent.Awaitor<SnmpWrappedNotification> awaitor2 = client.addNotificationListener(new OID(prefix + ".20.1"));
        writeAttribute("1.0", "NOTIFICATION TEST", String.class);
        final SnmpWrappedNotification p1 = awaitor1.await(new TimeSpan(4, TimeUnit.MINUTES));
        //final SnmpWrappedNotification p2 = awaitor2.await(new TimeSpan(4, TimeUnit.SECONDS));
        assertNotNull(p1);
        assertEquals(NotificationSupport.Notification.Severity.NOTICE, p1.getSeverity());
        //assertEquals(NotificationSupport.Notification.Severity.PANIC, p2.getSeverity());
        assertEquals(0L, p1.getSequenceNumber());
        assertEquals("Property string is changed", p1.getMessage());
        //assertEquals("Property string is changed", p2.getMessage());
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
        writeTable(POSTFIX, table);
        table = readTable(SNMPManager.ReadMethod.GETBULK, POSTFIX, new HashMap<Integer, Class<?>>(){{
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
        writeAttribute("2.0", valueToCheck, Boolean.class);
        assertTrue(readAttribute(SNMPManager.ReadMethod.GET, "2.0", Boolean.class));
        assertTrue(readAttribute(SNMPManager.ReadMethod.GETBULK, "2.0", Boolean.class));
    }

    @Test
    public final void testForInt32Property() throws IOException{
        final int valueToCheck = 42;
        writeAttribute("3.0", valueToCheck, Integer.class);
        assertEquals(valueToCheck, (int) readAttribute(SNMPManager.ReadMethod.GET, "3.0", Integer.class));
        assertEquals(valueToCheck, (int) readAttribute(SNMPManager.ReadMethod.GETBULK, "3.0", Integer.class));
    }

    @Test
    public final void testForBigIntProperty() throws IOException{
        final BigInteger valueToCheck = new BigInteger("100500");
        writeAttribute("4.0", valueToCheck, BigInteger.class);
        assertEquals(valueToCheck, readAttribute(SNMPManager.ReadMethod.GET, "4.0", BigInteger.class));
        assertEquals(valueToCheck, readAttribute(SNMPManager.ReadMethod.GETBULK, "4.0", BigInteger.class));
    }

    @Test
    public final void testForFloatProperty() throws IOException{
        final float valueToCheck = 31.337F;
        writeAttribute("8.0", valueToCheck, Float.class);
        assertEquals(valueToCheck, (float) readAttribute(SNMPManager.ReadMethod.GET, "8.0", Float.class), 0.000001);
        assertEquals(valueToCheck, (float) readAttribute(SNMPManager.ReadMethod.GETBULK, "8.0", Float.class), 0.000001);
    }

    @Test
    public final void testForDatePropertyCustomDisplayFormat() throws IOException{
        final Calendar cal = Calendar.getInstance(); cal.set(1994, 3, 5); // Kurt Donald Cobain, good night, sweet prince
        final String valueToCheck = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(cal.getTime());
        writeAttribute("9.0", valueToCheck, String.class);
        assertEquals(valueToCheck, readAttribute(SNMPManager.ReadMethod.GET, "9.0", String.class));
        assertEquals(valueToCheck, readAttribute(SNMPManager.ReadMethod.GETBULK, "9.0", String.class));
    }

    @Test
    public final void testForDatePropertyRFC1903HumanReadable() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Calendar cal = Calendar.getInstance(); cal.set(1994, 3, 5); // Kurt Donald Cobain, good night, sweet prince
        final SnmpHelpers.DateTimeFormatter formatter = SnmpHelpers.createDateTimeFormatter("rfc1903-human-readable");
        final String valueToCheck = new String(formatter.convert(cal.getTime()));
        writeAttribute("10.0", valueToCheck, String.class);
        assertEquals(valueToCheck, readAttribute(SNMPManager.ReadMethod.GET, "10.0", String.class));
        assertEquals(valueToCheck, readAttribute(SNMPManager.ReadMethod.GETBULK, "10.0", String.class));
    }

    @Test
    public final void testForDatePropertyRFC1903() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Calendar cal = Calendar.getInstance(); cal.set(1994, 3, 5); // Kurt Donald Cobain, good night, sweet prince
        final SnmpHelpers.DateTimeFormatter formatter = SnmpHelpers.createDateTimeFormatter("rfc1903");
        final byte[] byteString = formatter.convert(cal.getTime());
        writeAttribute("11.0", byteString, byte[].class);
        assertArrayEquals(byteString, readAttribute(SNMPManager.ReadMethod.GET, "11.0", byte[].class));
        assertArrayEquals(byteString, readAttribute(SNMPManager.ReadMethod.GETBULK, "11.0", byte[].class));
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

        /*event = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedEventConfiguration();
        event.setCategory(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getAdditionalElements().put("severity", "panic");
        event.getAdditionalElements().put("objectName", BEAN_NAME);
        event.getAdditionalElements().put("receiverAddress", addressForSNMP+"/"+client.getClientPort());
        event.getAdditionalElements().put("receiverName","test-receiver");
        events.put("20.1", event);*/
    }
}
