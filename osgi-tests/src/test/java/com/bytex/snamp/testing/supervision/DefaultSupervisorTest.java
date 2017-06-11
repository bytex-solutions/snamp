package com.bytex.snamp.testing.supervision;

import com.bytex.snamp.Box;
import com.bytex.snamp.Convert;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.attributes.checkers.ColoredAttributeChecker;
import com.bytex.snamp.connector.attributes.checkers.IsInRangePredicate;
import com.bytex.snamp.connector.attributes.checkers.NumberComparatorPredicate;
import com.bytex.snamp.connector.health.InvalidAttributeValue;
import com.bytex.snamp.connector.health.OkStatus;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.json.ThreadLocalJsonFactory;
import com.bytex.snamp.supervision.GroupCompositionChangedEvent;
import com.bytex.snamp.supervision.SupervisionEventListener;
import com.bytex.snamp.supervision.SupervisorClient;
import com.bytex.snamp.supervision.discovery.ResourceDiscoveryException;
import com.bytex.snamp.supervision.discovery.ResourceDiscoveryService;
import com.bytex.snamp.supervision.health.HealthStatusProvider;
import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connector.jmx.TestOpenMBean;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@SnampDependencies({SnampFeature.STANDARD_TOOLS, SnampFeature.INTEGRATION_TOOLS})
public final class DefaultSupervisorTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String GROUP_NAME = "trip-manager";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public DefaultSupervisorTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(TestOpenMBean.BEAN_NAME));
    }

    @Test
    public void defaultSupervisorConfig() {
        final ConfigurationEntityDescription<SupervisorConfiguration> description =
                SupervisorClient.getConfigurationDescriptor(getTestBundleContext(), SupervisorConfiguration.DEFAULT_TYPE);
        testConfigurationDescriptor(description, "checkPeriod");
    }

    @Test
    public void coloredChecker() throws JMException, InterruptedException {
        try (final SupervisorClient supervisor = SupervisorClient.tryCreate(getTestBundleContext(), GROUP_NAME)
                .orElseThrow(AssertionError::new)) {
            assertTrue(supervisor.get().getResources().contains(TEST_RESOURCE_NAME));

            testAttribute("3.0", TypeToken.of(Integer.class), 90);
            Thread.sleep(1000L);
            ResourceGroupHealthStatus status = supervisor.queryObject(HealthStatusProvider.class).map(HealthStatusProvider::getStatus).orElseThrow(AssertionError::new);
            assertTrue(status.getSummaryStatus() instanceof OkStatus);

            testAttribute("3.0", TypeToken.of(Integer.class), 1000);
            Thread.sleep(1000L);
            status = supervisor.queryObject(HealthStatusProvider.class).map(HealthStatusProvider::getStatus).orElseThrow(AssertionError::new);
            assertTrue(status.getSummaryStatus() instanceof InvalidAttributeValue);

            testAttribute("3.0", TypeToken.of(Integer.class), 2001);
            Thread.sleep(1000L);
            status = supervisor.queryObject(HealthStatusProvider.class).map(HealthStatusProvider::getStatus).orElseThrow(AssertionError::new);
            assertTrue(status.getSummaryStatus() instanceof InvalidAttributeValue);
        }
    }

    @Test
    public void groovyChecker() throws JMException, InterruptedException {
        try (final SupervisorClient supervisor = SupervisorClient.tryCreate(getTestBundleContext(), GROUP_NAME)
                .orElseThrow(AssertionError::new)) {
            assertTrue(supervisor.get().getResources().contains(TEST_RESOURCE_NAME));

            testAttribute("8.0", TypeToken.of(Float.class), 40F);
            Thread.sleep(1000L);
            ResourceGroupHealthStatus status = supervisor.queryObject(HealthStatusProvider.class).map(HealthStatusProvider::getStatus).orElseThrow(AssertionError::new);
            assertTrue(status.getSummaryStatus() instanceof OkStatus);

            testAttribute("8.0", TypeToken.of(Float.class), 50F);
            Thread.sleep(1000L);
            status = supervisor.queryObject(HealthStatusProvider.class).map(HealthStatusProvider::getStatus).orElseThrow(AssertionError::new);
            assertTrue(status.getSummaryStatus() instanceof InvalidAttributeValue);
        }
    }

    @Test
    public void serviceDiscovery() throws ResourceDiscoveryException, TimeoutException, InterruptedException {
        try (final SupervisorClient supervisor = SupervisorClient.tryCreate(getTestBundleContext(), GROUP_NAME)
                .orElseThrow(AssertionError::new)) {
            final Box<String> addedResource = Box.of("");
            final SupervisionEventListener listener = (event, handback) ->
                Convert.toType(event, GroupCompositionChangedEvent.class).ifPresent(changed -> {
                    addedResource.set(changed.getResourceName());
                });
            supervisor.addSupervisionEventListener(listener);
            final ResourceDiscoveryService discoveryService =
                    supervisor.queryObject(ResourceDiscoveryService.class).orElseThrow(AssertionError::new);
            final String NEW_RESOURCE_NAME = "newResource";
            discoveryService.registerResource(NEW_RESOURCE_NAME, getConnectionString(), ImmutableMap.of());
            waitForConnector(Duration.ofSeconds(3), NEW_RESOURCE_NAME, getTestBundleContext());
            supervisor.removeSupervisionEventListener(listener);
            assertEquals(NEW_RESOURCE_NAME, addedResource.get());
            assertTrue(discoveryService.removeResource(NEW_RESOURCE_NAME));
            waitForNoConnector(Duration.ofSeconds(3), NEW_RESOURCE_NAME, getTestBundleContext());
        }
    }

    private static void registerResource(final String resourceName) throws IOException {
        final URL query = new URL("http://localhost:8181" + ResourceDiscoveryService.HTTP_ENDPOINT + '/' + GROUP_NAME + '/' + resourceName);
        final HttpURLConnection connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);
        final ObjectNode announcement = ThreadLocalJsonFactory.getFactory().objectNode();
        announcement.put("connectionString", getConnectionString());
        announcement.put("parameters", ThreadLocalJsonFactory.getFactory().objectNode());
        MAPPER.writeValue(connection.getOutputStream(), announcement);
        connection.connect();
        try {
            assertEquals(201, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }
    }

    private static void unregisterResource(final String resourceName) throws IOException {
        final URL query = new URL("http://localhost:8181" + ResourceDiscoveryService.HTTP_ENDPOINT + '/' + GROUP_NAME + '/' + resourceName);
        final HttpURLConnection connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("DELETE");
        connection.connect();
        try {
            assertEquals(204, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void serviceDiscoveryOverHttp() throws IOException, TimeoutException, InterruptedException{
        //trying to achieve the same goals as in serviceDiscovery test but over HTTP
        final String NEW_RESOURCE_NAME = "newResource";
        registerResource(NEW_RESOURCE_NAME);
        waitForConnector(Duration.ofSeconds(3), NEW_RESOURCE_NAME, getTestBundleContext());
        unregisterResource(NEW_RESOURCE_NAME);
        waitForNoConnector(Duration.ofSeconds(3), NEW_RESOURCE_NAME, getTestBundleContext());
    }

    @Override
    protected String getGroupName() {
        return GROUP_NAME;
    }

    @Override
    protected void fillSupervisors(final EntityMap<? extends SupervisorConfiguration> watchers) {
        final String groovyTrigger;
        try {
            groovyTrigger = IOUtils.toString(getClass().getResourceAsStream("GroovyTrigger.groovy"));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        watchers.addAndConsume(GROUP_NAME, watcher -> {
            watcher.put("discoveryOverHttp", "false");
            watcher.getHealthCheckConfig().getAttributeCheckers().addAndConsume("3.0", scriptlet -> {
                final ColoredAttributeChecker checker = new ColoredAttributeChecker();
                checker.setGreenPredicate(new NumberComparatorPredicate(NumberComparatorPredicate.Operator.LESS_THAN, 1000D));
                checker.setYellowPredicate(new IsInRangePredicate(1000D, true, 2000D, true));
                checker.configureScriptlet(scriptlet);
            });
            watcher.getHealthCheckConfig().getAttributeCheckers().addAndConsume("8.0", scriptlet -> {
                scriptlet.setLanguage(ScriptletConfiguration.GROOVY_LANGUAGE);
                scriptlet.setScript("attributeValue < 42 ? OK : MALFUNCTION");
            });
            watcher.getHealthCheckConfig().getTrigger().setLanguage(ScriptletConfiguration.GROOVY_LANGUAGE);
            watcher.getHealthCheckConfig().getTrigger().setScript(groovyTrigger);
        });
    }

    @Override
    protected void fillGroups(final EntityMap<? extends ManagedResourceGroupConfiguration> groups) {
        groups.addAndConsume(GROUP_NAME, groupConfig -> {
            groupConfig.setType(CONNECTOR_NAME);
            groupConfig.putAll(DEFAULT_PARAMS);
            fillAttributes(groupConfig.getAttributes());
        });
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("3.0");
        attribute.setAlternativeName("int32");
        attribute.put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("8.0");
        attribute.setAlternativeName("float");
        attribute.put("objectName", TestOpenMBean.BEAN_NAME);
    }
}
