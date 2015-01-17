package com.itworks.snamp.testing.adapters.snmp;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.itworks.snamp.ExceptionalCallable;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.adapters.ResourceAdapterClient;
import com.itworks.snamp.concurrent.SynchronizationEvent;
import com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.connectors.notifications.Severity;
import com.itworks.snamp.testing.Matrix;
import com.itworks.snamp.testing.MatrixImpl;
import com.itworks.snamp.testing.SnampFeature;
import com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.TestOpenMBean;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

import javax.management.AttributeChangeNotification;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.itworks.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * Represents integration tests for JMX resource connector and SNMP resource adapter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class JmxToSnmpV2Test extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String ADAPTER_NAME = "snmp";
    private static final String SNMP_PORT = "3222";
    private static final String SNMP_HOST = "127.0.0.1";
    private static final SnmpClient client = SnmpClientFactory.createSnmpV2("udp:" + SNMP_HOST + "/" + SNMP_PORT);

    public JmxToSnmpV2Test() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME),
                mavenBundle("org.apache.aries.jndi", "org.apache.aries.jndi", "1.0.0"),
                mavenBundle("org.apache.aries.jndi", "org.apache.aries.jndi.core", "1.0.0"),
                mavenBundle("org.apache.aries.jndi", "org.apache.aries.jndi.url", "1.0.0"),
                mavenBundle("org.apache.aries.jndi", "org.apache.aries.jndi.api", "1.0.0"),
                mavenBundle("org.apache.aries.jndi", "org.apache.aries.jndi.rmi", "1.0.0"),
                mavenBundle("org.apache.aries", "org.apache.aries.util", "1.0.0"),
                mavenBundle("org.apache.aries.proxy", "org.apache.aries.proxy.api", "1.0.0"),
                SnampFeature.SNMP4J.getReference(),
                SnampFeature.SNMP_ADAPTER.getReference());
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        super.beforeStartTest(context);
        beforeCleanupTest(context);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws BundleException, TimeoutException, InterruptedException {
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
    protected void beforeCleanupTest(final BundleContext context) throws BundleException, TimeoutException, InterruptedException {
        ResourceAdapterActivator.stopResourceAdapter(context, ADAPTER_NAME);
        stopResourceConnector(context);
    }

    @Test
    public final void testForStringProperty() throws IOException {
            final String valueToCheck = "SETTED VALUE";
            final OID attributeId = new OID("1.1.1.0");
            client.writeAttribute(attributeId, valueToCheck, String.class);
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, attributeId, String.class));
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GETBULK, attributeId, String.class));
    }

    @Test
    public final void testForFloatProperty() throws IOException {
        final float valueToCheck = 31.337F;
        final OID oid = new OID("1.1.8.0");
        client.writeAttribute(oid, valueToCheck, Float.class);
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, oid, Float.class), 0.000001);
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GETBULK, oid, Float.class), 0.000001);
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
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GETBULK, oid, String.class));
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
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GETBULK, oid, String.class));
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
        assertArrayEquals(byteString, client.readAttribute(ReadMethod.GETBULK, oid, byte[].class));

    }

    @Override
    protected void fillAdapters(final Map<String, ResourceAdapterConfiguration> adapters, final Supplier<ResourceAdapterConfiguration> adapterFactory) {
        final ResourceAdapterConfiguration snmpAdapter = adapterFactory.get();
        snmpAdapter.setAdapterName(ADAPTER_NAME);
        snmpAdapter.getParameters().put("port", SNMP_PORT);
        snmpAdapter.getParameters().put("host", SNMP_HOST);
        snmpAdapter.getParameters().put("socketTimeout", "5000");
        adapters.put("test-snmp", snmpAdapter);
    }

    @Test
    public final void testForBooleanProperty() throws IOException {
            final boolean valueToCheck = true;
            final OID oid = new OID("1.1.2.0");
            client.writeAttribute(oid, valueToCheck, Boolean.class);
            assertTrue(client.readAttribute(ReadMethod.GET, oid, Boolean.class));
            assertTrue(client.readAttribute(ReadMethod.GETBULK, oid, Boolean.class));
    }

    @Test
    public final void testForInt32Property() throws IOException {
            final int valueToCheck = 42;
            final OID oid = new OID("1.1.3.0");
            client.writeAttribute(oid, valueToCheck, Integer.class);
            assertEquals(valueToCheck, (int) client.readAttribute(ReadMethod.GET, oid, Integer.class));
            assertEquals(valueToCheck, (int) client.readAttribute(ReadMethod.GETBULK, oid, Integer.class));
    }

    @Test
    public final void testForBigIntProperty() throws IOException {
        final BigInteger valueToCheck = new BigInteger("100500");
        final OID oid = new OID("1.1.4.0");
        client.writeAttribute(oid, valueToCheck, BigInteger.class);
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, oid, BigInteger.class));
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GETBULK, oid, BigInteger.class));
    }

    @Test
    public final void testForTableProperty() throws Exception {
        final Matrix<Variable> table = MatrixImpl.create(ImmutableList.of(
                new HashMap<Integer, Variable>() {{
                    put(2, new Integer32(0));//false
                    put(3, new Integer32(4230));
                    put(4, new OctetString("Row #1"));
                }},
                new HashMap<Integer, Variable>() {{
                    put(2, new Integer32(1));//true
                    put(3, new Integer32(4231));
                    put(4, new OctetString("Row #2"));
                }},
                new HashMap<Integer, Variable>() {{
                    put(2, new Integer32(1));//true
                    put(3, new Integer32(4232));
                    put(4, new OctetString("Row #3"));
                }},
                new HashMap<Integer, Variable>() {{
                    put(2, new Integer32(1));//true
                    put(3, new Integer32(4233));
                    put(4, new OctetString("Row #4"));
                }}
        ));
        client.writeTable("1.1.7.1", table);
        final Matrix<?> result = client.readTable(ReadMethod.GETBULK, new OID("1.1.7.1"), new HashMap<Integer, Class<?>>() {{
            put(2, Boolean.class);//bool
            put(3, Integer.class);//int
            put(4, String.class);//str
        }});
        assertEquals(4, result.size());
        final List<? extends Map<Integer, ?>> rows = MatrixImpl.toList(result);

        assertEquals(false, rows.get(0).get(2));
        assertEquals(4230, rows.get(0).get(3));
        assertEquals("Row #1", rows.get(0).get(4));

        assertEquals(true, rows.get(3).get(2));
        assertEquals(4233, rows.get(3).get(3));
        assertEquals("Row #4", rows.get(3).get(4));
    }

    @Test
    public final void testForArrayProperty() throws Exception {
        final Matrix<Variable> array = MatrixImpl.create(ImmutableList.of(
                new HashMap<Integer, Variable>(2) {{
                    put(2, new Integer32(20));
                }},
                new HashMap<Integer, Variable>(2) {{
                    put(2, new Integer32(30));
                }}
        ));
        client.writeTable("1.1.5.1", array);
        final Matrix<?> result = client.readTable(ReadMethod.GETBULK, new OID("1.1.5.1"), new HashMap<Integer, Class<?>>() {{
            put(2, Integer.class);
        }});
        final List<? extends Map<Integer, ?>> rows = MatrixImpl.toList(result);
        assertEquals(2, result.size());
        assertEquals(20, rows.get(0).get(2));
        assertEquals(30, rows.get(1).get(2));
    }

    @Test
    public final void testForDictionaryProperty() throws Exception {
        final Matrix<Variable> dict = MatrixImpl.create(ImmutableList.of(
                new HashMap<Integer, Variable>() {{
                    put(2, new Integer32(0));//false
                    put(3, new Integer32(4230));
                    put(4, new OctetString("Test for dictionary property"));
                }}
        ));
        client.writeTable("1.1.6.1", dict);
        final Matrix<?> result = client.readTable(ReadMethod.GETBULK, new OID("1.1.6.1"), new HashMap<Integer, Class<?>>() {{
            put(2, Boolean.class);
            put(3, Integer.class);
            put(4, String.class);
        }});
        assertEquals(1, result.size());
        final List<? extends Map<Integer, ?>> rows = MatrixImpl.toList(result);
        assertEquals(false, rows.get(0).get(2));
        assertEquals(4230, rows.get(0).get(3));
        assertEquals("Test for dictionary property", rows.get(0).get(4));

    }

    @Test
    public final void notificationTest() throws IOException, TimeoutException, InterruptedException {
        final SynchronizationEvent.EventAwaitor<SnmpNotification> awaitor1 = client.addNotificationListener(new OID("1.1.19.1"));
        final SynchronizationEvent.EventAwaitor<SnmpNotification> awaitor2 = client.addNotificationListener(new OID("1.1.20.1"));
        final SynchronizationEvent.EventAwaitor<SnmpNotification> awaitor3 = client.addNotificationListener(new OID("1.1.21.1"));
        client.writeAttribute(new OID("1.1.1.0"), "NOTIFICATION TEST", String.class);
        final SnmpNotification p1 = awaitor1.await(new TimeSpan(4, TimeUnit.MINUTES));
        final SnmpNotification p2 = awaitor2.await(new TimeSpan(4, TimeUnit.MINUTES));
        final SnmpNotification p3 = awaitor3.await(new TimeSpan(4, TimeUnit.MINUTES));
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
    public void licenseDescriptionTest() {
        final Map<String, String> lims = ResourceAdapterClient.getLicenseLimitations(getTestBundleContext(), ADAPTER_NAME, null);
        assertFalse(lims.isEmpty());
        assertEquals(2, lims.size());
    }

    @Test
    public void configurationDescriptorTest() throws BundleException {
        final ConfigurationEntityDescription desc = ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, ResourceAdapterConfiguration.class);
        assertNotNull(desc);
        final ConfigurationEntityDescription.ParameterDescription param = desc.getParameterDescriptor("ldap-uri");
        assertNotNull(param);
        assertFalse(param.getDescription(null).isEmpty());
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
        event.setCategory("com.itworks.snamp.connectors.tests.impl.testnotif");
        event.getParameters().put("severity", "panic");
        event.getParameters().put("objectName", BEAN_NAME);
        event.getParameters().put("receiverAddress", SNMP_HOST + "/" + client.getClientPort());
        event.getParameters().put("receiverName", "test-receiver-2");
        event.getParameters().put("oid", "1.1.20.1");
        events.put("20.1", event);

        event = eventFactory.get();
        event.setCategory("com.itworks.snamp.connectors.tests.impl.plainnotif");
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
