package com.bytex.snamp.testing.web;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.connector.attributes.checkers.ColoredAttributeChecker;
import com.bytex.snamp.connector.attributes.checkers.IsInRangePredicate;
import com.bytex.snamp.connector.attributes.checkers.NumberComparatorPredicate;
import com.bytex.snamp.connector.health.MalfunctionStatus;
import com.bytex.snamp.connector.metrics.MetricsInterval;
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
import com.bytex.snamp.jmx.WellKnownType;
import com.bytex.snamp.json.JsonUtils;
import com.bytex.snamp.moa.ReduceOperation;
import com.bytex.snamp.supervision.SupervisorClient;
import com.bytex.snamp.supervision.discovery.ResourceDiscoveryException;
import com.bytex.snamp.supervision.discovery.ResourceDiscoveryService;
import com.bytex.snamp.supervision.elasticity.policies.AbstractWeightedScalingPolicy;
import com.bytex.snamp.supervision.elasticity.policies.AttributeBasedScalingPolicy;
import com.bytex.snamp.supervision.elasticity.policies.HealthStatusBasedScalingPolicy;
import com.bytex.snamp.testing.PropagateSystemProperties;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.AbstractResourceConnectorTest;
import com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connector.jmx.TestOpenMBean;
import com.bytex.snamp.testing.supervision.DefaultSupervisorTest;
import com.bytex.snamp.web.serviceModel.charts.*;
import com.bytex.snamp.web.serviceModel.commons.AttributeInformation;
import com.bytex.snamp.web.serviceModel.e2e.ChildComponentsView;
import com.bytex.snamp.web.serviceModel.e2e.ComponentModulesView;
import com.bytex.snamp.web.serviceModel.e2e.LandscapeView;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.TextNode;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.Assume;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.*;
import javax.ws.rs.core.HttpHeaders;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.net.*;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntUnaryOperator;
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
        SnampFeature.STUB_CONNECTOR,
        SnampFeature.HTTP_ACCEPTOR
})
@PropagateSystemProperties(AbstractWebTest.DUMMY_TEST_PROPERTY)
public final class WebConsoleTest extends AbstractWebTest {
    private static final String GROUP_NAME = "web-server";

    private static final String WS_ENDPOINT = "ws://localhost:8181/snamp/console/events";
    private static final String ADAPTER_INSTANCE_NAME = "test-snmp";

    private static final String JMX_CONNECTOR_TYPE = "jmx";
    private static final String HTTP_ACCEPTOR_TYPE = "http";

    private static final String TEST_PARAMETER = "testParameter";

    private static final String FIRST_BEAN_NAME = BEAN_NAME + "_1";
    private static final String FIRST_RESOURCE_NAME = "node#1";

    private static final String SECOND_BEAN_NAME = BEAN_NAME + "_2";
    private static final String SECOND_RESOURCE_NAME = "node#2";

    private static final String THIRD_BEAN_NAME = BEAN_NAME + "_3";
    private static final String THIRD_RESOURCE_NAME = "node#3";

    private static final String FOURTH_RESOURCE_NAME = "iOS";
    private static final String GROUP1_NAME = "mobileApp";
    private static final String FIFTH_RESOURCE_NAME = "node#5";
    private static final String GROUP2_NAME = "dispatcher";
    private static final String SIXTH_RESOURCE_NAME = "paypal";
    private static final String GROUP3_NAME = "paymentSystem";

    private static final String STUB_CONNECTOR_TYPE = "stub";
    private static final String STUB_RESOURCE_NAME = "stubResource";
    private static final String SCALABLE_GROUP_NAME = "scalableGroup";

    private static final String IMPLICIT_GROUP = "implicitGroup";

    private static final class TestApplicationInfo extends ApplicationInfo {
        static void setName(final String componentName, final String instanceName){
            setName(componentName);
            setInstance(instanceName);
        }
    }

    enum TestTopology{
        //FOURTH_RESOURCE_NAME => FIFTH_RESOURCE_NAME, FOURTH_RESOURCE_NAME => SIXTH_RESOURCE_NAME
        ONE_TO_MANY {
            @Override
            void sendTestSpans(final MetricRegistry registry, final IntUnaryOperator delay) throws IOException, InterruptedException {
                TestApplicationInfo.setName(GROUP1_NAME, FOURTH_RESOURCE_NAME);
                final Identifier parentSpanId;
                final Identifier correlationID = Identifier.randomID(4);
                try (final TraceScope scope = registry.tracer(TRACE_NAME).beginTrace(correlationID)) {
                    Thread.sleep(delay.applyAsInt(200));
                    parentSpanId = scope.getSpanID();
                }
                TestApplicationInfo.setName(GROUP2_NAME, FIFTH_RESOURCE_NAME);
                try (final TraceScope ignored = registry.tracer(TRACE_NAME).beginTrace(correlationID, parentSpanId, "RESTController")) {
                    Thread.sleep(delay.applyAsInt(100));
                }
                TestApplicationInfo.setName(GROUP3_NAME, SIXTH_RESOURCE_NAME);
                try (final TraceScope ignored = registry.tracer(TRACE_NAME).beginTrace(correlationID, parentSpanId, "DAO")) {
                    Thread.sleep(delay.applyAsInt(300));
                }
            }
        },
        //FOURTH_RESOURCE_NAME => FIFTH_RESOURCE_NAME, FIFTH_RESOURCE_NAME => SIXTH_RESOURCE_NAME
        LINEAR {
            @Override
            void sendTestSpans(final MetricRegistry registry, final IntUnaryOperator delay) throws IOException, InterruptedException {
                TestApplicationInfo.setName(GROUP1_NAME, FOURTH_RESOURCE_NAME);
                Identifier parentSpanId;
                final Identifier correlationID = Identifier.randomID(4);
                try (final TraceScope scope = registry.tracer(TRACE_NAME).beginTrace(correlationID)) {
                    Thread.sleep(delay.applyAsInt(200));
                    parentSpanId = scope.getSpanID();
                }
                TestApplicationInfo.setName(GROUP2_NAME, FIFTH_RESOURCE_NAME);
                try (final TraceScope scope = registry.tracer(TRACE_NAME).beginTrace(correlationID, parentSpanId)) {
                    Thread.sleep(delay.applyAsInt(100));
                    parentSpanId = scope.getSpanID();
                }
                TestApplicationInfo.setName(GROUP3_NAME, SIXTH_RESOURCE_NAME);
                try (final TraceScope ignored = registry.tracer(TRACE_NAME).beginTrace(correlationID, parentSpanId)) {
                    Thread.sleep(delay.applyAsInt(300));
                }
            }
        },
        //FOURTH_RESOURCE_NAME => FIFTH_RESOURCE_NAME, FIFTH_RESOURCE_NAME => SIXTH_RESOURCE_NAME, SIXTH_RESOURCE_NAME => FOURTH_RESOURCE_NAME
        LOOP {
            @Override
            void sendTestSpans(final MetricRegistry registry, final IntUnaryOperator delay) throws IOException, InterruptedException {
                TestApplicationInfo.setName(GROUP1_NAME, FOURTH_RESOURCE_NAME);
                Identifier parentSpanId;
                final Identifier correlationID = Identifier.randomID(4);
                try (final TraceScope scope = registry.tracer(TRACE_NAME).beginTrace(correlationID)) {
                    Thread.sleep(delay.applyAsInt(200));
                    parentSpanId = scope.getSpanID();
                }
                TestApplicationInfo.setName(GROUP2_NAME, FIFTH_RESOURCE_NAME);
                try (final TraceScope scope = registry.tracer(TRACE_NAME).beginTrace(correlationID, parentSpanId)) {
                    Thread.sleep(delay.applyAsInt(100));
                    parentSpanId = scope.getSpanID();
                }
                TestApplicationInfo.setName(GROUP3_NAME, SIXTH_RESOURCE_NAME);
                try (final TraceScope scope = registry.tracer(TRACE_NAME).beginTrace(correlationID, parentSpanId)) {
                    Thread.sleep(delay.applyAsInt(300));
                    parentSpanId = scope.getSpanID();
                }
                TestApplicationInfo.setName(GROUP1_NAME, FOURTH_RESOURCE_NAME);
                try (final TraceScope ignored = registry.tracer(TRACE_NAME).beginTrace(correlationID, parentSpanId)) {
                    Thread.sleep(delay.applyAsInt(150));
                }
            }
        },
        DELAYED_TOPOLOGY {
            private final AtomicLong counter = new AtomicLong();
            @Override
            void sendTestSpans(final MetricRegistry registry, final IntUnaryOperator delay) throws IOException, InterruptedException {
                if(counter.incrementAndGet() > 4)
                    ONE_TO_MANY.sendTestSpans(registry, delay);
            }
        };
        static final String TRACE_NAME = "myTrace";

        abstract void sendTestSpans(final MetricRegistry registry, final IntUnaryOperator delay) throws IOException, InterruptedException;
    }

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
    private TestAuthenticator authenticator;
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



    @Test
    public void overridingSubEntitiesTest() throws IOException {
        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        final String prefix = "http://localhost:8181/snamp/management/configuration";
        final String attrValue = "{\"readWriteTimeout\": \"PT2M24.5S\",\"parameters\": {\"name\": \"bigint\", \"description\": \"Used amount of memory, in megabytes\", \"units\": \"MBytes\"}, \"override\": true}";
        final JsonNode actualObj = FORMATTER.readTree(attrValue);

        httpPut(prefix, String.format("/resource/%s/attributes/usedMemoryNEW", URLEncoder.encode(FIRST_RESOURCE_NAME, "UTF-8")), authenticationToken, actualObj);
        JsonNode node = httpGet(prefix, "/resource", authenticationToken);
        assertNotNull(node);
        assertTrue(node.get("node#1").get("attributes").get("usedMemoryNEW").has("readWriteTimeout"));
        assertFalse(node.get("node#1").get("attributes").get("usedMemoryNEW").get("readWriteTimeout").isNull());

        httpPut(prefix, String.format("/resource/%s/attributes/usedMemory", URLEncoder.encode(FIRST_RESOURCE_NAME, "UTF-8")), authenticationToken, actualObj);

        node = httpGet(prefix, "/resource", authenticationToken);
        assertNotNull(node);
        assertTrue(node.get("node#1").get("attributes").get("usedMemory").has("readWriteTimeout"));
        assertFalse(node.get("node#1").get("attributes").get("usedMemory").get("readWriteTimeout").isNull());
        assertTrue(node.get("node#1").get("attributes").get("usedMemory").get("override").asBoolean());


    }

    @Test
    public void versionEndpointTest() throws IOException {
        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        final JsonNode node = httpGet("/version", authenticationToken);
        assertNotNull(node);
        assertEquals(new TextNode(Utils.getBundleContext(FrameworkService.class).getBundle().getVersion().toString()), node);
    }

    @Test
    public void listOfGroupsTest() throws IOException{
        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        final JsonNode node = httpGet("/groups", authenticationToken);
        assertNotNull(node);
        assertTrue(node.isArray());
        assertEquals(3, node.size());
    }

    @Test
    public void listOfResourcesTest() throws IOException{
        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        final JsonNode node = httpGet("/groups/resources", authenticationToken);
        assertNotNull(node);
        assertTrue(node.isArray());
        assertTrue(node.size() > 6);
    }

    @Test
    public void listOfResourcesInGroupTest() throws IOException{
        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        final JsonNode node = httpGet(String.format("/groups/%s/resources", GROUP_NAME), authenticationToken);
        assertNotNull(node);
        assertTrue(node.isArray());
        assertEquals(3, node.size());
    }

    @Test
    public void listOfAttributesTest() throws IOException{
        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        final JsonNode node = httpGet("/groups/resources/" + URLEncoder.encode(FIRST_RESOURCE_NAME, "UTF-8") + "/attributes", authenticationToken);
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
    public void listOfGroupAttributes() throws IOException{
        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        JsonNode attributes = httpGet("/groups/" + URLEncoder.encode(IMPLICIT_GROUP, "UTF-8") + "/attributes", authenticationToken);
        assertTrue(attributes instanceof ArrayNode);
        assertEquals(1, attributes.size());
        final JsonNode otherAttributes = httpPost("/groups/resources/attributes",
                authenticationToken,
                JsonUtils.toJsonArray("implicit-group-resource1", "implicit-group-resource2", "implicit-group-resource3"));
        assertEquals(attributes, otherAttributes);
    }

    @Test
    public void recommendationTest() throws IOException{
        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        final JsonNode node = httpGet("/resource-group-watcher/" + URLEncoder.encode(SCALABLE_GROUP_NAME, "UTF-8") + "/scaling-policies/attribute-based/stagPolicy/recommendation", authenticationToken);
        assertTrue(node.isTextual());
    }

    @Test
    public void landscapeViewTest() throws URISyntaxException, InterruptedException, IOException {
        final HttpReporter reporter = new HttpReporter("http://localhost:8181/", ImmutableMap.of());
        reporter.setAsynchronous(false);
        try (final MetricRegistry registry = new MetricRegistry(reporter)) {
            TestTopology.ONE_TO_MANY.sendTestSpans(registry, IntUnaryOperator.identity());
            TestTopology.ONE_TO_MANY.sendTestSpans(registry, IntUnaryOperator.identity());
        }
        Thread.sleep(2000L);    //wait because span processing is asynchronous operation
        final LandscapeView landscapeView = new LandscapeView();
        landscapeView.setName("myLandscapeView");
        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        final JsonNode graph = httpPost("/e2e/compute", authenticationToken, FORMATTER.valueToTree(landscapeView));
        assertNotNull(graph);
    }

    @Test
    public void childComponentsViewTest() throws URISyntaxException, InterruptedException, IOException {
        final HttpReporter reporter = new HttpReporter("http://localhost:8181/", ImmutableMap.of());
        reporter.setAsynchronous(false);
        try (final MetricRegistry registry = new MetricRegistry(reporter)) {
            TestTopology.LINEAR.sendTestSpans(registry, IntUnaryOperator.identity());
        }
        Thread.sleep(2000L);    //wait because span processing is asynchronous operation
        final ChildComponentsView landscapeView = new ChildComponentsView();
        landscapeView.setName("myView");
        landscapeView.setTargetComponent(GROUP2_NAME);
        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        final JsonNode graph = httpPost("/e2e/compute", authenticationToken, FORMATTER.valueToTree(landscapeView));
        assertNotNull(graph);
    }

    @Test
    public void modulesViewTest() throws URISyntaxException, InterruptedException, IOException {
        final HttpReporter reporter = new HttpReporter("http://localhost:8181/", ImmutableMap.of());
        reporter.setAsynchronous(false);
        try (final MetricRegistry registry = new MetricRegistry(reporter)) {
            TestTopology.ONE_TO_MANY.sendTestSpans(registry, IntUnaryOperator.identity());
        }
        Thread.sleep(2000L);    //wait because span processing is asynchronous operation
        final ComponentModulesView landscapeView = new ComponentModulesView();
        landscapeView.setName("myModules");
        landscapeView.setTargetComponent(GROUP2_NAME);
        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        final JsonNode graph = httpPost("/e2e/compute", authenticationToken, FORMATTER.valueToTree(landscapeView));
        assertNotNull(graph);
    }

    @Test
    public void notificationTypesTest() throws IOException{
        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        JsonNode result = httpGet("/notifications/types", authenticationToken);
        assertTrue(result.isArray());
        assertTrue(result instanceof ArrayNode);
        assertTrue(JsonUtils.contains((ArrayNode)result, new TextNode(AttributeChangeNotification.ATTRIBUTE_CHANGE)));
    }

    @Test
    public void dashboardWithChartsTest() throws Exception {
        final LineChartOfAttributeValues lineChart = new LineChartOfAttributeValues();
        lineChart.addResource(FIRST_RESOURCE_NAME);
        lineChart.setName("lineChart");
        lineChart.setGroupName(GROUP_NAME);
        lineChart.setAxisX(new ChronoAxis());
        lineChart.setAxisY(new AttributeValueAxis(new AttributeInformation("requestsPerSecond", WellKnownType.LONG, "rps")));

        final PanelOfAttributeValues panelChart = new PanelOfAttributeValues();
        panelChart.setName("panel");
        panelChart.setGroupName(GROUP_NAME);
        panelChart.setAxisX(new ResourceNameAxis());
        panelChart.setAxisY(new AttributeValueAxis(new AttributeInformation("requestsPerSecond", WellKnownType.LONG, "rps")));

        final ResourceGroupHealthStatusChart hsChart = new ResourceGroupHealthStatusChart();
        hsChart.setGroupName(GROUP_NAME);
        hsChart.setName("healthStatuses");

        final NumberOfResourcesChart resourcesChart = new NumberOfResourcesChart();
        resourcesChart.setGroupName(GROUP_NAME);
        resourcesChart.setName("resources");

        final ScaleInChart scaleInChart = new ScaleInChart();
        scaleInChart.setGroupName(SCALABLE_GROUP_NAME);
        scaleInChart.setName("downscale");
        scaleInChart.setInterval(MetricsInterval.MINUTE);
        scaleInChart.setMetrics(RateChart.RateMetric.LAST_RATE, RateChart.RateMetric.MEAN_RATE);

        final ScaleOutChart scaleOutChart = new ScaleOutChart();
        scaleOutChart.setGroupName(SCALABLE_GROUP_NAME);
        scaleOutChart.setName("upscale");
        scaleOutChart.setInterval(MetricsInterval.MINUTE);
        scaleOutChart.setMetrics(RateChart.RateMetric.LAST_RATE, RateChart.RateMetric.MEAN_RATE);

        final VotingResultChart votingResult = new VotingResultChart();
        votingResult.setGroupName(SCALABLE_GROUP_NAME);
        votingResult.setName("scaling");

        final String authenticationToken = authenticator.authenticateTestUser().getValue();
        final JsonNode node = httpPost("/charts/compute", authenticationToken, FORMATTER.valueToTree(new Chart[]{lineChart, panelChart, hsChart, resourcesChart, scaleInChart, scaleOutChart, votingResult}));
        assertNotNull(node);
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

    @Test
    public void dummyTest() throws InterruptedException, URISyntaxException, IOException, ResourceDiscoveryException, TimeoutException {
        Assume.assumeTrue("Dummy test for webconsole is disabled. Please check the profile if needed", isDummyTestEnabled());
        final Random rnd = new Random(248284792L);
        final HttpReporter reporter = new HttpReporter("http://localhost:8181/", ImmutableMap.of());
        reporter.setAsynchronous(false);
        final MetricRegistry registry = new MetricRegistry(reporter);
        while (!Thread.interrupted()) {
            //1. generate random values for attributes
            beanMap.values().forEach(bean -> {
                bean.setInt32((int) Math.round(Math.abs(rnd.nextGaussian())) * 100);
                bean.setBigInt(BigInteger.valueOf(1000 * rnd.nextInt(1000)));
                bean.setFloat(rnd.nextFloat() * 100F);
                // append new int for third attribute changer pls
            });
            //2. generate spans for correct topology
            TestTopology.DELAYED_TOPOLOGY.sendTestSpans(registry, rnd::nextInt);
            //3. generate resources through resource discovery
            try (final SupervisorClient supervisor = SupervisorClient.tryCreate(getTestBundleContext(), GROUP_NAME)
                    .orElseThrow(AssertionError::new)) {
                final ResourceDiscoveryService discoveryService =
                        supervisor.queryObject(ResourceDiscoveryService.class).orElseThrow(AssertionError::new);
                final String NEW_RESOURCE_NAME = "newResource";
                discoveryService.registerResource(NEW_RESOURCE_NAME,
                        AbstractJmxConnectorTest.getConnectionString(),
                        ImmutableMap.of());
                AbstractResourceConnectorTest.waitForConnector(Duration.ofSeconds(3), NEW_RESOURCE_NAME, getTestBundleContext());
                assertTrue(discoveryService.removeResource(NEW_RESOURCE_NAME));
                AbstractResourceConnectorTest.waitForNoConnector(Duration.ofSeconds(3), NEW_RESOURCE_NAME, getTestBundleContext());
                Thread.sleep(5_000L);
            }
        }
        registry.close();
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
        authenticator = createAuthenticator();
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        beanMap.forEach((key, value) -> {
            try {
                if (mbs.isRegistered(key)) {
                    mbs.unregisterMBean(key);
                } else {
                    mbs.registerMBean(value, key);
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

    private static void startConnectors(final BundleContext context) throws BundleException, TimeoutException, InterruptedException {
        ManagedResourceActivator.enableConnector(context, JMX_CONNECTOR_TYPE);
        ManagedResourceActivator.enableConnector(context, HTTP_ACCEPTOR_TYPE);
        ManagedResourceActivator.enableConnector(context, STUB_CONNECTOR_TYPE);
        AbstractResourceConnectorTest.waitForConnector(Duration.ofSeconds(5), FIRST_RESOURCE_NAME, context);
        AbstractResourceConnectorTest.waitForConnector(Duration.ofSeconds(5), SECOND_RESOURCE_NAME, context);
        AbstractResourceConnectorTest.waitForConnector(Duration.ofSeconds(5), THIRD_RESOURCE_NAME, context);
        AbstractResourceConnectorTest.waitForConnector(Duration.ofSeconds(5), FOURTH_RESOURCE_NAME, context);
        AbstractResourceConnectorTest.waitForConnector(Duration.ofSeconds(5), FIFTH_RESOURCE_NAME, context);
        AbstractResourceConnectorTest.waitForConnector(Duration.ofSeconds(5), SIXTH_RESOURCE_NAME, context);
    }

    private static void stopConnectors(final BundleContext context) throws BundleException, TimeoutException, InterruptedException {
        ManagedResourceActivator.disableConnector(context, JMX_CONNECTOR_TYPE);
        ManagedResourceActivator.disableConnector(context, HTTP_ACCEPTOR_TYPE);
        ManagedResourceActivator.disableConnector(context, STUB_CONNECTOR_TYPE);
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
        syncWithGatewayStartedEvent(HTTP_ACCEPTOR_TYPE, () -> {
            GatewayActivator.enableGateway(context, HTTP_ACCEPTOR_TYPE);
            return null;
        }, Duration.ofSeconds(30));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        GatewayActivator.disableGateway(context, HTTP_ACCEPTOR_TYPE);
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
        authenticator = null;
    }

    private static void fillGateways(final EntityMap<? extends GatewayConfiguration> gateways) {
        GatewayConfiguration adapter = gateways.getOrAdd(ADAPTER_INSTANCE_NAME);
        adapter.setType(HTTP_ACCEPTOR_TYPE);
        adapter.put(TEST_PARAMETER, "parameter");
        adapter.put("socketTimeout", "5000");

        // second instance of gateways for better console default content (dummyTest support)
        adapter = gateways.getOrAdd("new_http_adapter");
        adapter.setType(HTTP_ACCEPTOR_TYPE);
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
        fillManagedResources(config.getResources());
        fillGateways(config.getGateways());
        fillGroups(config.getResourceGroups());
        fillSupervisors(config.getSupervisors());
    }

    private static void fillGroups(final EntityMap<? extends ManagedResourceGroupConfiguration> groups) {
        ManagedResourceGroupConfiguration group = groups.getOrAdd(GROUP_NAME);
        group.setType(JMX_CONNECTOR_TYPE);
        fillJmxAttributes(group.getAttributes());
        fillJmxEvents(group.getEvents());
        group.put("objectName", FIRST_BEAN_NAME);
        group.putAll(AbstractJmxConnectorTest.DEFAULT_PARAMS);

        group = groups.getOrAdd(GROUP2_NAME);
        group.setType(HTTP_ACCEPTOR_TYPE);
        //fillAlternativeJmxAttributes(group.getFeatures(AttributeConfiguration.class));
        fillSpanEvents(group.getEvents());

        group = groups.getOrAdd(SCALABLE_GROUP_NAME);
        group.setType(STUB_CONNECTOR_TYPE);
        fillStubAttributes(group.getAttributes());
    }

    private static void fillSupervisors(final EntityMap<? extends SupervisorConfiguration> watchers) {
        final String groovyTrigger;
        try {
            groovyTrigger = IOUtils.toString(DefaultSupervisorTest.class.getResourceAsStream("GroovyTrigger.groovy"));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        watchers.addAndConsume(GROUP_NAME, supervisor -> {
            supervisor.getHealthCheckConfig().getAttributeCheckers().addAndConsume("requestsPerSecond", scriptlet -> {
                final ColoredAttributeChecker checker = new ColoredAttributeChecker();
                checker.setGreenPredicate(new NumberComparatorPredicate(NumberComparatorPredicate.Operator.LESS_THAN, 1000D));
                checker.setYellowPredicate(new IsInRangePredicate(1000D, true, 2000D, true));
                checker.configureScriptlet(scriptlet);
            });
            supervisor.getHealthCheckConfig().getAttributeCheckers().addAndConsume("CPU", scriptlet -> {
                scriptlet.setLanguage(ScriptletConfiguration.GROOVY_LANGUAGE);
                scriptlet.setScript("attributeValue > 3 ? OK : MALFUNCTION");
            });
            supervisor.getHealthCheckConfig().getTrigger().setLanguage(ScriptletConfiguration.GROOVY_LANGUAGE);
            supervisor.getHealthCheckConfig().getTrigger().setScript(groovyTrigger);
            supervisor.getDiscoveryConfig().setConnectionStringTemplate("{first(addresses.private).addr}");
        });
        final String ELASTMAN_NAME = "GroovyElasticityManager.groovy";
        final String path = "file:" + getPathToFileInProjectRoot("sample-groovy-scripts") + File.separator;
        watchers.addAndConsume(SCALABLE_GROUP_NAME, supervisor -> {
            supervisor.getAutoScalingConfig().setEnabled(true);
            supervisor.put("groovyElasticityManager", ELASTMAN_NAME + ';' + path);
            supervisor.getAutoScalingConfig().setMinClusterSize(0);
            supervisor.getAutoScalingConfig().setMaxClusterSize(10);
            supervisor.getAutoScalingConfig().setCooldownTime(Duration.ofSeconds(5));
            supervisor.getAutoScalingConfig().setScalingSize(1);
            AbstractWeightedScalingPolicy policy = new AttributeBasedScalingPolicy("stag",
                    1.5D,
                    Range.closed(-5D, 5D),
                    Duration.ZERO,
                    ReduceOperation.MEAN,
                    false,
                    Duration.ofMinutes(1));
            supervisor.getAutoScalingConfig().getPolicies().addAndConsume("stagPolicy", policy::configureScriptlet);
            policy = new HealthStatusBasedScalingPolicy(1D, MalfunctionStatus.Level.CRITICAL);
            supervisor.getAutoScalingConfig().getPolicies().addAndConsume("hsPolicy", policy::configureScriptlet);
        });
    }

    private static void fillManagedResources(final EntityMap<? extends ManagedResourceConfiguration> resources){
        ManagedResourceConfiguration resource = resources.getOrAdd(FIRST_RESOURCE_NAME);
        resource.setConnectionString(AbstractJmxConnectorTest.getConnectionString());
        resource.setGroupName(GROUP_NAME);

        resource = resources.getOrAdd(SECOND_RESOURCE_NAME);
        resource.overrideProperties(ImmutableSet.of("objectName"));
        resource.put("objectName", SECOND_BEAN_NAME);
        resource.setConnectionString(AbstractJmxConnectorTest.getConnectionString());
        resource.setGroupName(GROUP_NAME);
        
        resource = resources.getOrAdd(THIRD_RESOURCE_NAME);
        resource.put("objectName", THIRD_BEAN_NAME);
        resource.overrideProperties(ImmutableSet.of("objectName"));
        resource.setConnectionString(AbstractJmxConnectorTest.getConnectionString());
        resource.setGroupName(GROUP_NAME);
        
        resource = resources.getOrAdd(FOURTH_RESOURCE_NAME);
        resource.setGroupName(GROUP1_NAME);
        resource.setType(HTTP_ACCEPTOR_TYPE);
        fillSpanEvents(resource.getEvents());

        resource = resources.getOrAdd(FIFTH_RESOURCE_NAME);
        resource.setGroupName(GROUP2_NAME);
        resource.setType(HTTP_ACCEPTOR_TYPE);
        //events are configured through group configuration

        resource = resources.getOrAdd(SIXTH_RESOURCE_NAME);
        resource.setGroupName(GROUP3_NAME);
        resource.setType(HTTP_ACCEPTOR_TYPE);
        fillSpanEvents(resource.getEvents());

        resource = resources.getOrAdd(STUB_RESOURCE_NAME);
        resource.setGroupName(SCALABLE_GROUP_NAME);

        resource = resources.getOrAdd("implicit-group-resource1");
        resource.setGroupName(IMPLICIT_GROUP);
        resource.setType(STUB_CONNECTOR_TYPE);
        resource.getAttributes().addAndConsume("a", attribute -> attribute.setAlternativeName("randomBytes"));

        resource = resources.getOrAdd("implicit-group-resource2");
        resource.setGroupName(IMPLICIT_GROUP);
        resource.setType(STUB_CONNECTOR_TYPE);
        resource.getAttributes().addAndConsume("a", attribute -> attribute.setAlternativeName("randomBytes"));
        resource.getAttributes().addAndConsume("b", attribute -> attribute.setAlternativeName("randomBoolean"));

        resource = resources.getOrAdd("implicit-group-resource3");
        resource.setGroupName(IMPLICIT_GROUP);
        resource.setType(STUB_CONNECTOR_TYPE);
        resource.getAttributes().addAndConsume("a", attribute -> attribute.setAlternativeName("randomBytes"));
        resource.getAttributes().addAndConsume("b", attribute -> attribute.setAlternativeName("randomBoolean"));
        resource.getAttributes().addAndConsume("c", attribute -> attribute.setAlternativeName("intValue"));
    }

    private static void fillSpanEvents(final EntityMap<? extends EventConfiguration> events) {
        events.getOrAdd(SpanNotification.TYPE);
    }

    private static void fillJmxAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("1.0");
        attribute.setAlternativeName("string");

        attribute = attributes.getOrAdd("online");
        attribute.setDescription("Is service online?");
        attribute.setAlternativeName("boolean");

        attribute = attributes.getOrAdd("requestsPerSecond");
        attribute.setUnitOfMeasurement("requests");
        attribute.setDescription("Number of requests per second handled by Web Server");
        attribute.setAlternativeName("int32");

        attribute = attributes.getOrAdd("usedMemory");
        attribute.setUnitOfMeasurement("MBytes");
        attribute.setDescription("Used amount of memory, in megabytes");
        attribute.setAlternativeName("bigint");

        attribute = attributes.getOrAdd("CPU");
        attribute.setUnitOfMeasurement("%");
        attribute.setDescription("CPU utilization");
        attribute.setAlternativeName("float");
    }

    private static void fillJmxEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.put("severity", "notice");

        event = events.getOrAdd("com.bytex.snamp.connector.tests.impl.testnotif");
        event.put("severity", "panic");

        event = events.getOrAdd("com.bytex.snamp.connector.tests.impl.plainnotif");
        event.put("severity", "notice");
    }

    private static void fillStubAttributes(final EntityMap<? extends AttributeConfiguration> attributes){
        attributes.addAndConsume("stag", attribute -> attribute.setAlternativeName("staggeringValue"));
    }
}
