package com.bytex.snamp.testing.connectors.mda;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.connectors.ManagedResourceConnector;
import com.bytex.snamp.connectors.notifications.NotificationSupport;
import com.bytex.snamp.connectors.notifications.SynchronizationListener;
import com.bytex.snamp.jmx.CompositeDataUtils;
import com.bytex.snamp.jmx.json.JsonUtils;
import com.bytex.snamp.testing.MavenDependencies;
import com.bytex.snamp.testing.MavenFeature;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.Test;

import javax.management.JMException;
import javax.management.Notification;
import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@MavenDependencies(@MavenFeature(groupId = "org.apache.karaf.cellar", artifactId = "apache-karaf-cellar", version = "3.0.3", name = "cellar"))
public final class HazelcastMdaHttpConnectorTest extends AbstractMdaConnectorTest {
    public HazelcastMdaHttpConnectorTest() {
        super(ImmutableMap.of("expirationTime", "10000", "waitForHazelcast", "15000"));
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

        final ManagedResourceConnector connector = getManagementConnector();
        try {
            assertEquals((short)52, connector.getAttribute("attr1"));
            assertEquals((short)52, connector.getAttribute("alias"));
        }
        finally {
            releaseManagementConnector();
        }
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

        final ManagedResourceConnector connector = getManagementConnector();
        try {
            assertEquals(expectedValue, connector.getAttribute("attr3"));
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void strAttributeTest() throws IOException, JMException {
        final String expectedValue = "Frank Underwood";
        JsonElement result = setAttributeViaHttp("str", new JsonPrimitive(expectedValue));
        assertEquals("", result.getAsString());
        result = getAttributeViaHttp("str");
        assertEquals(expectedValue, result.getAsString());

        final ManagedResourceConnector connector = getManagementConnector();
        try {
            assertEquals(expectedValue, connector.getAttribute("attr4"));
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void arrayAttributeTest() throws IOException, JMException {
        final byte[] expectedValue = {1, 4, 10, 19};
        JsonElement result = setAttributeViaHttp("array", JsonUtils.toJsonArray(expectedValue));
        assertArrayEquals(ArrayUtils.emptyArray(byte[].class), JsonUtils.parseByteArray(result.getAsJsonArray()));
        result = getAttributeViaHttp("array");
        assertArrayEquals(expectedValue, JsonUtils.parseByteArray(result.getAsJsonArray()));

        final ManagedResourceConnector connector = getManagementConnector();
        try {
            assertArrayEquals(expectedValue, connector.getAttribute("attr5"));
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void booleanAttributeTest() throws IOException, JMException {
        JsonElement result = setAttributeViaHttp("boolean", new JsonPrimitive(true));
        assertFalse(result.getAsBoolean());
        result = getAttributeViaHttp("boolean");
        assertTrue(result.getAsBoolean());

        final ManagedResourceConnector connector = getManagementConnector();
        try {
            assertEquals(Boolean.TRUE, connector.getAttribute("attr6"));
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void longAttributeTest() throws IOException, JMException {
        JsonElement result = setAttributeViaHttp("long", new JsonPrimitive(901L));
        assertEquals(0L, result.getAsLong());
        result = getAttributeViaHttp("long");
        assertEquals(901L, result.getAsLong());

        final ManagedResourceConnector connector = getManagementConnector();
        try {
            assertEquals(901L, connector.getAttribute("attr7"));
        }
        finally {
            releaseManagementConnector();
        }
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
        final ManagedResourceConnector connector = getManagementConnector();
        try {
            assertEquals(506L, connector.getAttribute("attr7"));
            assertEquals(Boolean.TRUE, connector.getAttribute("attr6"));
            assertEquals("Barry Burton", connector.getAttribute("attr4"));
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void notificationTest1() throws Exception {
        final Future<Notification> notifAwaitor;
        final NotificationSupport connector = getManagementConnector().queryObject(NotificationSupport.class);
        assertNotNull(connector);
        try {
            final SynchronizationListener listener = new SynchronizationListener("e1");
            connector.addNotificationListener(listener, null, null);
            notifAwaitor = listener.getAwaitor();
        }
        finally {
            releaseManagementConnector();
        }
        sendNotification("testEvent1", "Frank Underwood", 10L, 50L, new JsonPrimitive(100500L));
        final Notification received = notifAwaitor.get(3, TimeUnit.SECONDS);
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

        final ManagedResourceConnector connector = getManagementConnector();
        try {
            assertEquals(502L, connector.getAttribute("attr7"));
            Thread.sleep(10010);
            try {
                connector.getAttribute("attr7");
            }catch (final JMException e){
                assertTrue(e.getCause() instanceof IllegalStateException);
                return;
            }
            fail("No exception");
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Override
    protected void fillEvents(final Map<String, EventConfiguration> events, final Supplier<EventConfiguration> eventFactory) {
        EventConfiguration event = eventFactory.get();
        event.setCategory("testEvent1");
        event.getParameters().put("expectedType", "int64");
        events.put("e1", event);
    }

    @Override
    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes,
                                  final Supplier<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attr = attributeFactory.get();
        attr.setAttributeName("short");
        attr.getParameters().put("expectedType", "int16");
        attributes.put("attr1", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("short");
        attr.getParameters().put("expectedType", "int16");
        attributes.put("alias", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("date");
        attr.getParameters().put("expectedType", "datetime");
        attributes.put("attr2", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("biginteger");
        attr.getParameters().put("expectedType", "bigint");
        attributes.put("attr3", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("str");
        attr.getParameters().put("expectedType", "string");
        attributes.put("attr4", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("array");
        attr.getParameters().put("expectedType", "array(int8)");
        attributes.put("attr5", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("boolean");
        attr.getParameters().put("expectedType", "bool");
        attributes.put("attr6", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("long");
        attr.getParameters().put("expectedType", "int64");
        attributes.put("attr7", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("dict");
        attr.getParameters().put("expectedType", "dictionary");
        attr.getParameters().put("dictionaryName", "MemoryStatus");
        attr.getParameters().put("dictionaryItemNames", "free, total");
        attr.getParameters().put("dictionaryItemTypes", "int32, int32");
        attributes.put("attr8", attr);
    }
}
