package com.bytex.snamp.testing.web;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.core.FrameworkService;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.json.JsonUtils;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.jmx.AbstractMultiBeanJmxConnectorTest;
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

import javax.management.AttributeChangeNotification;
import javax.management.DynamicMBean;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
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
        SnampFeature.SNMP_GATEWAY,
        SnampFeature.GROOVY_GATEWAY,
        SnampFeature.NAGIOS_GATEWAY,
        SnampFeature.NRDP_GATEWAY,
        SnampFeature.SSH_GATEWAY,
        SnampFeature.STANDARD_TOOLS,
        SnampFeature.GROOVY_CONNECTOR,
        SnampFeature.COMPOSITE_CONNECTOR
})
public final class WebConsoleTest extends AbstractMultiBeanJmxConnectorTest {
    private static final ObjectMapper FORMATTER = new ObjectMapper();
    private static final String GROUP_NAME = "myGroup";
    private static final String WS_ENDPOINT = "ws://localhost:8181/snamp/console/events";
    private static final String ADAPTER_INSTANCE_NAME = "test-snmp";
    private static final String ADAPTER_NAME = "snmp";
    private static final String SNMP_PORT = "3222";
    private static final String SNMP_HOST = "127.0.0.1";
    private static final String TEST_PARAMETER = "testParameter";

    private static final String FIRST_BEAN_NAME = BEAN_NAME + "_1";
    private static final String SECOND_BEAN_NAME = BEAN_NAME + "_2";
    private static final String THIRD_BEAN_NAME = BEAN_NAME + "_3";

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

    private static Map<ObjectName, DynamicMBean> createBeans() throws MalformedObjectNameException {
        return ImmutableMap.of(
                new ObjectName(FIRST_BEAN_NAME), new TestOpenMBean(),
                new ObjectName(SECOND_BEAN_NAME), new TestOpenMBean(),
                new ObjectName(THIRD_BEAN_NAME), new TestOpenMBean()
        );
    }

    private void simulateChanging() throws InterruptedException {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    fail("Something went wrong with thread for simulateChanging");
                }
                this.beanMap.values().forEach(dynamicMBean -> {
                    ((TestOpenMBean)dynamicMBean).setInt32(new Random().nextInt(100));
                    ((TestOpenMBean)dynamicMBean).setBigInt(BigInteger.valueOf(10 + new Random().nextInt(100)));
                    // append new int for third attribute changer pls
                });
            }
        }).start();
    }

    /**
     * Instantiates a new Web console test.
     *
     * @throws MalformedObjectNameException the malformed object name exception
     */
    public WebConsoleTest() throws MalformedObjectNameException {
        super(createBeans());
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
        assertEquals(JsonUtils.toJsonArray(TEST_RESOURCE_NAME), node);
    }

    /**
     * This test should pass as well as the following one. But it does not =(
     * @throws IOException
     */
    @Test
    public void listOfInstancesWithGroupNameTest() throws IOException{
        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        final JsonNode groupName = httpGet("/managedResources/components", authenticationToken);
        assertEquals(JsonUtils.toJsonArray(GROUP_NAME), groupName);
        final JsonNode node = httpGet(String.format("/managedResources/%s", groupName.get(0).asText()), authenticationToken);
        assertNotNull(node);
        assertEquals(JsonUtils.toJsonArray(TEST_RESOURCE_NAME), node);
    }

    @Test
    public void listOfAttributesTest() throws IOException{
        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        final JsonNode node = httpGet("/managedResources/" + TEST_RESOURCE_NAME + "/attributes", authenticationToken);
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
                "}", TEST_RESOURCE_NAME, GROUP_NAME, "3.0", GROUP_NAME, "3.0");
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
        simulateChanging();
        while (true) {
            Thread.sleep(3000);
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
        super.beforeStartTest(context);
        client = new WebSocketClient();
        client.setConnectTimeout(4000);
        client.setDaemon(true);
        client.start();
        beforeCleanupTest(context);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        startResourceConnector(context);
        syncWithGatewayStartedEvent(ADAPTER_NAME, () -> {
            GatewayActivator.enableGateway(context, ADAPTER_NAME);
            return null;
        }, Duration.ofSeconds(30));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        GatewayActivator.disableGateway(context, ADAPTER_NAME);
        stopResourceConnector(context);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        super.afterCleanupTest(context);
        client.stop();
        client = null;
    }

    @Override
    protected String getGroupName() {
        return GROUP_NAME;
    }

    @Override
    protected void fillGateways(final EntityMap<? extends GatewayConfiguration> gateways) {
        final GatewayConfiguration snmpAdapter = gateways.getOrAdd(ADAPTER_INSTANCE_NAME);
        snmpAdapter.setType(ADAPTER_NAME);
        snmpAdapter.put("port", SNMP_PORT);
        snmpAdapter.put("host", SNMP_HOST);
        snmpAdapter.put(TEST_PARAMETER, "parameter");
        snmpAdapter.put("socketTimeout", "5000");
        snmpAdapter.put("context", "1.1");

        // second instance of gateways for better console default content (dummyTest support)
        final GatewayConfiguration snmpAdapterDummy = gateways.getOrAdd("new_snmp_adapter");
        snmpAdapterDummy.setType(ADAPTER_NAME);
        snmpAdapterDummy.put("port", "3232");
        snmpAdapterDummy.put("host", SNMP_HOST);
        snmpAdapterDummy.put(TEST_PARAMETER, "parameter");
        snmpAdapterDummy.put("socketTimeout", "5000");
        snmpAdapterDummy.put("context", "1.2");
        snmpAdapter.put("port", SNMP_PORT);
        snmpAdapter.put("host", SNMP_HOST);
        snmpAdapter.put(TEST_PARAMETER, "parameter");
        snmpAdapter.put("socketTimeout", "5000");
        snmpAdapter.put("context", "1.1");
    }

    @Override
    protected final void startResourceConnector(final BundleContext context) {
        Arrays.stream(new int[] {1,2,3}).forEach(i -> {
            try {
                startResourceConnector(testName,
                        connectorType,
                        TEST_RESOURCE_NAME + "_" + String.valueOf(i),
                        context);
            } catch (final TimeoutException | InterruptedException | BundleException | ExecutionException e) {
                fail(e.getMessage());
            }
        });
    }

    /**
     * Creates a new configuration for running this test.
     *
     * @param config The configuration to modify.
     */
    @Override
    protected final void setupTestConfiguration(final AgentConfiguration config) {
        Arrays.stream(new int[] {1,2,3}).forEach(i -> {
            final ManagedResourceConfiguration targetConfig =
                    config.getEntities(ManagedResourceConfiguration.class).getOrAdd(TEST_RESOURCE_NAME + "_" + String.valueOf(i));
            targetConfig.putAll(connectorParameters);
            targetConfig.setConnectionString(connectionString);
            targetConfig.setType(connectorType);
            targetConfig.setGroupName(getGroupName());
            fillAttributesForBean(BEAN_NAME + "_" + String.valueOf(i), i, targetConfig.getFeatures(AttributeConfiguration.class));
            fillEventsForBean(BEAN_NAME + "_" + String.valueOf(i), targetConfig.getFeatures(EventConfiguration.class));
            fillOperationsForBean(BEAN_NAME + "_" + String.valueOf(i), targetConfig.getFeatures(OperationConfiguration.class));
        });
        fillGateways(config.getEntities(GatewayConfiguration.class));
    }

    private void fillAttributesForBean(final String beanName, final int prefix, final EntityMap<? extends AttributeConfiguration> attributes) {
        final String strPrefix = String.valueOf(prefix);
        AttributeConfiguration attribute = attributes.getOrAdd("1.0");
        attribute.setAlternativeName("string");

        attribute.put("objectName", beanName);
        attribute.put("oid", strPrefix + "1.1.0");

        attribute = attributes.getOrAdd("2.0");
        attribute.setAlternativeName("boolean");
        attribute.put("objectName", beanName);
        attribute.put("oid", strPrefix + "1.2.0");

        attribute = attributes.getOrAdd("3.0");
        attribute.setAlternativeName("int32");
        attribute.put("objectName", beanName);
        attribute.put("oid", strPrefix + "1.3.0");

        attribute = attributes.getOrAdd("4.0");
        attribute.setAlternativeName("bigint");
        attribute.put("objectName", beanName);
        attribute.put("oid", strPrefix + "1.4.0");

        attribute = attributes.getOrAdd("5.1");
        attribute.setAlternativeName("array");
        attribute.put("objectName", beanName);
        attribute.put("oid", strPrefix + "1.5.1");

        attribute = attributes.getOrAdd("6.1");
        attribute.setAlternativeName("dictionary");
        attribute.put("objectName", beanName);
        attribute.put("oid", strPrefix + "1.6.1");

        attribute = attributes.getOrAdd("7.1");
        attribute.setAlternativeName("table");
        attribute.put("objectName", beanName);
        attribute.put("oid", strPrefix + "1.7.1");

        attribute = attributes.getOrAdd("8.0");
        attribute.setAlternativeName("float");
        attribute.put("objectName", beanName);
        attribute.put("oid", strPrefix + "1.8.0");

        attribute = attributes.getOrAdd("9.0");
        attribute.setAlternativeName("date");
        attribute.put("objectName", beanName);
        attribute.put("displayFormat", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        attribute.put("oid", strPrefix + "1.9.0");

        attribute = attributes.getOrAdd("10.0");
        attribute.setAlternativeName("date");
        attribute.put("objectName", beanName);
        attribute.put("displayFormat", "rfc1903-human-readable");
        attribute.put("oid", strPrefix + "1.10.0");

        attribute = attributes.getOrAdd("11.0");
        attribute.setAlternativeName("date");
        attribute.put("objectName", beanName);
        attribute.put("displayFormat", "rfc1903");
        attribute.put("oid", strPrefix + "1.11.0");
    }

    private void fillEventsForBean(final String beanName, final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.put("severity", "notice");
        event.put("objectName", beanName);

        event = events.getOrAdd("com.bytex.snamp.connector.tests.impl.testnotif");
        event.put("severity", "panic");
        event.put("objectName", beanName);

        event = events.getOrAdd("com.bytex.snamp.connector.tests.impl.plainnotif");
        event.put("severity", "notice");
        event.put("objectName", beanName);
    }

    private void fillOperationsForBean(final String beanName, final EntityMap<? extends OperationConfiguration> events) {
        // not ready yet
    }
}
