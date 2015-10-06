package com.bytex.snamp.testing.adapters.snmp;

import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.ExceptionalCallable;
import com.bytex.snamp.SafeConsumer;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.adapters.ResourceAdapter;
import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.adapters.ResourceAdapterClient;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.bytex.snamp.configuration.ConfigurationEntityDescription;
import com.bytex.snamp.connectors.notifications.Severity;
import com.bytex.snamp.internal.EntryReader;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.SnmpTable;
import com.bytex.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connectors.jmx.TestOpenMBean;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanAttributeInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.bytex.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;

/**
 * Represents integration tests for JMX resource connector and SNMP resource adapter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.SNMP_ADAPTER)
public final class JmxToSnmpV2Test extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String INSTANCE_NAME = "test-snmp";
    private static final String ADAPTER_NAME = "snmp";
    private static final String SNMP_PORT = "3222";
    private static final String SNMP_HOST = "127.0.0.1";
    private final SnmpClient client;

    public JmxToSnmpV2Test() throws MalformedObjectNameException, IOException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME));
        client = SnmpClientFactory.createSnmpV2("udp:" + SNMP_HOST + "/" + SNMP_PORT);
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
        }, TimeSpan.ofSeconds(4));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        ResourceAdapterActivator.stopResourceAdapter(context, ADAPTER_NAME);
        stopResourceConnector(context);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        try {
            client.close();
        }
        finally {
            super.afterCleanupTest(context);
        }
    }

    @Test
    public void renameResourceTest() throws Exception {
        final String NEW_RESOURCE_NAME = "temp-resource";
        //change the name of the resource
        processConfiguration(new SafeConsumer<AgentConfiguration>() {
            @Override
            public void accept(final AgentConfiguration config) {
                final ManagedResourceConfiguration resource = config.getManagedResources().get(TEST_RESOURCE_NAME);
                assertNotNull(resource);
                config.getManagedResources().remove(TEST_RESOURCE_NAME);
                config.getManagedResources().put(NEW_RESOURCE_NAME, resource);
            }
        }, true);
        Thread.sleep(1000);
        processConfiguration(new SafeConsumer<AgentConfiguration>() {
            @Override
            public void accept(final AgentConfiguration config) {
                final ManagedResourceConfiguration resource = config.getManagedResources().get(NEW_RESOURCE_NAME);
                assertNotNull(resource);
                config.getManagedResources().remove(NEW_RESOURCE_NAME);
                config.getManagedResources().put(TEST_RESOURCE_NAME, resource);
            }
        }, true);
    }

    @Test
    public void startStopTest() throws Exception {
        final TimeSpan TIMEOUT = TimeSpan.ofSeconds(14);
        //stop adapter and connector
        ResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        stopResourceConnector(getTestBundleContext());
        //start empty adapter
        syncWithAdapterStartedEvent(ADAPTER_NAME, new ExceptionalCallable<Void, BundleException>() {
            @Override
            public Void call() throws BundleException {
                ResourceAdapterActivator.startResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
                return null;
            }
        }, TIMEOUT);
        //start connector, this causes attribute registration and SNMP adapter updating
        syncWithAdapterUpdatedEvent(ADAPTER_NAME, new ExceptionalCallable<Void, Exception>() {
            @Override
            public Void call() throws Exception {
                startResourceConnector(getTestBundleContext());
                return null;
            }
        }, TIMEOUT);
        //check whether the attribute is accessible
        testForStringProperty();
        //now stops the connector again
        stopResourceConnector(getTestBundleContext());
        //stop the adapter
        ResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
    }

    @Test
    public final void testForStringProperty() throws IOException {
            final String valueToCheck = "SETTED VALUE";
            final OID attributeId = new OID("1.1.1.0");
            client.writeAttribute(attributeId, valueToCheck, String.class);
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, attributeId, String.class));
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET_BULK, attributeId, String.class));
    }

    @Test
    public final void testForFloatProperty() throws IOException {
        final float valueToCheck = 31.337F;
        final OID oid = new OID("1.1.8.0");
        client.writeAttribute(oid, valueToCheck, Float.class);
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, oid, Float.class), 0.000001);
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET_BULK, oid, Float.class), 0.000001);
    }

    @Test
    public final void testForDatePropertyCustomDisplayFormat() throws IOException {
        final Calendar cal = Calendar.getInstance();
        cal.set(1994, Calendar.APRIL, 5); // Kurt Donald Cobain, good night, sweet prince
        cal.set(Calendar.MILLISECOND, 0);
        final String valueToCheck = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(cal.getTime());
        final OID oid = new OID("1.1.9.0");
        client.writeAttribute(oid, valueToCheck, String.class);
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, oid, String.class));
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET_BULK, oid, String.class));
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Test
    public final void testForDatePropertyRFC1903HumanReadable() throws IOException {
        final Calendar cal = Calendar.getInstance();
        cal.set(1994, Calendar.APRIL, 5); // Kurt Donald Cobain, good night, sweet prince
        cal.set(Calendar.MILLISECOND, 0);
        final SnmpHelpers.DateTimeFormatter formatter = SnmpHelpers.createDateTimeFormatter("rfc1903-human-readable");
        final String valueToCheck = new String(formatter.convert(cal.getTime()));
        final OID oid = new OID("1.1.10.0");
        client.writeAttribute(oid, valueToCheck, String.class);
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, oid, String.class));
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET_BULK, oid, String.class));
    }

    @Test
    public final void testForDatePropertyRFC1903() throws IOException, ParseException {
        final Calendar cal = Calendar.getInstance();
        cal.set(1994, Calendar.APRIL, 5); // Kurt Donald Cobain, good night, sweet prince
        cal.set(Calendar.MILLISECOND, 0);
        final SnmpHelpers.DateTimeFormatter formatter = SnmpHelpers.createDateTimeFormatter("rfc1903");
        final byte[] byteString = formatter.convert(cal.getTime());
        assertEquals(formatter.convert(byteString), cal.getTime());
        final OID oid = new OID("1.1.11.0");
        client.writeAttribute(oid, byteString, byte[].class);
        final byte[] actual = client.readAttribute(ReadMethod.GET, oid, byte[].class);
        assertArrayEquals(byteString, actual);
        assertArrayEquals(byteString, client.readAttribute(ReadMethod.GET_BULK, oid, byte[].class));

    }

    @Override
    protected void fillAdapters(final Map<String, ResourceAdapterConfiguration> adapters, final Supplier<ResourceAdapterConfiguration> adapterFactory) {
        final ResourceAdapterConfiguration snmpAdapter = adapterFactory.get();
        snmpAdapter.setAdapterName(ADAPTER_NAME);
        snmpAdapter.getParameters().put("port", SNMP_PORT);
        snmpAdapter.getParameters().put("hostName", SNMP_HOST);
        snmpAdapter.getParameters().put("socketTimeout", "5000");
        snmpAdapter.getParameters().put("context", "1.1");
        snmpAdapter.getParameters().put("restartTimeout", "4000");
        adapters.put(INSTANCE_NAME, snmpAdapter);
    }

    @Test
    public final void testForBooleanProperty() throws IOException {
            final boolean valueToCheck = true;
            final OID oid = new OID("1.1.2.0");
            client.writeAttribute(oid, valueToCheck, Boolean.class);
            assertTrue(client.readAttribute(ReadMethod.GET, oid, Boolean.class));
            assertTrue(client.readAttribute(ReadMethod.GET_BULK, oid, Boolean.class));
    }

    @Test
    public final void testForInt32Property() throws IOException {
            final int valueToCheck = 42;
            final OID oid = new OID("1.1.3.0");
            client.writeAttribute(oid, valueToCheck, Integer.class);
            assertEquals(valueToCheck, (int) client.readAttribute(ReadMethod.GET, oid, Integer.class));
            assertEquals(valueToCheck, (int) client.readAttribute(ReadMethod.GET_BULK, oid, Integer.class));
    }

    @Test
    public final void testForBigIntProperty() throws IOException {
        final BigInteger valueToCheck = new BigInteger("100500");
        final OID oid = new OID("1.1.4.0");
        client.writeAttribute(oid, valueToCheck, BigInteger.class);
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, oid, BigInteger.class));
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET_BULK, oid, BigInteger.class));
    }

    @Test
    public final void testForTableProperty() throws Exception {
        final SnmpTable table = new AbstractSnmpTable(Boolean.class, Integer.class, String.class) {
            private final ImmutableList<Variable[]> rows = ImmutableList.of(
                    new Variable[]{new Integer32(0), new Integer32(4230), new OctetString("Row #1")},
                    new Variable[]{new Integer32(1), new Integer32(4231), new OctetString("Row #2")},
                    new Variable[]{new Integer32(1), new Integer32(4232), new OctetString("Row #3")},
                    new Variable[]{new Integer32(1), new Integer32(4233), new OctetString("Row #4")}
                );

            @Override
            public int getRowCount() {
                return rows.size();
            }

            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public Variable getRawCell(final int columnIndex, final int rowIndex) {
                return rows.get(rowIndex)[columnIndex];
            }
        };
        client.writeTable("1.1.7.1", table);
        final SnmpTable result = client.readTable(ReadMethod.GET_BULK, new OID("1.1.7.1"),
                Boolean.class,
            Integer.class,
            String.class
        );
        assertEquals(4, result.getRowCount());
        assertEquals(false, result.getCell(0, 0));
        assertEquals(4230, result.getCell(1, 0));
        assertEquals("Row #1", result.getCell(2, 0));

        assertEquals(true, result.getCell(0, 3));
        assertEquals(4233, result.getCell(1, 3));
        assertEquals("Row #4", result.getCell(2, 3));
    }

    @Test
    public final void testForArrayProperty() throws Exception {
        SnmpTable array = new AbstractSnmpTable() {
            private final ImmutableList<Variable[]> rows = ImmutableList.of(
                new Variable[]{new Integer32(20)},
                new Variable[]{new Integer32(30)}
            );

            @Override
            public int getRowCount() {
                return rows.size();
            }

            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public Variable getRawCell(final int columnIndex, final int rowIndex) {
                return rows.get(rowIndex)[columnIndex];
            }
        };
        client.writeTable("1.1.5.1", array);
        array = client.readTable(ReadMethod.GET_BULK, new OID("1.1.5.1"), Integer.class);
        assertEquals(2, array.getRowCount());
        assertEquals(20, array.getCell(0, 0));
        assertEquals(30, array.getCell(0, 1));
    }

    @Test
    public final void testForDictionaryProperty() throws Exception {
        SnmpTable dict = new AbstractSnmpTable() {
            private final Variable[] row = {
                new Integer32(0),
                new Integer32(4230),
                new OctetString("Test for dictionary property")
            };

            @Override
            public int getRowCount() {
                return 1;
            }

            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public Variable getRawCell(final int columnIndex, final int rowIndex) {
                return row[columnIndex];
            }
        };
        client.writeTable("1.1.6.1", dict);
        dict = client.readTable(ReadMethod.GET_BULK, new OID("1.1.6.1"),
            Boolean.class,
            Integer.class,
            String.class
        );
        assertEquals(1, dict.getRowCount());
        assertEquals(false, dict.getCell(0, 0));
        assertEquals(4230, dict.getCell(1, 0));
        assertEquals("Test for dictionary property", dict.getCell(2, 0));
    }

    @Test
    public final void notificationTest() throws Exception {
        final Future<SnmpNotification> awaitor1 = client.addNotificationListener(new OID("1.1.19.1"));
        final Future<SnmpNotification> awaitor2 = client.addNotificationListener(new OID("1.1.20.1"));
        final Future<SnmpNotification> awaitor3 = client.addNotificationListener(new OID("1.1.21.1"));
        client.writeAttribute(new OID("1.1.1.0"), "NOTIFICATION TEST", String.class);
        final SnmpNotification p1 = awaitor1.get(10, TimeUnit.SECONDS);
        final SnmpNotification p2 = awaitor2.get(10, TimeUnit.SECONDS);
        final SnmpNotification p3 = awaitor3.get(10, TimeUnit.SECONDS);
        assertNotNull(p1);
        assertNotNull(p2);
        assertEquals(Severity.NOTICE, p1.getSeverity());
        assertEquals(Severity.PANIC, p2.getSeverity());
        assertEquals(Severity.NOTICE, p3.getSeverity());
        assertEquals(0L, p1.getSequenceNumber());
        assertEquals(1L, p2.getSequenceNumber());
        assertEquals(2L, p3.getSequenceNumber());
        assertNotNull(p3.getCategory());
        assertEquals("Property string is changed", p1.getMessage());
        assertEquals("Property changed", p2.getMessage());
        assertTrue(p3.size() > 10);
    }

    @Test
    public void configurationDescriptorTest() throws BundleException {
        ConfigurationEntityDescription desc = ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, ResourceAdapterConfiguration.class);
        testConfigurationDescriptor(desc,
                "context",
                "engineID",
                "snmpv3-groups",
                "socketTimeout",
                "port",
                "host",
                "ldap-uri",
                "minPoolSize",
                "maxPoolSize",
                "queueSize",
                "keepAliveTime",
                "priority",
                "restartTimeout",
                "ldap-user",
                "ldap-password",
                "ldap-auth-protocol",
                "ldap-base-dn",
                "ldap-user-search-filter");
        desc = ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, AttributeConfiguration.class);
        testConfigurationDescriptor(desc,
                "oid",
                "displayFormat");
        desc = ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, EventConfiguration.class);
        testConfigurationDescriptor(desc,
                "oid",
                "displayFormat",
                "receiverAddress",
                "receiverName",
                "sendingTimeout",
                "retryCount");
    }

    @Test
    public void attributesBindingTest() throws TimeoutException, InterruptedException, ExecutionException {
        final ResourceAdapterClient client = new ResourceAdapterClient(getTestBundleContext(), INSTANCE_NAME, TimeSpan.ofSeconds(2));
        try {
            assertTrue(client.forEachFeature(MBeanAttributeInfo.class, new EntryReader<String, ResourceAdapter.FeatureBindingInfo<MBeanAttributeInfo>, ExceptionPlaceholder>() {
                @Override
                public boolean read(final String resourceName, final ResourceAdapter.FeatureBindingInfo<MBeanAttributeInfo> bindingInfo) {
                    return bindingInfo.getProperty(ResourceAdapter.FeatureBindingInfo.MAPPED_TYPE) instanceof Enum &&
                            bindingInfo.getProperty("OID") instanceof OID;
                }
            }));
        } finally {
            client.release(getTestBundleContext());
        }
    }

    @Test
    public void notificationsBindingTest() throws TimeoutException, InterruptedException, ExecutionException {
        final ResourceAdapterClient client = new ResourceAdapterClient(getTestBundleContext(), INSTANCE_NAME, TimeSpan.ofSeconds(2));
        try {
            assertTrue(client.forEachFeature(MBeanAttributeInfo.class, new EntryReader<String, ResourceAdapter.FeatureBindingInfo<MBeanAttributeInfo>, ExceptionPlaceholder>() {
                @Override
                public boolean read(final String resourceName, final ResourceAdapter.FeatureBindingInfo<MBeanAttributeInfo> bindingInfo) {
                    return bindingInfo.getProperty("OID") instanceof OID;
                }
            }));
        } finally {
            client.release(getTestBundleContext());
        }
    }

    @Override
    protected void fillEvents(final Map<String, EventConfiguration> events, final Supplier<EventConfiguration> eventFactory) {
        EventConfiguration event = eventFactory.get();
        event.setCategory(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);
        event.getParameters().put("receiverAddress", SNMP_HOST + "/" + client.getClientPort());
        event.getParameters().put("receiverName", "test-receiver-1");
        event.getParameters().put("oid", "1.1.19.1");
        events.put("19.1", event);

        event = eventFactory.get();
        event.setCategory("com.bytex.snamp.connectors.tests.impl.testnotif");
        event.getParameters().put("severity", "panic");
        event.getParameters().put("objectName", BEAN_NAME);
        event.getParameters().put("receiverAddress", SNMP_HOST + "/" + client.getClientPort());
        event.getParameters().put("receiverName", "test-receiver-2");
        event.getParameters().put("oid", "1.1.20.1");
        events.put("20.1", event);

        event = eventFactory.get();
        event.setCategory("com.bytex.snamp.connectors.tests.impl.plainnotif");
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);
        event.getParameters().put("receiverAddress", SNMP_HOST + "/" + client.getClientPort());
        event.getParameters().put("receiverName", "test-receiver-3");
        event.getParameters().put("oid", "1.1.21.1");
        events.put("21.1", event);
    }

    @Override
    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Supplier<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attribute = attributeFactory.get();
        attribute.setAttributeName("string");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.1.0");
        attributes.put("1.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("boolean");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.2.0");
        attributes.put("2.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("int32");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.3.0");
        attributes.put("3.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("bigint");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.4.0");
        attributes.put("4.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("array");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.5.1");
        attributes.put("5.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("dictionary");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.6.1");
        attributes.put("6.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("table");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.7.1");
        attributes.put("7.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("float");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.8.0");
        attributes.put("8.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("displayFormat", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        attribute.getParameters().put("oid", "1.1.9.0");
        attributes.put("9.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("displayFormat", "rfc1903-human-readable");
        attribute.getParameters().put("oid", "1.1.10.0");
        attributes.put("10.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("displayFormat", "rfc1903");
        attribute.getParameters().put("oid", "1.1.11.0");
        attributes.put("11.0", attribute);
    }
}
