package com.bytex.snamp.moa.services;

import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.ManagedResourceGroupWatcherConfiguration;
import com.bytex.snamp.connector.attributes.checkers.ColoredAttributeChecker;
import com.bytex.snamp.connector.attributes.checkers.InvalidAttributeCheckerException;
import com.bytex.snamp.connector.attributes.checkers.IsInRangePredicate;
import com.bytex.snamp.connector.attributes.checkers.NumberComparatorPredicate;
import com.bytex.snamp.connector.supervision.InvalidAttributeValue;
import com.bytex.snamp.connector.supervision.OkStatus;
import com.bytex.snamp.connector.supervision.ResourceInGroupIsNotUnavailable;
import com.bytex.snamp.connector.supervision.triggers.InvalidTriggerException;
import org.junit.Assert;
import org.junit.Test;

import javax.management.Attribute;
import javax.management.JMException;

import static java.util.Collections.singleton;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class HealthAnalyzerTest extends Assert {

    @Test
    public void updateComponentWatcherTest() throws InvalidAttributeCheckerException, InvalidTriggerException {
        final String RESOURCE_NAME = "myResource";
        final String ATTRIBUTE_NAME = "memory";
        final ManagedResourceGroupWatcherConfiguration watcherConfiguration = ConfigurationManager.createEntityConfiguration(getClass().getClassLoader(), ManagedResourceGroupWatcherConfiguration.class);
        assertNotNull(watcherConfiguration);
        watcherConfiguration.getAttributeCheckers().addAndConsume(ATTRIBUTE_NAME, scriptlet -> {
            final ColoredAttributeChecker checker = new ColoredAttributeChecker();
            checker.setGreenPredicate(new NumberComparatorPredicate(NumberComparatorPredicate.Operator.LESS_THAN, 1000D));
            checker.setYellowPredicate(new IsInRangePredicate(1000D, true, 2000D, true));
            checker.configureScriptlet(scriptlet);
        });
        final UpdatableGroupWatcher watcher = new UpdatableGroupWatcher(watcherConfiguration, null);
        watcher.updateStatus(RESOURCE_NAME, singleton(new Attribute(ATTRIBUTE_NAME, 500)));
        assertTrue(watcher.getStatus() instanceof OkStatus);
        watcher.updateStatus(RESOURCE_NAME, singleton(new Attribute("invalidAttributeName", 100500))); //attribute without checker should be ignored
        assertTrue(watcher.getStatus() instanceof OkStatus);
        watcher.updateStatus(RESOURCE_NAME, singleton(new Attribute(ATTRIBUTE_NAME, 1000)));
        assertTrue(watcher.getStatus() instanceof InvalidAttributeValue);
        watcher.updateStatus(RESOURCE_NAME, singleton(new Attribute(ATTRIBUTE_NAME, 50)));
        assertTrue(watcher.getStatus() instanceof OkStatus);
        watcher.updateStatus(RESOURCE_NAME, new JMException());
        assertTrue(watcher.getStatus() instanceof ResourceInGroupIsNotUnavailable);
        watcher.removeResource(RESOURCE_NAME);
        assertTrue(watcher.getStatus() instanceof OkStatus);
    }
}
