package com.bytex.snamp.testing.management;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.ExceptionalCallable;
import com.bytex.snamp.SafeConsumer;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.concurrent.SynchronizationEvent;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.jmx.TabularDataUtils;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connectors.jmx.TestOpenMBean;
import org.junit.ComparisonFailure;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;

/**
 * The type Snamp manager test.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.SNMP_ADAPTER)
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
    public void availableAttributesTest() throws IOException, JMException {
        try(final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(JMX_RMI_CONNECTION_STRING), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))) {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName commonsObj = new ObjectName(SNAMP_MBEAN);
            final TabularData attrs =
                    (TabularData)connection.invoke(commonsObj, "getAvailableAttributes", new Object[]{TEST_RESOURCE_NAME}, new String[]{String.class.getName()});
            assertFalse(attrs.isEmpty());
            TabularDataUtils.forEachRow(attrs, new SafeConsumer<CompositeData>() {
                @Override
                public void accept(final CompositeData row) {
                    assertTrue(row.get("userDefinedName") instanceof String);
                    assertTrue(row.get("description") instanceof String);
                    assertTrue(row.get("parameters") instanceof TabularData);
                    assertTrue(row.get("type") instanceof String);
                    assertTrue(row.get("readable") instanceof Boolean);
                    assertTrue(row.get("writable") instanceof Boolean);
                    assertTrue(row.get("name") instanceof String);
                }
            });
        }
    }

    @Test
    public void bindingOfAttributesTest() throws IOException, JMException {
        try (final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(JMX_RMI_CONNECTION_STRING), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))) {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName commonsObj = new ObjectName(SNAMP_MBEAN);
            final TabularData attrs =
                    (TabularData)connection.invoke(commonsObj, "getBindingOfAttributes", new Object[]{ADAPTER_INSTANCE_NAME}, new String[]{String.class.getName()});
            assertFalse(attrs.isEmpty());
            TabularDataUtils.forEachRow(attrs, new SafeConsumer<CompositeData>() {
                @Override
                public void accept(final CompositeData row) {
                    assertTrue(row.get("resourceName") instanceof String);
                    assertTrue(row.get("userDefinedName") instanceof String);
                    assertTrue(row.get("name") instanceof String);
                    assertTrue(row.get("mappedType") instanceof String);
                    assertTrue(row.get("details") instanceof TabularData);
                }
            });
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
    public void jmxMonitoringTest() throws IOException, JMException, InterruptedException, TimeoutException {
        try(final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(JMX_RMI_CONNECTION_STRING), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))){
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName commonsObj = new ObjectName(SNAMP_MBEAN);
            assertNotNull(connection.getMBeanInfo(commonsObj));
            assertTrue(connection.getMBeanCount() > 0);
            assertTrue(connection.getMBeanInfo(commonsObj).getAttributes().length > 0);
            assertEquals(5000L, connection.getAttribute(commonsObj, "StatisticRenewalTime"));
            connection.setAttribute(commonsObj, new Attribute("StatisticRenewalTime", 3000L));
            assertEquals(3000L, connection.getAttribute(commonsObj, "StatisticRenewalTime"));
            //emits some errors
            final ServiceReference<LogService> loggerRef = getTestBundleContext().getServiceReference(LogService.class);
            assertNotNull(loggerRef);
            final LogService logger = getTestBundleContext().getService(loggerRef);
            logger.log(LogService.LOG_ERROR, "Some error #1");
            logger.log(LogService.LOG_ERROR, "Some error #2");
            logger.log(LogService.LOG_WARNING, "Some warning #1");
            //expected 2 errors and 1 warning
            Thread.sleep(500);  //wait for updating counters
            assertEquals(2L, connection.getAttribute(commonsObj, "FaultsCount"));
            assertEquals(1L, connection.getAttribute(commonsObj, "WarningMessagesCount"));
            //wait for refresh counters
            Thread.sleep(3000);
            assertEquals(0L, connection.getAttribute(commonsObj, "FaultsCount"));
            assertEquals(0L, connection.getAttribute(commonsObj, "WarningMessagesCount"));
            //test notifications
            assertTrue(connection.getMBeanInfo(commonsObj).getNotifications().length > 0);
            final SynchronizationEvent<Notification> syncEvent = new SynchronizationEvent<>(true);
            connection.addNotificationListener(commonsObj, new NotificationListener() {
                @Override
                public void handleNotification(final Notification notification, final Object handback) {
                    syncEvent.fire(notification);
                }
            }, null, null);
            final String eventPayload = "Hello, world!";
            logger.log(LogService.LOG_ERROR, eventPayload);
            Notification notif = syncEvent.getAwaitor().await(TimeSpan.fromSeconds(3));
            assertEquals(eventPayload, notif.getMessage());
            assertEquals("com.bytex.snamp.monitoring.error", notif.getType());
            logger.log(LogService.LOG_WARNING, eventPayload, new Exception("WAAGH!"));
            notif = syncEvent.getAwaitor().await(TimeSpan.fromSeconds(3));
            //assertEquals(String.format("%s. Reason: %s", eventPayload, new Exception("WAAGH!")), notif.getMessage());
            assertEquals("com.bytex.snamp.monitoring.warning", notif.getType());
            final TabularData table = (TabularData)connection.getAttribute(commonsObj, "InstalledComponents");
            assertFalse(table.isEmpty());
            final CompositeData jmxConnectorInfo = table.get(new String[]{"JMX Connector"});
            assertEquals("JMX Connector", jmxConnectorInfo.get("Name"));
            assertEquals(Bundle.ACTIVE, jmxConnectorInfo.get("State"));
            assertEquals(true, jmxConnectorInfo.get("IsManageable"));
            assertEquals(true, jmxConnectorInfo.get("IsCommerciallyLicensed"));
            assertEquals(true, jmxConnectorInfo.get("IsConfigurationDescriptionAvailable"));
            getTestBundleContext().ungetService(loggerRef);
        }
    }

    /**
     * Configuration get test.
     *
     * @throws IOException the iO exception
     * @throws JMException the jM exception
     * @throws InterruptedException the interrupted exception
     * @throws TimeoutException the timeout exception
     */
    @Test
    public void configurationGetTest() throws IOException, JMException, InterruptedException, TimeoutException {
        try (final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(JMX_RMI_CONNECTION_STRING), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))) {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName commonsObj = new ObjectName(SNAMP_MBEAN);
            Object configurationContent = connection.getAttribute(commonsObj, "configuration");
            assertNotNull(configurationContent);
            assertTrue(configurationContent instanceof CompositeData);
            final CompositeData configurationData = (CompositeData) configurationContent;

            // snmp adapter check
            assertTrue(configurationData.containsKey("ResourceAdapters"));
            assertTrue(configurationData.get("ResourceAdapters") instanceof TabularData);

            final TabularData resourceAdapters = (TabularData) configurationData.get("ResourceAdapters");
            assertTrue(resourceAdapters.values().size() == 1);

            assertTrue(resourceAdapters.containsKey(new Object[]{"test-snmp"}));
            final CompositeData currentAdapter = resourceAdapters.get(new Object[]{"test-snmp"});
            assertTrue(currentAdapter.get("adapter") instanceof  CompositeData);
            final CompositeData adapterComposite = (CompositeData) currentAdapter.get("adapter");
            assertEquals(adapterComposite.get("Name"), ADAPTER_NAME);
            assertTrue(adapterComposite.containsKey("Parameters"));
            assertTrue(adapterComposite.get("Parameters") instanceof TabularData);

            final TabularData adapterAdditionalParameters = (TabularData) adapterComposite.get("Parameters");

            assertTrue(adapterAdditionalParameters.containsKey(new Object[]{"port"}));
            assertTrue(adapterAdditionalParameters.containsKey(new Object[]{"host"}));
            assertTrue(adapterAdditionalParameters.containsKey(new Object[]{"socketTimeout"}));

            assertEquals(adapterAdditionalParameters.get(new Object[]{"port"}).get("value"), SNMP_PORT);
            assertEquals(adapterAdditionalParameters.get(new Object[]{"host"}).get("value"), SNMP_HOST);
            assertEquals(adapterAdditionalParameters.get(new Object[]{"socketTimeout"}).get("value"), "5000");

            // jmx connector check
            assertTrue(configurationData.containsKey("ManagedResources"));

            final TabularData managedResources = (TabularData) configurationData.get("ManagedResources");
            assertTrue(managedResources.values().size() == 1);

            assertTrue(managedResources.containsKey(new Object[]{"test-target"}));

            final CompositeData jmxConnector = managedResources.get(new Object[]{"test-target"});

            assertTrue(jmxConnector.get("connector") instanceof  CompositeData);
            final CompositeData connectorComposite = (CompositeData) jmxConnector.get("connector");
            assertEquals(connectorComposite.get("ConnectionString"), JMX_RMI_CONNECTION_STRING);
            assertEquals(connectorComposite.get("ConnectionType"), CONNECTOR_NAME);

            assertTrue(connectorComposite.containsKey("Parameters"));
            assertTrue(connectorComposite.get("Parameters") instanceof TabularData);

            final TabularData connectorAdditionalParameters = (TabularData) connectorComposite.get("Parameters");


            assertTrue(connectorAdditionalParameters.containsKey(new Object[]{"login"}));
            assertTrue(connectorAdditionalParameters.containsKey(new Object[]{"password"}));

            assertEquals(connectorAdditionalParameters.get(new Object[]{"login"}).get("value"), JMX_LOGIN);
            assertEquals(connectorAdditionalParameters.get(new Object[]{"password"}).get("value"), JMX_PASSWORD);

            assertTrue(connectorComposite.containsKey("Attributes"));
            assertTrue(connectorComposite.get("Attributes") instanceof TabularData);

            final TabularData connectorAttributes = (TabularData) connectorComposite.get("Attributes");

            assertTrue(connectorAttributes.containsKey(new Object[]{"1.0"}));
            assertTrue(connectorAttributes.containsKey(new Object[]{"2.0"}));
            assertTrue(connectorAttributes.containsKey(new Object[]{"3.0"}));
            assertTrue(connectorAttributes.containsKey(new Object[]{"4.0"}));
            assertTrue(connectorAttributes.containsKey(new Object[]{"5.1"}));
            assertTrue(connectorAttributes.containsKey(new Object[]{"6.1"}));
            assertTrue(connectorAttributes.containsKey(new Object[]{"7.1"}));
            assertTrue(connectorAttributes.containsKey(new Object[]{"8.0"}));
            assertTrue(connectorAttributes.containsKey(new Object[]{"9.0"}));
            assertTrue(connectorAttributes.containsKey(new Object[]{"10.0"}));
            assertTrue(connectorAttributes.containsKey(new Object[]{"11.0"}));

            assertTrue(connectorAttributes.get(new Object[]{"1.0"}).containsKey("attribute"));
            assertTrue(connectorAttributes.get(new Object[]{"2.0"}).containsKey("attribute"));
            assertTrue(connectorAttributes.get(new Object[]{"3.0"}).containsKey("attribute"));
            assertTrue(connectorAttributes.get(new Object[]{"4.0"}).containsKey("attribute"));
            assertTrue(connectorAttributes.get(new Object[]{"5.1"}).containsKey("attribute"));
            assertTrue(connectorAttributes.get(new Object[]{"6.1"}).containsKey("attribute"));
            assertTrue(connectorAttributes.get(new Object[]{"7.1"}).containsKey("attribute"));
            assertTrue(connectorAttributes.get(new Object[]{"8.0"}).containsKey("attribute"));
            assertTrue(connectorAttributes.get(new Object[]{"9.0"}).containsKey("attribute"));
            assertTrue(connectorAttributes.get(new Object[]{"10.0"}).containsKey("attribute"));
            assertTrue(connectorAttributes.get(new Object[]{"11.0"}).containsKey("attribute"));

            assertTrue(connectorAttributes.get(new Object[]{"1.0"}).get("attribute") instanceof CompositeData);
            assertTrue(connectorAttributes.get(new Object[]{"2.0"}).get("attribute") instanceof CompositeData);
            assertTrue(connectorAttributes.get(new Object[]{"3.0"}).get("attribute") instanceof CompositeData);
            assertTrue(connectorAttributes.get(new Object[]{"4.0"}).get("attribute") instanceof CompositeData);
            assertTrue(connectorAttributes.get(new Object[]{"5.1"}).get("attribute") instanceof CompositeData);
            assertTrue(connectorAttributes.get(new Object[]{"6.1"}).get("attribute") instanceof CompositeData);
            assertTrue(connectorAttributes.get(new Object[]{"7.1"}).get("attribute") instanceof CompositeData);
            assertTrue(connectorAttributes.get(new Object[]{"8.0"}).get("attribute") instanceof CompositeData);
            assertTrue(connectorAttributes.get(new Object[]{"9.0"}).get("attribute") instanceof CompositeData);
            assertTrue(connectorAttributes.get(new Object[]{"10.0"}).get("attribute") instanceof CompositeData);
            assertTrue(connectorAttributes.get(new Object[]{"11.0"}).get("attribute") instanceof CompositeData);

            final CompositeData attribute10 = (CompositeData) connectorAttributes.get(new Object[]{"1.0"}).get("attribute");
            assertEquals(attribute10.get("Name"), "string");
            assertEquals(attribute10.get("ReadWriteTimeout"), Long.MAX_VALUE);
            assertTrue(attribute10.containsKey("AdditionalProperties"));
            assertTrue(attribute10.get("AdditionalProperties") instanceof TabularData);
            assertTrue(((TabularData) attribute10.get("AdditionalProperties")).containsKey(new Object[]{"objectName"}));
            assertEquals(((TabularData) attribute10.get("AdditionalProperties")).get(new Object[]{"objectName"}).get("value"), BEAN_NAME);
            assertEquals(((TabularData) attribute10.get("AdditionalProperties")).get(new Object[]{"oid"}).get("value"), "1.1.1.0");

            final CompositeData attribute20 = (CompositeData) connectorAttributes.get(new Object[]{"2.0"}).get("attribute");
            assertEquals(attribute20.get("Name"), "boolean");
            assertEquals(attribute20.get("ReadWriteTimeout"), Long.MAX_VALUE);
            assertTrue(attribute20.containsKey("AdditionalProperties"));
            assertTrue(attribute20.get("AdditionalProperties") instanceof TabularData);
            assertTrue(((TabularData) attribute20.get("AdditionalProperties")).containsKey(new Object[]{"objectName"}));
            assertEquals(((TabularData) attribute20.get("AdditionalProperties")).get(new Object[]{"objectName"}).get("value"), BEAN_NAME);
            assertEquals(((TabularData) attribute20.get("AdditionalProperties")).get(new Object[]{"oid"}).get("value"), "1.1.2.0");

            final CompositeData attribute30 = (CompositeData) connectorAttributes.get(new Object[]{"3.0"}).get("attribute");
            assertEquals(attribute30.get("Name"), "int32");
            assertEquals(attribute30.get("ReadWriteTimeout"), Long.MAX_VALUE);
            assertTrue(attribute30.containsKey("AdditionalProperties"));
            assertTrue(attribute30.get("AdditionalProperties") instanceof TabularData);
            assertTrue(((TabularData) attribute30.get("AdditionalProperties")).containsKey(new Object[]{"objectName"}));
            assertEquals(((TabularData) attribute30.get("AdditionalProperties")).get(new Object[]{"objectName"}).get("value"), BEAN_NAME);
            assertEquals(((TabularData) attribute30.get("AdditionalProperties")).get(new Object[]{"oid"}).get("value"), "1.1.3.0");

            final CompositeData attribute40 = (CompositeData) connectorAttributes.get(new Object[]{"4.0"}).get("attribute");
            assertEquals(attribute40.get("Name"), "bigint");
            assertEquals(attribute40.get("ReadWriteTimeout"), Long.MAX_VALUE);
            assertTrue(attribute40.containsKey("AdditionalProperties"));
            assertTrue(attribute40.get("AdditionalProperties") instanceof TabularData);
            assertTrue(((TabularData) attribute40.get("AdditionalProperties")).containsKey(new Object[]{"objectName"}));
            assertEquals(((TabularData) attribute40.get("AdditionalProperties")).get(new Object[]{"objectName"}).get("value"), BEAN_NAME);
            assertEquals(((TabularData) attribute40.get("AdditionalProperties")).get(new Object[]{"oid"}).get("value"), "1.1.4.0");

            final CompositeData attribute51 = (CompositeData) connectorAttributes.get(new Object[]{"5.1"}).get("attribute");
            assertEquals(attribute51.get("Name"), "array");
            assertEquals(attribute51.get("ReadWriteTimeout"), Long.MAX_VALUE);
            assertTrue(attribute51.containsKey("AdditionalProperties"));
            assertTrue(attribute51.get("AdditionalProperties") instanceof TabularData);
            assertTrue(((TabularData) attribute51.get("AdditionalProperties")).containsKey(new Object[]{"objectName"}));
            assertEquals(((TabularData) attribute51.get("AdditionalProperties")).get(new Object[]{"objectName"}).get("value"), BEAN_NAME);
            assertEquals(((TabularData) attribute51.get("AdditionalProperties")).get(new Object[]{"oid"}).get("value"), "1.1.5.1");

            final CompositeData attribute61 = (CompositeData) connectorAttributes.get(new Object[]{"6.1"}).get("attribute");
            assertEquals(attribute61.get("Name"), "dictionary");
            assertEquals(attribute61.get("ReadWriteTimeout"), Long.MAX_VALUE);
            assertTrue(attribute61.containsKey("AdditionalProperties"));
            assertTrue(attribute61.get("AdditionalProperties") instanceof TabularData);
            assertTrue(((TabularData) attribute61.get("AdditionalProperties")).containsKey(new Object[]{"objectName"}));
            assertEquals(((TabularData) attribute61.get("AdditionalProperties")).get(new Object[]{"objectName"}).get("value"), BEAN_NAME);
            assertEquals(((TabularData) attribute61.get("AdditionalProperties")).get(new Object[]{"oid"}).get("value"), "1.1.6.1");

            final CompositeData attribute71 = (CompositeData) connectorAttributes.get(new Object[]{"7.1"}).get("attribute");
            assertEquals(attribute71.get("Name"), "table");
            assertEquals(attribute71.get("ReadWriteTimeout"), Long.MAX_VALUE);
            assertTrue(attribute71.containsKey("AdditionalProperties"));
            assertTrue(attribute71.get("AdditionalProperties") instanceof TabularData);
            assertTrue(((TabularData) attribute71.get("AdditionalProperties")).containsKey(new Object[]{"objectName"}));
            assertEquals(((TabularData) attribute71.get("AdditionalProperties")).get(new Object[]{"objectName"}).get("value"), BEAN_NAME);
            assertEquals(((TabularData) attribute71.get("AdditionalProperties")).get(new Object[]{"oid"}).get("value"), "1.1.7.1");

            final CompositeData attribute80 = (CompositeData) connectorAttributes.get(new Object[]{"8.0"}).get("attribute");
            assertEquals(attribute80.get("Name"), "float");
            assertEquals(attribute80.get("ReadWriteTimeout"), Long.MAX_VALUE);
            assertTrue(attribute80.containsKey("AdditionalProperties"));
            assertTrue(attribute80.get("AdditionalProperties") instanceof TabularData);
            assertTrue(((TabularData) attribute80.get("AdditionalProperties")).containsKey(new Object[]{"objectName"}));
            assertEquals(((TabularData) attribute80.get("AdditionalProperties")).get(new Object[]{"objectName"}).get("value"), BEAN_NAME);
            assertEquals(((TabularData) attribute80.get("AdditionalProperties")).get(new Object[]{"oid"}).get("value"), "1.1.8.0");

            final CompositeData attribute90 = (CompositeData) connectorAttributes.get(new Object[]{"9.0"}).get("attribute");
            assertEquals(attribute90.get("Name"), "date");
            assertEquals(attribute90.get("ReadWriteTimeout"), Long.MAX_VALUE);
            assertTrue(attribute90.containsKey("AdditionalProperties"));
            assertTrue(attribute90.get("AdditionalProperties") instanceof TabularData);
            assertTrue(((TabularData) attribute90.get("AdditionalProperties")).containsKey(new Object[]{"objectName"}));
            assertEquals(((TabularData) attribute90.get("AdditionalProperties")).get(new Object[]{"objectName"}).get("value"), BEAN_NAME);
            assertEquals(((TabularData) attribute90.get("AdditionalProperties")).get(new Object[]{"displayFormat"}).get("value"), "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            assertEquals(((TabularData) attribute90.get("AdditionalProperties")).get(new Object[]{"oid"}).get("value"), "1.1.9.0");

            final CompositeData attribute100 = (CompositeData) connectorAttributes.get(new Object[]{"10.0"}).get("attribute");
            assertEquals(attribute100.get("Name"), "date");
            assertEquals(attribute100.get("ReadWriteTimeout"), Long.MAX_VALUE);
            assertTrue(attribute100.containsKey("AdditionalProperties"));
            assertTrue(attribute100.get("AdditionalProperties") instanceof TabularData);
            assertTrue(((TabularData) attribute100.get("AdditionalProperties")).containsKey(new Object[]{"objectName"}));
            assertEquals(((TabularData) attribute100.get("AdditionalProperties")).get(new Object[]{"objectName"}).get("value"), BEAN_NAME);
            assertEquals(((TabularData) attribute100.get("AdditionalProperties")).get(new Object[]{"displayFormat"}).get("value"), "rfc1903-human-readable");
            assertEquals(((TabularData) attribute100.get("AdditionalProperties")).get(new Object[]{"oid"}).get("value"), "1.1.10.0");

            final CompositeData attribute110 = (CompositeData) connectorAttributes.get(new Object[]{"11.0"}).get("attribute");
            assertEquals(attribute110.get("Name"), "date");
            assertEquals(attribute110.get("ReadWriteTimeout"), Long.MAX_VALUE);
            assertTrue(attribute110.containsKey("AdditionalProperties"));
            assertTrue(attribute110.get("AdditionalProperties") instanceof TabularData);
            assertTrue(((TabularData) attribute110.get("AdditionalProperties")).containsKey(new Object[]{"objectName"}));
            assertEquals(((TabularData) attribute110.get("AdditionalProperties")).get(new Object[]{"objectName"}).get("value"), BEAN_NAME);
            assertEquals(((TabularData) attribute110.get("AdditionalProperties")).get(new Object[]{"displayFormat"}).get("value"), "rfc1903");
            assertEquals(((TabularData) attribute110.get("AdditionalProperties")).get(new Object[]{"oid"}).get("value"), "1.1.11.0");
        }
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }


    /**
     * Configuration set test.
     *
     * @throws Exception the exception
     */
    @Test
    public void configurationSetTest() throws Exception {
        try (final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(JMX_RMI_CONNECTION_STRING), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))) {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName commonsObj = new ObjectName(SNAMP_MBEAN);

            Object configurationContent = connection.getAttribute(commonsObj, "configuration");
            assertNotNull(configurationContent);
            assertTrue(configurationContent instanceof CompositeData);

            // test to reset default test case settings
            connection.setAttribute(commonsObj, new Attribute("configuration", configurationContent));
            configurationGetTest();

            // change adapter's parameter value
            final CompositeData snmpAdapter = ((TabularData) ((CompositeData) configurationContent).get("ResourceAdapters")).get(new Object[]{"test-snmp"});
            final TabularData paramData = ((TabularData)((CompositeData) snmpAdapter.get("adapter")).get("Parameters"));
            paramData.remove(new Object[]{"socketTimeout"});
            paramData.put(new CompositeDataSupport(paramData.getTabularType().getRowType(), ImmutableMap.of("key", "socketTimeout", "value", "4000")));
            connection.setAttribute(commonsObj, new Attribute("configuration", configurationContent));

            // we must get junit comparison failure
            try {
                configurationGetTest();
            } catch (final ComparisonFailure ex) {
                assertEquals(ex.getActual(), "5000");
                assertEquals(ex.getExpected(), "4000");
            }

            // turn configuration back
            paramData.remove(new Object[]{"socketTimeout"});
            paramData.put(new CompositeDataSupport(paramData.getTabularType().getRowType(), ImmutableMap.of("key", "socketTimeout", "value", "5000")));
            connection.setAttribute(commonsObj, new Attribute("configuration", configurationContent));

            // check if current configuration is ok
            configurationGetTest();

            // change connector's param
            final CompositeData jmxConnector = ((TabularData) ((CompositeData) configurationContent).get("ManagedResources")).get(new Object[]{"test-target"});
            final TabularData attributesData = ((TabularData)((CompositeData) jmxConnector.get("connector")).get("Attributes"));
            final TabularData connectorAttributeParams = ((TabularData)((CompositeData)(attributesData.get(new Object[]{"2.0"}).get("attribute"))).get("AdditionalProperties"));
            connectorAttributeParams.remove(new Object[]{"oid"});
            connectorAttributeParams.put(new CompositeDataSupport(connectorAttributeParams.getTabularType().getRowType(),
                    ImmutableMap.of("key", "oid", "value", "1.1.2.1")));
            connection.setAttribute(commonsObj, new Attribute("configuration", configurationContent));

            // we must get junit comparison failure
            try {
                configurationGetTest();
            } catch (final ComparisonFailure ex) {
                assertEquals(ex.getActual(), "1.1.2.0");
                assertEquals(ex.getExpected(), "1.1.2.1");
            }

            // turn the configuration back
            connectorAttributeParams.remove(new Object[]{"oid"});
            connectorAttributeParams.put(new CompositeDataSupport(connectorAttributeParams.getTabularType().getRowType(),
                    ImmutableMap.of("key", "oid", "value", "1.1.2.0")));
            connection.setAttribute(commonsObj, new Attribute("configuration", configurationContent));

            // check if current configuration is ok
            configurationGetTest();
        }
    }

    /**
     * Adapter snmp runned.
     *
     * @throws IOException the iO exception
     */
    @Test
    public void adapterSnmpRunned() throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        try (final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(JMX_RMI_CONNECTION_STRING), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))) {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName commonsObj = new ObjectName(SNAMP_MBEAN);

            // checking if the we have SNMP adapter installed
            Object installedAdapters = connection.getAttribute(commonsObj, "InstalledAdapters");
            assertNotNull(installedAdapters);
            assertTrue(installedAdapters instanceof String[]);
            assertTrue(new ArrayList<>(Arrays.asList((String[]) installedAdapters)).contains("snmp"));

            // getting the adapter info
            Object snmpAdapterInfo = connection.invoke(commonsObj,
                    "getAdapterInfo",
                    new Object[]{"snmp", ""},
                    new String[]{String.class.getName(), String.class.getName()});
            assertNotNull(snmpAdapterInfo);
            assertTrue(snmpAdapterInfo instanceof CompositeData);
            assertTrue(((CompositeData) snmpAdapterInfo).containsKey("State"));
            assertNotNull(((CompositeData) snmpAdapterInfo).get("State"));
            assertTrue(((CompositeData) snmpAdapterInfo).get("State") instanceof Integer);
            assertEquals(Bundle.ACTIVE, ((CompositeData) snmpAdapterInfo).get("State"));
        }
    }

    /**
     * Connector jmx runned.
     *
     * @throws IOException the iO exception
     */
    @Test
    public void connectorJmxRunned() throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        try (final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(JMX_RMI_CONNECTION_STRING), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))) {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName commonsObj = new ObjectName(SNAMP_MBEAN);

            // checking if the we have SNMP adapter installed
            Object installedConnectors = connection.getAttribute(commonsObj, "InstalledConnectors");
            assertNotNull(installedConnectors);
            assertTrue(installedConnectors instanceof String[]);
            assertTrue(new ArrayList<>(Arrays.asList((String[]) installedConnectors)).contains("jmx"));

            // getting the adapter info
            Object snmpConnectorInfo = connection.invoke(commonsObj,
                    "getConnectorInfo",
                    new Object[]{"jmx", ""},
                    new String[]{String.class.getName(), String.class.getName()});
            assertNotNull(snmpConnectorInfo);
            assertTrue(snmpConnectorInfo instanceof CompositeData);
            assertTrue(((CompositeData) snmpConnectorInfo).containsKey("State"));
            assertNotNull(((CompositeData) snmpConnectorInfo).get("State"));
            assertTrue(((CompositeData) snmpConnectorInfo).get("State") instanceof Integer);
            assertEquals(Bundle.ACTIVE, ((CompositeData) snmpConnectorInfo).get("State"));
        }
    }

    /**
     * Adapter management start stop test.
     *
     * @throws IOException the iO exception
     */
    @Test
    public void adapterManagementTest() throws IOException, MalformedObjectNameException, MBeanException, InstanceNotFoundException, ReflectionException, AttributeNotFoundException {
        try (final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(JMX_RMI_CONNECTION_STRING), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))) {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName commonsObj = new ObjectName(SNAMP_MBEAN);

            // checking if the we have SNMP adapter installed
            Object installedAdapters = connection.getAttribute(commonsObj, "InstalledAdapters");
            assertNotNull(installedAdapters);
            assertTrue(installedAdapters instanceof String[]);
            assertTrue(new ArrayList<>(Arrays.asList((String[]) installedAdapters)).contains("snmp"));

            // check if adapter is alive
            adapterSnmpRunned();

            // stopping the adapter
            connection.invoke(commonsObj,
                    "stopAdapter",
                    new Object[]{"snmp"},
                    new String[]{String.class.getName()});

            // check if the adapter has appropriate status after stopping
            final Object snmpAdapterInfo = connection.invoke(commonsObj,
                    "getAdapterInfo",
                    new Object[]{"snmp", ""},
                    new String[]{String.class.getName(), String.class.getName()});
            assertNotNull(snmpAdapterInfo);
            assertTrue(snmpAdapterInfo instanceof CompositeData);
            assertTrue(((CompositeData) snmpAdapterInfo).containsKey("State"));
            assertNotNull(((CompositeData) snmpAdapterInfo).get("State"));
            assertTrue(((CompositeData) snmpAdapterInfo).get("State") instanceof Integer);
            assertEquals(Bundle.RESOLVED, ((CompositeData) snmpAdapterInfo).get("State"));

            // starting the adapter
            connection.invoke(commonsObj,
                    "startAdapter",
                    new Object[]{"snmp"},
                    new String[]{String.class.getName()});

            // check if adapter is ok after start
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
        try (final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(JMX_RMI_CONNECTION_STRING), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))) {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName commonsObj = new ObjectName(SNAMP_MBEAN);

            // checking if the we have JMX connector installed
            Object installedConnectors = connection.getAttribute(commonsObj, "InstalledConnectors");
            assertNotNull(installedConnectors);
            assertTrue(installedConnectors instanceof String[]);
            assertTrue(ArrayUtils.contains((String[])installedConnectors, "jmx"));

            // check if connector is alive
            connectorJmxRunned();

            // stopping the adapter
            connection.invoke(commonsObj,
                    "stopConnector",
                    new Object[]{"jmx"},
                    new String[]{String.class.getName()});

            // check if the connector has appropriate status after stopping
            final Object snmpConnectorInfo = connection.invoke(commonsObj,
                    "getConnectorInfo",
                    new Object[]{"jmx", ""},
                    new String[]{String.class.getName(), String.class.getName()});
            assertNotNull(snmpConnectorInfo);
            assertTrue(snmpConnectorInfo instanceof CompositeData);
            assertTrue(((CompositeData) snmpConnectorInfo).containsKey("State"));
            assertNotNull(((CompositeData) snmpConnectorInfo).get("State"));
            assertTrue(((CompositeData) snmpConnectorInfo).get("State") instanceof Integer);
            assertEquals(Bundle.RESOLVED, ((CompositeData) snmpConnectorInfo).get("State"));

            // starting the connector
            connection.invoke(commonsObj,
                    "startConnector",
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
        try (final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(JMX_RMI_CONNECTION_STRING), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))) {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName commonsObj = new ObjectName(SNAMP_MBEAN);
            Object voidReturn = connection.invoke(commonsObj, "restart", null, null);
            assertTrue(voidReturn instanceof Void);
        }
    }

    /**
     * Gets connector configuration schema test.
     *
     * @throws IOException the iO exception
     * @throws JMException the jM exception
     * @throws InterruptedException the interrupted exception
     * @throws TimeoutException the timeout exception
     */
    @Test
    public void getConnectorConfigurationSchemaTest() throws IOException, JMException, InterruptedException, TimeoutException {
        try (final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(JMX_RMI_CONNECTION_STRING), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))) {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName commonsObj = new ObjectName(SNAMP_MBEAN);
            final Object result = connection.invoke(commonsObj,
                    "getConnectorConfigurationSchema",
                    new Object[]{"jmx", ""},
                    new String[]{String.class.getName(), String.class.getName()});
            assertTrue(result instanceof CompositeData);
        }
    }

    /**
     * Gets adapter configuration schema test.
     *
     * @throws IOException the iO exception
     * @throws JMException the jM exception
     * @throws InterruptedException the interrupted exception
     * @throws TimeoutException the timeout exception
     */
    @Test
    public void getAdapterConfigurationSchemaTest() throws IOException, JMException, InterruptedException, TimeoutException {
        try (final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(JMX_RMI_CONNECTION_STRING), ImmutableMap.of(JMXConnector.CREDENTIALS, new String[]{JMX_LOGIN, JMX_PASSWORD}))) {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            final ObjectName commonsObj = new ObjectName(SNAMP_MBEAN);
            final Object result = connection.invoke(commonsObj,
                    "getAdapterConfigurationSchema",
                    new Object[]{"snmp", ""},
                    new String[]{String.class.getName(), String.class.getName()});
            assertTrue(result instanceof CompositeData);
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
    protected void fillAdapters(final Map<String, AgentConfiguration.ResourceAdapterConfiguration> adapters, final Supplier<AgentConfiguration.ResourceAdapterConfiguration> adapterFactory) {
        final AgentConfiguration.ResourceAdapterConfiguration snmpAdapter = adapterFactory.get();
        snmpAdapter.setAdapterName(ADAPTER_NAME);
        snmpAdapter.getParameters().put("port", SNMP_PORT);
        snmpAdapter.getParameters().put("host", SNMP_HOST);
        snmpAdapter.getParameters().put("socketTimeout", "5000");
        snmpAdapter.getParameters().put("context", "1.1");
        adapters.put(ADAPTER_INSTANCE_NAME, snmpAdapter);
    }

    @Override
    protected void fillAttributes(final Map<String, AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration> attributes, final Supplier<AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration> attributeFactory) {
        AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration attribute = attributeFactory.get();
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