package com.itworks.snamp.testing.adapters.rest;

import com.google.common.base.Supplier;
import com.google.gson.*;
import com.itworks.snamp.adapters.AbstractResourceAdapterActivator;
import com.itworks.snamp.adapters.ResourceAdapterClient;
import com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.testing.SnampArtifact;
import com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.TestOpenMBean;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.AttributeChangeNotification;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.ws.rs.core.MediaType;
import java.math.BigInteger;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.itworks.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class RestAdapterTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String ADAPTER_NAME = "REST";
    private static final String HTTP_HOST = "127.0.0.1";
    private static final String HTTP_PORT = "3344";

    private final Gson jsonFormatter;
    private final JsonParser jsonParser;

    public RestAdapterTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME),
                SnampArtifact.REST_ADAPTER.getReference(),
                mavenBundle("org.eclipse.jetty", "jetty-xml", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty", "jetty-security", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty", "jetty-io", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty", "jetty-http", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty", "jetty-util", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty.websocket", "websocket-server", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty.websocket", "websocket-servlet", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty.websocket", "websocket-common", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty.websocket", "websocket-client", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty.websocket", "websocket-api", "9.1.1.v20140108"),
                mavenBundle("javax.servlet", "javax.servlet-api", "3.1.0"),
                mavenBundle("org.eclipse.jetty", "jetty-server", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty", "jetty-webapp", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty", "jetty-servlet", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty", "jetty-servlet", "9.1.1.v20140108"),
                mavenBundle("com.sun.jersey", "jersey-core", "1.17.1"),
                mavenBundle("com.sun.jersey", "jersey-server", "1.17.1"),
                mavenBundle("com.sun.jersey", "jersey-servlet", "1.17.1"),
                mavenBundle("com.sun.jersey", "jersey-client", "1.17.1"),
                mavenBundle("com.google.code.gson", "gson", "2.2.4"),
                mavenBundle("org.eclipse.jetty", "jetty-jaas", "9.1.1.v20140108"),
                mavenBundle("net.engio", "mbassador", "1.1.10"));
        jsonFormatter = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").create();
        jsonParser = new JsonParser();
    }

    private void testJsonAttribute(final JsonElement newValue, final String attributeName){
        final Client webConsoleClient = new Client();
        final WebResource config = webConsoleClient.resource("http://127.0.0.1:3344/snamp/managedResource/attributes/test-target/" + attributeName);
        config.getRequestBuilder().type(MediaType.APPLICATION_JSON_TYPE).post(jsonFormatter.toJson(newValue));
        final String attributeValue = config.get(String.class);
        assertEquals(newValue, jsonParser.parse(attributeValue));
    }

    @Test
    public void testStringAttribute() throws BundleException {
        try{
            testJsonAttribute(new JsonPrimitive("Frank Underwood"), "1.0");
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public void testBooleanAttribute() throws BundleException {
        try{
            testJsonAttribute(new JsonPrimitive(true), "2.0");
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public void testInt32Attribute() throws BundleException {
        try{
            testJsonAttribute(new JsonPrimitive(1234), "3.0");
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public void testBigIntAttribute() throws BundleException {
        try{
            testJsonAttribute(new JsonPrimitive(new BigInteger("100500")), "4.0");
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public void testFloatAttribute() throws BundleException {
        try{
            testJsonAttribute(new JsonPrimitive(1234F), "8.0");
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public void testDateAttribute() throws BundleException {
        try{
            testJsonAttribute(jsonFormatter.toJsonTree(new Date(), Date.class), "9.0");
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public void testArrayAttribute() throws BundleException {
        try{
            testJsonAttribute(jsonFormatter.toJsonTree(new short[]{8, 7, 6, 5, 4}, short[].class), "5.1");
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public void testDictionaryAttribute() throws BundleException {
        try{
            final JsonObject dic = new JsonObject();
            dic.add("col1", new JsonPrimitive(true));
            dic.add("col2", new JsonPrimitive(42));
            dic.add("col3", new JsonPrimitive("Hello, world!"));
            testJsonAttribute(dic, "6.1");
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    private static JsonArray createTestTable1(){
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
    public void testTableAttribute() throws BundleException {
        try{
            testJsonAttribute(createTestTable1(), "7.1");
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public void configurationDescriptorTest() throws BundleException {
        try {
            final ConfigurationEntityDescription desc = ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, ResourceAdapterConfiguration.class);
            assertNotNull(desc);
            final ConfigurationEntityDescription.ParameterDescription param = desc.getParameterDescriptor("loginModule");
            assertNotNull(param);
            assertFalse(param.getDescription(null).isEmpty());
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    @Test
    public void notificationsTest() throws Exception {
        try{
            final WebSocketClient webSocketClient = new WebSocketClient();
            final JsonNotificationBox notificationBox = new JsonNotificationBox(jsonParser);
            webSocketClient.start();
            //subscribe to event
            final ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setSubProtocols("text");
            try(final Session ignored = webSocketClient.connect(notificationBox, new URI(String.format("ws://%s:%s/snamp/managedResource/notifications", HTTP_HOST, HTTP_PORT)), request).get()){
                //forces attribute changing
                testJsonAttribute(new JsonPrimitive(1234), "3.0");
                int counter = 0;
                while(notificationBox.size() < 2) {
                    Thread.sleep(100);
                    if(counter++ > 50) break;
                }
                assertEquals(2, notificationBox.size());
            }
            finally {
                webSocketClient.stop();
            }
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    private static final class JsonNotificationBox extends ConcurrentLinkedQueue<JsonElement> implements WebSocketListener {
        private final JsonParser jsonFormatter;

        public JsonNotificationBox(final JsonParser formatter){
            this.jsonFormatter = formatter;
        }

        public final void receiveMessage(final String message){
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
        adapters.put("test-rest", restAdapter);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        super.afterStartTest(context);
        AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        AbstractResourceAdapterActivator.startResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
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
