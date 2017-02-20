package com.bytex.snamp.testing.connector.jmx;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.metrics.*;
import com.bytex.snamp.connector.notifications.Mailbox;
import com.bytex.snamp.connector.notifications.MailboxFactory;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.connector.operations.OperationSupport;
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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class JmxConnectorWithOpenMBeanTest extends AbstractJmxConnectorTest<TestOpenMBean> {

    public JmxConnectorWithOpenMBeanTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(TestOpenMBean.BEAN_NAME));
    }

    @Override
    protected void fillOperations(final EntityMap<? extends OperationConfiguration> operations) {
        operations.addAndConsume("res", operation -> {
            operation.setAlternativeName("reverse");
            operation.put("objectName", TestOpenMBean.BEAN_NAME);
        });
        operations.addAndConsume("connectionAbort", operation -> operation.setAlternativeName("simulateConnectionAbort"));
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("1.0");
        attribute.setAlternativeName("string");
        attribute.put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("2.0");
        attribute.setAlternativeName("boolean");
        attribute.put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("3.0");
        attribute.setAlternativeName("int32");
        attribute.put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("4.0");
        attribute.setAlternativeName("bigint");
        attribute.put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("5.1");
        attribute.setAlternativeName("array");
        attribute.put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("6.1");
        attribute.setAlternativeName("dictionary");
        attribute.put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("7.1");
        attribute.setAlternativeName("table");
        attribute.put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("8.0");
        attribute.setAlternativeName("float");
        attribute.put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("9.0");
        attribute.setAlternativeName("date");
        attribute.put("objectName", TestOpenMBean.BEAN_NAME);
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd("19.1");
        event.setAlternativeName(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.put("severity", "notice");
        event.put("objectName", TestOpenMBean.BEAN_NAME);

        event = events.getOrAdd("20.1");
        event.setAlternativeName("com.bytex.snamp.connector.tests.impl.testnotif");
        event.put("severity", "panic");
        event.put("objectName", TestOpenMBean.BEAN_NAME);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        stopResourceConnector(context);
        super.afterCleanupTest(context);
    }

    @Test
    public void notificationTest() throws Exception {
        final ManagedResourceConnector resourceConnector = getManagementConnector();
        final NotificationSupport notificationSupport = resourceConnector.queryObject(NotificationSupport.class);
        final AttributeSupport attributeSupport = resourceConnector.queryObject(AttributeSupport.class);
        try {
            assertNotNull(notificationSupport);
            assertNotNull(attributeSupport);
            assertEquals(2, notificationSupport.getNotificationInfo().length);
            final Mailbox listener1 = MailboxFactory.newMailbox("19.1");
            final Mailbox listener2 = MailboxFactory.newMailbox("20.1");
            notificationSupport.addNotificationListener(listener1, listener1, null);
            notificationSupport.addNotificationListener(listener2, listener2, null);
            //force property changing
            attributeSupport.setAttribute(new Attribute("1.0", "Frank Underwood"));
            final Notification notif1 = listener1.poll(5, TimeUnit.SECONDS);
            assertNotNull(notif1);
            assertEquals("Property string is changed", notif1.getMessage());
            assertTrue(notif1.getUserData() instanceof CompositeData);
            final CompositeData attachment = (CompositeData) notif1.getUserData();
            assertEquals("string", attachment.get("attributeName"));
            assertEquals(String.class.getName(), attachment.get("attributeType"));
            final Notification notif2 = listener2.poll(5, TimeUnit.SECONDS);
            assertNotNull(notif2);
            assertEquals("Property changed", notif2.getMessage());
        } finally {
            releaseManagementConnector();
        }
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
        final ManagedResourceConnector resourceConnector = getManagementConnector();
        final NotificationSupport notificationSupport = resourceConnector.queryObject(NotificationSupport.class);
        final AttributeSupport attributeSupport = resourceConnector.queryObject(AttributeSupport.class);
        final OperationSupport operationSupport = resourceConnector.queryObject(OperationSupport.class);
        try {
            assertNotNull(notificationSupport);
            assertNotNull(attributeSupport);
            assertEquals(2, notificationSupport.getNotificationInfo().length);
            final Mailbox listener1 = MailboxFactory.newMailbox("19.1");
            final Mailbox listener2 = MailboxFactory.newMailbox("20.1");
            notificationSupport.addNotificationListener(listener1, listener1, null);
            notificationSupport.addNotificationListener(listener2, listener2, null);
            //simulate connection abort
            operationSupport.invoke("connectionAbort", ArrayUtils.emptyArray(Object[].class), ArrayUtils.emptyArray(String[].class));
            //force property changing
            attributeSupport.setAttribute(new Attribute("1.0", "Frank Underwood"));
            final Notification notif1 = listener1.poll(5, TimeUnit.SECONDS);
            assertNotNull(notif1);
            assertEquals("Property string is changed", notif1.getMessage());
            assertTrue(notif1.getUserData() instanceof CompositeData);
            final CompositeData attachment = (CompositeData) notif1.getUserData();
            assertEquals("string", attachment.get("attributeName"));
            assertEquals(String.class.getName(), attachment.get("attributeType"));
            final Notification notif2 = listener2.poll(5, TimeUnit.SECONDS);
            assertNotNull(notif2);
            assertEquals("Property changed", notif2.getMessage());
        } finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void operationTest() throws Exception{
        final OperationSupport operationSupport = getManagementConnector().queryObject(OperationSupport.class);
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
                .declareColumns(columns -> columns
                        .addColumn("col1", "dummy column", SimpleType.BOOLEAN, false)
                        .addColumn("col2", "dummy column", SimpleType.INTEGER, false)
                        .addColumn("col3", "dummy column", SimpleType.STRING, true))
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
        final ManagedResourceConnector client = getManagementConnector();
        try {
            final MetricsSupport metrics = client.queryObject(MetricsSupport.class);
            assertNotNull(metrics);
            assertTrue(metrics.getMetrics(AttributeMetric.class).iterator().hasNext());
            assertTrue(metrics.getMetrics(NotificationMetric.class).iterator().hasNext());
            assertTrue(metrics.getMetrics(OperationMetric.class).iterator().hasNext());
            //read and write attributes
            testForStringProperty();
            //verify metrics
            final AttributeMetric attrMetrics = metrics.getMetrics(AttributeMetric.class).iterator().next();
            assertTrue(attrMetrics.reads().getLastRate(MetricsInterval.HOUR) > 0);
            assertTrue(attrMetrics.reads().getLastRate(MetricsInterval.HOUR) > 0);
            assertTrue(attrMetrics.reads().getTotalRate() > 0);
            assertTrue(attrMetrics.writes().getTotalRate() > 0);
        } finally {
            releaseManagementConnector();
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
        final CompletableFuture<Boolean> unregistered = new CompletableFuture<>();
        final CompletableFuture<Boolean> registered = new CompletableFuture<>();
        ManagedResourceConnectorClient.addResourceListener(context, event -> {
            switch (event.getType()){
                case ServiceEvent.UNREGISTERING:
                    unregistered.complete(Utils.isInstanceOf(event.getServiceReference(), ManagedResourceConnector.class));
                    return;
                case ServiceEvent.REGISTERED:
                    registered.complete(Utils.isInstanceOf(event.getServiceReference(), ManagedResourceConnector.class));
            }
        });
        stopResourceConnector(context);
        startResourceConnector(context);
        assertTrue(unregistered.get(2, TimeUnit.SECONDS));
        assertTrue(registered.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testForAttributeConfigDescription(){
        testConfigurationDescriptor(ManagedResourceConfiguration.class, ImmutableSet.of(
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
                getConnectionString(),
                ImmutableMap.of("login", AbstractJmxConnectorTest.JMX_LOGIN, "password", AbstractJmxConnectorTest.JMX_PASSWORD),
                AttributeConfiguration.class);
        assertTrue(discoveredAttributes.size() > 30);
        for(final AttributeConfiguration config: discoveredAttributes) {
            assertTrue(config.containsKey("objectName"));
            assertTrue(config.containsKey(AttributeConfiguration.NAME_KEY));
        }
    }

    @Test
    public void testForNotificationsDiscovery(){
        final Collection<EventConfiguration> discoveredEvents = ManagedResourceConnectorClient.discoverEntities(getTestBundleContext(),
                CONNECTOR_NAME,
                getConnectionString(),
                ImmutableMap.of("login", AbstractJmxConnectorTest.JMX_LOGIN, "password", AbstractJmxConnectorTest.JMX_PASSWORD),
                EventConfiguration.class);
        assertTrue(discoveredEvents.size() > 2);
        for(final EventConfiguration config: discoveredEvents) {
            assertTrue(config.containsKey("objectName"));
            assertTrue(config.containsKey(EventConfiguration.NAME_KEY));
        }
    }
}
