package com.bytex.snamp.testing.connectors.aggregator;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ConfigurationEntityDescription;
import com.bytex.snamp.connectors.ManagedResourceConnector;
import com.bytex.snamp.connectors.ManagedResourceConnectorClient;
import com.bytex.snamp.jmx.CompositeDataUtils;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connectors.AbstractResourceConnectorTest;
import com.bytex.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connectors.jmx.TestOpenMBean;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import java.math.BigInteger;

import static com.bytex.snamp.configuration.AgentConfiguration.*;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.*;
import static com.bytex.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@SnampDependencies({SnampFeature.RESOURCE_AGGREGATOR, SnampFeature.JMX_CONNECTOR})
public final class ResourceAggregatorTest extends AbstractSnampIntegrationTest {
    private interface TestLogic{
        void runTest(final DynamicMBean jmxConnector, final DynamicMBean aggregator) throws JMException;
    }

    private static final String JMX_RESOURCE_NAME = "JMX-Resource";
    private static final String AGGREG_RESOURCE_NAME = "AGG";
    private static final String AGGREGATOR_CONNECTOR = "aggregator";

    private final ObjectName beanName;
    private final TestOpenMBean beanInstance;

    public ResourceAggregatorTest() throws MalformedObjectNameException {
        beanName = new ObjectName(BEAN_NAME);
        beanInstance = new TestOpenMBean();
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        AbstractResourceConnectorTest.startResourceConnector(testName,
                AbstractJmxConnectorTest.CONNECTOR_NAME,
                JMX_RESOURCE_NAME,
                context);
        AbstractResourceConnectorTest.startResourceConnector(testName,
                AGGREGATOR_CONNECTOR,
                AGGREG_RESOURCE_NAME,
                context);
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        AbstractJmxConnectorTest.beforeStartTest(beanName, beanInstance);
        beforeCleanupTest(context);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws JMException {
        AbstractJmxConnectorTest.afterCleanupTest(beanName, beanInstance);
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        AbstractResourceConnectorTest.stopResourceConnector(testName,
                AbstractJmxConnectorTest.CONNECTOR_NAME,
                JMX_RESOURCE_NAME,
                context);
        AbstractResourceConnectorTest.stopResourceConnector(testName,
                AGGREGATOR_CONNECTOR,
                AGGREG_RESOURCE_NAME,
                context);
    }

    @Test
    public void patternMatcherTest() throws JMException{
        runTest(new TestLogic() {
            @Override
            public void runTest(final DynamicMBean jmxConnector, final DynamicMBean aggregator) throws JMException {
                jmxConnector.setAttribute(new Attribute("1.0", "9"));
                Object val = aggregator.getAttribute("42");
                assertTrue(val instanceof Boolean);
                assertTrue((Boolean) val);
                jmxConnector.setAttribute(new Attribute("1.0", "aaa"));
                val = aggregator.getAttribute("42");
                assertTrue(val instanceof Boolean);
                assertFalse((Boolean) val);
            }
        });
    }

    @Test
    public void comparisonWithTest() throws JMException{
        runTest(new TestLogic() {
            @Override
            public void runTest(final DynamicMBean jmxConnector, final DynamicMBean aggregator) throws JMException {
                jmxConnector.setAttribute(new Attribute("3.0", 56));
                Object val = aggregator.getAttribute("43");
                assertTrue(val instanceof Boolean);
                assertTrue((Boolean) val);
                jmxConnector.setAttribute(new Attribute("3.0", 9));
                val = aggregator.getAttribute("43");
                assertTrue(val instanceof Boolean);
                assertFalse((Boolean) val);
            }
        });
    }

    @Test
    public void percentFromTest() throws JMException{
        runTest(new TestLogic() {
            @Override
            public void runTest(final DynamicMBean jmxConnector, final DynamicMBean aggregator) throws JMException {
                jmxConnector.setAttribute(new Attribute("3.0", 25));
                Object val = aggregator.getAttribute("45");
                assertTrue(val instanceof Double);
                assertEquals(50, (Double) val, 2);
                jmxConnector.setAttribute(new Attribute("3.0", 40));
                val = aggregator.getAttribute("45");
                assertTrue(val instanceof Double);
                assertEquals(80, (Double) val, 2);
            }
        });
    }

    @Test
    public void percentTest() throws JMException{
        runTest(new TestLogic() {
            @Override
            public void runTest(final DynamicMBean jmxConnector, final DynamicMBean aggregator) throws JMException {
                jmxConnector.setAttribute(new Attribute("3.0", 25));
                jmxConnector.setAttribute(new Attribute("4.0", BigInteger.valueOf(50L)));
                Object val = aggregator.getAttribute("46");
                assertTrue(val instanceof Double);
                assertEquals(50, (Double) val, 2);
                jmxConnector.setAttribute(new Attribute("3.0", 40));
                val = aggregator.getAttribute("46");
                assertTrue(val instanceof Double);
                assertEquals(80, (Double) val, 2);
            }
        });
    }

    @Test
    public void comparisonTest() throws JMException{
        runTest(new TestLogic() {
            @Override
            public void runTest(final DynamicMBean jmxConnector, final DynamicMBean aggregator) throws JMException {
                jmxConnector.setAttribute(new Attribute("3.0", 56));
                jmxConnector.setAttribute(new Attribute("4.0", BigInteger.ONE));
                Object val = aggregator.getAttribute("44");
                assertTrue(val instanceof Boolean);
                assertTrue((Boolean) val);
                jmxConnector.setAttribute(new Attribute("3.0", 150));
                jmxConnector.setAttribute(new Attribute("4.0", new BigInteger("100500")));
                val = aggregator.getAttribute("44");
                assertTrue(val instanceof Boolean);
                assertFalse((Boolean) val);
            }
        });
    }

    @Test
    public void counterTest() throws JMException{
        runTest(new TestLogic() {
            @Override
            public void runTest(final DynamicMBean jmxConnector, final DynamicMBean aggregator) throws JMException {
                jmxConnector.setAttribute(new Attribute("3.0", 5));
                Object val = aggregator.getAttribute("47");
                assertEquals(5L, val);
                val = aggregator.getAttribute("47");
                assertEquals(10L, val);
                val = aggregator.getAttribute("47");
                assertEquals(15L, val);
                try {
                    Thread.sleep(5000);
                } catch (final InterruptedException e) {
                    throw new ReflectionException(e);
                }
                val = aggregator.getAttribute("47");
                assertEquals(5L, val);
            }
        });
    }

    @Test
    public void averageTest() throws JMException{
        runTest(new TestLogic() {
            @Override
            public void runTest(final DynamicMBean jmxConnector, final DynamicMBean aggregator) throws JMException {
                jmxConnector.setAttribute(new Attribute("3.0", 20));
                Object val = aggregator.getAttribute("48");
                assertEquals(20F, (Double)val, 3);
                jmxConnector.setAttribute(new Attribute("3.0", 40));
                val = aggregator.getAttribute("48");
                assertEquals(30F, (Double)val, 3);
                try {
                    Thread.sleep(5000);
                } catch (final InterruptedException e) {
                    throw new ReflectionException(e);
                }
                val = aggregator.getAttribute("48");
                assertEquals(40F, (Double)val, 3);
            }
        });
    }

    @Test
    public void peakTest() throws JMException{
        runTest(new TestLogic() {
            @Override
            public void runTest(final DynamicMBean jmxConnector, final DynamicMBean aggregator) throws JMException {
                jmxConnector.setAttribute(new Attribute("3.0", 20));
                Object val = aggregator.getAttribute("49");
                assertEquals(20L, val);
                jmxConnector.setAttribute(new Attribute("3.0", 40));
                val = aggregator.getAttribute("49");
                assertEquals(40L, val);
                jmxConnector.setAttribute(new Attribute("3.0", 30));
                val = aggregator.getAttribute("49");
                assertEquals(40L, val);
                try {
                    Thread.sleep(5000);
                } catch (final InterruptedException e) {
                    throw new ReflectionException(e);
                }
                val = aggregator.getAttribute("49");
                assertEquals(30L, val);
            }
        });
    }

    @Test
    public void decomposerTest() throws JMException{
        runTest(new TestLogic() {
            @Override
            public void runTest(final DynamicMBean jmxConnector, final DynamicMBean aggregator) throws JMException {
                Object val = aggregator.getAttribute("50");
                assertEquals("10", val);
            }
        });
    }

    @Test
    public void composerTest() throws JMException{
        runTest(new TestLogic() {
            @Override
            public void runTest(final DynamicMBean jmxConnector, final DynamicMBean aggregator) throws JMException {
                jmxConnector.setAttribute(new Attribute("3.0", 907));
                jmxConnector.setAttribute(new Attribute("1.0", "Frank Underwood"));
                jmxConnector.setAttribute(new Attribute("4.0", BigInteger.valueOf(100500L)));
                jmxConnector.setAttribute(new Attribute("8.0", 34.2F));

                final CompositeData value = (CompositeData) aggregator.getAttribute("51");
                assertNotNull(value);
                assertEquals(907, CompositeDataUtils.getInteger(value, "3.0", 0));
                assertEquals("Frank Underwood", CompositeDataUtils.getString(value, "1.0", ""));
                assertEquals(BigInteger.valueOf(100500L), CompositeDataUtils.getBigInteger(value, "4.0", BigInteger.ZERO));
                assertEquals(34.2F, CompositeDataUtils.getFloat(value, "8.0", 0F), 0.01);
            }
        });
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }


    private void runTest(final TestLogic logic) throws JMException {
        final ServiceReference<ManagedResourceConnector> jmxConnectorRef =
                ManagedResourceConnectorClient.getResourceConnector(getTestBundleContext(), JMX_RESOURCE_NAME);
        final ServiceReference<ManagedResourceConnector> aggregatorRef =
                ManagedResourceConnectorClient.getResourceConnector(getTestBundleContext(), AGGREG_RESOURCE_NAME);
        assertNotNull(jmxConnectorRef);
        assertNotNull(aggregatorRef);
        try{
            logic.runTest(getTestBundleContext().getService(jmxConnectorRef),
                    getTestBundleContext().getService(aggregatorRef));
        }
        finally {
            getTestBundleContext().ungetService(jmxConnectorRef);
            getTestBundleContext().ungetService(aggregatorRef);
        }
    }

    @Test
    public void configurationTest(){
        ConfigurationEntityDescription description = ManagedResourceConnectorClient.getConfigurationEntityDescriptor(getTestBundleContext(), AGGREGATOR_CONNECTOR, ManagedResourceConfiguration.class);
        AbstractResourceConnectorTest.testConfigurationDescriptor(description, "notificationFrequency");
        description = ManagedResourceConnectorClient.getConfigurationEntityDescriptor(getTestBundleContext(), AGGREGATOR_CONNECTOR, AttributeConfiguration.class);
        AbstractResourceConnectorTest.testConfigurationDescriptor(description,
                "source",
                "foreignAttribute",
                "firstForeignAttribute",
                "secondForeignAttribute",
                "comparer",
                "value",
                "timeInterval");
        description = ManagedResourceConnectorClient.getConfigurationEntityDescriptor(getTestBundleContext(), AGGREGATOR_CONNECTOR, EventConfiguration.class);
        AbstractResourceConnectorTest.testConfigurationDescriptor(description,
                "foreignAttribute",
                "source");
    }

    /**
     * Creates a new configuration for running this test.
     *
     * @param config The configuration to set.
     */
    @Override
    protected void setupTestConfiguration(final AgentConfiguration config) {
        //JMX Connector config
        ManagedResourceConfiguration connector = config.getManagedResources().getOrAdd(JMX_RESOURCE_NAME);
        connector.setConnectionString(AbstractJmxConnectorTest.JMX_RMI_CONNECTION_STRING);
        connector.setConnectionType(AbstractJmxConnectorTest.CONNECTOR_NAME);
        connector.getParameters().put("login", AbstractJmxConnectorTest.JMX_LOGIN);
        connector.getParameters().put("password", AbstractJmxConnectorTest.JMX_PASSWORD);

        AttributeConfiguration attribute = connector.getFeatures(AttributeConfiguration.class).getOrAdd("1.0");
        setFeatureName(attribute, "string");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = connector.getFeatures(AttributeConfiguration.class).getOrAdd("2.0");
        setFeatureName(attribute, "boolean");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = connector.getFeatures(AttributeConfiguration.class).getOrAdd("3.0");
        setFeatureName(attribute, "int32");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = connector.getFeatures(AttributeConfiguration.class).getOrAdd("4.0");
        setFeatureName(attribute, "bigint");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = connector.getFeatures(AttributeConfiguration.class).getOrAdd("5.1");
        setFeatureName(attribute, "array");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = connector.getFeatures(AttributeConfiguration.class).getOrAdd("6.1");
        setFeatureName(attribute, "dictionary");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("typeName", "dict");

        attribute = connector.getFeatures(AttributeConfiguration.class).getOrAdd("7.1");
        setFeatureName(attribute, "table");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("typeName", "table");

        attribute = connector.getFeatures(AttributeConfiguration.class).getOrAdd("8.0");
        setFeatureName(attribute, "float");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = connector.getFeatures(AttributeConfiguration.class).getOrAdd("9.0");
        setFeatureName(attribute, "date");
        attribute.getParameters().put("objectName", BEAN_NAME);

        //Aggregator config
        connector =
                config.getManagedResources().getOrAdd(AGGREG_RESOURCE_NAME);
        connector.setConnectionType(AGGREGATOR_CONNECTOR);

        attribute = connector.getFeatures(AttributeConfiguration.class).getOrAdd("42");
        setFeatureName(attribute, "matcher");
        attribute.getParameters().put("value", "[0-9]");
        attribute.getParameters().put("source", JMX_RESOURCE_NAME);
        attribute.getParameters().put("foreignAttribute", "1.0");

        attribute = connector.getFeatures(AttributeConfiguration.class).getOrAdd("43");
        setFeatureName(attribute, "comparisonWith");
        attribute.getParameters().put("comparer", ">=");
        attribute.getParameters().put("source", JMX_RESOURCE_NAME);
        attribute.getParameters().put("foreignAttribute", "3.0");
        attribute.getParameters().put("value", "10");

        attribute = connector.getFeatures(AttributeConfiguration.class).getOrAdd("44");
        setFeatureName(attribute, "comparison");
        attribute.getParameters().put("comparer", ">=");
        attribute.getParameters().put("source", JMX_RESOURCE_NAME);
        attribute.getParameters().put("firstForeignAttribute", "3.0");
        attribute.getParameters().put("secondForeignAttribute", "4.0");

        attribute = connector.getFeatures(AttributeConfiguration.class).getOrAdd("45");
        setFeatureName(attribute, "percentFrom");
        attribute.getParameters().put("source", JMX_RESOURCE_NAME);
        attribute.getParameters().put("foreignAttribute", "3.0");
        attribute.getParameters().put("value", "50");

        attribute = connector.getFeatures(AttributeConfiguration.class).getOrAdd("46");
        setFeatureName(attribute, "percent");
        attribute.getParameters().put("source", JMX_RESOURCE_NAME);
        attribute.getParameters().put("firstForeignAttribute", "3.0");
        attribute.getParameters().put("secondForeignAttribute", "4.0");

        attribute = connector.getFeatures(AttributeConfiguration.class).getOrAdd("47");
        setFeatureName(attribute, "counter");
        attribute.getParameters().put("source", JMX_RESOURCE_NAME);
        attribute.getParameters().put("foreignAttribute", "3.0");
        attribute.getParameters().put("timeInterval", "5000");

        attribute = connector.getFeatures(AttributeConfiguration.class).getOrAdd("48");
        setFeatureName(attribute, "average");
        attribute.getParameters().put("source", JMX_RESOURCE_NAME);
        attribute.getParameters().put("foreignAttribute", "3.0");
        attribute.getParameters().put("timeInterval", "5000");

        attribute = connector.getFeatures(AttributeConfiguration.class).getOrAdd("49");
        setFeatureName(attribute, "peak");
        attribute.getParameters().put("source", JMX_RESOURCE_NAME);
        attribute.getParameters().put("foreignAttribute", "3.0");
        attribute.getParameters().put("timeInterval", "5000");

        attribute = connector.getFeatures(AttributeConfiguration.class).getOrAdd("50");
        setFeatureName(attribute, "decomposer");
        attribute.getParameters().put("source", JMX_RESOURCE_NAME);
        attribute.getParameters().put("foreignAttribute", "6.1");
        attribute.getParameters().put("fieldPath", "col2");

        attribute = connector.getFeatures(AttributeConfiguration.class).getOrAdd("51");
        setFeatureName(attribute, "composer");
        attribute.getParameters().put("source", JMX_RESOURCE_NAME);
    }
}