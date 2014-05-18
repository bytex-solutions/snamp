package com.itworks.snamp.testing.connectors.jmx;

import com.itworks.snamp.SimpleTable;
import com.itworks.snamp.SynchronizationEvent;
import com.itworks.snamp.Table;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.notifications.Notification;
import com.itworks.snamp.connectors.notifications.NotificationSupport;
import org.apache.commons.collections4.Equator;
import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.SetUtils;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import javax.management.AttributeChangeNotification;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
public final class JmxConnectorTest extends AbstractJmxConnectorTest<TestManagementBean> {

    public JmxConnectorTest() throws MalformedObjectNameException {
        super(new TestManagementBean(), new ObjectName(TestManagementBean.BEAN_NAME));
    }

    @Override
    protected final void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Factory<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attribute = attributeFactory.create();
        attribute.setAttributeName("string");
        attribute.getParameters().put("objectName", TestManagementBean.BEAN_NAME);
        attributes.put("1.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("boolean");
        attribute.getParameters().put("objectName", TestManagementBean.BEAN_NAME);
        attributes.put("2.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("int32");
        attribute.getParameters().put("objectName", TestManagementBean.BEAN_NAME);
        attributes.put("3.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("bigint");
        attribute.getParameters().put("objectName", TestManagementBean.BEAN_NAME);
        attributes.put("4.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("array");
        attribute.getParameters().put("objectName", TestManagementBean.BEAN_NAME);
        attributes.put("5.1", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("dictionary");
        attribute.getParameters().put("objectName", TestManagementBean.BEAN_NAME);
        attributes.put("6.1", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("table");
        attribute.getParameters().put("objectName", TestManagementBean.BEAN_NAME);
        attributes.put("7.1", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("float");
        attribute.getParameters().put("objectName", TestManagementBean.BEAN_NAME);
        attributes.put("8.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", TestManagementBean.BEAN_NAME);
        attributes.put("9.0", attribute);
    }

    @Override
    protected void fillEvents(final Map<String, EventConfiguration> events, final Factory<EventConfiguration> eventFactory) {
        EventConfiguration event = eventFactory.create();
        event.setCategory(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", TestManagementBean.BEAN_NAME);
        events.put("19.1", event);

        event = eventFactory.create();
        event.setCategory("com.itworks.snamp.connectors.tests.jmx.testnotif");
        event.getParameters().put("severity", "panic");
        event.getParameters().put("objectName", TestManagementBean.BEAN_NAME);
        events.put("20.1", event);
    }

    @Test
    public final void notificationTest() throws TimeoutException, InterruptedException {
        final NotificationSupport notificationSupport = getManagementConnector(getTestBundleContext()).queryObject(NotificationSupport.class);
        final AttributeSupport attributeSupport = getManagementConnector(getTestBundleContext()).queryObject(AttributeSupport.class);
        assertNotNull(notificationSupport);
        assertEquals(2, notificationSupport.getEnabledNotifications().size());
        assertTrue(notificationSupport.getEnabledNotifications().contains("19.1"));
        assertTrue(notificationSupport.getEnabledNotifications().contains("20.1"));
        final String TEST_LISTENER1_ID = "test-listener";
        final String TEST_LISTENER2_ID = "test-listener-2";
        final SynchronizationListener listener1 = new SynchronizationListener("19.1");
        final SynchronizationListener listener2 = new SynchronizationListener("20.1");
        assertTrue(notificationSupport.subscribe(TEST_LISTENER1_ID,  listener1));
        assertTrue(notificationSupport.subscribe(TEST_LISTENER2_ID, listener2));
        final SynchronizationEvent.Awaitor<Notification> awaitor1 = listener1.getAwaitor();
        final SynchronizationEvent.Awaitor<Notification> awaitor2 = listener2.getAwaitor();
        //force property changing
        assertTrue(attributeSupport.setAttribute("1.0", TimeSpan.INFINITE, "Frank Underwood"));
        final Notification notif1 = awaitor1.await(TimeSpan.fromSeconds(5L));
        assertNotNull(notif1);
        assertEquals(Notification.Severity.NOTICE, notif1.getSeverity());
        assertEquals("Property string is changed", notif1.getMessage());
        assertEquals("string", notif1.get("attributeName"));
        assertEquals("NO VALUE", notif1.get("oldValue"));
        assertEquals("Frank Underwood", notif1.get("newValue"));
        assertEquals(String.class.getName(), notif1.get("attributeType"));
        final Notification notif2 = awaitor2.await(TimeSpan.fromSeconds(5L));
        assertNotNull(notif2);
        assertEquals(Notification.Severity.PANIC, notif2.getSeverity());
        assertEquals("Property changed", notif2.getMessage());
    }

    @Test
    public final void testForTableProperty() throws TimeoutException{
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

            @Override
            public int hash(final Table o) {
                return System.identityHashCode(o);
            }
        });
    }

    @Test
    public final void testForDictionaryProperty() throws TimeoutException{
        final Map<String, Object> dict = new HashMap<>(3);
        dict.put("col1", Boolean.TRUE);
        dict.put("col2", 42);
        dict.put("col3", "Frank Underwood");
        testAttribute("6.1", "dictionary", Map.class, dict, new Equator<Map>() {
            @Override
            public boolean equate(final Map o1, final Map o2) {
                if(o1.size() == o2.size()) {
                    for (final Object key : o1.keySet())
                        if (!o2.containsKey(key)) return false;
                    return true;
                }
                else return false;
            }

            @Override
            public int hash(final Map o) {
                return System.identityHashCode(o);
            }
        });
    }

    @Test
    public final void testForArrayProperty() throws TimeoutException{
        final Object[] array = new Short[]{10, 20, 30, 40, 50};
        testAttribute("5.1", "array", Object[].class, array, new Equator<Object[]>() {
            @Override
            public boolean equate(final Object[] o1, final Object[] o2) {
                return Arrays.equals(o1, o2);
            }

            @Override
            public int hash(final Object[] o) {
                return System.identityHashCode(o);
            }
        });
    }

    @Test
    public final void testForDateProperty() throws TimeoutException{
        testAttribute("9.0", "date", Date.class, new Date());
    }

    @Test
    public final void testForFloatProperty() throws TimeoutException{
        testAttribute("8.0", "float", Float.class, 3.14F);
    }

    @Test
    public final void testForBigIntProperty() throws TimeoutException{
        testAttribute("4.0", "bigint", BigInteger.class, BigInteger.valueOf(100500));
    }

    @Test
    public final void testForInt32Property() throws TimeoutException{
        testAttribute("3.0", "int32", Integer.class, 42);
    }

    @Test
    public final void testForBooleanProperty() throws TimeoutException {
        testAttribute("2.0", "boolean", Boolean.class, Boolean.TRUE);
    }

    @Test
    public final void testForStringProperty() throws TimeoutException {
        testAttribute("1.0", "string", String.class, "Frank Underwood");
    }
}
