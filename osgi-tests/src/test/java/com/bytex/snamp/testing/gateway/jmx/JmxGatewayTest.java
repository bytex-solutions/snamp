package com.bytex.snamp.testing.gateway.jmx;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.gateway.GatewayClient;
import com.bytex.snamp.jmx.CompositeDataBuilder;
import com.bytex.snamp.jmx.TabularDataBuilder;
import com.bytex.snamp.testing.BundleExceptionCallable;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connector.jmx.TestOpenMBean;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Hashtable;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.gateway.Gateway.FeatureBindingInfo;
import static com.bytex.snamp.testing.connector.jmx.TestOpenMBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.JMX_GATEWAY)
public final class JmxGatewayTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String GATEWAY_NAME = "jmx";
    private static final String INSTANCE_NAME = "test-jmx";

    private static final String ROOT_OBJECT_NAME = "com.bytex.snamp.testing:type=TestOpenMBean";

    public JmxGatewayTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME));
    }

    private static ObjectName createObjectName() throws MalformedObjectNameException {
        final ObjectName root =  new ObjectName(ROOT_OBJECT_NAME);
        final Hashtable<String, String> params = new Hashtable<>(root.getKeyPropertyList());
        params.put("resource", TEST_RESOURCE_NAME);
        return new ObjectName(root.getDomain(), params);
    }

    private static void verifyAttributeExists(final String name, final MBeanAttributeInfo[] attrs){
        for(final MBeanAttributeInfo attribute: attrs)
            if(Objects.equals(name, attribute.getName()))
                return;
        fail("Attribute " + name + " doesn't exist");
    }

    private void testJmxAttribute(final Attribute attr) throws BundleException, JMException, IOException{
            final String connectionString = AbstractJmxConnectorTest.getConnectionString();
            try(final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(connectionString), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))) {
                final MBeanServerConnection connection = connector.getMBeanServerConnection();
                final ObjectName resourceObjectName = createObjectName();
                assertNotNull(connection.getMBeanInfo(resourceObjectName));
                final MBeanAttributeInfo[] attributes = connection.getMBeanInfo(resourceObjectName).getAttributes();
                assertNotNull(attributes.length > 0);
                verifyAttributeExists(attr.getName(), attributes);
                connection.setAttribute(resourceObjectName, attr);
                if(attr.getValue().getClass().isArray())
                    assertArrayEquals(attr.getValue(), connection.getAttribute(resourceObjectName, attr.getName()));
                else assertEquals(attr.getValue(), connection.getAttribute(resourceObjectName, attr.getName()));
            }
    }

    @Test
    public void testStringProperty() throws BundleException, JMException, IOException {
        testJmxAttribute(new Attribute("1.0", "Frank Underwood"));
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Test
    public void testBooleanProperty() throws BundleException, JMException, IOException {
        testJmxAttribute(new Attribute("2.0", Boolean.TRUE));
    }

    @Test
    public void testInt32Property() throws BundleException, JMException, IOException {
        testJmxAttribute(new Attribute("3.0", 19081));
    }

    @Test
    public void testBigintProperty() throws BundleException, JMException, IOException {
        testJmxAttribute(new Attribute("4.0", new BigInteger("100500")));
    }

    @Test
    public void testArrayProperty() throws BundleException, JMException, IOException {
        testJmxAttribute(new Attribute("5.1", new short[]{8, 4, 2, 1}));
    }

    @Test
    public void testDictionaryProperty() throws BundleException, JMException, IOException {
        final CompositeData dict = new CompositeDataBuilder()
                .setTypeName("dictionary")
                .setTypeDescription("dummy")
                .put("col1", "dummy item", SimpleType.BOOLEAN, true)
                .put("col2", "dummy item", SimpleType.INTEGER, 42)
                .put("col3", "dummy item", SimpleType.STRING, "Frank Underwood")
                .call();
        testJmxAttribute(new Attribute("6.1", dict));
    }

    @Test
    public void testTableProperty() throws BundleException, JMException, IOException {
        final TabularData table = new TabularDataBuilder()
                .setTypeName("SimpleTable", true)
                .setTypeDescription("Test table", true)
                .declareColumns(columns -> columns
                    .addColumn("col1", "dummy item", SimpleType.BOOLEAN, false)
                    .addColumn("col2", "dummy item", SimpleType.INTEGER, false)
                    .addColumn("col3", "dummy item", SimpleType.STRING, true))
                .add(true, 67, "Dostoevsky")
                .add(false, 98, "Pushkin")
                .call();
        testJmxAttribute(new Attribute("7.1", table));
    }

    @Test
    public void notificationTest() throws BundleException, JMException, IOException, TimeoutException, InterruptedException, ExecutionException {
        final Attribute attr = new Attribute("1.0", "Garry Oldman");
        final String connectionString = AbstractJmxConnectorTest.getConnectionString();
        try(final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(connectionString), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))) {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName resourceObjectName = createObjectName();
            assertNotNull(connection.getMBeanInfo(resourceObjectName));
            assertNotNull(connection.getMBeanInfo(resourceObjectName).getAttributes().length > 0);
            final CompletableFuture<Notification> attributeChangedEvent = new CompletableFuture<>();
            final CompletableFuture<Notification> testEvent = new CompletableFuture<>();
            final CompletableFuture<Notification> eventWithAttachmentHolder = new CompletableFuture<>();
            connection.addNotificationListener(resourceObjectName, (notification, handback) -> {
                switch (notification.getType()){
                    case "19.1":
                        attributeChangedEvent.complete(notification); return;
                    case "21.1":
                        eventWithAttachmentHolder.complete(notification); return;
                    case "20.1":
                        testEvent.complete(notification);
                }
            }, null, null);
            //force attribute change
            connection.setAttribute(resourceObjectName, attr);
            assertNotNull(attributeChangedEvent.get(10, TimeUnit.SECONDS));
            assertNotNull(testEvent.get(10, TimeUnit.SECONDS));
            final Notification withAttachment = eventWithAttachmentHolder.get(10, TimeUnit.SECONDS);
            assertNotNull(withAttachment);
            assertNotNull(withAttachment.getUserData() instanceof TabularData);
        }
    }

    @Test
    public void configurationDescriptorTest() throws BundleException {
        ConfigurationEntityDescription desc = GatewayClient.getConfigurationEntityDescriptor(getTestBundleContext(), GATEWAY_NAME, GatewayConfiguration.class);
        testConfigurationDescriptor(desc, "objectName", "usePlatformMBean");
        desc = GatewayClient.getConfigurationEntityDescriptor(getTestBundleContext(), GATEWAY_NAME, EventConfiguration.class);
        testConfigurationDescriptor(desc, "severity");
    }

    @Test
    public void attributeBindingTest() throws TimeoutException, InterruptedException, ExecutionException {
        final GatewayClient client = new GatewayClient(getTestBundleContext(), INSTANCE_NAME, Duration.ofSeconds(2));
        try {
            assertTrue(client.forEachFeature(MBeanAttributeInfo.class, (resourceName, bindingInfo) -> bindingInfo.getProperty(FeatureBindingInfo.MAPPED_TYPE) instanceof OpenType<?>));
        } finally {
            client.release(getTestBundleContext());
        }
    }

    @Test
    public void notificationBindingTest() throws TimeoutException, InterruptedException, ExecutionException {
        final GatewayClient client = new GatewayClient(getTestBundleContext(), INSTANCE_NAME, Duration.ofSeconds(2));
        try {
            assertTrue(client.forEachFeature(MBeanNotificationInfo.class, (resourceName, bindingInfo) -> bindingInfo != null));
        } finally {
            client.release(getTestBundleContext());
        }
    }

    @Test
    public void metricsTest(){
        final ManagedResourceConnector connector = getManagementConnector();
        try{
            assertNotNull(connector.queryObject(MetricsSupport.class));
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Override
    protected void fillGateways(final EntityMap<? extends GatewayConfiguration> gateways) {
        final GatewayConfiguration httpGateway = gateways.getOrAdd(INSTANCE_NAME);
        httpGateway.setType(GATEWAY_NAME);
        httpGateway.getParameters().put("objectName", ROOT_OBJECT_NAME);
        httpGateway.getParameters().put("usePlatformMBean", Boolean.toString(isInTestContainer()));
        httpGateway.getParameters().put("dbgUsePureSerialization", "true");
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        super.beforeStartTest(context);
        beforeCleanupTest(context);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        startResourceConnector(context);
        syncWithGatewayStartedEvent(GATEWAY_NAME, (BundleExceptionCallable) () -> {
                GatewayActivator.enableGateway(context, GATEWAY_NAME);
                return null;
        }, Duration.ofMinutes(4));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        GatewayActivator.disableGateway(context, GATEWAY_NAME);
        stopResourceConnector(context);
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd("19.1");
        event.setAlternativeName(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);

        event = events.getOrAdd("20.1");
        event.setAlternativeName("com.bytex.snamp.connector.tests.impl.testnotif");
        event.getParameters().put("severity", "panic");
        event.getParameters().put("objectName", BEAN_NAME);

        event = events.getOrAdd("21.1");
        event.setAlternativeName("com.bytex.snamp.connector.tests.impl.plainnotif");
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);
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

        attribute = attributes.getOrAdd("4.0");
        attribute.setAlternativeName("bigint");
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

        attribute = attributes.getOrAdd("8.0");
        attribute.setAlternativeName("float");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("9.0");
        attribute.setAlternativeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
    }
}
