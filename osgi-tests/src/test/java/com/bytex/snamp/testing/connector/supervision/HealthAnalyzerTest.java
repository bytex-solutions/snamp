package com.bytex.snamp.testing.connector.supervision;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceGroupWatcherConfiguration;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.connector.attributes.checkers.ColoredAttributeChecker;
import com.bytex.snamp.connector.attributes.checkers.IsInRangePredicate;
import com.bytex.snamp.connector.attributes.checkers.NumberComparatorPredicate;
import com.bytex.snamp.connector.supervision.HealthStatus;
import com.bytex.snamp.connector.supervision.HealthSupervisor;
import com.bytex.snamp.connector.supervision.InvalidAttributeValue;
import com.bytex.snamp.connector.supervision.OkStatus;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connector.jmx.TestOpenMBean;
import com.google.common.reflect.TypeToken;
import org.junit.Test;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@SnampDependencies(SnampFeature.STANDARD_TOOLS)
public final class HealthAnalyzerTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String GROUP_NAME = "trip-manager";

    public HealthAnalyzerTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(TestOpenMBean.BEAN_NAME));
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Test
    public void coloredCheckerTest() throws JMException, InterruptedException {
        final ServiceHolder<HealthSupervisor> supervisor = ServiceHolder.tryCreate(getTestBundleContext(), HealthSupervisor.class);
        assertNotNull(supervisor);
        try{
            assertTrue(supervisor.get().getWatchingGroups().contains(GROUP_NAME));

            testAttribute("3.0", TypeToken.of(Integer.class), 90);
            Thread.sleep(1000L);
            HealthStatus status = supervisor.get().getHealthStatus(GROUP_NAME);
            assertTrue(status instanceof OkStatus);

            testAttribute("3.0", TypeToken.of(Integer.class), 1000);
            Thread.sleep(1000L);
            status = supervisor.get().getHealthStatus(GROUP_NAME);
            assertTrue(status instanceof InvalidAttributeValue);
            assertEquals(TEST_RESOURCE_NAME, status.getResourceName());
            assertFalse(status.isCritical());

            testAttribute("3.0", TypeToken.of(Integer.class), 2001);
            Thread.sleep(1000L);
            status = supervisor.get().getHealthStatus(GROUP_NAME);
            assertTrue(status instanceof InvalidAttributeValue);
            assertEquals(TEST_RESOURCE_NAME, status.getResourceName());
            assertTrue(status.isCritical());
        } finally {
            supervisor.release(getTestBundleContext());
        }
    }

    @Test
    public void groovyCheckerTest() throws JMException, InterruptedException {
        final ServiceHolder<HealthSupervisor> supervisor = ServiceHolder.tryCreate(getTestBundleContext(), HealthSupervisor.class);
        assertNotNull(supervisor);
        try {
            assertTrue(supervisor.get().getWatchingGroups().contains(GROUP_NAME));

            testAttribute("8.0", TypeToken.of(Float.class), 40F);
            Thread.sleep(1000L);
            HealthStatus status = supervisor.get().getHealthStatus(GROUP_NAME);
            assertTrue(status instanceof OkStatus);

            testAttribute("8.0", TypeToken.of(Float.class), 50F);
            Thread.sleep(1000L);
            status = supervisor.get().getHealthStatus(GROUP_NAME);
            assertTrue(status instanceof InvalidAttributeValue);
            assertEquals(TEST_RESOURCE_NAME, status.getResourceName());
            assertTrue(status.isCritical());
        } finally {
            supervisor.release(getTestBundleContext());
        }
    }

    @Override
    protected String getGroupName() {
        return GROUP_NAME;
    }

    @Override
    protected void fillWatchers(final EntityMap<? extends ManagedResourceGroupWatcherConfiguration> watchers) {
        watchers.addAndConsume(GROUP_NAME, watcher -> {
            watcher.getAttributeCheckers().addAndConsume("3.0", scriptlet -> {
                final ColoredAttributeChecker checker = new ColoredAttributeChecker();
                checker.setGreenPredicate(new NumberComparatorPredicate(NumberComparatorPredicate.Operator.LESS_THAN, 1000D));
                checker.setYellowPredicate(new IsInRangePredicate(1000D, true, 2000D, true));
                checker.configureScriptlet(scriptlet);
            });
            watcher.getAttributeCheckers().addAndConsume("8.0", scriptlet -> {
                scriptlet.setLanguage(ScriptletConfiguration.GROOVY_LANGUAGE);
                scriptlet.setScript("attributeValue < 42 ? OK : MALFUNCTION");
            });
        });
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("3.0");
        attribute.setAlternativeName("int32");
        attribute.put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("8.0");
        attribute.setAlternativeName("float");
        attribute.put("objectName", TestOpenMBean.BEAN_NAME);
    }
}
