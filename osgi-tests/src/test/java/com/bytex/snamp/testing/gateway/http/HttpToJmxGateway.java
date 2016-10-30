package com.bytex.snamp.testing.gateway.http;

import com.bytex.snamp.concurrent.SpinWait;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.gateway.GatewayClient;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.jmx.CompositeDataBuilder;
import com.bytex.snamp.jmx.TabularDataBuilder;
import com.bytex.snamp.jmx.json.JsonUtils;
import com.bytex.snamp.testing.BundleExceptionCallable;
import com.bytex.snamp.testing.ImportPackages;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connector.jmx.TestOpenMBean;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.atmosphere.wasync.*;
import org.atmosphere.wasync.impl.AtmosphereClient;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.gateway.Gateway.FeatureBindingInfo;
import static com.bytex.snamp.jmx.json.JsonUtils.toJsonArray;
import static com.bytex.snamp.testing.connector.jmx.TestOpenMBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@SnampDependencies({SnampFeature.HTTP_GATEWAY, SnampFeature.WRAPPED_LIBS})
@ImportPackages({"com.bytex.snamp.jmx.json;version=\"[1.0,2)\"",
        "org.atmosphere.wasync;version=\"[2.0.0,3)\""})
public final class HttpToJmxGateway extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final class NotificationReceiver extends LinkedBlockingQueue<JsonElement> implements Function<String>{
        private static final long serialVersionUID = 2056675059549300951L;
        private final Gson formatter;

        private NotificationReceiver(final Gson formatter) {
            this.formatter = formatter;
        }

        @Override
        public void on(final String notification) {
            offer(formatter.toJsonTree(notification));
        }
    }

    private static final String GATEWAY_NAME = "http";
    private static final String INSTANCE_NAME = "test-http";
    private final Gson formatter;

    public HttpToJmxGateway() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME));
        formatter = new Gson();
    }

    @Override
    protected void fillGateways(final EntityMap<? extends GatewayConfiguration> gateways) {
        final GatewayConfiguration httpGateway = gateways.getOrAdd(INSTANCE_NAME);
        httpGateway.setType(GATEWAY_NAME);
    }

    private void testAttribute(final String attributeID,
                               final JsonElement value) throws IOException {
        final URL attributeQuery = new URL(String.format("http://localhost:8181/snamp/gateway/http/%s/attributes/%s/%s", INSTANCE_NAME, TEST_RESOURCE_NAME, attributeID));
        //write attribute
        HttpURLConnection connection = (HttpURLConnection)attributeQuery.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        IOUtils.writeString(formatter.toJson(value), connection.getOutputStream(), Charset.defaultCharset());
        connection.connect();
        try{
            assertEquals(200, connection.getResponseCode());
        }
        finally {
            connection.disconnect();
        }
        //read attribute
        connection = (HttpURLConnection)attributeQuery.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        try{
            assertEquals(200, connection.getResponseCode());
            final String attributeValue = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            assertNotNull(attributeValue);
            assertFalse(attributeValue.isEmpty());
            assertEquals(value, formatter.fromJson(attributeValue, JsonElement.class));
        }
        finally {
            connection.disconnect();
        }
    }

    @Test
    public void startStopTest() throws Exception {
        final Duration TIMEOUT = Duration.ofSeconds(15);
        //stop gateway and connector
        GatewayActivator.disableGateway(getTestBundleContext(), GATEWAY_NAME);
        stopResourceConnector(getTestBundleContext());
        //start empty gateway
        syncWithGatewayStartedEvent(GATEWAY_NAME, (BundleExceptionCallable) () -> {
                GatewayActivator.enableGateway(getTestBundleContext(), GATEWAY_NAME);
                return null;
        }, TIMEOUT);
        //start connector, this causes attribute registration and SNMP gateway restarting,
        //waiting is not required because HTTP gateway supports hot reconfiguring
        startResourceConnector(getTestBundleContext());
        //Reconfiguration of HTTP gateway is asynchronous event so we
        //should give a chance to catch a connector starting event
        Thread.sleep(2000);
        //check whether the attribute is accessible
        testStringAttribute();
        //now stops the connector again
        stopResourceConnector(getTestBundleContext());
        //stop the gateway
        GatewayActivator.disableGateway(getTestBundleContext(), GATEWAY_NAME);
    }

    @Test
    public void testStringAttribute() throws IOException {
        testAttribute("1.0", new JsonPrimitive("Hello, world!"));
    }

    @Test
    public void testBooleanAttribute() throws IOException{
        testAttribute("2.0", new JsonPrimitive(true));
    }

    @Test
    public void testInt32Attribute() throws IOException{
        testAttribute("3.0", new JsonPrimitive(42));
    }

    @Test
    public void testBigIntAttribute() throws IOException{
        testAttribute("bigint", new JsonPrimitive(new BigInteger("100500")));
    }

    @Test
    public void testArrayAttribute() throws IOException{
        testAttribute("5.1", toJsonArray((short) 4, (short) 3));
    }

    @Test
    public void testTableAttribute() throws IOException, OpenDataException {
        //[{'col1':false,'col2':2,'col3':'pp'}]
        final TabularData data = new TabularDataBuilder()
                .setTypeName("SimpleTable", true)
                .setTypeDescription("descr", true)
                .declareColumns(columns -> columns
                    .addColumn("col1", "desc", SimpleType.BOOLEAN, false)
                    .addColumn("col2", "desc", SimpleType.INTEGER, false)
                    .addColumn("col3", "desc", SimpleType.STRING, true))
                .add(false, 2, "pp")
                .build();
        final Gson formatter = JsonUtils.registerOpenTypeAdapters(new GsonBuilder()).create();
        testAttribute("7.1", formatter.toJsonTree(data));
    }

    @Test
    public void testDictionaryAttribute() throws IOException, OpenDataException {
        //{'col1':false,'col2':42,'col3':'hello, world!'}
        final CompositeData data = new CompositeDataBuilder("dictionary", "desc")
            .put("col1", "desc", false)
            .put("col2", "desc", 42)
            .put("col3", "desc", "Hello, world!")
            .build();
        final Gson formatter = JsonUtils.registerOpenTypeAdapters(new GsonBuilder()).create();
        testAttribute("6.1", formatter.toJsonTree(data));

    }

    private void testNotificationTransport(final Request.TRANSPORT transport) throws IOException{
        final AtmosphereClient client = ClientFactory.getDefault().newClient(AtmosphereClient.class);
        final RequestBuilder requestBuilder = client.newRequestBuilder()
                .method(Request.METHOD.GET)
                .uri(String.format("http://localhost:8181/snamp/gateway/http/%s/notifications/%s", INSTANCE_NAME, TEST_RESOURCE_NAME))
                //.trackMessageLength(true)
                .transport(transport);
        final Socket sock = client.create();
        final NotificationReceiver receiver = new NotificationReceiver(formatter);
        sock.on("message", receiver).open(requestBuilder.build());
        try{
            //force attribute change
            testStringAttribute();
            //wait for notifications
            SpinWait.spinUntil(() -> receiver.size() < 1, Duration.ofSeconds(3));
        } catch (final InterruptedException | TimeoutException e) {
            fail(String.format("Invalid message count: %s", receiver.size()));
        } finally {
            sock.close();
        }
    }

    @Test
    public void testNotificationViaWebSocket() throws IOException {
        testNotificationTransport(Request.TRANSPORT.WEBSOCKET);
    }

    @Test
    public void testNotificationViaComet() throws IOException{
        testNotificationTransport(Request.TRANSPORT.LONG_POLLING);
    }

    @Test
    public void configurationDescriptorTest() throws BundleException {
        final ConfigurationEntityDescription desc = GatewayClient.getConfigurationEntityDescriptor(getTestBundleContext(), GATEWAY_NAME, GatewayConfiguration.class);
        testConfigurationDescriptor(desc, "dateFormat");
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        super.beforeStartTest(context);
        beforeCleanupTest(context);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        startResourceConnector(context);
        syncWithGatewayStartedEvent(GATEWAY_NAME, (BundleExceptionCallable)() -> {
                GatewayActivator.enableGateway(getTestBundleContext(), GATEWAY_NAME);
                return null;
        }, Duration.ofSeconds(15));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        GatewayActivator.disableGateway(context, GATEWAY_NAME);
        stopResourceConnector(context);
    }

    @Test
    public void attributeBindingTest() throws TimeoutException, InterruptedException, ExecutionException {
        final GatewayClient client = new GatewayClient(getTestBundleContext(), INSTANCE_NAME, Duration.ofSeconds(2));
        try {
            assertTrue(client.forEachFeature(MBeanAttributeInfo.class, (resourceName, bindingInfo) -> bindingInfo.getProperty("path") instanceof String &&
                    bindingInfo.getProperty(FeatureBindingInfo.MAPPED_TYPE) instanceof String));
        } finally {
            client.release(getTestBundleContext());
        }
    }

    @Test
    public void notificationBindingTest() throws TimeoutException, InterruptedException, ExecutionException {
        final GatewayClient client = new GatewayClient(getTestBundleContext(), INSTANCE_NAME, Duration.ofSeconds(2));
        try {
            assertTrue(client.forEachFeature(MBeanNotificationInfo.class, (resourceName, bindingInfo) -> bindingInfo.getProperty("path") instanceof String));
        } finally {
            client.release(getTestBundleContext());
        }
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("1.0");
        attribute.setAlternativeName("string");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("2.0");
        attribute.setAlternativeName("boolean");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("3.0");
        attribute.setAlternativeName("int32");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("bigint");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("5.1");
        attribute.setAlternativeName("array");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("6.1");
        attribute.setAlternativeName("dictionary");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("typeName", "dict");

        attribute = attributes.getOrAdd("7.1");
        attribute.setAlternativeName("table");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("typeName", "table");
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);

        event = events.getOrAdd("com.bytex.snamp.connector.tests.impl.testnotif");
        event.getParameters().put("severity", "panic");
        event.getParameters().put("objectName", BEAN_NAME);

        event = events.getOrAdd("com.bytex.snamp.connector.tests.impl.plainnotif");
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);
    }
}