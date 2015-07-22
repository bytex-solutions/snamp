package com.itworks.snamp.testing.adapters.jmx;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.ExceptionalCallable;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.ResourceAdapter;
import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.adapters.ResourceAdapterClient;
import com.itworks.snamp.concurrent.Awaitor;
import com.itworks.snamp.concurrent.SynchronizationEvent;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.internal.RecordReader;
import com.itworks.snamp.jmx.CompositeDataBuilder;
import com.itworks.snamp.jmx.TabularDataBuilder;
import com.itworks.snamp.testing.SnampDependencies;
import com.itworks.snamp.testing.SnampFeature;
import com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.TestOpenMBean;
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
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import static com.itworks.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;
import static com.itworks.snamp.adapters.ResourceAdapter.FeatureBindingInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.JMX_ADAPTER)
public final class JmxAdapterTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String ADAPTER_NAME = "jmx";
    private static final String INSTANCE_NAME = "test-jmx";

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

    private static void verifyAttributeExists(final String name, final MBeanAttributeInfo[] attrs){
        for(final MBeanAttributeInfo attribute: attrs)
            if(Objects.equals(name, attribute.getName()))
                return;
        fail("Attribute " + name + " doesn't exist");
    }

    private void testJmxAttribute(final Attribute attr) throws BundleException, JMException, IOException{
            final String connectionString = String.format("service:jmx:rmi:///jndi/rmi://localhost:%s/karaf-root", JMX_KARAF_PORT);
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
                .build();
        testJmxAttribute(new Attribute("6.1", dict));
    }

    @Test
    public void testTableProperty() throws BundleException, JMException, IOException {
        final TabularData table = new TabularDataBuilder()
                .setTypeName("SimpleTable", true)
                .setTypeDescription("Test table", true)
                .columns()
                .addColumn("col1", "dummy item", SimpleType.BOOLEAN, false)
                .addColumn("col2", "dummy item", SimpleType.INTEGER, false)
                .addColumn("col3", "dummy item", SimpleType.STRING, true)
                .queryObject(TabularDataBuilder.class)
                .add(true, 67, "Dostoevsky")
                .add(false, 98, "Pushkin")
                .build();
        testJmxAttribute(new Attribute("7.1", table));
    }

    @Test
    public void notificationTest() throws BundleException, JMException, IOException, TimeoutException, InterruptedException {
        final Attribute attr = new Attribute("1.0", "Garry Oldman");
        final String connectionString = String.format("service:jmx:rmi:///jndi/rmi://localhost:%s/karaf-root", JMX_KARAF_PORT);
        try(final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(connectionString), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))) {
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
                        case "19.1":
                            attributeChangedEvent.fire(notification); return;
                        case "21.1":
                            eventWithAttachmentHolder.fire(notification); return;
                        case "20.1":
                            testEvent.fire(notification);
                    }
                }
            }, null, null);
            final Awaitor<Notification, ExceptionPlaceholder> attributeChangedEventAwaitor = attributeChangedEvent.getAwaitor();
            final Awaitor<Notification, ExceptionPlaceholder> testEventAwaitor = testEvent.getAwaitor();
            final Awaitor<Notification, ExceptionPlaceholder> eventWithAttachmentHolderAwaitor = eventWithAttachmentHolder.getAwaitor();
            //force attribute change
            connection.setAttribute(resourceObjectName, attr);
            assertNotNull(attributeChangedEventAwaitor.await(TimeSpan.fromSeconds(10)));
            assertNotNull(testEventAwaitor.await(TimeSpan.fromSeconds(10)));
            final Notification withAttachment = eventWithAttachmentHolderAwaitor.await(TimeSpan.fromSeconds(10));
            assertNotNull(withAttachment);
            assertNotNull(withAttachment.getUserData() instanceof TabularData);
        }
    }

    @Test
    public void configurationDescriptorTest() throws BundleException {
        ConfigurationEntityDescription desc = ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, ResourceAdapterConfiguration.class);
        assertNotNull(desc);
        final ConfigurationEntityDescription.ParameterDescription param = desc.getParameterDescriptor("usePlatformMBean");
        assertNotNull(param);
        assertFalse(param.getDescription(null).isEmpty());
        desc = ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, EventConfiguration.class);
        assertNotNull(desc);
    }

    @Test
    public void attributeBindingTest() throws TimeoutException, InterruptedException {
        final ResourceAdapterClient client = new ResourceAdapterClient(getTestBundleContext(), INSTANCE_NAME, TimeSpan.fromSeconds(2));
        try {
            assertTrue(client.forEachFeature(MBeanAttributeInfo.class, new RecordReader<String, FeatureBindingInfo<MBeanAttributeInfo>, ExceptionPlaceholder>() {
                @Override
                public boolean read(final String resourceName, final FeatureBindingInfo<MBeanAttributeInfo> bindingInfo) {
                    return bindingInfo.getProperty(FeatureBindingInfo.MAPPED_TYPE) instanceof OpenType<?>;
                }
            }));
        } finally {
            client.release(getTestBundleContext());
        }
    }

    @Test
    public void notificationBindingTest() throws TimeoutException, InterruptedException {
        final ResourceAdapterClient client = new ResourceAdapterClient(getTestBundleContext(), INSTANCE_NAME, TimeSpan.fromSeconds(2));
        try {
            assertTrue(client.forEachFeature(MBeanNotificationInfo.class, new RecordReader<String, FeatureBindingInfo<MBeanNotificationInfo>, ExceptionPlaceholder>() {
                @Override
                public boolean read(final String resourceName, final FeatureBindingInfo<MBeanNotificationInfo> bindingInfo) {
                    return bindingInfo != null;
                }
            }));
        } finally {
            client.release(getTestBundleContext());
        }
    }

    @Override
    protected void fillAdapters(final Map<String, ResourceAdapterConfiguration> adapters, final Supplier<ResourceAdapterConfiguration> adapterFactory) {
        final ResourceAdapterConfiguration restAdapter = adapterFactory.get();
        restAdapter.setAdapterName(ADAPTER_NAME);
        restAdapter.getParameters().put("objectName", ROOT_OBJECT_NAME);
        restAdapter.getParameters().put("usePlatformMBean", Boolean.toString(isInTestContainer()));
        restAdapter.getParameters().put("dbgUsePureSerialization", "true");
        adapters.put(INSTANCE_NAME, restAdapter);
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
        }, TimeSpan.fromMinutes(4));
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
