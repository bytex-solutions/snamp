package com.snamp.adapters; /**
 * Created with IntelliJ IDEA.
 * User: temni
 * Date: 20.10.13
 * Time: 17:26
 */

import com.google.gson.*;
import com.snamp.connectors.JmxConnectorTest;
import com.snamp.hosting.Agent;
import com.snamp.hosting.AgentConfiguration;
import com.snamp.hosting.EmbeddedAgentConfiguration;
import junit.framework.Assert;
import org.junit.Test;
import org.snmp4j.smi.OID;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class JmxToSnmpTest extends JmxConnectorTest<TestManagementBean> {
    private static final Map<String, String> restAdapterSettings = new HashMap<String, String>(2){{
        put(Adapter.portParamName, "3222");
        put(Adapter.addressParamName, "127.0.0.1");
    }};
    private static final String BEAN_NAME = "com.snampy.jmx:type=com.snamp.adapters.TestManagementBean";

    private final Gson jsonFormatter;

    public JmxToSnmpTest() throws MalformedObjectNameException {
        super("rest", restAdapterSettings, new TestManagementBean(), new ObjectName(BEAN_NAME));
        jsonFormatter = new Gson();
    }

    @Override
    protected String getAttributesNamespace() {
        return "test";
    }

    private URL buildAttributeURL(final String postfix) throws MalformedURLException {
        return new URL(String.format("http://%s:%s/snamp/management/attribute/%s/%s", restAdapterSettings.get(Adapter.addressParamName), restAdapterSettings.get(Adapter.portParamName), getAttributesNamespace(), postfix));
    }

    private String readAttribute(final String postfix) throws IOException {
        final URL attributeGetter = buildAttributeURL(postfix);
        final HttpURLConnection connection = (HttpURLConnection)attributeGetter.openConnection();
        connection.setRequestMethod("GET");
        assertEquals(MediaType.APPLICATION_JSON, connection.getContentType());
        final StringBuilder result = new StringBuilder();
        try(final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))){
            String line = null;
            while ((line = reader.readLine()) != null) result.append(line);
        }
        finally {
            connection.disconnect();
        }
        assertEquals(200, connection.getResponseCode());
        return result.toString();
    }

    private final  <T> T readAttribute(final String postfix, final Class<T> attributeType) throws IOException {
        return jsonFormatter.fromJson(readAttribute(postfix), attributeType);
    }

    private final JsonElement readAttributeAsJson(final String postfix) throws IOException{
        final JsonParser reader = new JsonParser();
        return reader.parse(readAttribute(postfix));
    }

    private void writeAttribute(final String postfix, final String attributeValue) throws IOException{
        final URL attributeSetter = buildAttributeURL(postfix);
        final HttpURLConnection connection = (HttpURLConnection)attributeSetter.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("content-type", MediaType.APPLICATION_JSON);
        connection.setDoOutput(true);
        try(final OutputStream os = connection.getOutputStream()){
            os.write(attributeValue.getBytes("UTF-8"));
        }
        finally {
            connection.disconnect();
        }
        assertEquals(200, connection.getResponseCode());
    }

    private <T> void writeAttribute(final String postfix, final T value, final Class<T> valueType) throws IOException {
        writeAttribute(postfix, jsonFormatter.toJson(value, valueType));
    }

    private void writeAttributeAsJson(final String postfix, final JsonElement value) throws IOException {
        writeAttribute(postfix, jsonFormatter.toJson(value));
    }

    @Test
    public final void testForStringProperty() throws IOException {
        writeAttribute("stringProperty", "NO VALUE", String.class);
        assertEquals("NO VALUE", readAttribute("stringProperty", String.class));
    }

    @Test
    public final void testForBooleanProperty() throws IOException{
        writeAttribute("booleanProperty", true, Boolean.class);
        assertTrue(readAttribute("booleanProperty", Boolean.class));
    }

    @Test
    public final void testForInt32Property() throws IOException{
        writeAttribute("int32Property", 42, Integer.class);
        assertEquals(42, (int) readAttribute("int32Property", Integer.class));
    }

    @Test
    public final void testForBigIntProperty() throws IOException{
        writeAttribute("bigintProperty", new BigInteger("100500"), BigInteger.class);
        assertEquals(new BigInteger("100500"), readAttribute("bigintProperty", BigInteger.class));
    }

    @Test
    public final void testForArrayProperty() throws IOException{
        writeAttribute("arrayProperty", new short[]{1, 2, 3}, short[].class);
        assertArrayEquals(new short[]{1, 2, 3}, readAttribute("arrayProperty", short[].class));
    }

    @Test
    public final void testForDictionaryProperty() throws IOException{
        JsonObject dic = new JsonObject();
        dic.add("col1", new JsonPrimitive(true));
        dic.add("col2", new JsonPrimitive(42));
        dic.add("col3", new JsonPrimitive("Hello, world!"));
        writeAttributeAsJson("dictionaryProperty", dic);
        //now read dictionary and test
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
        writeAttributeAsJson("tableProperty", table);
        //read table
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

    @Override
    protected final void fillAttributes(final Map<String, ManagementTargetConfiguration.AttributeConfiguration> attributes) {
        EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration attribute = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("string");
        attribute.getAdditionalElements().put("objectName", BEAN_NAME);
        attributes.put("stringProperty", attribute);

        attribute = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("boolean");
        attribute.getAdditionalElements().put("objectName", BEAN_NAME);
        attributes.put("booleanProperty", attribute);

        attribute = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("int32");
        attribute.getAdditionalElements().put("objectName", BEAN_NAME);
        attributes.put("int32Property", attribute);

        attribute = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("bigint");
        attribute.getAdditionalElements().put("objectName", BEAN_NAME);
        attributes.put("bigintProperty", attribute);

        attribute = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("array");
        attribute.getAdditionalElements().put("objectName", BEAN_NAME);
        attributes.put("arrayProperty", attribute);

        attribute = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("dictionary");
        attribute.getAdditionalElements().put("objectName", BEAN_NAME);
        attributes.put("dictionaryProperty", attribute);

        attribute = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("table");
        attribute.getAdditionalElements().put("objectName", BEAN_NAME);
        attributes.put("tableProperty", attribute);
    }
}
