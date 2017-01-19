package com.bytex.snamp.configuration;

import org.junit.Assert;
import org.junit.Test;
import static com.bytex.snamp.configuration.impl.SerializableAgentConfiguration.newEntityConfiguration;

/**
 * Represents tests for read-only view of configuration entities.
 */
public final class ReadOnlyViewTest extends Assert {
    @Test(expected = UnsupportedOperationException.class)
    public void eventTest(){
        EventConfiguration config = newEntityConfiguration(EventConfiguration.class);
        assertNotNull(config);
        config.setAlternativeName("myEvent");
        config.setAutomaticallyAdded(true);
        config = config.asReadOnly();
        assertEquals("myEvent", config.getAlternativeName());
        config.setAutomaticallyAdded(false);
    }
}
