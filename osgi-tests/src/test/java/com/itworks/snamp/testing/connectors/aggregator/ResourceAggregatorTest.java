package com.itworks.snamp.testing.connectors.aggregator;

import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.connectors.ManagedResourceConnector;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.testing.AbstractSnampIntegrationTest;
import com.itworks.snamp.testing.SnampDependencies;
import com.itworks.snamp.testing.SnampFeature;
import com.itworks.snamp.testing.connectors.AbstractResourceConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.TestOpenMBean;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.management.Attribute;
import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies({SnampFeature.RESOURCE_AGGREGATOR, SnampFeature.JMX_CONNECTOR})
public final class ResourceAggregatorTest extends AbstractSnampIntegrationTest {
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

    private void patternMatcherTest(final AttributeSupport jmxConnector,
                                    final AttributeSupport aggregator) throws JMException{
        jmxConnector.setAttribute(new Attribute("1.0", "9"));
        Object val = aggregator.getAttribute("42");
        assertTrue(val instanceof Boolean);
        assertTrue((Boolean)val);
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return true;
    }

    private void aggregationTest(final AttributeSupport jmxConnector,
                                 final AttributeSupport aggregator) throws JMException{
        patternMatcherTest(jmxConnector, aggregator);
    }

    @Test
    public void aggregationTest() throws JMException {
        final ServiceReference<ManagedResourceConnector> jmxConnectorRef =
                ManagedResourceConnectorClient.getResourceConnector(getTestBundleContext(), JMX_RESOURCE_NAME);
        final ServiceReference<ManagedResourceConnector> aggregatorRef =
                ManagedResourceConnectorClient.getResourceConnector(getTestBundleContext(), AGGREG_RESOURCE_NAME);
        assertNotNull(jmxConnectorRef);
        assertNotNull(aggregatorRef);
        try{
            aggregationTest(
                    getTestBundleContext().getService(jmxConnectorRef).queryObject(AttributeSupport.class),
                    getTestBundleContext().getService(aggregatorRef).queryObject(AttributeSupport.class));
        }
        finally {
            getTestBundleContext().ungetService(jmxConnectorRef);
            getTestBundleContext().ungetService(aggregatorRef);
        }
    }

    /**
     * Creates a new configuration for running this test.
     *
     * @param config The configuration to set.
     */
    @Override
    protected void setupTestConfiguration(final AgentConfiguration config) {
        //JMX Connector config
        ManagedResourceConfiguration connector =
                config.newConfigurationEntity(ManagedResourceConfiguration.class);
        connector.setConnectionString(AbstractJmxConnectorTest.JMX_RMI_CONNECTION_STRING);
        connector.setConnectionType(AbstractJmxConnectorTest.CONNECTOR_NAME);
        connector.getParameters().put("login", AbstractJmxConnectorTest.JMX_LOGIN);
        connector.getParameters().put("password", AbstractJmxConnectorTest.JMX_PASSWORD);

        AttributeConfiguration attribute = connector.newElement(AttributeConfiguration.class);
        attribute.setAttributeName("string");
        attribute.getParameters().put("objectName", BEAN_NAME);
        connector.getElements(AttributeConfiguration.class).put("1.0", attribute);

        attribute = connector.newElement(AttributeConfiguration.class);
        attribute.setAttributeName("boolean");
        attribute.getParameters().put("objectName", BEAN_NAME);
        connector.getElements(AttributeConfiguration.class).put("2.0", attribute);

        attribute = connector.newElement(AttributeConfiguration.class);
        attribute.setAttributeName("int32");
        attribute.getParameters().put("objectName", BEAN_NAME);
        connector.getElements(AttributeConfiguration.class).put("3.0", attribute);

        attribute = connector.newElement(AttributeConfiguration.class);
        attribute.setAttributeName("bigint");
        attribute.getParameters().put("objectName", BEAN_NAME);
        connector.getElements(AttributeConfiguration.class).put("4.0", attribute);

        attribute = connector.newElement(AttributeConfiguration.class);
        attribute.setAttributeName("array");
        attribute.getParameters().put("objectName", BEAN_NAME);
        connector.getElements(AttributeConfiguration.class).put("5.1", attribute);

        attribute = connector.newElement(AttributeConfiguration.class);
        attribute.setAttributeName("dictionary");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("typeName", "dict");
        connector.getElements(AttributeConfiguration.class).put("6.1", attribute);

        attribute = connector.newElement(AttributeConfiguration.class);
        attribute.setAttributeName("table");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("typeName", "table");
        connector.getElements(AttributeConfiguration.class).put("7.1", attribute);

        attribute = connector.newElement(AttributeConfiguration.class);
        attribute.setAttributeName("float");
        attribute.getParameters().put("objectName", BEAN_NAME);
        connector.getElements(AttributeConfiguration.class).put("8.0", attribute);

        attribute = connector.newElement(AttributeConfiguration.class);
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        connector.getElements(AttributeConfiguration.class).put("9.0", attribute);

        config.getManagedResources().put(JMX_RESOURCE_NAME, connector);

        //Aggregator config
        connector =
                config.newConfigurationEntity(ManagedResourceConfiguration.class);
        connector.setConnectionType(AGGREGATOR_CONNECTOR);

        attribute = connector.newElement(AttributeConfiguration.class);
        attribute.setAttributeName("matches");
        attribute.getParameters().put("pattern", "[0-9]");
        attribute.getParameters().put("source", JMX_RESOURCE_NAME);
        attribute.getParameters().put("foreignAttribute", "1.0");
        connector.getElements(AttributeConfiguration.class).put("42", attribute);

        config.getManagedResources().put(AGGREG_RESOURCE_NAME, connector);
    }
}