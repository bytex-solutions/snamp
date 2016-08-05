package com.bytex.snamp.testing.connectors.mda;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.connectors.ManagedResourceConnector;
import com.bytex.snamp.jmx.CompositeDataUtils;
import com.bytex.snamp.jmx.json.JsonUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.Test;

import javax.management.JMException;
import javax.management.Notification;
import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Date;

import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.EventConfiguration;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class StandaloneMdaHttpConnectorTest extends AbstractMdaConnectorTest {
    public StandaloneMdaHttpConnectorTest() {
        super(ImmutableMap.of("expirationTime", "10000"));
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Test
    public void shortAttributeTest() throws IOException, JMException {
        JsonElement result = setAttributeViaHttp("short", new JsonPrimitive((short)52));
        assertEquals(0, result.getAsShort());
        result = getAttributeViaHttp("short");
        assertEquals(52, result.getAsShort());
        testAttribute("attr1", TypeToken.of(Short.class), (short) 52, true);
        testAttribute("alias", TypeToken.of(Short.class), (short)52, true);
    }

    @Test
    public void resetTest() throws IOException {
        JsonElement result = setAttributeViaHttp("short", new JsonPrimitive((short) 52));
        assertEquals(0, result.getAsShort());
        result = getAttributeViaHttp("short");
        assertEquals((short) 52, result.getAsShort());
        resetAttributes();
        result = getAttributeViaHttp("short");
        assertEquals((short) 0, result.getAsShort());
    }

    @Test
    public void dateAttributeTest() throws IOException, JMException{
        final Date current = new Date();
        JsonElement result = setAttributeViaHttp("date", formatter.toJsonTree(current));
        assertEquals(new Date(0L), formatter.fromJson(result, Date.class));
        result = getAttributeViaHttp("date");
        assertEquals(formatter.toJsonTree(current), result);

        final ManagedResourceConnector connector = getManagementConnector();
        try {
            assertEquals(formatter.toJsonTree(current), formatter.toJsonTree(connector.getAttribute("attr2")));
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void bigintAttributeTest() throws IOException, JMException {
        final BigInteger expectedValue = new BigInteger("100500");
        JsonElement result = setAttributeViaHttp("biginteger", new JsonPrimitive(expectedValue));
        assertEquals(BigInteger.ZERO, result.getAsBigInteger());
        result = getAttributeViaHttp("biginteger");
        assertEquals(expectedValue, result.getAsBigInteger());
        testAttribute("attr3", TypeToken.of(BigInteger.class), expectedValue, true);
    }

    @Test
    public void strAttributeTest() throws IOException, JMException {
        final String expectedValue = "Frank Underwood";
        JsonElement result = setAttributeViaHttp("str", new JsonPrimitive(expectedValue));
        assertEquals("", result.getAsString());
        result = getAttributeViaHttp("str");
        assertEquals(expectedValue, result.getAsString());
        testAttribute("attr4", TypeToken.of(String.class), expectedValue, true);
    }

    @Test
    public void arrayAttributeTest() throws IOException, JMException {
        final byte[] expectedValue = {1, 4, 10, 19};
        JsonElement result = setAttributeViaHttp("array", JsonUtils.toJsonArray(expectedValue));
        assertArrayEquals(ArrayUtils.emptyArray(byte[].class), JsonUtils.parseByteArray(result.getAsJsonArray()));
        result = getAttributeViaHttp("array");
        assertArrayEquals(expectedValue, JsonUtils.parseByteArray(result.getAsJsonArray()));
        testAttribute("attr5", TypeToken.of(byte[].class), expectedValue, ArrayUtils::strictEquals, true);
    }

    @Test
    public void booleanAttributeTest() throws IOException, JMException {
        JsonElement result = setAttributeViaHttp("boolean", new JsonPrimitive(true));
        assertFalse(result.getAsBoolean());
        result = getAttributeViaHttp("boolean");
        assertTrue(result.getAsBoolean());
        testAttribute("attr6", TypeToken.of(Boolean.class), Boolean.TRUE, true);
    }

    @Test
    public void longAttributeTest() throws IOException, JMException {
        JsonElement result = setAttributeViaHttp("long", new JsonPrimitive(901L));
        assertEquals(0L, result.getAsLong());
        result = getAttributeViaHttp("long");
        assertEquals(901L, result.getAsLong());
        testAttribute("attr7", TypeToken.of(Long.class), 901L, true);
    }

    @Test
    public void dictionaryAttributeTest() throws IOException, JMException {
        final JsonObject expectedValue = JsonUtils.toJsonObject(
                "free", new JsonPrimitive(10),
                "total", new JsonPrimitive(100)
        );
        JsonElement result = setAttributeViaHttp("dict", expectedValue);
        assertEquals(0, result.getAsJsonObject().get("free").getAsInt());
        assertEquals(0, result.getAsJsonObject().get("total").getAsInt());

        result = getAttributeViaHttp("dict");
        assertEquals(10, result.getAsJsonObject().get("free").getAsInt());
        assertEquals(100, result.getAsJsonObject().get("total").getAsInt());


        final ManagedResourceConnector connector = getManagementConnector();
        try {
            final CompositeData data =  (CompositeData)connector.getAttribute("attr8");
            assertNotNull(data);
            assertEquals(10, CompositeDataUtils.getInteger(data, "free", 0));
            assertEquals(100, CompositeDataUtils.getInteger(data, "total", 0));
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void multipleWriteTest() throws IOException, JMException {
        final JsonObject values = new JsonObject();
        values.addProperty("long", 506L);
        values.addProperty("boolean", true);
        values.addProperty("str", "Barry Burton");
        assertNotNull(setAttributesViaHttp(values));
        testAttribute("attr7", TypeToken.of(Long.class), 506L, true);
        testAttribute("attr6", TypeToken.of(Boolean.class), Boolean.TRUE, true);
        testAttribute("attr4", TypeToken.of(String.class), "Barry Burton", true);
    }

    @Test
    public void notificationTest1() throws Exception {
        final Notification received = waitForNotification("e1", connector -> sendNotification("testEvent1", "Frank Underwood", 10L, 50L, new JsonPrimitive(100500L)), Duration.ofSeconds(3L));
        assertNotNull(received);
        assertEquals("Frank Underwood", received.getMessage());
        assertEquals(10L, received.getSequenceNumber());
        assertEquals(50L, received.getTimeStamp());
        assertEquals(100500L, received.getUserData());
    }

    @Test
    public void longAttributeExpirationTest() throws IOException, JMException, InterruptedException {
        JsonElement result = setAttributeViaHttp("long", new JsonPrimitive(502L));
        assertEquals(0L, result.getAsLong());
        result = getAttributeViaHttp("long");
        assertEquals(502L, result.getAsLong());
        testAttribute("attr7", TypeToken.of(Long.class), 502L, true);
        Thread.sleep(10010);
        try {
            testAttribute("attr7", TypeToken.of(Long.class), 502L, true);
        }catch (final JMException e){
            assertTrue(e.getCause() instanceof IllegalStateException);
            return;
        }
        fail("No exception");
    }

    @Test
    public void configurationTest(){
        testConfigurationDescriptor(AttributeConfiguration.class, ImmutableSet.of(
                "expectedType",
                "dictionaryItemNames",
                "dictionaryItemTypes",
                "dictionaryName"
        ));
        testConfigurationDescriptor(EventConfiguration.class, ImmutableSet.of(
                "expectedType",
                "dictionaryItemNames",
                "dictionaryItemTypes",
                "dictionaryName"
        ));
        testConfigurationDescriptor(ManagedResourceConfiguration.class, ImmutableSet.of(
                "socketTimeout",
                "expirationTime"
        ));
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd("e1");
        setFeatureName(event, "testEvent1");
        event.getParameters().put("expectedType", "int64");
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attr = attributes.getOrAdd("attr1");
        setFeatureName(attr, "short");
        attr.getParameters().put("expectedType", "int16");

        attr = attributes.getOrAdd("alias");
        setFeatureName(attr, "short");
        attr.getParameters().put("expectedType", "int16");

        attr = attributes.getOrAdd("attr2");
        setFeatureName(attr, "date");
        attr.getParameters().put("expectedType", "datetime");

        attr = attributes.getOrAdd("attr3");
        setFeatureName(attr, "biginteger");
        attr.getParameters().put("expectedType", "bigint");

        attr = attributes.getOrAdd("attr4");
        setFeatureName(attr, "str");
        attr.getParameters().put("expectedType", "string");

        attr = attributes.getOrAdd("attr5");
        setFeatureName(attr, "array");
        attr.getParameters().put("expectedType", "array(int8)");

        attr = attributes.getOrAdd("attr6");
        setFeatureName(attr, "boolean");
        attr.getParameters().put("expectedType", "bool");

        attr = attributes.getOrAdd("attr7");
        setFeatureName(attr, "long");
        attr.getParameters().put("expectedType", "int64");

        attr = attributes.getOrAdd("attr8");
        setFeatureName(attr, "dict");
        attr.getParameters().put("expectedType", "dictionary");
        attr.getParameters().put("dictionaryName", "MemoryStatus");
        attr.getParameters().put("dictionaryItemNames", "free, total");
        attr.getParameters().put("dictionaryItemTypes", "int32, int32");
    }
}
