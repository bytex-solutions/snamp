package com.itworks.snamp.testing.adapters.rest;

import com.google.common.base.Supplier;
import com.google.gson.*;
import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.ExceptionalCallable;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.testing.SnampDependencies;
import com.itworks.snamp.testing.SnampFeature;
import com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.TestOpenMBean;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.AttributeChangeNotification;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.math.BigInteger;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.itworks.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.REST_ADAPTER)
public final class SecureRestAdapterTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String ADAPTER_NAME = "REST";
    private static final String HTTP_HOST = "127.0.0.1";
    private static final String HTTP_PORT = "3344";

    private final Gson jsonFormatter;
    private final JsonParser jsonParser;

    public SecureRestAdapterTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME));
        jsonFormatter = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").create();
        jsonParser = new JsonParser();
    }

    private void testJsonAttribute(final JsonElement newValue, final String attributeName) {
        final Client webConsoleClient = ClientBuilder.newClient()
                .register(HttpAuthenticationFeature.digest("roman", "mypassword"));
        final WebTarget config = webConsoleClient.target("http://127.0.0.1:3344/snamp/managedResource/attributes/test-target/" + attributeName);

        config.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(jsonFormatter.toJson(newValue)));
        final String attributeValue = config.request().get(String.class);
        assertEquals(newValue, jsonParser.parse(attributeValue));
    }

    @Test
    public void testStringAttribute() {
        testJsonAttribute(new JsonPrimitive("Frank Underwood"), "1.0");
    }

    @Test
    public void testBooleanAttribute() {
        testJsonAttribute(new JsonPrimitive(true), "2.0");
    }

    @Test
    public void testInt32Attribute() {
        testJsonAttribute(new JsonPrimitive(1234), "3.0");
    }

    @Test
    public void testBigIntAttribute() {
        testJsonAttribute(new JsonPrimitive(new BigInteger("100500")), "4.0");
    }

    @Test
    public void testFloatAttribute() {
        testJsonAttribute(new JsonPrimitive(1234F), "8.0");
    }

    @Test
    public void testDateAttribute() {
        testJsonAttribute(jsonFormatter.toJsonTree(new Date(), Date.class), "9.0");
    }

    @Test
    public void testArrayAttribute() {
        testJsonAttribute(jsonFormatter.toJsonTree(new short[]{8, 7, 6, 5, 4}, short[].class), "5.1");
    }

    @Test
    public void testDictionaryAttribute() {
        final JsonObject dic = new JsonObject();
        dic.add("col1", new JsonPrimitive(true));
        dic.add("col2", new JsonPrimitive(42));
        dic.add("col3", new JsonPrimitive("Hello, world!"));
        testJsonAttribute(dic, "6.1");
    }

    private static JsonArray createTestTable1() {
        final JsonArray table = new JsonArray();
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
        return table;
    }

    @Test
    public void testTableAttribute() {
        testJsonAttribute(createTestTable1(), "7.1");
    }

    @Test
    public void notificationsTest() throws Exception {
        final WebSocketClient webSocketClient = Utils.withContextClassLoader(getClass().getClassLoader(), new ExceptionalCallable<WebSocketClient, ExceptionPlaceholder>() {
            @Override
            public WebSocketClient call() {
                return new WebSocketClient();
            }
        });
        final JsonNotificationBox notificationBox = new JsonNotificationBox(jsonParser);
        webSocketClient.start();
        //subscribe to event
        final ClientUpgradeRequest request = new ClientUpgradeRequest();
        request.setSubProtocols("text");
        try (final Session ignored = webSocketClient.connect(notificationBox, new URI(String.format("ws://%s:%s/snamp/managedResource/notifications", HTTP_HOST, HTTP_PORT)), request).get()) {
            //forces attribute changing
            testJsonAttribute(new JsonPrimitive(1234), "3.0");
            int counter = 0;
            while (notificationBox.size() < 2) {
                Thread.sleep(100);
                if (counter++ > 50) break;
            }
            assertEquals(2, notificationBox.size());
        } finally {
            webSocketClient.stop();
        }
    }

    private static final class JsonNotificationBox extends ConcurrentLinkedQueue<JsonElement> implements WebSocketListener {
        private final JsonParser jsonFormatter;

        public JsonNotificationBox(final JsonParser formatter) {
            this.jsonFormatter = formatter;
        }

        public final void receiveMessage(final String message) {
            add(jsonFormatter.parse(message));
        }

        @Override
        public void onWebSocketBinary(final byte[] payload, final int offset, final int len) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void onWebSocketClose(final int statusCode, final String reason) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void onWebSocketConnect(final Session session) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void onWebSocketError(final Throwable cause) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void onWebSocketText(final String message) {
            receiveMessage(message);
        }
    }

    @Override
    protected void fillAdapters(final Map<String, ResourceAdapterConfiguration> adapters, final Supplier<ResourceAdapterConfiguration> adapterFactory) {
        final ResourceAdapterConfiguration restAdapter = adapterFactory.get();
        restAdapter.setAdapterName(ADAPTER_NAME);
        restAdapter.getParameters().put("port", HTTP_PORT);
        restAdapter.getParameters().put("host", HTTP_HOST);
        restAdapter.getParameters().put("webSocketIdleTimeout", "100000");
        restAdapter.getParameters().put("dateFormat", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        restAdapter.getParameters().put("loginModule", "REST_ADAPTER");
        adapters.put("test-rest", restAdapter);
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
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
        }, TimeSpan.fromSeconds(4));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws BundleException, TimeoutException, InterruptedException {
        ResourceAdapterActivator.stopResourceAdapter(context, ADAPTER_NAME);
        stopResourceConnector(context);
    }

    @Override
    protected void fillEvents(final Map<String, EventConfiguration> events, final Supplier<EventConfiguration> eventFactory) {
        EventConfiguration event = eventFactory.get();
        event.setCategory(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);
        events.put("19.1", event);

        event = eventFactory.get();
        event.setCategory("com.itworks.snamp.connectors.tests.impl.testnotif");
        event.getParameters().put("severity", "panic");
        event.getParameters().put("objectName", BEAN_NAME);
        events.put("20.1", event);
    }

    @Override
    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Supplier<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attribute = attributeFactory.get();
        attribute.setAttributeName("string");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("1.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("boolean");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("2.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("int32");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("3.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("bigint");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("4.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("array");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("5.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("dictionary");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("6.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("table");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("7.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("float");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("8.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("9.0", attribute);
    }
}
