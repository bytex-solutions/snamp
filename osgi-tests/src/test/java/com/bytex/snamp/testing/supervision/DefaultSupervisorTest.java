package com.bytex.snamp.testing.supervision;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.attributes.checkers.ColoredAttributeChecker;
import com.bytex.snamp.connector.attributes.checkers.IsInRangePredicate;
import com.bytex.snamp.connector.attributes.checkers.NumberComparatorPredicate;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.InvalidAttributeValue;
import com.bytex.snamp.connector.health.OkStatus;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.supervision.SupervisorClient;
import com.bytex.snamp.supervision.health.HealthStatusProvider;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connector.jmx.TestOpenMBean;
import com.google.common.reflect.TypeToken;
import org.junit.Test;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@SnampDependencies(SnampFeature.STANDARD_TOOLS)
public final class DefaultSupervisorTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String GROUP_NAME = "trip-manager";

    public DefaultSupervisorTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(TestOpenMBean.BEAN_NAME));
    }

    @Test
    public void defaultSupervisorConfigTest() {
        final ConfigurationEntityDescription<SupervisorConfiguration> description =
                SupervisorClient.getConfigurationDescriptor(getTestBundleContext(), SupervisorConfiguration.DEFAULT_TYPE);
        testConfigurationDescriptor(description, "checkPeriod");
    }

    @Test
    public void coloredCheckerTest() throws JMException, InterruptedException {
        try (final SupervisorClient supervisor = SupervisorClient.tryCreate(getTestBundleContext(), GROUP_NAME)
                .orElseThrow(AssertionError::new)) {
            assertTrue(supervisor.get().getResources().contains(TEST_RESOURCE_NAME));

            testAttribute("3.0", TypeToken.of(Integer.class), 90);
            Thread.sleep(1000L);
            HealthStatus status = supervisor.queryObject(HealthStatusProvider.class).map(HealthStatusProvider::getStatus).orElseThrow(AssertionError::new);
            assertTrue(status instanceof OkStatus);

            testAttribute("3.0", TypeToken.of(Integer.class), 1000);
            Thread.sleep(1000L);
            status = supervisor.queryObject(HealthStatusProvider.class).map(HealthStatusProvider::getStatus).orElseThrow(AssertionError::new);
            assertTrue(status instanceof InvalidAttributeValue);
            assertEquals(TEST_RESOURCE_NAME, status.getResourceName());
            assertFalse(status.isCritical());

            testAttribute("3.0", TypeToken.of(Integer.class), 2001);
            Thread.sleep(1000L);
            status = supervisor.queryObject(HealthStatusProvider.class).map(HealthStatusProvider::getStatus).orElseThrow(AssertionError::new);
            assertTrue(status instanceof InvalidAttributeValue);
            assertEquals(TEST_RESOURCE_NAME, status.getResourceName());
            assertTrue(status.isCritical());
        }
    }

    @Test
    public void groovyCheckerTest() throws JMException, InterruptedException {
        try (final SupervisorClient supervisor = SupervisorClient.tryCreate(getTestBundleContext(), GROUP_NAME)
                .orElseThrow(AssertionError::new)) {
            assertTrue(supervisor.get().getResources().contains(TEST_RESOURCE_NAME));

            testAttribute("8.0", TypeToken.of(Float.class), 40F);
            Thread.sleep(1000L);
            HealthStatus status = supervisor.queryObject(HealthStatusProvider.class).map(HealthStatusProvider::getStatus).orElseThrow(AssertionError::new);
            assertTrue(status instanceof OkStatus);

            testAttribute("8.0", TypeToken.of(Float.class), 50F);
            Thread.sleep(1000L);
            status = supervisor.queryObject(HealthStatusProvider.class).map(HealthStatusProvider::getStatus).orElseThrow(AssertionError::new);
            assertTrue(status instanceof InvalidAttributeValue);
            assertEquals(TEST_RESOURCE_NAME, status.getResourceName());
            assertTrue(status.isCritical());
        }
    }

    @Override
    protected String getGroupName() {
        return GROUP_NAME;
    }

    @Override
    protected void fillSupervisors(final EntityMap<? extends SupervisorConfiguration> watchers) {
        final String groovyTrigger;
        try {
            groovyTrigger = IOUtils.toString(getClass().getResourceAsStream("GroovyTrigger.groovy"));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        watchers.addAndConsume(GROUP_NAME, watcher -> {
            watcher.getHealthCheckConfig().getAttributeCheckers().addAndConsume("3.0", scriptlet -> {
                final ColoredAttributeChecker checker = new ColoredAttributeChecker();
                checker.setGreenPredicate(new NumberComparatorPredicate(NumberComparatorPredicate.Operator.LESS_THAN, 1000D));
                checker.setYellowPredicate(new IsInRangePredicate(1000D, true, 2000D, true));
                checker.configureScriptlet(scriptlet);
            });
            watcher.getHealthCheckConfig().getAttributeCheckers().addAndConsume("8.0", scriptlet -> {
                scriptlet.setLanguage(ScriptletConfiguration.GROOVY_LANGUAGE);
                scriptlet.setScript("attributeValue < 42 ? OK : MALFUNCTION");
            });
            watcher.getHealthCheckConfig().getTrigger().setLanguage(ScriptletConfiguration.GROOVY_LANGUAGE);
            watcher.getHealthCheckConfig().getTrigger().setScript(groovyTrigger);
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
