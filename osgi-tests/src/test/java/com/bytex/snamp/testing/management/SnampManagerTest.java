package com.bytex.snamp.testing.management;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connector.jmx.TestOpenMBean;
import com.google.common.collect.ImmutableMap;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.testing.connector.jmx.TestOpenMBean.BEAN_NAME;


/**
 * The type Snamp manager test.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@SnampDependencies({SnampFeature.SNMP_GATEWAY, SnampFeature.STANDARD_TOOLS})
public final class SnampManagerTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String ADAPTER_INSTANCE_NAME = "test-snmp";
    private static final String SNAMP_MBEAN = "com.bytex.snamp.management:type=SnampCore";
    private static final String ADAPTER_NAME = "snmp";
    private static final String SNMP_PORT = "3222";
    private static final String SNMP_HOST = "127.0.0.1";

    /**
     * Instantiates a new Snamp manager test.
     *
     * @throws MalformedObjectNameException the malformed object name exception
     */
    public SnampManagerTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(TestOpenMBean.BEAN_NAME));
    }

    @Test
    public void metricsTest() throws JMException, IOException {
        try (final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(getConnectionString()), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))) {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName commonsObj = new ObjectName(SNAMP_MBEAN);
            assertTrue(connection.getAttribute(commonsObj, "SummaryMetrics") instanceof CompositeData);
            assertTrue(connection.getAttribute(commonsObj, "Metrics") instanceof TabularData);
        }
    }

    /**
     * Jmx monitoring test.
     *
     * @throws IOException the iO exception
     * @throws JMException the jM exception
     * @throws InterruptedException the interrupted exception
     * @throws TimeoutException the timeout exception
     */
    @Test
    public void jmxMonitoringTest() throws Exception {
        try(final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(getConnectionString()), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))){
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName commonsObj = new ObjectName(SNAMP_MBEAN);
            assertNotNull(connection.getMBeanInfo(commonsObj));
            assertTrue(connection.getMBeanCount() > 0);
            assertTrue(connection.getMBeanInfo(commonsObj).getAttributes().length > 0);
            assertEquals(5000L, connection.getAttribute(commonsObj, "StatisticRenewalTime"));
            connection.setAttribute(commonsObj, new Attribute("StatisticRenewalTime", 3000L));
            assertEquals(3000L, connection.getAttribute(commonsObj, "StatisticRenewalTime"));
            final TabularData table = (TabularData)connection.getAttribute(commonsObj, "InstalledComponents");
            assertFalse(table.isEmpty());
            final CompositeData jmxConnectorInfo = table.get(new String[]{"JMX Connector"});
            assertEquals("JMX Connector", jmxConnectorInfo.get("Name"));
            assertEquals(Bundle.ACTIVE, jmxConnectorInfo.get("State"));
            assertEquals(true, jmxConnectorInfo.get("IsConfigurationDescriptionAvailable"));
        }
    }

    @Test
    public void adapterSnmpRunned() throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        try (final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(getConnectionString()), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))) {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName commonsObj = new ObjectName(SNAMP_MBEAN);

            // checking if the we have SNMP gateway installed
            Object installedAdapters = connection.getAttribute(commonsObj, "InstalledGateways");
            assertNotNull(installedAdapters);
            assertTrue(installedAdapters instanceof String[]);
            assertTrue(new ArrayList<>(Arrays.asList((String[]) installedAdapters)).contains("snmp"));
        }
    }

    @Test
    public void connectorJmxRunned() throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        try (final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(getConnectionString()), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))) {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName commonsObj = new ObjectName(SNAMP_MBEAN);

            // checking if the we have SNMP gateway installed
            Object installedConnectors = connection.getAttribute(commonsObj, "InstalledConnectors");
            assertNotNull(installedConnectors);
            assertTrue(installedConnectors instanceof String[]);
            assertTrue(new ArrayList<>(Arrays.asList((String[]) installedConnectors)).contains("jmx"));
        }
    }

    @Test
    public void gatewayManagementTest() throws IOException, MalformedObjectNameException, MBeanException, InstanceNotFoundException, ReflectionException, AttributeNotFoundException {
        try (final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(getConnectionString()), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))) {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName commonsObj = new ObjectName(SNAMP_MBEAN);

            // checking if the we have SNMP gateway installed
            Object installedGateways = connection.getAttribute(commonsObj, "InstalledGateways");
            assertNotNull(installedGateways);
            assertTrue(installedGateways instanceof String[]);
            assertTrue(new ArrayList<>(Arrays.asList((String[]) installedGateways)).contains("snmp"));

            // check if gateway is alive
            adapterSnmpRunned();

            // stopping the gateway
            connection.invoke(commonsObj,
                    "disableGateway",
                    new Object[]{"snmp"},
                    new String[]{String.class.getName()});

            // starting the gateway
            connection.invoke(commonsObj,
                    "enableGateway",
                    new Object[]{"snmp"},
                    new String[]{String.class.getName()});

            // check if gateway is ok after start
            adapterSnmpRunned();
        }
    }

    /**
     * Connectors management test.
     *
     * @throws IOException the iO exception
     * @throws MalformedObjectNameException the malformed object name exception
     * @throws MBeanException the m bean exception
     * @throws InstanceNotFoundException the instance not found exception
     * @throws ReflectionException the reflection exception
     * @throws AttributeNotFoundException the attribute not found exception
     */
    @Test
    public void connectorsManagementTest() throws IOException, MalformedObjectNameException, MBeanException, InstanceNotFoundException, ReflectionException, AttributeNotFoundException {
        try (final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(getConnectionString()), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))) {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName commonsObj = new ObjectName(SNAMP_MBEAN);

            // checking if the we have JMX connector installed
            Object installedConnectors = connection.getAttribute(commonsObj, "InstalledConnectors");
            assertNotNull(installedConnectors);
            assertTrue(installedConnectors instanceof String[]);
            assertTrue(ArrayUtils.contains((String[])installedConnectors, "jmx"));

            // check if connector is alive
            connectorJmxRunned();

            // stopping the gateway
            connection.invoke(commonsObj,
                    "disableConnector",
                    new Object[]{"jmx"},
                    new String[]{String.class.getName()});

            // starting the connector
            connection.invoke(commonsObj,
                    "enableConnector",
                    new Object[]{"jmx"},
                    new String[]{String.class.getName()});

            // check if connector is ok after start
            connectorJmxRunned();
        }
    }

    /**
     * Restart test.
     *
     * @throws IOException the iO exception
     */
    @Ignore
    @Test
    public void restartTest() throws IOException, JMException, InterruptedException, TimeoutException {
        try (final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(getConnectionString()), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))) {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName commonsObj = new ObjectName(SNAMP_MBEAN);
            Object voidReturn = connection.invoke(commonsObj, "restart", null, null);
            assertTrue(voidReturn instanceof Void);
        }
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        super.beforeStartTest(context);
        beforeCleanupTest(context);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        startResourceConnector(context);
        syncWithGatewayStartedEvent(ADAPTER_NAME, () -> {
                GatewayActivator.enableGateway(context, ADAPTER_NAME);
                return null;
        }, Duration.ofSeconds(4));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        GatewayActivator.disableGateway(context, ADAPTER_NAME);
        stopResourceConnector(context);
    }

    @Override
    protected void fillGateways(final EntityMap<? extends GatewayConfiguration> gateways) {
        final GatewayConfiguration snmpAdapter = gateways.getOrAdd(ADAPTER_INSTANCE_NAME);
        snmpAdapter.setType(ADAPTER_NAME);
        snmpAdapter.put("port", SNMP_PORT);
        snmpAdapter.put("host", SNMP_HOST);
        snmpAdapter.put("socketTimeout", "5000");
        snmpAdapter.put("context", "1.1");
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("1.0");
        attribute.setAlternativeName("string");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("oid", "1.1.1.0");

        attribute = attributes.getOrAdd("2.0");
        attribute.setAlternativeName("boolean");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("oid", "1.1.2.0");

        attribute = attributes.getOrAdd("3.0");
        attribute.setAlternativeName("int32");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("oid", "1.1.3.0");

        attribute = attributes.getOrAdd("4.0");
        attribute.setAlternativeName("bigint");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("oid", "1.1.4.0");

        attribute = attributes.getOrAdd("5.1");
        attribute.setAlternativeName("array");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("oid", "1.1.5.1");

        attribute = attributes.getOrAdd("6.1");
        attribute.setAlternativeName("dictionary");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("oid", "1.1.6.1");

        attribute = attributes.getOrAdd("7.1");
        attribute.setAlternativeName("table");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("oid", "1.1.7.1");

        attribute = attributes.getOrAdd("8.0");
        attribute.setAlternativeName("float");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("oid", "1.1.8.0");

        attribute = attributes.getOrAdd("9.0");
        attribute.setAlternativeName("date");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("displayFormat", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        attribute.put("oid", "1.1.9.0");

        attribute = attributes.getOrAdd("10.0");
        attribute.setAlternativeName("date");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("displayFormat", "rfc1903-human-readable");
        attribute.put("oid", "1.1.10.0");

        attribute = attributes.getOrAdd("11.0");
        attribute.setAlternativeName("date");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("displayFormat", "rfc1903");
        attribute.put("oid", "1.1.11.0");
    }
}