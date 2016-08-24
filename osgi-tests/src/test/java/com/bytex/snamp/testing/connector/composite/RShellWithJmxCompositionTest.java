package com.bytex.snamp.testing.connector.composite;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.OperationConfiguration;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.metrics.AttributeMetrics;
import com.bytex.snamp.connector.metrics.MetricsInterval;
import com.bytex.snamp.connector.metrics.MetricsReader;
import com.bytex.snamp.internal.OperatingSystem;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connector.jmx.TestOpenMBean;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.junit.Assume;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import java.lang.management.ManagementFactory;

import static com.bytex.snamp.jmx.CompositeDataUtils.getLong;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies({SnampFeature.RSHELL_CONNECTOR, SnampFeature.JMX_CONNECTOR})
public final class RShellWithJmxCompositionTest extends AbstractCompositeConnectorTest {
    private static String buildConnectionString() {
        return "jmx:=" +
                AbstractJmxConnectorTest.getConnectionString() +
                ';' +
                "rshell:=process";
    }

    private final ObjectName beanName;
    private final TestOpenMBean beanInstance;


    public RShellWithJmxCompositionTest() throws MalformedObjectNameException {
        super(buildConnectionString(), ImmutableMap.of(
            "jmx:login", AbstractJmxConnectorTest.JMX_LOGIN,
            "jmx:password", AbstractJmxConnectorTest.JMX_PASSWORD,
            "jmx:objectName", TestOpenMBean.BEAN_NAME
        ));
        beanName = new ObjectName(TestOpenMBean.BEAN_NAME);
        beanInstance = new TestOpenMBean();
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Test
    public void stringAttributeTest() throws JMException {
        testAttribute("jmx:str", TypeToken.of(String.class), "Frank Underwood");
    }

    @Test
    public void booleanAttributeTest() throws JMException{
        testAttribute("jmx:bool", TypeToken.of(Boolean.class), Boolean.TRUE);
    }

    @Test
    public void memoryStatusTest() throws JMException{
        Assume.assumeTrue("Linux test only", OperatingSystem.isLinux());
        testAttribute("rshell:ms", TypeToken.of(CompositeData.class), null, (value1, value2) -> {
            assertNull(value1);
            assertNotNull(value2);
            assertTrue(getLong(value2, "total", 0L) > 0);
            assertTrue(getLong(value2, "used", 0L) > 0);
            assertTrue(getLong(value2, "free", 0L) > 0);
            return true;
        }, true);
    }

    @Test
    public void testForMetrics() throws Exception {
        final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(getTestBundleContext(), TEST_RESOURCE_NAME);
        try{
            final MetricsReader metrics = client.queryObject(MetricsReader.class);
            assertNotNull(metrics);
            assertTrue(metrics.getMetrics(MBeanAttributeInfo.class) instanceof AttributeMetrics);
            assertNotNull(metrics.queryObject(AttributeMetrics.class));
            //read and write attributes
            booleanAttributeTest();
            //verify metrics
            final AttributeMetrics attrMetrics = metrics.queryObject(AttributeMetrics.class);
            assertTrue(attrMetrics.getNumberOfReads(MetricsInterval.HOUR) > 0);
            assertTrue(attrMetrics.getNumberOfReads() > 0);
        } finally {
            client.release(getTestBundleContext());
        }
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws JMException {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        if(mbs.isRegistered(beanName))
            mbs.unregisterMBean(beanName);
        mbs.registerMBean(beanInstance, beanName);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws JMException {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbs.unregisterMBean(beanName);
    }

    @Override
    protected void fillOperations(final EntityMap<? extends OperationConfiguration> operations) {
        operations.addAndConsume("jmx:forceGC", operation -> operation.setAlternativeName("gc"));
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        events.addAndConsume("jmx:attributeChange", event -> {
            event.setAlternativeName(AttributeChangeNotification.ATTRIBUTE_CHANGE);
            event.getParameters().put("severity", "notice");
            event.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        });
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        attributes.addAndConsume("jmx:str", attribute -> {
            attribute.setAlternativeName("string");
            attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        });

        attributes.addAndConsume("jmx:bool", attribute -> {
            attribute.setAlternativeName("boolean");
            attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        });

        attributes.addAndConsume("rshell:ms", attribute -> {
            attribute.setAlternativeName(getPathToFileInProjectRoot("freemem-tool-profile.xml"));
            attribute.getParameters().put("format", "-m");
        });
    }
}
