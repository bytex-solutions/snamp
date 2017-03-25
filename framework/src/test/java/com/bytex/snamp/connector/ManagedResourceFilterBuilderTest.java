package com.bytex.snamp.connector;

import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Filter;

/**
 * Represents tests for {@link ManagedResourceFilterBuilder}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ManagedResourceFilterBuilderTest extends Assert {
    @Test
    public void emptyFilter(){
        final String filter = new ManagedResourceFilterBuilder().toString();
        assertNotNull(filter);
        assertFalse(filter.isEmpty());
        assertNotNull(new ManagedResourceFilterBuilder().get());
    }

    @Test
    public void filterConstructedFromClient(){
        final String filter = ManagedResourceConnectorClient.filterBuilder().toString();
        assertNotNull(filter);
        assertFalse(filter.isEmpty());
        assertNotNull(ManagedResourceConnectorClient.filterBuilder().get());
    }

    @Test
    public void withUserFilter(){
        final Filter filter = ManagedResourceConnectorClient.filterBuilder().get("(&(a=b)(c=d))");
        assertNotNull(filter);
    }

    @Test
    public void withFilterSetters(){
        final Filter filter = new ManagedResourceFilterBuilder()
                .setConnectorType("jmx")
                .setGroupName("myGroup")
                .get();
        assertNotNull(filter);
    }
}
