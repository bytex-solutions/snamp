package com.bytex.snamp.testing.connector.composite;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.metrics.AttributeMetrics;
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

import static com.bytex.snamp.jmx.CompositeDataUtils.getDouble;
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
                "groovyPath", "file:" + getPathToFileInProjectRoot("sample-groovy-scripts") + File.separator,
                "jmx:login", AbstractJmxConnectorTest.JMX_LOGIN,
                "jmx:password", AbstractJmxConnectorTest.JMX_PASSWORD,
                "jmx:objectName", TestOpenMBean.BEAN_NAME
        ));
        beanName = new ObjectName(TestOpenMBean.BEAN_NAME);
        beanInstance = new TestOpenMBean();
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
    public void rangedMetricsTest() throws JMException{
        //write
        testAttribute("integer", TypeToken.of(Integer.class), 70);
        testAttribute("ranged", TypeToken.of(CompositeData.class), null, (value1, value2) -> true, true);
        testAttribute("integer", TypeToken.of(Integer.class), 15);
        testAttribute("ranged", TypeToken.of(CompositeData.class), null, (value1, value2) -> true, true);
        testAttribute("integer", TypeToken.of(Integer.class), 3);
        testAttribute("ranged", TypeToken.of(CompositeData.class), null, (value1, value2) -> true, true);
        testAttribute("integer", TypeToken.of(Integer.class), 5);
        testAttribute("ranged", TypeToken.of(CompositeData.class), null, (value1, value2) -> {
            assertEquals(2D / 4D, getDouble(value2, "lessThanRange", Double.NaN), 0.01D);
            assertEquals(1D / 4D, getDouble(value2, "greaterThanRange", Double.NaN), 0.01D);
            assertEquals(1D / 4D, getDouble(value2, "isInRange", Double.NaN), 0.01D);
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
        final ManagedResourceConnector client = getManagementConnector();
        try {
            final MetricsSupport metrics = client.queryObject(MetricsSupport.class).orElseThrow(AssertionError::new);
            assertTrue(metrics.getMetrics(AttributeMetrics.class).iterator().hasNext());
            //read and write attributes
            booleanAttributeTest();
            //verify metrics
            final AttributeMetrics attrMetrics = metrics.getMetrics(AttributeMetrics.class).iterator().next();
            assertTrue(attrMetrics.reads().getLastRate(MetricsInterval.HOUR) > 0);
            assertTrue(attrMetrics.reads().getTotalRate() > 0);
        } finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void operationTest() throws JMException {
        final OperationSupport operationSupport = getManagementConnector().queryObject(OperationSupport.class).orElseThrow(AssertionError::new);
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
        final NotificationSupport notificationSupport = getManagementConnector().queryObject(NotificationSupport.class).orElseThrow(AssertionError::new);
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
            operation.put("source", "jmx");
        });
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        events.addAndConsume("attributeChange", event -> {
            event.setAlternativeName(AttributeChangeNotification.ATTRIBUTE_CHANGE);
            event.put("severity", "notice");
            event.put("objectName", TestOpenMBean.BEAN_NAME);
            event.put("source", "jmx");
        });
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        attributes.addAndConsume("str", attribute -> {
            attribute.setAlternativeName("string");
            attribute.put("objectName", TestOpenMBean.BEAN_NAME);
            attribute.put("source", "jmx");
        });

        attributes.addAndConsume("bool", attribute -> {
            attribute.setAlternativeName("boolean");
            attribute.put("objectName", TestOpenMBean.BEAN_NAME);
            attribute.put("source", "jmx");
        });

        attributes.addAndConsume("ms", attribute -> {
            attribute.setAlternativeName(getPathToFileInProjectRoot("freemem-tool-profile.xml"));
            attribute.put("format", "-m");
            attribute.put("source", "rshell");
        });

        attributes.addAndConsume("integer", attribute -> {
            attribute.setAlternativeName("int32");
            attribute.put("source", "jmx");
            attribute.put("objectName", TestOpenMBean.BEAN_NAME);
        });

        attributes.addAndConsume("maxInt", attribute -> {
            attribute.setAlternativeName("integer");
            attribute.put("formula", "max()");
        });

        attributes.addAndConsume("avgInt", attribute -> {
            attribute.setAlternativeName("integer");
            attribute.put("formula", "avg(10s)");
        });

        attributes.addAndConsume("gauge_fp", attribute -> {
            attribute.setAlternativeName("integer");
            attribute.put("formula", "gauge_fp()");
        });

        attributes.addAndConsume("gauge_int", attribute -> {
            attribute.setAlternativeName("integer");
            attribute.put("formula", "gauge_int()");
        });

        attributes.addAndConsume("extr", attribute -> {
            attribute.setAlternativeName("ms");
            attribute.put("formula", "extract(string, total)");
        });

        attributes.addAndConsume("extr_int", attribute -> {
            attribute.setAlternativeName("ms");
            attribute.put("formula", "extract(int64, total)");
        });

        attributes.addAndConsume("extr_fp", attribute -> {
            attribute.setAlternativeName("ms");
            attribute.put("formula", "extract(float64, total)");
        });

        attributes.addAndConsume("ranged", attribute -> {
            attribute.setAlternativeName("integer");
            attribute.put("formula", "ranged_fp(10, 20)");
        });

        attributes.addAndConsume("notifRate", attribute -> {
            attribute.setAlternativeName("attributeChange");
            attribute.put("formula", "rate()");
        });

        attributes.addAndConsume("groovy", attribute -> {
            attribute.setAlternativeName("Composition.groovy");
            attribute.put("formula", "groovy()");
        });
    }
}
