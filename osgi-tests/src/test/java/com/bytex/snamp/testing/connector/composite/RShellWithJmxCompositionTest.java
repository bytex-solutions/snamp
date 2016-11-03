package com.bytex.snamp.testing.connector.composite;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.metrics.AttributeMetric;
import com.bytex.snamp.connector.metrics.MetricsInterval;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.connector.notifications.Mailbox;
import com.bytex.snamp.connector.notifications.MailboxFactory;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.connector.operations.OperationSupport;
import com.bytex.snamp.internal.OperatingSystem;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connector.jmx.TestOpenMBean;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import org.junit.Assume;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import static com.bytex.snamp.jmx.CompositeDataUtils.*;

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
        testAttribute("str", TypeToken.of(String.class), "Frank Underwood");
    }

    @Test
    public void booleanAttributeTest() throws JMException{
        testAttribute("bool", TypeToken.of(Boolean.class), Boolean.TRUE);
    }

    @Test
    public void intAttributeTest() throws JMException, InterruptedException {
        testAttribute("integer", TypeToken.of(Integer.class), 100);
        testAttribute("maxInt", TypeToken.of(Double.class), 100D, true);
        Thread.sleep(1001);
        testAttribute("avgInt", TypeToken.of(Double.class), 100D, true);

        testAttribute("integer", TypeToken.of(Integer.class), 50);
        testAttribute("maxInt", TypeToken.of(Double.class), 100D, true);
        Thread.sleep(1001);
        testAttribute("avgInt", TypeToken.of(Double.class), 0D, (expected, actual) -> actual < 100D, true);

        testAttribute("integer", TypeToken.of(Integer.class), 30);
        testAttribute("maxInt", TypeToken.of(Double.class), 100D, true);
        Thread.sleep(1001);
        testAttribute("avgInt", TypeToken.of(Double.class), 0D, (expected, actual) -> actual < 100D, true);
    }

    @Test
    public void memoryStatusTest() throws JMException{
        Assume.assumeTrue("Linux test only", OperatingSystem.isLinux());
        testAttribute("ms", TypeToken.of(CompositeData.class), null, (value1, value2) -> {
            assertNull(value1);
            assertNotNull(value2);
            assertTrue(getLong(value2, "total", 0L) > 0);
            assertTrue(getLong(value2, "used", 0L) > 0);
            assertTrue(getLong(value2, "free", 0L) > 0);
            return true;
        }, true);
    }

    @Test
    public void gaugeTest() throws JMException{
        //write
        testAttribute("integer", TypeToken.of(Integer.class), 70);
        //test
        testAttribute("gauge_fp", TypeToken.of(CompositeData.class), null, (value1, value2) -> {
            assertEquals(70D, getDouble(value2, "lastValue", Double.NaN), 0.1D);
            assertEquals(70D, getDouble(value2, "maxValue", Double.NaN), 0.1D);
            return true;
        }, true);
        testAttribute("gauge_int", TypeToken.of(CompositeData.class), null, (value1, value2) -> {
            assertEquals(70L, getLong(value2, "lastValue", 0L));
            assertEquals(70L, getLong(value2, "maxValue", 0L));
            return true;
        }, true);
    }

    @Test
    public void extractTest() throws JMException {
        Assume.assumeTrue("Linux test only", OperatingSystem.isLinux());
        //extract
        testAttribute("extr", TypeToken.of(String.class), null, (value1, value2) -> {
            assertNotNull(value2);
            assertTrue(Integer.parseInt(value2) > 0);
            return true;
        }, true);
        testAttribute("extr_int", TypeToken.of(Long.class), null, (value1, value2) -> {
            assertNotNull(value2);
            assertTrue(value2 > 0L);
            return true;
        }, true);
        testAttribute("extr_fp", TypeToken.of(Double.class), null, (value1, value2) -> {
            assertNotNull(value2);
            assertTrue(value2 > 0D);
            return true;
        }, true);
    }

    @Test
    public void groovyTest() throws JMException {
        testAttribute("integer", TypeToken.of(Integer.class), 100);
        testAttribute("groovy", TypeToken.of(Long.class), 110L, true);
    }

    @Test
    public void testForMetrics() throws Exception {
        final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(getTestBundleContext(), TEST_RESOURCE_NAME);
        try{
            final MetricsSupport metrics = client.queryObject(MetricsSupport.class);
            assertNotNull(metrics);
            assertTrue(metrics.getMetrics(AttributeMetric.class).iterator().hasNext());
            //read and write attributes
            booleanAttributeTest();
            //verify metrics
            final AttributeMetric attrMetrics = metrics.getMetrics(AttributeMetric.class).iterator().next();
            assertTrue(attrMetrics.reads().getLastRate(MetricsInterval.HOUR) > 0);
            assertTrue(attrMetrics.reads().getTotalRate() > 0);
        } finally {
            client.release(getTestBundleContext());
        }
    }

    @Test
    public void operationTest() throws ReflectionException, MBeanException {
        final OperationSupport operationSupport = getManagementConnector(getTestBundleContext())
                .queryObject(OperationSupport.class);
        try{
            final Object result = operationSupport.invoke("rev", new Object[]{new byte[]{1, 2, 3}}, new String[0]);
            assertTrue(result instanceof byte[]);
            assertArrayEquals(new byte[]{3, 2, 1}, (byte[])result);
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void notificationTest() throws JMException, InterruptedException {
        final NotificationSupport notificationSupport = getManagementConnector(getTestBundleContext())
                .queryObject(NotificationSupport.class);
        try{
            final Mailbox mailbox = MailboxFactory.newMailbox();
            notificationSupport.addNotificationListener(mailbox, null, null);
            stringAttributeTest();
            final Notification n = mailbox.poll(1, TimeUnit.SECONDS);
            assertNotNull(n);
            assertEquals("attributeChange", n.getType());
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void rateAttributeTest() throws JMException, InterruptedException {
        stringAttributeTest();
        stringAttributeTest();
        Thread.sleep(300);
        testAttribute("notifRate", TypeToken.of(CompositeData.class), null, (value1, value2) -> {
            assertNull(value1);
            assertNotNull(value2);
            assertEquals(2L, getLong(value2, "totalRate", 0L));
            return true;
        }, true);
    }

    @Test
    public void configurationTest(){
        testConfigurationDescriptor(ManagedResourceConfiguration.class, ImmutableSet.of(
                "separator"
        ));
        testConfigurationDescriptor(AttributeConfiguration.class, ImmutableSet.of(
                "source",
                "formula"
        ));
        testConfigurationDescriptor(EventConfiguration.class, ImmutableSet.of(
                "source"
        ));
        testConfigurationDescriptor(OperationConfiguration.class, ImmutableSet.of(
                "source"
        ));
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
        operations.addAndConsume("rev", operation -> {
            operation.setAlternativeName("reverse");
            operation.getParameters().put("source", "jmx");
        });
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        events.addAndConsume("attributeChange", event -> {
            event.setAlternativeName(AttributeChangeNotification.ATTRIBUTE_CHANGE);
            event.getParameters().put("severity", "notice");
            event.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
            event.getParameters().put("source", "jmx");
        });
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        attributes.addAndConsume("str", attribute -> {
            attribute.setAlternativeName("string");
            attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
            attribute.getParameters().put("source", "jmx");
        });

        attributes.addAndConsume("bool", attribute -> {
            attribute.setAlternativeName("boolean");
            attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
            attribute.getParameters().put("source", "jmx");
        });

        attributes.addAndConsume("ms", attribute -> {
            attribute.setAlternativeName(getPathToFileInProjectRoot("freemem-tool-profile.xml"));
            attribute.getParameters().put("format", "-m");
            attribute.getParameters().put("source", "rshell");
        });

        attributes.addAndConsume("integer", attribute -> {
            attribute.setAlternativeName("int32");
            attribute.getParameters().put("source", "jmx");
            attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        });

        attributes.addAndConsume("maxInt", attribute -> {
            attribute.setAlternativeName("integer");
            attribute.getParameters().put("formula", "max()");
        });

        attributes.addAndConsume("avgInt", attribute -> {
            attribute.setAlternativeName("integer");
            attribute.getParameters().put("formula", "avg(10s)");
        });

        attributes.addAndConsume("gauge_fp", attribute -> {
            attribute.setAlternativeName("integer");
            attribute.getParameters().put("formula", "gauge_fp()");
        });

        attributes.addAndConsume("gauge_int", attribute -> {
            attribute.setAlternativeName("integer");
            attribute.getParameters().put("formula", "gauge_int()");
        });

        attributes.addAndConsume("extr", attribute -> {
            attribute.setAlternativeName("ms");
            attribute.getParameters().put("formula", "extract(total)");
        });

        attributes.addAndConsume("extr_int", attribute -> {
            attribute.setAlternativeName("ms");
            attribute.getParameters().put("formula", "extract_int(total)");
        });

        attributes.addAndConsume("extr_fp", attribute -> {
            attribute.setAlternativeName("ms");
            attribute.getParameters().put("formula", "extract_fp(total)");
        });

        attributes.addAndConsume("notifRate", attribute -> {
            attribute.setAlternativeName("attributeChange");
            attribute.getParameters().put("formula", "rate()");
        });

        attributes.addAndConsume("groovy", attribute -> {
            attribute.setAlternativeName("Composition.groovy");
            attribute.getParameters().put("formula", "groovy()");
            attribute.getParameters().put("groovyPath", getPathToFileInProjectRoot("sample-groovy-scripts") + File.separator);
        });
    }
}
