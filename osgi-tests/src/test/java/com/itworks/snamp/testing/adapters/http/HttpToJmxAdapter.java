package com.itworks.snamp.testing.adapters.http;

import com.google.common.base.Supplier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.itworks.snamp.ExceptionalCallable;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.adapters.ResourceAdapterClient;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.io.IOUtils;
import com.itworks.snamp.jmx.CompositeDataBuilder;
import com.itworks.snamp.jmx.TabularDataBuilder;
import com.itworks.snamp.jmx.json.Formatters;
import com.itworks.snamp.testing.CollectionSizeAwaitor;
import com.itworks.snamp.testing.ImportPackages;
import com.itworks.snamp.testing.SnampDependencies;
import com.itworks.snamp.testing.SnampFeature;
import com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.TestOpenMBean;
import org.atmosphere.wasync.*;
import org.atmosphere.wasync.impl.AtmosphereClient;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.AttributeChangeNotification;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import static com.itworks.snamp.jmx.json.JsonUtils.toJsonArray;
import static com.itworks.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies({SnampFeature.HTTP_ADAPTER, SnampFeature.WRAPPED_LIBS})
@ImportPackages({"com.itworks.snamp.jmx.json;version=\"[1.0,2)\"",
        "org.atmosphere.wasync;version=\"[2.0.0,3)\""})
public final class HttpToJmxAdapter extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final class NotificationReceiver extends ArrayBlockingQueue<JsonElement> implements Function<String>{
        private static final long serialVersionUID = 2056675059549300951L;
        private final Gson formatter;

        private NotificationReceiver(final int capacity,
                                    final Gson formatter) {
            super(capacity);
            this.formatter = formatter;
        }

        @Override
        public void on(final String notification) {
            offer(formatter.toJsonTree(notification));
        }
    }

    private static final String ADAPTER_NAME = "http";
    private static final String ADAPTER_INSTANCE = "test-http";
    private final Gson formatter;

    public HttpToJmxAdapter() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME));
        formatter = new Gson();
    }

    @Override
    protected void fillAdapters(final Map<String, ResourceAdapterConfiguration> adapters, final Supplier<ResourceAdapterConfiguration> adapterFactory) {
        final ResourceAdapterConfiguration restAdapter = adapterFactory.get();
        restAdapter.setAdapterName(ADAPTER_NAME);
        adapters.put(ADAPTER_INSTANCE, restAdapter);
    }

    private void testAttribute(final String attributeID,
                               final JsonElement value) throws IOException {
        final URL attributeQuery = new URL(String.format("http://localhost:8181/snamp/adapters/http/%s/attributes/%s/%s", ADAPTER_INSTANCE, TEST_RESOURCE_NAME, attributeID));
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
        final TimeSpan TIMEOUT = TimeSpan.fromSeconds(15);
        //stop adapter and connector
        ResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        stopResourceConnector(getTestBundleContext());
        //start empty adapter
        syncWithAdapterStartedEvent(ADAPTER_NAME, new ExceptionalCallable<Void, BundleException>() {
            @Override
            public Void call() throws BundleException {
                ResourceAdapterActivator.startResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
                return null;
            }
        }, TIMEOUT);
        //start connector, this causes attribute registration and SNMP adapter restarting,
        //waiting is not required because HTTP adapter supports hot reconfiguring
        startResourceConnector(getTestBundleContext());
        //Reconfiguration of HTTP Adapter is asynchronous event so we
        //should give a chance to catch a connector starting event
        Thread.sleep(2000);
        //check whether the attribute is accessible
        testStringAttribute();
        //now stops the connector again
        stopResourceConnector(getTestBundleContext());
        //stop the adapter
        ResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
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
        testAttribute("4.0", new JsonPrimitive(new BigInteger("100500")));
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
                .columns()
                .addColumn("col1", "desc", SimpleType.BOOLEAN, false)
                .addColumn("col2", "desc", SimpleType.INTEGER, false)
                .addColumn("col3", "desc", SimpleType.STRING, true)
                .queryObject(TabularDataBuilder.class)
                .add(false, 2, "pp")
                .build();
        final Gson formatter = Formatters.enableOpenTypeSystemSupport(new GsonBuilder()).create();
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
        final Gson formatter = Formatters.enableOpenTypeSystemSupport(new GsonBuilder()).create();
        testAttribute("6.1", formatter.toJsonTree(data));

    }

    private void testNotificationTransport(final Request.TRANSPORT transport) throws IOException{
        final AtmosphereClient client = ClientFactory.getDefault().newClient(AtmosphereClient.class);
        final RequestBuilder requestBuilder = client.newRequestBuilder()
                .method(Request.METHOD.GET)
                .uri(String.format("http://localhost:8181/snamp/adapters/http/%s/notifications/%s", ADAPTER_INSTANCE, TEST_RESOURCE_NAME))
                //.trackMessageLength(true)
                .transport(transport);
        final Socket sock = client.create();
        final NotificationReceiver receiver = new NotificationReceiver(10, formatter);
        final CollectionSizeAwaitor awaitor = new CollectionSizeAwaitor(receiver, 3);
        sock.on("message", receiver).open(requestBuilder.build());
        try{
            //force attribute change
            testStringAttribute();
            //wait for notifications
            assertTrue(awaitor.await(TimeSpan.fromSeconds(3)));
        } catch (final InterruptedException | TimeoutException e) {
            fail(e.getMessage());
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
        final ConfigurationEntityDescription desc = ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, ResourceAdapterConfiguration.class);
        assertNotNull(desc);
        final ConfigurationEntityDescription.ParameterDescription param = desc.getParameterDescriptor("dateFormat");
        assertNotNull(param);
        assertFalse(param.getDescription(null).isEmpty());
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
        syncWithAdapterStartedEvent(ADAPTER_NAME, new ExceptionalCallable<Void, BundleException>() {
            @Override
            public Void call() throws BundleException {
                ResourceAdapterActivator.startResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
                return null;
            }
        }, TimeSpan.fromSeconds(15));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        ResourceAdapterActivator.stopResourceAdapter(context, ADAPTER_NAME);
        stopResourceConnector(context);
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
        attribute.getParameters().put("typeName", "dict");
        attributes.put("6.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("table");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("typeName", "table");
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

        event = eventFactory.get();
        event.setCategory("com.itworks.snamp.connectors.tests.impl.plainnotif");
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);
        events.put("21.1", event);
    }
}
