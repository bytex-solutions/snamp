package com.itworks.snamp.testing.connectors.jmx;

import com.itworks.snamp.SimpleTable;
import com.itworks.snamp.SynchronizationEvent;
import com.itworks.snamp.Table;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.notifications.Notification;
import com.itworks.snamp.connectors.notifications.NotificationSupport;
import com.itworks.snamp.connectors.notifications.Severity;
import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.SetUtils;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import javax.management.AttributeChangeNotification;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.itworks.snamp.connectors.notifications.NotificationUtils.SynchronizationListener;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@ExamReactorStrategy(PerMethod.class)
public final class JmxConnectorWIthOpenMBeanTest extends AbstractJmxConnectorTest<TestOpenMBean> {

    public JmxConnectorWIthOpenMBeanTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(TestOpenMBean.BEAN_NAME));
    }

    @Override
    protected final void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Factory<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attribute = attributeFactory.create();
        attribute.setAttributeName("string");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("1.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("boolean");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("2.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("int32");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("3.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("bigint");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("4.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("array");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("5.1", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("dictionary");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("6.1", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("table");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("7.1", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("float");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("8.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("9.0", attribute);
    }

    @Override
    protected void fillEvents(final Map<String, EventConfiguration> events, final Factory<EventConfiguration> eventFactory) {
        EventConfiguration event = eventFactory.create();
        event.setCategory(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        events.put("19.1", event);

        event = eventFactory.create();
        event.setCategory("com.itworks.snamp.connectors.tests.impl.testnotif");
        event.getParameters().put("severity", "panic");
        event.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        events.put("20.1", event);
    }

    @Test
    public final void notificationTest() throws TimeoutException, InterruptedException {
        final NotificationSupport notificationSupport = getManagementConnector(getTestBundleContext()).queryObject(NotificationSupport.class);
        final AttributeSupport attributeSupport = getManagementConnector(getTestBundleContext()).queryObject(AttributeSupport.class);
        assertNotNull(notificationSupport);
        assertNotNull(attributeSupport);
        assertNotNull(attributeSupport.connectAttribute("1.0", "string", new HashMap<String, String>(2) {{
            put("objectName", TestOpenMBean.BEAN_NAME);
        }}));
        assertNotNull(notificationSupport.enableNotifications("19.1", AttributeChangeNotification.ATTRIBUTE_CHANGE, new HashMap<String, String>(2) {{
            put("severity", "notice");
            put("objectName", TestOpenMBean.BEAN_NAME);
        }}));
        assertNotNull(notificationSupport.enableNotifications("20.1", "com.itworks.snamp.connectors.tests.impl.testnotif", new HashMap<String, String>(2){{
            put("severity", "panic");
            put("objectName", TestOpenMBean.BEAN_NAME);
        }}));
        assertEquals(2, notificationSupport.getEnabledNotifications().size());
        assertTrue(notificationSupport.getEnabledNotifications().contains("19.1"));
        assertTrue(notificationSupport.getEnabledNotifications().contains("20.1"));
        final String TEST_LISTENER1_ID = "test-listener";
        final String TEST_LISTENER2_ID = "test-listener-2";
        final SynchronizationListener listener1 = new SynchronizationListener("19.1");
        final SynchronizationListener listener2 = new SynchronizationListener("20.1");
        assertTrue(notificationSupport.subscribe(TEST_LISTENER1_ID,  listener1, false));
        assertTrue(notificationSupport.subscribe(TEST_LISTENER2_ID, listener2, false));
        final SynchronizationEvent.Awaitor<Notification> awaitor1 = listener1.getAwaitor();
        final SynchronizationEvent.Awaitor<Notification> awaitor2 = listener2.getAwaitor();
        //force property changing
        assertTrue(attributeSupport.setAttribute("1.0", TimeSpan.INFINITE, "Frank Underwood"));
        final Notification notif1 = awaitor1.await(TimeSpan.fromSeconds(5L));
        assertNotNull(notif1);
        assertEquals(Severity.NOTICE, notif1.getSeverity());
        assertEquals("Property string is changed", notif1.getMessage());
        assertEquals("string", notif1.get("attributeName"));
        assertEquals("NO VALUE", notif1.get("oldValue"));
        assertEquals("Frank Underwood", notif1.get("newValue"));
        assertEquals(String.class.getName(), notif1.get("attributeType"));
        final Notification notif2 = awaitor2.await(TimeSpan.fromSeconds(5L));
        assertNotNull(notif2);
        assertEquals(Severity.PANIC, notif2.getSeverity());
        assertEquals("Property changed", notif2.getMessage());
    }

    @Test
    public final void simulateConnectionAbortTest() throws TimeoutException, InterruptedException, ExecutionException {
        final NotificationSupport notificationSupport = getManagementConnector(getTestBundleContext()).queryObject(NotificationSupport.class);
        final AttributeSupport attributeSupport = getManagementConnector(getTestBundleContext()).queryObject(AttributeSupport.class);
        assertNotNull(notificationSupport);
        assertNotNull(attributeSupport);
        assertNotNull(attributeSupport.connectAttribute("1.0", "string", new HashMap<String, String>(2) {{
            put("objectName", TestOpenMBean.BEAN_NAME);
        }}));
        assertNotNull(notificationSupport.enableNotifications("19.1", AttributeChangeNotification.ATTRIBUTE_CHANGE, new HashMap<String, String>(2) {{
            put("severity", "notice");
            put("objectName", TestOpenMBean.BEAN_NAME);
        }}));
        assertNotNull(notificationSupport.enableNotifications("20.1", "com.itworks.snamp.connectors.tests.impl.testnotif", new HashMap<String, String>(2){{
            put("severity", "panic");
            put("objectName", TestOpenMBean.BEAN_NAME);
        }}));
        assertEquals(2, notificationSupport.getEnabledNotifications().size());
        assertTrue(notificationSupport.getEnabledNotifications().contains("19.1"));
        assertTrue(notificationSupport.getEnabledNotifications().contains("20.1"));
        final String TEST_LISTENER1_ID = "test-listener";
        final String TEST_LISTENER2_ID = "test-listener-2";
        final SynchronizationListener listener1 = new SynchronizationListener("19.1");
        final SynchronizationListener listener2 = new SynchronizationListener("20.1");
        assertTrue(notificationSupport.subscribe(TEST_LISTENER1_ID,  listener1, false));
        assertTrue(notificationSupport.subscribe(TEST_LISTENER2_ID, listener2, false));
        final SynchronizationEvent.Awaitor<Notification> awaitor1 = listener1.getAwaitor();
        final SynchronizationEvent.Awaitor<Notification> awaitor2 = listener2.getAwaitor();
        //simulate connection abort
        assertEquals("OK", ManagedResourceConnectorClient.invokeMaintenanceAction(getTestBundleContext(), CONNECTOR_NAME, "simulateConnectionAbort", null, null).get(3, TimeUnit.SECONDS));
        //force property changing
        assertTrue(attributeSupport.setAttribute("1.0", TimeSpan.INFINITE, "Frank Underwood"));
        final Notification notif1 = awaitor1.await(TimeSpan.fromSeconds(5L));
        assertNotNull(notif1);
        assertEquals(Severity.NOTICE, notif1.getSeverity());
        assertEquals("Property string is changed", notif1.getMessage());
        assertEquals("string", notif1.get("attributeName"));
        assertEquals("NO VALUE", notif1.get("oldValue"));
        assertEquals("Frank Underwood", notif1.get("newValue"));
        assertEquals(String.class.getName(), notif1.get("attributeType"));
        final Notification notif2 = awaitor2.await(TimeSpan.fromSeconds(5L));
        assertNotNull(notif2);
        assertEquals(Severity.PANIC, notif2.getSeverity());
        assertEquals("Property changed", notif2.getMessage());
    }

    @Test
    public final void testForTableProperty() throws TimeoutException, IOException {
        final Table<String> table = new SimpleTable<>(new HashMap<String, Class<?>>(3){{
            put("col1", Boolean.class);
            put("col2", Integer.class);
            put("col3", String.class);
        }});
        table.addRow(new HashMap<String, Object>(3){{
            put("col1", true);
            put("col2", 42);
            put("col3", "Frank Underwood");
        }});
        table.addRow(new HashMap<String, Object>(3){{
            put("col1", true);
            put("col2", 43);
            put("col3", "Peter Russo");
        }});
        testAttribute("7.1", "table", Table.class, table, new Equator<Table>() {
            @Override
            public boolean equate(final Table o1, final Table o2) {
                return o1.getRowCount() == o2.getRowCount() &&
                        SetUtils.isEqualSet(o1.getColumns(), o2.getColumns());
            }
        });
    }

    @Test
    public final void testForDictionaryProperty() throws TimeoutException, IOException {
        final Map<String, Object> dict = new HashMap<>(3);
        dict.put("col1", Boolean.TRUE);
        dict.put("col2", 42);
        dict.put("col3", "Frank Underwood");
        testAttribute("6.1", "dictionary", Map.class, dict, mapEquator());
    }

    @Test
    public final void testForArrayProperty() throws TimeoutException, IOException {
        final Object[] array = new Short[]{10, 20, 30, 40, 50};
        testAttribute("5.1", "array", Object[].class, array, arrayEquator());
    }

    @Test
    public final void testForDateProperty() throws TimeoutException, IOException {
        testAttribute("9.0", "date", Date.class, new Date());
    }

    @Test
    public final void testForFloatProperty() throws TimeoutException, IOException {
        testAttribute("8.0", "float", Float.class, 3.14F);
    }

    @Test
    public final void testForBigIntProperty() throws TimeoutException, IOException {
        testAttribute("4.0", "bigint", BigInteger.class, BigInteger.valueOf(100500));
    }

    @Test
    public final void testForInt32Property() throws TimeoutException, IOException {
        testAttribute("3.0", "int32", Integer.class, 42);
    }

    @Test
    public final void testForBooleanProperty() throws TimeoutException, IOException {
        testAttribute("2.0", "boolean", Boolean.class, Boolean.TRUE);
    }

    @Test
    public final void testForStringProperty() throws TimeoutException, IOException {
        testAttribute("1.0", "string", String.class, "Frank Underwood");
    }

    @Test
    public final void testForAttributeConfigDescription(){
        final ConfigurationEntityDescription<AttributeConfiguration> description = ManagedResourceConnectorClient.getConfigurationEntityDescriptor(getTestBundleContext(),
                CONNECTOR_NAME,
                AttributeConfiguration.class);
        final ConfigurationEntityDescription.ParameterDescription param = description.getParameterDescriptor("objectName");
        final String defValue = param.getDescription(null);//default locale
        assertTrue(defValue.length() > 0);
        final String ruValue = param.getDescription(Locale.forLanguageTag("RU"));
        assertTrue(ruValue.length() > 0);
        assertNotEquals(defValue, ruValue);
    }

    @Test
    public final void testForAttributesDiscovery(){
        final Collection<AttributeConfiguration> discoveredAttributes = ManagedResourceConnectorClient.discoverEntities(getTestBundleContext(),
                CONNECTOR_NAME,
                getJmxConnectionString(),
                Collections.<String, String>emptyMap(),
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
                getJmxConnectionString(),
                Collections.<String, String>emptyMap(),
                EventConfiguration.class);
        assertTrue(discoveredEvents.size() > 2);
        for(final EventConfiguration config: discoveredEvents) {
            assertTrue(config.getParameters().containsKey("objectName"));
            assertTrue(config.getCategory().length() > 0);
        }
    }

    @Test
    public final void licenseLimitationsDiscoveryTest(){
        final Map<String, String> lims = ManagedResourceConnectorClient.getLicenseLimitations(getTestBundleContext(), CONNECTOR_NAME, null);
        assertFalse(lims.isEmpty());
        assertEquals(3, lims.size());
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
