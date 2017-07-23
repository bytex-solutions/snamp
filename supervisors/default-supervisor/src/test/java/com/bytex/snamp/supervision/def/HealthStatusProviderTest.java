package com.bytex.snamp.supervision.def;

import com.bytex.snamp.BooleanBox;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.configuration.SupervisorConfiguration;
import com.bytex.snamp.configuration.SupervisorInfo;
import com.bytex.snamp.connector.ManagedResourceConnectorBean;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.attributes.checkers.AttributeCheckerFactory;
import com.bytex.snamp.connector.attributes.checkers.ColoredAttributeChecker;
import com.bytex.snamp.connector.attributes.checkers.IsInRangePredicate;
import com.bytex.snamp.connector.attributes.checkers.NumberComparatorPredicate;
import com.bytex.snamp.connector.attributes.reflection.ManagementAttribute;
import com.bytex.snamp.connector.health.InvalidAttributeValue;
import com.bytex.snamp.connector.health.OkStatus;
import com.bytex.snamp.connector.health.ResourceConnectorMalfunction;
import com.bytex.snamp.core.ScriptletCompilationException;
import com.bytex.snamp.supervision.health.triggers.HealthStatusTrigger;
import com.bytex.snamp.supervision.health.triggers.TriggerFactory;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.beans.IntrospectionException;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class HealthStatusProviderTest extends Assert {
    private static final class TestHealthStatusProvider extends DefaultHealthStatusProvider{
        private final AttributeCheckerFactory checkerFactory = new AttributeCheckerFactory();
        private final TriggerFactory triggerFactory = new TriggerFactory();

        HealthStatusTrigger setupHealthCheck(@Nonnull final SupervisorInfo.HealthCheckInfo healthCheckInfo) throws ScriptletCompilationException {
            removeCheckers();
            for (final Map.Entry<String, ? extends ScriptletConfiguration> attributeChecker : healthCheckInfo.getAttributeCheckers().entrySet())
                addChecker(attributeChecker.getKey(), checkerFactory.compile(attributeChecker.getValue()));
            return triggerFactory.compile(healthCheckInfo.getTrigger());
        }
    }

    public static final class TestResourceConnector extends ManagedResourceConnectorBean{
        private long memory;
        private double cpu;
        private boolean notAvailable;

        private TestResourceConnector(final String resourceName) throws IntrospectionException {
            super(resourceName);
            cpu = memory = 0L;
        }

        void setNotAvailable(final boolean value){
            notAvailable = value;
        }

        @ManagementAttribute
        public double getCPU(){
            return cpu;
        }

        public void setCPU(final double value){
            cpu = value;
        }

        @ManagementAttribute
        public long getMemory(){
            if(notAvailable)
                throw new IllegalStateException();
            return memory;
        }

        void setMemory(final long value){
            memory = value;
        }
    }

    @Test
    public void changeCompositionTest() throws Exception {
        try(final TestHealthStatusProvider watcher = new TestHealthStatusProvider()) {
            watcher.statusBuilder()
                    .updateResourceStatus("resource1", new OkStatus())
                    .updateResourceStatus("resource2", new OkStatus())
                    .build(HealthStatusTrigger.NO_OP)
                    .close();
            final BooleanBox triggered = BooleanBox.of(false);
            watcher.statusBuilder()
                    .updateResourceStatus("resource3", new OkStatus())
                    .updateResourceStatus("resource1", new OkStatus())
                    .build((prev, fresh) -> triggered.set(true))
                    .close();
            assertFalse(triggered.get());
        }
    }

    @Test
    public void updateComponentWatcherTest() throws Exception {
        final SupervisorConfiguration watcherConfiguration = ConfigurationManager.createEntityConfiguration(getClass().getClassLoader(), SupervisorConfiguration.class);
        assertNotNull(watcherConfiguration);
        watcherConfiguration.getHealthCheckConfig().getAttributeCheckers().addAndConsume("memory", scriptlet -> {
            final ColoredAttributeChecker checker = new ColoredAttributeChecker();
            checker.setGreenPredicate(new NumberComparatorPredicate(NumberComparatorPredicate.Operator.LESS_THAN, 1000D));
            checker.setYellowPredicate(new IsInRangePredicate(1000D, true, 2000D, true));
            checker.configureScriptlet(scriptlet);
        });
        try(final TestHealthStatusProvider watcher = new TestHealthStatusProvider()) {
            final HealthStatusTrigger trigger = watcher.setupHealthCheck(watcherConfiguration.getHealthCheckConfig());
            //setup first resource
            final String FIRST_RESOURCE_NAME = "resource1";
            final TestResourceConnector connector1 = new TestResourceConnector(FIRST_RESOURCE_NAME);
            connector1.queryObject(AttributeSupport.class).ifPresent(attributes -> {
                attributes.addAttribute("memory", AttributeDescriptor.EMPTY_DESCRIPTOR);
                attributes.addAttribute("CPU", AttributeDescriptor.EMPTY_DESCRIPTOR);
            });
            //setup second resource
            final String SECOND_RESOURCE_NAME = "resource2";
            final TestResourceConnector connector2 = new TestResourceConnector(SECOND_RESOURCE_NAME);
            connector1.queryObject(AttributeSupport.class).ifPresent(attributes -> {
                attributes.addAttribute("memory", AttributeDescriptor.EMPTY_DESCRIPTOR);
                attributes.addAttribute("CPU", AttributeDescriptor.EMPTY_DESCRIPTOR);
            });
            //
            connector1.setMemory(500);
            connector2.setMemory(0);
            watcher.statusBuilder()
                    .updateResourceStatus(FIRST_RESOURCE_NAME, connector1)
                    .updateResourceStatus(SECOND_RESOURCE_NAME, connector2)
                    .build(trigger)
                    .close();
            assertTrue(watcher.getStatus().getSummaryStatus() instanceof OkStatus);
            connector1.setCPU(100500);
            connector2.setCPU(0);
            watcher.statusBuilder()
                    .updateResourceStatus(FIRST_RESOURCE_NAME, connector1)
                    .updateResourceStatus(SECOND_RESOURCE_NAME, connector2)
                    .build(trigger)
                    .close();
            assertTrue(watcher.getStatus().getSummaryStatus() instanceof OkStatus);
            connector1.setMemory(1000);
            connector2.setMemory(500);
            watcher.statusBuilder()
                    .updateResourceStatus(FIRST_RESOURCE_NAME, connector1)
                    .updateResourceStatus(SECOND_RESOURCE_NAME, connector2)
                    .build(trigger)
                    .close();
            assertTrue(watcher.getStatus().getSummaryStatus() instanceof InvalidAttributeValue);
            assertTrue(watcher.getStatus().get(FIRST_RESOURCE_NAME) instanceof InvalidAttributeValue);
            connector1.setMemory(50);
            watcher.statusBuilder()
                    .updateResourceStatus(FIRST_RESOURCE_NAME, connector1)
                    .updateResourceStatus(SECOND_RESOURCE_NAME, connector2)
                    .build(trigger)
                    .close();
            assertTrue(watcher.getStatus().getSummaryStatus() instanceof OkStatus);
            connector1.setNotAvailable(true);
            watcher.statusBuilder()
                    .updateResourceStatus(FIRST_RESOURCE_NAME, connector1)
                    .updateResourceStatus(SECOND_RESOURCE_NAME, connector2)
                    .build(trigger)
                    .close();
            connector1.setNotAvailable(false);
            assertTrue(watcher.getStatus().getSummaryStatus() instanceof ResourceConnectorMalfunction);
            watcher.removeResource(FIRST_RESOURCE_NAME);
            assertTrue(watcher.getStatus().getSummaryStatus() instanceof OkStatus);
        }
    }
}
