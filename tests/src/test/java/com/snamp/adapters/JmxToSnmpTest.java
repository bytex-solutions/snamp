package com.snamp.adapters;

/**
 * @author Evgeniy Kirichenko
 */

import com.snamp.connectors.JmxConnectorTest;
import com.snamp.hosting.EmbeddedAgentConfiguration;
import org.junit.Test;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.smi.*;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JmxToSnmpTest extends JmxConnectorTest<TestManagementBean> {
    private static final String portForSNMP = "3222";
    private static final String addressForSNMP = "127.0.0.1";
    private static final String prefix = "1.1";
    private static final SNMPManager client = new SNMPManager("udp:"+addressForSNMP+"/"+portForSNMP);

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

    private <T>T readAttribute(final String postfix, Class<T> className) throws IOException {
        final ResponseEvent value = client.get(new OID[]{new OID(prefix + "." + postfix)});

        assertNotNull(value);

        final Variable var = value.getResponse().getVariable(new OID(prefix + "." + postfix));
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

        assertNotNull(result);
        assertTrue(className.isInstance(result));
        return className.cast(result);

    }

    private <T>void writeAttribute(final String postfix, final T value, final Class<T> valueType) throws IOException{
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
        for(int i = 0; i < 100; i++){
            List<Variable[]> table = client.getTable(new OID(prefix + "." + "5.1"), 1);
            table.toString();
        }
        //Thread.sleep(6000000);

        //Thread.sleep(1000000L);
        //writeAttribute("1.5", new short[]{1, 2, 3}, short[].class);

       // Thread.sleep(10000000);

      //  List<Variable[]> table = client.getTable(new OID(prefix + "." + "5.1"), 1, 4);
      // table.toString();
        //writeAttribute("1.5", new short[]{1, 2, 3}, short[].class);
       // Integer f = readAttribute("5.1.2.1", Integer.class);
       // f.toString();
        //assertArrayEquals(new short[]{1, 2, 3}, readAttribute("1.5", short[].class));
    }

    @Test
    public final void testForStringProperty() throws IOException {
        final String valueToCheck = "SETTED VALUE";
        writeAttribute("1.0", valueToCheck, String.class);
        assertEquals(valueToCheck, readAttribute("1.0", String.class));
    }

   /* @Test
    public final void testForBooleanProperty() throws IOException{
        final boolean valueToCheck = true;
        writeAttribute("2.0", valueToCheck, Boolean.class);
        assertTrue((boolean) readAttribute("2.0", Boolean.class));
    }

    @Test
    public final void testForInt32Property() throws IOException{
        final int valueToCheck = 42;
        writeAttribute("3.0", valueToCheck, Integer.class);
        assertEquals(valueToCheck, (int) readAttribute("3.0", Integer.class));
    }

    @Test
    public final void testForBigIntProperty() throws IOException{
        final BigInteger valueToCheck = new BigInteger("100500");
        writeAttribute("4.0", valueToCheck, BigInteger.class);
        assertEquals(valueToCheck, readAttribute("4.0", BigInteger.class));
    }

    @Test
    public final void testForFloatProperty() throws IOException{
        final float valueToCheck = 31.337F;
        writeAttribute("8.0", valueToCheck, Float.class);
        assertEquals(valueToCheck, (float) readAttribute("8.0", Float.class), 0.000001);
    }

    @Test
    public final void testForDatePropertyCustomDisplayFormat() throws IOException{
        final Calendar cal = Calendar.getInstance(); cal.set(1994, 3, 5); // Kurt Donald Cobain, good night, sweet prince
        final String valueToCheck = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(cal.getTime());
        writeAttribute("9.0", valueToCheck, String.class);
        assertEquals(valueToCheck, readAttribute("9.0", String.class));
    }

    @Test
    public final void testForDatePropertyRFC1903HumaReadable() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Calendar cal = Calendar.getInstance(); cal.set(1994, 3, 5); // Kurt Donald Cobain, good night, sweet prince

        Class<?> c = Class.forName("com.snamp.adapters.SnmpUnixTimeObject$Rfc1903HumanReadableDateTimeFormatter");
        Method method = c.getDeclaredMethod ("convert", Calendar.class);
        method.setAccessible(true);

        final String valueToCheck = (String) method.invoke (null, cal);

        writeAttribute("10.0", valueToCheck, String.class);
        assertEquals(valueToCheck, readAttribute("10.0", String.class));
    }


    @Test
    public final void testForDatePropertyRFC1903() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Calendar cal = Calendar.getInstance(); cal.set(1994, 3, 5); // Kurt Donald Cobain, good night, sweet prince

        Class<?> c = Class.forName("com.snamp.adapters.SnmpUnixTimeObject$Rfc1903BinaryDateTimeFormatter");
        Method method = c.getDeclaredMethod ("convert", Calendar.class);
        method.setAccessible(true);

        final byte[] byteString = (byte[]) method.invoke (null, cal);
        writeAttribute("11.0", byteString, byte[].class);
        assertArrayEquals(byteString, readAttribute("11.0", byte[].class));
    }*/


    /*
 @Test
 public final void testForDictionaryProperty() throws IOException{
  JsonObject dic = new JsonObject();
  dic.add("col1", new JsonPrimitive(true));
  dic.add("col2", new JsonPrimitive(42));
  dic.add("col3", new JsonPrimitive("Hello, world!"));
  writeAttributeAsJson("1.6", dic);
  //now invoke dictionary and test
  JsonElement elem = readAttributeAsJson("dictionaryProperty");
  assertTrue(elem instanceof JsonObject);
  dic = (JsonObject)elem;
  assertEquals(new JsonPrimitive(true), dic.get("col1"));
  assertEquals(new JsonPrimitive(42), dic.get("col2"));
  assertEquals(new JsonPrimitive("Hello, world!"), dic.get("col3"));
 }

 @Test
 public final void testForTableProperty() throws IOException{
  JsonArray table = new JsonArray();
  //row 1
  JsonObject row = new JsonObject();
  table.add(row);
  row.add("col1", new JsonPrimitive(true));
  row.add("col2", new JsonPrimitive(100500));
  row.add("col3", new JsonPrimitive("Row 1"));
  //row 2
  row = new JsonObject();
  table.add(row);
  row.add("col1", new JsonPrimitive(true));
  row.add("col2", new JsonPrimitive(100501));
  row.add("col3", new JsonPrimitive("Row 2"));
  //row 3
  row = new JsonObject();
  table.add(row);
  row.add("col1", new JsonPrimitive(true));
  row.add("col2", new JsonPrimitive(100502));
  row.add("col3", new JsonPrimitive("Row 3"));
  //row 4
  row = new JsonObject();
  table.add(row);
  row.add("col1", new JsonPrimitive(true));
  row.add("col2", new JsonPrimitive(100503));
  row.add("col3", new JsonPrimitive("Row 4"));
  writeAttributeAsJson("1.7", table);
  //invoke table
  JsonElement elem = readAttributeAsJson("tableProperty");
  assertTrue(elem instanceof JsonArray);
  table = (JsonArray)elem;
  assertEquals(4, table.size());
  //row 1
  assertEquals(new JsonPrimitive(true), ((JsonObject)table.get(0)).get("col1"));
  assertEquals(new JsonPrimitive(100500), ((JsonObject)table.get(0)).get("col2"));
  assertEquals(new JsonPrimitive("Row 1"), ((JsonObject)table.get(0)).get("col3"));
  //row 2
  assertEquals(new JsonPrimitive(true), ((JsonObject)table.get(1)).get("col1"));
  assertEquals(new JsonPrimitive(100501), ((JsonObject)table.get(1)).get("col2"));
  assertEquals(new JsonPrimitive("Row 2"), ((JsonObject)table.get(1)).get("col3"));
  //row 3
  assertEquals(new JsonPrimitive(true), ((JsonObject)table.get(2)).get("col1"));
  assertEquals(new JsonPrimitive(100502), ((JsonObject)table.get(2)).get("col2"));
  assertEquals(new JsonPrimitive("Row 3"), ((JsonObject)table.get(2)).get("col3"));
  //row 4
  assertEquals(new JsonPrimitive(true), ((JsonObject)table.get(3)).get("col1"));
  assertEquals(new JsonPrimitive(100503), ((JsonObject)table.get(3)).get("col2"));
  assertEquals(new JsonPrimitive("Row 4"), ((JsonObject)table.get(3)).get("col3"));
 }



      */
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

        /*attribute = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("dictionary");
        attribute.getAdditionalElements().put("objectName", BEAN_NAME);
        attributes.put("6.1", attribute);*/

        /*attribute = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("table");
        attribute.getAdditionalElements().put("objectName", BEAN_NAME);
        attributes.put("7.1", attribute);*/

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

}
