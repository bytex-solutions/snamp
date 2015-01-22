package com.itworks.snamp.testing.adapters.jmx;

import com.google.common.base.Supplier;
import com.itworks.snamp.ExceptionalCallable;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.adapters.ResourceAdapterClient;
import com.itworks.snamp.concurrent.SynchronizationEvent;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.testing.SnampDependencies;
import com.itworks.snamp.testing.SnampFeature;
import com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.TestOpenMBean;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.*;
import javax.management.openmbean.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import static com.itworks.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.JMX_ADAPTER)
public final class JmxAdapterTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String ADAPTER_NAME = "jmx";
    private static final String ROOT_OBJECT_NAME = "com.itworks.snamp.testing:type=TestOpenMBean";

    public JmxAdapterTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME));
    }

    private static ObjectName createObjectName() throws MalformedObjectNameException {
        final ObjectName root =  new ObjectName(ROOT_OBJECT_NAME);
        final Hashtable<String, String> params = new Hashtable<>(root.getKeyPropertyList());
        params.put("resource", TEST_RESOURCE_NAME);
        return new ObjectName(root.getDomain(), params);
    }

    private void testJmxAttribute(final Attribute attr) throws BundleException, JMException, IOException{
            final String jmxPort =
                    System.getProperty("com.sun.management.jmxremote.port", "9010");
            final String connectionString = String.format("service:jmx:rmi:///jndi/rmi://localhost:%s/jmxrmi", jmxPort);
            try(final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(connectionString))) {
                final MBeanServerConnection connection = connector.getMBeanServerConnection();
                final ObjectName resourceObjectName = createObjectName();
                assertNotNull(connection.getMBeanInfo(resourceObjectName));
                assertNotNull(connection.getMBeanInfo(resourceObjectName).getAttributes().length > 0);
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
        final CompositeType ct = new CompositeType("dictionary", "dummy",
                new String[]{"col1", "col2", "col3"},
                new String[]{"col1", "col2", "col3"},
                new OpenType<?>[]{SimpleType.BOOLEAN, SimpleType.INTEGER, SimpleType.STRING});
        final Map<String, Object> dict = new HashMap<>(3);
        dict.put("col1", true);
        dict.put("col2", 42);
        dict.put("col3", "Frank Underwood");
        testJmxAttribute(new Attribute("6.1", new CompositeDataSupport(ct, dict)));
    }

    @Test
    public void testTableProperty() throws BundleException, JMException, IOException {
        final CompositeType rowType = new CompositeType("SimpleTable", "dummy",
                new String[]{"col1", "col2", "col3"},
                new String[]{"col1", "col2", "col3"},
                new OpenType<?>[]{SimpleType.BOOLEAN, SimpleType.INTEGER, SimpleType.STRING});
        final TabularData table = new TabularDataSupport(new TabularType("SimpleTable", "table", rowType,
                new String[]{"col3"}));
        table.put(new CompositeDataSupport(rowType,
                new String[]{"col1", "col2", "col3"},
                new Object[]{true, 67, "Dostoevsky"}));
        table.put(new CompositeDataSupport(rowType,
                new String[]{"col1", "col2", "col3"},
                new Object[]{false, 98, "Pushkin"}));
        testJmxAttribute(new Attribute("7.1", table));
    }

    @Test
    public void notificationTest() throws BundleException, JMException, IOException, TimeoutException, InterruptedException {
        final String jmxPort =
                System.getProperty("com.sun.management.jmxremote.port", "9010");
        final Attribute attr = new Attribute("1.0", "Garry Oldman");
        final String connectionString = String.format("service:jmx:rmi:///jndi/rmi://localhost:%s/jmxrmi", jmxPort);
        try(final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(connectionString))) {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName resourceObjectName = createObjectName();
            assertNotNull(connection.getMBeanInfo(resourceObjectName));
            assertNotNull(connection.getMBeanInfo(resourceObjectName).getAttributes().length > 0);
            final SynchronizationEvent<Notification> attributeChangedEvent = new SynchronizationEvent<>();
            final SynchronizationEvent<Notification> testEvent = new SynchronizationEvent<>();
            final SynchronizationEvent<Notification> eventWithAttachmentHolder = new SynchronizationEvent<>();
            connection.addNotificationListener(resourceObjectName, new NotificationListener() {
                @Override
                public void handleNotification(final Notification notification, final Object handback) {
                    switch (notification.getType()){
                        case "19.1": attributeChangedEvent.fire(notification); return;
                        case "21.1": eventWithAttachmentHolder.fire(notification); return;
                        case "20.1": testEvent.fire(notification);
                    }
                }
            }, null, null);
            //force attribute change
            connection.setAttribute(resourceObjectName, attr);
            assertNotNull(attributeChangedEvent.getAwaitor().await(TimeSpan.fromSeconds(10)));
            assertNotNull(testEvent.getAwaitor().await(TimeSpan.fromSeconds(10)));
            final Notification withAttachment = eventWithAttachmentHolder.getAwaitor().await(TimeSpan.fromSeconds(10));
            assertNotNull(withAttachment);
            assertNotNull(withAttachment.getUserData() instanceof TabularData);
        }
    }

    @Test
    public void configurationDescriptorTest() throws BundleException {
        final ConfigurationEntityDescription desc = ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, ResourceAdapterConfiguration.class);
        assertNotNull(desc);
        final ConfigurationEntityDescription.ParameterDescription param = desc.getParameterDescriptor("usePlatformMBean");
        assertNotNull(param);
        assertFalse(param.getDescription(null).isEmpty());
    }

    @Override
    protected void fillAdapters(final Map<String, ResourceAdapterConfiguration> adapters, final Supplier<ResourceAdapterConfiguration> adapterFactory) {
        final ResourceAdapterConfiguration restAdapter = adapterFactory.get();
        restAdapter.setAdapterName(ADAPTER_NAME);
        restAdapter.getParameters().put("objectName", ROOT_OBJECT_NAME);
        restAdapter.getParameters().put("usePlatformMBean", "true");
        restAdapter.getParameters().put("dbgUsePureSerialization", "true");
        adapters.put("test-jmx", restAdapter);
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
                ResourceAdapterActivator.startResourceAdapter(context, ADAPTER_NAME);
                return null;
            }
        }, TimeSpan.fromSeconds(4));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
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

        event = eventFactory.get();
        event.setCategory("com.itworks.snamp.connectors.tests.impl.plainnotif");
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);
        events.put("21.1", event);
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
}
