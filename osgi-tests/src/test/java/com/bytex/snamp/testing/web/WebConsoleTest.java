package com.bytex.snamp.testing.web;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.core.FrameworkService;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.instrumentation.ApplicationInfo;
import com.bytex.snamp.instrumentation.Identifier;
import com.bytex.snamp.instrumentation.MetricRegistry;
import com.bytex.snamp.instrumentation.TraceScope;
import com.bytex.snamp.instrumentation.measurements.jmx.SpanNotification;
import com.bytex.snamp.instrumentation.reporters.http.HttpReporter;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.json.JsonUtils;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.AbstractResourceConnectorTest;
import com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connector.jmx.TestOpenMBean;
import com.google.common.collect.ImmutableMap;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.TextNode;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.*;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import static com.bytex.snamp.testing.connector.jmx.TestOpenMBean.BEAN_NAME;

/**
 * The type Web console test.
 *
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies({
        SnampFeature.WRAPPED_LIBS,
        SnampFeature.HTTP_GATEWAY,
        SnampFeature.GROOVY_GATEWAY,
        SnampFeature.NAGIOS_GATEWAY,
        SnampFeature.NRDP_GATEWAY,
        SnampFeature.SSH_GATEWAY,
        SnampFeature.STANDARD_TOOLS,
        SnampFeature.GROOVY_CONNECTOR,
        SnampFeature.COMPOSITE_CONNECTOR,
        SnampFeature.JMX_CONNECTOR,
        SnampFeature.HTTP_ACCEPTOR
})
public final class WebConsoleTest extends AbstractSnampIntegrationTest {
    private static final class TestApplicationInfo extends ApplicationInfo {
        static void setName(final String componentName, final String instanceName){
            setName(componentName);
            setInstance(instanceName);
        }
    }

    private static final ObjectMapper FORMATTER = new ObjectMapper();
    private static final String TEST_RESOURCE_NAME = "myResource";

    private static final String GROUP_NAME = "myGroup";

    private static final String WS_ENDPOINT = "ws://localhost:8181/snamp/console/events";
    private static final String ADAPTER_INSTANCE_NAME = "test-snmp";

    private static final String JMX_CONNECTOR_TYPE = "jmx";
    private static final String HTTP_ACCEPTOR_TYPE = "http";

    private static final String ADAPTER_NAME = "http";
    private static final String TEST_PARAMETER = "testParameter";

    private static final String FIRST_BEAN_NAME = BEAN_NAME + "_1";
    private static final String FIRST_RESOURCE_NAME = TEST_RESOURCE_NAME + "_1";

    private static final String SECOND_BEAN_NAME = BEAN_NAME + "_2";
    private static final String SECOND_RESOURCE_NAME = TEST_RESOURCE_NAME + "_2";

    private static final String THIRD_BEAN_NAME = BEAN_NAME + "_3";
    private static final String THIRD_RESOURCE_NAME = TEST_RESOURCE_NAME + "_3";

    private static final String FOURTH_RESOURCE_NAME = "iOS";
    private static final String GROUP1_NAME = "mobileApp";
    private static final String FIFTH_RESOURCE_NAME = "node2";
    private static final String GROUP2_NAME = "dispatcher";
    private static final String SIXTH_RESOURCE_NAME = "paypal";
    private static final String GROUP3_NAME = "paymentSystem";

    //must be public
    @WebSocket
    public static final class EventReceiver extends LinkedBlockingQueue<JsonNode> {
        private static final long serialVersionUID = 2056675059549300951L;

        @OnWebSocketMessage
        @SpecialUse(SpecialUse.Case.REFLECTION)
        public void onMessage(final String event) throws IOException {
            offer(FORMATTER.readTree(event));
        }
    }

    private WebSocketClient client;
    private final TestAuthenticator authenticator;
    private final Map<ObjectName, TestOpenMBean> beanMap;

    /**
     * Instantiates a new Web console test.
     *
     * @throws MalformedObjectNameException the malformed object name exception
     */
    public WebConsoleTest() throws MalformedObjectNameException {
        beanMap = ImmutableMap.of(
                new ObjectName(FIRST_BEAN_NAME), new TestOpenMBean(),
                new ObjectName(SECOND_BEAN_NAME), new TestOpenMBean(),
                new ObjectName(THIRD_BEAN_NAME), new TestOpenMBean()
        );
        authenticator = new TestAuthenticator();
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    private <W, E extends Exception> void runWebSocketTest(final W webSocketHandler,
                                                           final String authenticationToken,
                                                           final Acceptor<? super W, E> testBody,
                                                           final Duration sessionWait,
                                                           final JsonNode... cachedUserData) throws Exception {
        final ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
        upgradeRequest.setHeader(HttpHeaders.AUTHORIZATION, authenticationToken);
        try (final Session session = client.connect(webSocketHandler, new URI(WS_ENDPOINT), upgradeRequest).get(10, TimeUnit.SECONDS)) {
            for (final JsonNode obj : cachedUserData)
                session.getRemote().sendString(FORMATTER.writeValueAsString(obj));
            Thread.sleep(sessionWait.toMillis());
            testBody.accept(webSocketHandler);
        }
    }

    private static JsonNode httpPost(final String servicePostfix, final String authenticationToken, final JsonNode data) throws IOException {
        final URL attributeQuery = new URL("http://localhost:8181/snamp/web/api" + servicePostfix);
        //write attribute
        HttpURLConnection connection = (HttpURLConnection) attributeQuery.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, "application/json");
        connection.setRequestProperty(HttpHeaders.AUTHORIZATION, authenticationToken);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        IOUtils.writeString(FORMATTER.writeValueAsString(data), connection.getOutputStream(), Charset.defaultCharset());
        connection.connect();
        try {
            assertEquals(200, connection.getResponseCode());
            return FORMATTER.readTree(connection.getInputStream());
        } finally {
            connection.disconnect();
        }
    }

    private static void httpPut(final String servicePostfix, final String authenticationToken, final JsonNode data) throws IOException {
        final URL attributeQuery = new URL("http://localhost:8181/snamp/web/api" + servicePostfix);
        //write attribute
        HttpURLConnection connection = (HttpURLConnection) attributeQuery.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, "application/json");
        connection.setRequestProperty(HttpHeaders.AUTHORIZATION, authenticationToken);
        connection.setDoOutput(true);
        IOUtils.writeString(FORMATTER.writeValueAsString(data), connection.getOutputStream(), Charset.defaultCharset());
        connection.connect();
        try {
            assertEquals(204, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }
    }

    private static JsonNode httpGet(final String servicePostfix, final String authenticationToken) throws IOException{
        final URL attributeQuery = new URL("http://localhost:8181/snamp/web/api" + servicePostfix);
        //write attribute
        HttpURLConnection connection = (HttpURLConnection) attributeQuery.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty(HttpHeaders.AUTHORIZATION, authenticationToken);
        connection.setDoInput(true);
        connection.connect();
        try(final Reader reader = new InputStreamReader(connection.getInputStream(), IOUtils.DEFAULT_CHARSET)){
            assertEquals(200, connection.getResponseCode());
            return FORMATTER.readTree(reader);
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void versionEndpointTest() throws IOException {
        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        final JsonNode node = httpGet("/version", authenticationToken);
        assertNotNull(node);
        assertEquals(new TextNode(Utils.getBundleContext(FrameworkService.class).getBundle().getVersion().toString()), node);
    }

    @Test
    public void listOfComponentsTest() throws IOException{
        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        final JsonNode node = httpGet("/managedResources/components", authenticationToken);
        assertNotNull(node);
        assertEquals(JsonUtils.toJsonArray(GROUP_NAME), node);
    }

    @Test
    public void listOfInstancesTest() throws IOException{
        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        final JsonNode node = httpGet("/managedResources", authenticationToken);
        assertNotNull(node);
        assertEquals(JsonUtils.toJsonArray(FIRST_RESOURCE_NAME, SECOND_RESOURCE_NAME, THIRD_RESOURCE_NAME), node);
    }

    @Test
    public void listOfInstancesWithGroupNameTest() throws IOException{
        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        final JsonNode groupName = httpGet("/managedResources/components", authenticationToken);
        assertEquals(JsonUtils.toJsonArray(SECOND_RESOURCE_NAME, THIRD_RESOURCE_NAME, GROUP_NAME), groupName);
        final JsonNode node = httpGet(String.format("/managedResources?component=%s", GROUP_NAME), authenticationToken);
        assertNotNull(node);
        assertEquals(JsonUtils.toJsonArray(FIRST_RESOURCE_NAME), node);
    }

    @Test
    public void listOfAttributesTest() throws IOException{
        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        final JsonNode node = httpGet("/managedResources/" + FIRST_RESOURCE_NAME + "/attributes", authenticationToken);
        assertTrue(node instanceof ArrayNode);
        assertTrue("Unexpected JSON " + node, node.size() > 0);
        for(int i = 0; i < node.size(); i++){
            final JsonNode name = node.get(i).get("name");
            final JsonNode type = node.get(i).get("type");
            assertTrue(name instanceof TextNode);
            assertTrue(type instanceof TextNode);
        }
    }

    @Test
    public void endToEndTest() throws URISyntaxException, InterruptedException, IOException {
        final HttpReporter reporter = new HttpReporter("http://localhost:8181/", ImmutableMap.of());
        reporter.setAsynchronous(false);
        try (final MetricRegistry registry = new MetricRegistry(reporter)) {
            TestApplicationInfo.setName(GROUP1_NAME, FOURTH_RESOURCE_NAME);
            final Identifier parentSpanId;
            final Identifier correlationID = Identifier.randomID(4);
            try (final TraceScope scope = registry.tracer("myTrace").beginTrace(correlationID)) {
                Thread.sleep(200L);
                parentSpanId = scope.getSpanID();
            }
            TestApplicationInfo.setName(GROUP2_NAME, FIFTH_RESOURCE_NAME);
            try (final TraceScope ignored = registry.tracer("myTrace").beginTrace(correlationID, parentSpanId)) {
                Thread.sleep(100L);
            }
            TestApplicationInfo.setName(GROUP3_NAME, SIXTH_RESOURCE_NAME);
            try (final TraceScope ignored = registry.tracer("myTrace").beginTrace(correlationID, parentSpanId)) {
                Thread.sleep(300L);
            }
        }
        Thread.sleep(2000L);    //wait because span processing is asynchronous operation
        final String landscapeView = "{\n" +
                "  \"@type\" : \"landscape\",\n" +
                "  \"preferences\" : { },\n" +
                "  \"name\" : \"myLandscape\"\n" +
                "}";
        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        final JsonNode graph = httpPost("/e2e/compute", authenticationToken, FORMATTER.readTree(landscapeView));
        assertNotNull(graph);
    }

    @Test
    public void dashboardWithChartsTest() throws Exception {
        final String dashboardDefinition = String.format("{\n" +
                "  \"@type\" : \"dashboardOfCharts\",\n" +
                "  \"charts\" : [ {\n" +
                "    \"@type\" : \"lineChartOfAttributeValues\",\n" +
                "    \"instances\" : [ \"%s\" ],\n" +
                "    \"name\" : \"attributes\",\n" +
                "    \"component\" : \"%s\",\n" +
                "    \"X\" : {\n" +
                "      \"@type\" : \"chrono\",\n" +
                "      \"name\" : \"\"\n" +
                "    },\n" +
                "    \"Y\" : {\n" +
                "      \"@type\" : \"attributeValue\",\n" +
                "      \"name\" : \"\",\n" +
                "      \"sourceAttribute\" : {\n" +
                "        \"name\" : \"%s\",\n" +
                "        \"type\" : \"int64\",\n" +
                "        \"unitOfMeasurement\" : \"bytes\"\n" +
                "      }\n" +
                "    }\n" +
                "  }, {\n" +
                "    \"@type\" : \"panelOfAttributeValues\",\n" +
                "    \"instances\" : [],\n" +
                "    \"name\" : \"myPanel\",\n" +
                "    \"component\" : \"%s\",\n" +
                "    \"X\" : {\n" +
                "      \"@type\" : \"instance\",\n" +
                "      \"name\" : \"instances\"\n" +
                "    },\n" +
                "    \"Y\" : {\n" +
                "      \"@type\" : \"attributeValue\",\n" +
                "      \"name\" : \"\",\n" +
                "      \"sourceAttribute\" : {\n" +
                "        \"name\" : \"%s\",\n" +
                "        \"type\" : \"int32\",\n" +
                "        \"unitOfMeasurement\" : \"units\"\n" +
                "      }\n" +
                "    }\n" +
                "  } ]\n" +
                "}", FIRST_RESOURCE_NAME, GROUP_NAME, "3.0", GROUP_NAME, "3.0");
        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        httpPut("/charts/settings", authenticationToken, FORMATTER.readTree(dashboardDefinition));
        runWebSocketTest(new EventReceiver(), authenticationToken, events -> {
            LoggerProvider.getLoggerForBundle(getTestBundleContext()).log(Level.SEVERE, "Test log", new Exception());
            final JsonNode element = events.poll(3L, TimeUnit.SECONDS);
            assertNotNull(element);
            assertEquals("Test log", element.get("message").asText());
            assertEquals("error", element.get("level").asText());
        }, Duration.ofSeconds(3));
    }

    /**
     * Log notification test.
     */
    @Test
    public void logNotificationTest() throws Exception {
        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        //read logging settings
        final JsonNode settings = httpGet("/logging/settings", authenticationToken);
        assertTrue(settings.isObject());
        assertEquals("error", settings.get("logLevel").asText());
        runWebSocketTest(new EventReceiver(), authenticationToken, events -> {
            LoggerProvider.getLoggerForBundle(getTestBundleContext()).log(Level.SEVERE, "Test log", new Exception());
            final JsonNode element = events.poll(5L, TimeUnit.SECONDS);
            assertNotNull(element);
            assertEquals("Test log", element.get("message").asText());
            assertEquals("error", element.get("level").asText());
        }, Duration.ofSeconds(1), settings);
    }

    /**
     * Test check simple resource with and without token.
     *
     * @throws IOException              the io exception
     * @throws InterruptedException     the interrupted exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws InvalidKeyException      the invalid key exception
     * @throws SignatureException       the signature exception
     */
    @Test
    public void testGetStaticFiles() throws IOException, InterruptedException, NoSuchAlgorithmException,
            InvalidKeyException, SignatureException {

        Thread.sleep(2000);
        // Test welcome files (no files are specified manually)
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8181/snamp/").openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(true);
        connection.connect();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            final String attributeValue = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            assertNotNull(attributeValue);
            assertTrue(attributeValue.contains("<h3 class=\"masthead-brand\">SNAMP WEB UI</h3>"));

        } finally {
            connection.disconnect();
        }
        // Test some html file
        connection = (HttpURLConnection) new URL("http://localhost:8181/snamp/login.html").openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(true);
        connection.connect();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            final String attributeValue = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            assertNotNull(attributeValue);
            assertTrue(attributeValue.contains("<h1>Login to SNAMP UI</h1>"));

        } finally {
            connection.disconnect();
        }
        // Test assets
        connection = (HttpURLConnection) new URL("http://localhost:8181/snamp/js/jquery.js").openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(true);
        connection.connect();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            final String attributeValue = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            assertNotNull(attributeValue);
            assertTrue(attributeValue.contains("/*! jQuery v3.0.0 | (c) jQuery Foundation | jquery.org/license */"));

        } finally {
            connection.disconnect();
        }

        // Test file that does not exist
        connection = (HttpURLConnection) new URL("http://localhost:8181/snamp/asdasdasdasd.ext").openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(true);
        connection.connect();
        try {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }

    }

    /**
     * Dummy test.
     *
     * @throws InterruptedException the interrupted exception
     */
    @Test
    public void dummyTest() throws InterruptedException {
        final Random rnd = new Random(248284792L);
        while (true) {
            beanMap.values().forEach(bean -> {
                bean.setInt32(rnd.nextInt(100));
                bean.setBigInt(BigInteger.valueOf(10 + rnd.nextInt(100)));
                // append new int for third attribute changer pls
            });
            Thread.sleep(500L);
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        beanMap.entrySet().forEach(entry -> {
            try {
                if(mbs.isRegistered(entry.getKey())) {
                    mbs.unregisterMBean(entry.getKey());
                } else {
                    mbs.registerMBean(entry.getValue(), entry.getKey());
                }
            } catch (final JMException e) {
                fail(e.getMessage());
            }
        });
        client = new WebSocketClient();
        client.setConnectTimeout(4000);
        client.setDaemon(true);
        client.start();
        beforeCleanupTest(context);
    }

    private void startConnectors(final BundleContext context) throws BundleException, TimeoutException, InterruptedException {
        ManagedResourceActivator.enableConnector(context, JMX_CONNECTOR_TYPE);
        ManagedResourceActivator.enableConnector(context, HTTP_ACCEPTOR_TYPE);
        AbstractResourceConnectorTest.waitForConnector(Duration.ofSeconds(5), FIRST_RESOURCE_NAME, context);
        AbstractResourceConnectorTest.waitForConnector(Duration.ofSeconds(5), SECOND_RESOURCE_NAME, context);
        AbstractResourceConnectorTest.waitForConnector(Duration.ofSeconds(5), THIRD_RESOURCE_NAME, context);
        AbstractResourceConnectorTest.waitForConnector(Duration.ofSeconds(5), FOURTH_RESOURCE_NAME, context);
        AbstractResourceConnectorTest.waitForConnector(Duration.ofSeconds(5), FIFTH_RESOURCE_NAME, context);
        AbstractResourceConnectorTest.waitForConnector(Duration.ofSeconds(5), SIXTH_RESOURCE_NAME, context);
    }

    private void stopConnectors(final BundleContext context) throws BundleException, TimeoutException, InterruptedException {
        ManagedResourceActivator.disableConnector(context, JMX_CONNECTOR_TYPE);
        ManagedResourceActivator.disableConnector(context, HTTP_ACCEPTOR_TYPE);
        AbstractResourceConnectorTest.waitForNoConnector(Duration.ofSeconds(5), SIXTH_RESOURCE_NAME, context);
        AbstractResourceConnectorTest.waitForNoConnector(Duration.ofSeconds(5), FIFTH_RESOURCE_NAME, context);
        AbstractResourceConnectorTest.waitForNoConnector(Duration.ofSeconds(5), FOURTH_RESOURCE_NAME, context);
        AbstractResourceConnectorTest.waitForNoConnector(Duration.ofSeconds(5), THIRD_RESOURCE_NAME, context);
        AbstractResourceConnectorTest.waitForNoConnector(Duration.ofSeconds(5), SECOND_RESOURCE_NAME, context);
        AbstractResourceConnectorTest.waitForNoConnector(Duration.ofSeconds(5), FIRST_RESOURCE_NAME, context);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        startConnectors(context);
        syncWithGatewayStartedEvent(ADAPTER_NAME, () -> {
            GatewayActivator.enableGateway(context, ADAPTER_NAME);
            return null;
        }, Duration.ofSeconds(30));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        GatewayActivator.disableGateway(context, ADAPTER_NAME);
        stopConnectors(context);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        beanMap.keySet().forEach(beanName -> {
            try {
                mbs.unregisterMBean(beanName);
            } catch (final InstanceNotFoundException | MBeanRegistrationException e) {
                fail(e.getMessage());
            }
        });
        client.stop();
        client = null;
    }

    private void fillGateways(final EntityMap<? extends GatewayConfiguration> gateways) {
        GatewayConfiguration adapter = gateways.getOrAdd(ADAPTER_INSTANCE_NAME);
        adapter.setType(ADAPTER_NAME);
        adapter.put(TEST_PARAMETER, "parameter");
        adapter.put("socketTimeout", "5000");

        // second instance of gateways for better console default content (dummyTest support)
        adapter = gateways.getOrAdd("new_http_adapter");
        adapter.setType(ADAPTER_NAME);
        adapter.put(TEST_PARAMETER, "parameter");
        adapter.put("socketTimeout", "5000");
    }

    /**
     * Creates a new configuration for running this test.
     *
     * @param config The configuration to modify.
     */
    @Override
    protected void setupTestConfiguration(final AgentConfiguration config) {
        fillManagedResources(config.getEntities(ManagedResourceConfiguration.class));
        fillGateways(config.getEntities(GatewayConfiguration.class));
    }

    private void fillManagedResources(final EntityMap<? extends ManagedResourceConfiguration> resources){
        ManagedResourceConfiguration resource = resources.getOrAdd(FIRST_RESOURCE_NAME);
        resource.put("objectName", FIRST_BEAN_NAME);
        resource.setConnectionString(AbstractJmxConnectorTest.getConnectionString());
        resource.setGroupName(GROUP_NAME);
        resource.setType(JMX_CONNECTOR_TYPE);
        resource.put("login", AbstractJmxConnectorTest.JMX_LOGIN);
        resource.put("password", AbstractJmxConnectorTest.JMX_PASSWORD);
        fillJmxAttributes(resource.getFeatures(AttributeConfiguration.class));
        fillJmxEvents(resource.getFeatures(EventConfiguration.class));

        resource = resources.getOrAdd(SECOND_RESOURCE_NAME);
        resource.put("objectName", SECOND_BEAN_NAME);
        resource.setConnectionString(AbstractJmxConnectorTest.getConnectionString());
        resource.setGroupName(GROUP_NAME);
        resource.setType(JMX_CONNECTOR_TYPE);
        resource.put("login", AbstractJmxConnectorTest.JMX_LOGIN);
        resource.put("password", AbstractJmxConnectorTest.JMX_PASSWORD);
        fillJmxAttributes(resource.getFeatures(AttributeConfiguration.class));
        fillJmxEvents(resource.getFeatures(EventConfiguration.class));

        resource = resources.getOrAdd(THIRD_RESOURCE_NAME);
        resource.put("objectName", THIRD_BEAN_NAME);
        resource.setConnectionString(AbstractJmxConnectorTest.getConnectionString());
        resource.setGroupName(GROUP_NAME);
        resource.setType(JMX_CONNECTOR_TYPE);
        resource.put("login", AbstractJmxConnectorTest.JMX_LOGIN);
        resource.put("password", AbstractJmxConnectorTest.JMX_PASSWORD);
        fillJmxAttributes(resource.getFeatures(AttributeConfiguration.class));
        fillJmxEvents(resource.getFeatures(EventConfiguration.class));

        resource = resources.getOrAdd(FOURTH_RESOURCE_NAME);
        resource.setGroupName(GROUP1_NAME);
        resource.setType(HTTP_ACCEPTOR_TYPE);
        fillSpanEvents(resource.getFeatures(EventConfiguration.class));

        resource = resources.getOrAdd(FIFTH_RESOURCE_NAME);
        resource.setGroupName(GROUP2_NAME);
        resource.setType(HTTP_ACCEPTOR_TYPE);
        fillSpanEvents(resource.getFeatures(EventConfiguration.class));

        resource = resources.getOrAdd(SIXTH_RESOURCE_NAME);
        resource.setGroupName(GROUP3_NAME);
        resource.setType(HTTP_ACCEPTOR_TYPE);
        fillSpanEvents(resource.getFeatures(EventConfiguration.class));
    }

    private static void fillSpanEvents(final EntityMap<? extends EventConfiguration> events) {
        events.getOrAdd(SpanNotification.TYPE);
    }

    private static void fillJmxAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("1.0");
        attribute.setAlternativeName("string");

        attribute = attributes.getOrAdd("2.0");
        attribute.setAlternativeName("boolean");

        attribute = attributes.getOrAdd("3.0");
        attribute.setAlternativeName("int32");

        attribute = attributes.getOrAdd("4.0");
        attribute.setAlternativeName("bigint");

        attribute = attributes.getOrAdd("5.1");
        attribute.setAlternativeName("array");

        attribute = attributes.getOrAdd("6.1");
        attribute.setAlternativeName("dictionary");

        attribute = attributes.getOrAdd("7.1");
        attribute.setAlternativeName("table");

        attribute = attributes.getOrAdd("8.0");
        attribute.setAlternativeName("float");

        attribute = attributes.getOrAdd("9.0");
        attribute.setAlternativeName("date");
        attribute.put("displayFormat", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        attribute = attributes.getOrAdd("10.0");
        attribute.setAlternativeName("date");
        attribute.put("displayFormat", "rfc1903-human-readable");

        attribute = attributes.getOrAdd("11.0");
        attribute.setAlternativeName("date");
        attribute.put("displayFormat", "rfc1903");
    }

    private static void fillJmxEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.put("severity", "notice");

        event = events.getOrAdd("com.bytex.snamp.connector.tests.impl.testnotif");
        event.put("severity", "panic");

        event = events.getOrAdd("com.bytex.snamp.connector.tests.impl.plainnotif");
        event.put("severity", "notice");
    }
}
