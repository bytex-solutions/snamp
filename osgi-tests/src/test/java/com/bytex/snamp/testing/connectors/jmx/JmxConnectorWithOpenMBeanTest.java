package com.bytex.snamp.testing.connectors.jmx;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.concurrent.SynchronizationEvent;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.connectors.ManagedResourceConnector;
import com.bytex.snamp.connectors.ManagedResourceConnectorClient;
import com.bytex.snamp.connectors.attributes.AttributeSupport;
import com.bytex.snamp.connectors.metrics.*;
import com.bytex.snamp.connectors.notifications.NotificationSupport;
import com.bytex.snamp.connectors.notifications.SynchronizationListener;
import com.bytex.snamp.connectors.operations.OperationSupport;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.CompositeDataBuilder;
import com.bytex.snamp.jmx.TabularDataBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.configuration.AgentConfiguration.EntityMap;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.*;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class JmxConnectorWithOpenMBeanTest extends AbstractJmxConnectorTest<TestOpenMBean> {

    public JmxConnectorWithOpenMBeanTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(TestOpenMBean.BEAN_NAME));
    }

    @Override
    protected void fillOperations(final EntityMap<? extends OperationConfiguration> operations) {
        OperationConfiguration operation = operations.getOrAdd("res");
        setFeatureName(operation, "reverse");
        operation.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("1.0");
        setFeatureName(attribute, "string");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("2.0");
        setFeatureName(attribute, "boolean");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("3.0");
        setFeatureName(attribute, "int32");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("4.0");
        setFeatureName(attribute, "bigint");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("5.1");
        setFeatureName(attribute, "array");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("6.1");
        setFeatureName(attribute, "dictionary");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("7.1");
        setFeatureName(attribute, "table");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("8.0");
        setFeatureName(attribute, "float");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("9.0");
        setFeatureName(attribute, "date");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd("19.1");
        setFeatureName(event, AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);

        event = events.getOrAdd("20.1");
        setFeatureName(event, "com.bytex.snamp.connectors.tests.impl.testnotif");
        event.getParameters().put("severity", "panic");
        event.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        stopResourceConnector(context);
        super.afterCleanupTest(context);
    }

    @Test
    public void notificationTest() throws Exception {
        final NotificationSupport notificationSupport = getManagementConnector(getTestBundleContext()).queryObject(NotificationSupport.class);
        final AttributeSupport attributeSupport = getManagementConnector(getTestBundleContext()).queryObject(AttributeSupport.class);
        assertNotNull(notificationSupport);
        assertNotNull(attributeSupport);
        assertEquals(2, notificationSupport.getNotificationInfo().length);
        final SynchronizationListener listener1 = new SynchronizationListener("19.1");
        final SynchronizationListener listener2 = new SynchronizationListener("20.1");
        notificationSupport.addNotificationListener(listener1, listener1, null);
        notificationSupport.addNotificationListener(listener2, listener2, null);
        final Future<Notification> awaitor1 = listener1.getAwaitor();
        final Future<Notification> awaitor2 = listener2.getAwaitor();
        //force property changing
        attributeSupport.setAttribute(new Attribute("1.0", "Frank Underwood"));
        final Notification notif1 = awaitor1.get(5, TimeUnit.SECONDS);
        assertNotNull(notif1);
        assertEquals("Property string is changed", notif1.getMessage());
        assertTrue(notif1.getUserData() instanceof CompositeData);
        final CompositeData attachment = (CompositeData)notif1.getUserData();
        assertEquals("string", attachment.get("attributeName"));
        assertEquals(String.class.getName(), attachment.get("attributeType"));
        final Notification notif2 = awaitor2.get(5, TimeUnit.SECONDS);
        assertNotNull(notif2);
        assertEquals("Property changed", notif2.getMessage());
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Test
    public void simulateConnectionAbortTest() throws TimeoutException,
            InterruptedException,
            ExecutionException,
            JMException {
        final NotificationSupport notificationSupport = getManagementConnector(getTestBundleContext()).queryObject(NotificationSupport.class);
        final AttributeSupport attributeSupport = getManagementConnector(getTestBundleContext()).queryObject(AttributeSupport.class);
        assertNotNull(notificationSupport);
        assertNotNull(attributeSupport);
        assertEquals(2, notificationSupport.getNotificationInfo().length);
        final SynchronizationListener listener1 = new SynchronizationListener("19.1");
        final SynchronizationListener listener2 = new SynchronizationListener("20.1");
        notificationSupport.addNotificationListener(listener1, listener1, null);
        notificationSupport.addNotificationListener(listener2, listener2, null);
        final Future<Notification> awaitor1 = listener1.getAwaitor();
        final Future<Notification> awaitor2 = listener2.getAwaitor();
        //simulate connection abort
        assertEquals("OK", ManagedResourceConnectorClient.invokeMaintenanceAction(getTestBundleContext(), CONNECTOR_NAME, "simulateConnectionAbort", null, null).get(3, TimeUnit.SECONDS));
        //force property changing
        attributeSupport.setAttribute(new Attribute("1.0", "Frank Underwood"));
        final Notification notif1 = awaitor1.get(5, TimeUnit.SECONDS);
        assertNotNull(notif1);
        assertEquals("Property string is changed", notif1.getMessage());
        assertTrue(notif1.getUserData() instanceof CompositeData);
        final CompositeData attachment = (CompositeData)notif1.getUserData();
        assertEquals("string", attachment.get("attributeName"));
        assertEquals(String.class.getName(), attachment.get("attributeType"));
        final Notification notif2 = awaitor2.get(5, TimeUnit.SECONDS);
        assertNotNull(notif2);
        assertEquals("Property changed", notif2.getMessage());
    }

    @Test
    public void operationTest() throws Exception{
        final OperationSupport operationSupport = getManagementConnector(getTestBundleContext()).queryObject(OperationSupport.class);
        try{
            final byte[] array = new byte[]{1, 4, 9};
            final Object result = operationSupport.invoke("res", new Object[]{array}, new String[]{byte[].class.getName()});
            assertTrue(result instanceof byte[]);
            assertArrayEquals(new byte[]{9, 4, 1}, (byte[])result);
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void testForTableProperty() throws Exception {
        final TabularData table = new TabularDataBuilder()
                .setTypeName("Table", true)
                .setTypeDescription("Dummy table", true)
                .columns()
                .addColumn("col1", "dummy column", SimpleType.BOOLEAN, false)
                .addColumn("col2", "dummy column", SimpleType.INTEGER, false)
                .addColumn("col3", "dummy column", SimpleType.STRING, true)
                .queryObject(TabularDataBuilder.class)
                .add(true, 42, "Frank Underwood")
                .add(true, 43, "Peter Russo")
                .build();
        testAttribute("7.1", TypeToken.of(TabularData.class), table, (o1, o2) -> o1.size() == o2.size() && o1.values().containsAll(o2.values()));
    }

    @Test
    public void testForDictionaryProperty() throws Exception {
        final CompositeData dict = new CompositeDataBuilder()
                .setTypeName("Dict")
                .setTypeDescription("Descr")
                .put("col1", "descr", true)
                .put("col2", "descr", 42)
                .put("col3", "descr", "Frank Underwood")
                .build();
        testAttribute("6.1", TypeToken.of(CompositeData.class), dict, Objects::equals);
    }

    @Test
    public void testForMetrics() throws Exception {
        final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(getTestBundleContext(), TEST_RESOURCE_NAME);
        try{
            final MetricsReader metrics = client.queryObject(MetricsReader.class);
            assertNotNull(metrics);
            assertTrue(metrics.getMetrics(MBeanAttributeInfo.class) instanceof AttributeMetrics);
            assertTrue(metrics.getMetrics(MBeanNotificationInfo.class) instanceof NotificationMetrics);
            assertTrue(metrics.getMetrics(MBeanOperationInfo.class) instanceof OperationMetrics);
            assertNotNull(metrics.queryObject(AttributeMetrics.class));
            assertNotNull(metrics.queryObject(NotificationMetrics.class));
            assertNotNull(metrics.queryObject(OperationMetrics.class));
            //read and write attributes
            testForStringProperty();
            //verify metrics
            final AttributeMetrics attrMetrics = metrics.queryObject(AttributeMetrics.class);
            assertTrue(attrMetrics.getNumberOfReads(MetricsInterval.HOUR) > 0);
            assertTrue(attrMetrics.getNumberOfWrites(MetricsInterval.HOUR) > 0);
            assertTrue(attrMetrics.getNumberOfReads() > 0);
            assertTrue(attrMetrics.getNumberOfWrites() > 0);
        } finally {
            client.release(getTestBundleContext());
        }
    }

    @Test
    public void testForArrayProperty() throws Exception {
        final short[] array = new short[]{10, 20, 30, 40, 50};
        testAttribute("5.1", TypeToken.of(short[].class), array, ArrayUtils::strictEquals);
    }

    @Test
    public void testForDateProperty() throws Exception {
        testAttribute("9.0", TypeToken.of(Date.class), new Date());
    }

    @Test
    public void testForFloatProperty() throws Exception {
        testAttribute("8.0", TypeToken.of(Float.class), 3.14F);
    }

    @Test
    public void testForBigIntProperty() throws Exception {
        testAttribute("4.0", TypeToken.of(BigInteger.class), BigInteger.valueOf(100500));
    }

    @Test
    public void testForInt32Property() throws Exception {
        testAttribute("3.0", TypeToken.of(Integer.class), 42);
    }

    @Test
    public void testForBooleanProperty() throws Exception {
        testAttribute("2.0", TypeToken.of(Boolean.class), Boolean.TRUE);
    }

    @Test
    public void testForStringProperty() throws Exception {
        testAttribute("1.0", TypeToken.of(String.class), "Frank Underwood");
    }

    @Test
    public void testForResourceConnectorListener() throws Exception {
        final BundleContext context = getTestBundleContext();
        final SynchronizationEvent<Boolean> unregistered = new SynchronizationEvent<>(false);
        final SynchronizationEvent<Boolean> registered = new SynchronizationEvent<>(false);
        ManagedResourceConnectorClient.addResourceListener(context, event -> {
            switch (event.getType()){
                case ServiceEvent.UNREGISTERING:
                    unregistered.fire(Utils.isInstanceOf(event.getServiceReference(), ManagedResourceConnector.class));
                    return;
                case ServiceEvent.REGISTERED:
                    registered.fire(Utils.isInstanceOf(event.getServiceReference(), ManagedResourceConnector.class));
            }
        });
        stopResourceConnector(context);
        startResourceConnector(context);
        assertTrue(unregistered.getAwaitor().get(2, TimeUnit.SECONDS));
        assertTrue(registered.getAwaitor().get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testForAttributeConfigDescription(){
        testConfigurationDescriptor(AgentConfiguration.ManagedResourceConfiguration.class, ImmutableSet.of(
            "login",
                "password",
                "connectionCheckPeriod",
                "smartMode",
                "objectName"
        ));
        testConfigurationDescriptor(AttributeConfiguration.class, ImmutableSet.of(
                "objectName",
                "useRegexp"
        ));
        testConfigurationDescriptor(EventConfiguration.class, ImmutableSet.of(
                "objectName",
                "severity",
                "useRegexp"
        ));
    }

    @Test
    public void testForAttributesDiscovery(){
        final Collection<AttributeConfiguration> discoveredAttributes = ManagedResourceConnectorClient.discoverEntities(getTestBundleContext(),
                CONNECTOR_NAME,
                JMX_RMI_CONNECTION_STRING,
                ImmutableMap.of("login", AbstractJmxConnectorTest.JMX_LOGIN, "password", AbstractJmxConnectorTest.JMX_PASSWORD),
                AttributeConfiguration.class);
        assertTrue(discoveredAttributes.size() > 30);
        for(final AttributeConfiguration config: discoveredAttributes) {
            assertTrue(config.getParameters().containsKey("objectName"));
            assertTrue(config.getParameters().containsKey(AttributeConfiguration.NAME_KEY));
        }
    }

    @Test
    public void testForNotificationsDiscovery(){
        final Collection<EventConfiguration> discoveredEvents = ManagedResourceConnectorClient.discoverEntities(getTestBundleContext(),
                CONNECTOR_NAME,
                JMX_RMI_CONNECTION_STRING,
                ImmutableMap.of("login", AbstractJmxConnectorTest.JMX_LOGIN, "password", AbstractJmxConnectorTest.JMX_PASSWORD),
                EventConfiguration.class);
        assertTrue(discoveredEvents.size() > 2);
        for(final EventConfiguration config: discoveredEvents) {
            assertTrue(config.getParameters().containsKey("objectName"));
            assertTrue(config.getParameters().containsKey(EventConfiguration.NAME_KEY));
        }
    }

    @Test
    public void maintenanceActionTest() throws InterruptedException, ExecutionException, TimeoutException {
        final Map<String, String> actions = ManagedResourceConnectorClient.getMaintenanceActions(getTestBundleContext(), CONNECTOR_NAME, null);
        assertFalse(actions.isEmpty());
        final String ACTION = "simulateConnectionAbort";
        assertTrue(actions.containsKey(ACTION));
        final Future<String> result = ManagedResourceConnectorClient.invokeMaintenanceAction(getTestBundleContext(), CONNECTOR_NAME, ACTION, null, null);
        assertEquals("OK", result.get(3, TimeUnit.SECONDS));
    }
}
