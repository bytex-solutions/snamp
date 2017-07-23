package com.bytex.snamp.connector;

import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;

/**
 * Represents tests for {@link ManagedResourceSelector}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ManagedResourceSelectorTest extends Assert {
    @Test
    public void emptyFilter(){
        final String filter = new ManagedResourceSelector().toString();
        assertNotNull(filter);
        assertFalse(filter.isEmpty());
        assertNotNull(new ManagedResourceSelector().get());
    }

    @Test
    public void filterConstructedFromClient(){
        final String filter = ManagedResourceConnectorClient.selector().toString();
        assertNotNull(filter);
        assertFalse(filter.isEmpty());
        assertNotNull(ManagedResourceConnectorClient.selector().get());
    }

    @Test
    public void withUserFilter() throws InvalidSyntaxException {
        final Filter filter = ManagedResourceConnectorClient.selector().get("(&(a=b)(c=d))");
        assertNotNull(filter);
    }

    @Test
    public void withFilterSetters(){
        final Filter filter = new ManagedResourceSelector()
                .setConnectorType("jmx")
                .setGroupName("myGroup")
                .get();
        assertNotNull(filter);
    }
}
