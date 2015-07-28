package com.bytex.snamp.testing.connectors.jmx;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.TypeTokens;
import com.bytex.snamp.concurrent.SynchronizationEvent;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.connectors.ManagedResourceConnector;
import com.bytex.snamp.connectors.ManagedResourceConnectorClient;
import com.bytex.snamp.connectors.attributes.AttributeSupport;
import com.bytex.snamp.connectors.notifications.NotificationSupport;
import com.bytex.snamp.connectors.notifications.SynchronizationListener;
import com.bytex.snamp.connectors.operations.OperationSupport;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.CompositeDataBuilder;
import com.bytex.snamp.jmx.TabularDataBuilder;
import com.bytex.snamp.testing.connectors.AbstractResourceConnectorTest;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class JmxConnectorWithOpenMBeanTest extends AbstractJmxConnectorTest<TestOpenMBean> {

    public JmxConnectorWithOpenMBeanTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(TestOpenMBean.BEAN_NAME));
    }

    @Override
    protected void fillOperations(final Map<String, OperationConfiguration> operations,
                                  final Supplier<OperationConfiguration> operationFactory) {
        OperationConfiguration operation = operationFactory.get();
        operation.setOperationName("reverse");
        operation.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        operations.put("res", operation);
    }

    @Override
    protected final void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Supplier<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attribute = attributeFactory.get();
        attribute.setAttributeName("string");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("1.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("boolean");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("2.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("int32");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("3.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("bigint");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("4.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("array");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("5.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("dictionary");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("6.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("table");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("7.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("float");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("8.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("9.0", attribute);
    }

    @Override
    protected void fillEvents(final Map<String, EventConfiguration> events, final Supplier<EventConfiguration> eventFactory) {
        EventConfiguration event = eventFactory.get();
        event.setCategory(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        events.put("19.1", event);

        event = eventFactory.get();
        event.setCategory("com.bytex.snamp.connectors.tests.impl.testnotif");
        event.getParameters().put("severity", "panic");
        event.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        events.put("20.1", event);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        stopResourceConnector(context);
        super.afterCleanupTest(context);
    }

    @Test
    public final void notificationTest() throws TimeoutException, InterruptedException, JMException {
        final NotificationSupport notificationSupport = getManagementConnector(getTestBundleContext()).queryObject(NotificationSupport.class);
        final AttributeSupport attributeSupport = getManagementConnector(getTestBundleContext()).queryObject(AttributeSupport.class);
        assertNotNull(notificationSupport);
        assertNotNull(attributeSupport);
        assertEquals(2, notificationSupport.getNotificationInfo().length);
        final SynchronizationListener listener1 = new SynchronizationListener("19.1");
        final SynchronizationListener listener2 = new SynchronizationListener("20.1");
        notificationSupport.addNotificationListener(listener1, listener1, null);
        notificationSupport.addNotificationListener(listener2, listener2, null);
        final SynchronizationEvent.EventAwaitor<Notification> awaitor1 = listener1.getAwaitor();
        final SynchronizationEvent.EventAwaitor<Notification> awaitor2 = listener2.getAwaitor();
        //force property changing
        attributeSupport.setAttribute(new Attribute("1.0", "Frank Underwood"));
        final Notification notif1 = awaitor1.await(TimeSpan.fromSeconds(5L));
        assertNotNull(notif1);
        assertEquals("Property string is changed", notif1.getMessage());
        assertTrue(notif1.getUserData() instanceof CompositeData);
        final CompositeData attachment = (CompositeData)notif1.getUserData();
        assertEquals("string", attachment.get("attributeName"));
        assertEquals(String.class.getName(), attachment.get("attributeType"));
        final Notification notif2 = awaitor2.await(TimeSpan.fromSeconds(5L));
        assertNotNull(notif2);
        assertEquals("Property changed", notif2.getMessage());
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Test
    public final void simulateConnectionAbortTest() throws TimeoutException,
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
        final SynchronizationEvent.EventAwaitor<Notification> awaitor1 = listener1.getAwaitor();
        final SynchronizationEvent.EventAwaitor<Notification> awaitor2 = listener2.getAwaitor();
        //simulate connection abort
        assertEquals("OK", ManagedResourceConnectorClient.invokeMaintenanceAction(getTestBundleContext(), CONNECTOR_NAME, "simulateConnectionAbort", null, null).get(3, TimeUnit.SECONDS));
        //force property changing
        attributeSupport.setAttribute(new Attribute("1.0", "Frank Underwood"));
        final Notification notif1 = awaitor1.await(TimeSpan.fromSeconds(5L));
        assertNotNull(notif1);
        assertEquals("Property string is changed", notif1.getMessage());
        assertTrue(notif1.getUserData() instanceof CompositeData);
        final CompositeData attachment = (CompositeData)notif1.getUserData();
        assertEquals("string", attachment.get("attributeName"));
        assertEquals(String.class.getName(), attachment.get("attributeType"));
        final Notification notif2 = awaitor2.await(TimeSpan.fromSeconds(5L));
        assertNotNull(notif2);
        assertEquals("Property changed", notif2.getMessage());
    }

    @Test
    public final void operationTest() throws Exception{
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
    public final void testForTableProperty() throws Exception {
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
        testAttribute("7.1", TypeToken.of(TabularData.class), table, new Equator<TabularData>() {
            @Override
            public boolean equate(final TabularData o1, final TabularData o2) {
                return o1.size() == o2.size() &&
                        Utils.collectionsAreEqual((Collection)o1.values(), (Collection)o2.values());
            }
        });
    }

    @Test
    public final void testForDictionaryProperty() throws Exception {
        final CompositeData dict = new CompositeDataBuilder()
                .setTypeName("Dict")
                .setTypeDescription("Descr")
                .put("col1", "descr", true)
                .put("col2", "descr", 42)
                .put("col3", "descr", "Frank Underwood")
                .build();
        testAttribute("6.1", TypeToken.of(CompositeData.class), dict, AbstractResourceConnectorTest.<CompositeData>valueEquator());
    }

    @Test
    public final void testForArrayProperty() throws Exception {
        final short[] array = new short[]{10, 20, 30, 40, 50};
        testAttribute("5.1", TypeToken.of(short[].class), array, arrayEquator());
    }

    @Test
    public final void testForDateProperty() throws Exception {
        testAttribute("9.0", TypeTokens.DATE, new Date());
    }

    @Test
    public final void testForFloatProperty() throws Exception {
        testAttribute("8.0", TypeTokens.FLOAT, 3.14F);
    }

    @Test
    public final void testForBigIntProperty() throws Exception {
        testAttribute("4.0", TypeTokens.BIG_INTEGER, BigInteger.valueOf(100500));
    }

    @Test
    public final void testForInt32Property() throws Exception {
        testAttribute("3.0", TypeTokens.INTEGER, 42);
    }

    @Test
    public final void testForBooleanProperty() throws Exception {
        testAttribute("2.0", TypeTokens.BOOLEAN, Boolean.TRUE);
    }

    @Test
    public final void testForStringProperty() throws Exception {
        testAttribute("1.0", TypeTokens.STRING, "Frank Underwood");
    }

    @Test
    public void testForResourceConnectorListener() throws BundleException, TimeoutException, InterruptedException {
        final BundleContext context = getTestBundleContext();
        final SynchronizationEvent<Boolean> unregistered = new SynchronizationEvent<>(false);
        final SynchronizationEvent<Boolean> registered = new SynchronizationEvent<>(false);
        ManagedResourceConnectorClient.addResourceListener(context, new ServiceListener() {
            @Override
            public void serviceChanged(final ServiceEvent event) {
                switch (event.getType()){
                    case ServiceEvent.UNREGISTERING:
                        unregistered.fire(Utils.isInstanceOf(event.getServiceReference(), ManagedResourceConnector.class));
                        return;
                    case ServiceEvent.REGISTERED:
                        registered.fire(Utils.isInstanceOf(event.getServiceReference(), ManagedResourceConnector.class));
                }
            }
        });
        stopResourceConnector(context);
        startResourceConnector(context);
        assertTrue(unregistered.getAwaitor().await(TimeSpan.fromSeconds(2)));
        assertTrue(registered.getAwaitor().await(TimeSpan.fromSeconds(2)));
    }

    @Test
    public final void testForAttributeConfigDescription(){
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
    public final void testForAttributesDiscovery(){
        final Collection<AttributeConfiguration> discoveredAttributes = ManagedResourceConnectorClient.discoverEntities(getTestBundleContext(),
                CONNECTOR_NAME,
                JMX_RMI_CONNECTION_STRING,
                ImmutableMap.of("login", AbstractJmxConnectorTest.JMX_LOGIN, "password", AbstractJmxConnectorTest.JMX_PASSWORD),
                AttributeConfiguration.class);
        assertTrue(discoveredAttributes.size() > 30);
        for(final AttributeConfiguration config: discoveredAttributes) {
            assertTrue(config.getParameters().containsKey("objectName"));
            assertTrue(config.getAttributeName().length() > 0);
        }
    }

    @Test
    public final void testForNotificationsDiscovery(){
        final Collection<EventConfiguration> discoveredEvents = ManagedResourceConnectorClient.discoverEntities(getTestBundleContext(),
                CONNECTOR_NAME,
                JMX_RMI_CONNECTION_STRING,
                ImmutableMap.of("login", AbstractJmxConnectorTest.JMX_LOGIN, "password", AbstractJmxConnectorTest.JMX_PASSWORD),
                EventConfiguration.class);
        assertTrue(discoveredEvents.size() > 2);
        for(final EventConfiguration config: discoveredEvents) {
            assertTrue(config.getParameters().containsKey("objectName"));
            assertTrue(config.getCategory().length() > 0);
        }
    }

    @Test
    public final void maintenanceActionTest() throws InterruptedException, ExecutionException, TimeoutException {
        final Map<String, String> actions = ManagedResourceConnectorClient.getMaintenanceActions(getTestBundleContext(), CONNECTOR_NAME, null);
        assertFalse(actions.isEmpty());
        final String ACTION = "simulateConnectionAbort";
        assertTrue(actions.containsKey(ACTION));
        final Future<String> result = ManagedResourceConnectorClient.invokeMaintenanceAction(getTestBundleContext(), CONNECTOR_NAME, ACTION, null, null);
        assertEquals("OK", result.get(3, TimeUnit.SECONDS));
    }
}