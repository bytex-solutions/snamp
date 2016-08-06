package com.bytex.snamp.adapters;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class ResourceAdapterUpdateManagerTest extends Assert {
    @Test
    public void stateTest() throws Exception {
        try(final ResourceAdapterUpdateManager manager = new ResourceAdapterUpdateManager("dummAdapterInstance", 2000)){
            //changes the state of the manager
            manager.beginUpdate(null);
            Thread.sleep(1500);
            assertTrue(manager.isUpdating());
            Thread.sleep(1500);
            assertFalse(manager.isUpdating());
            manager.beginUpdate(null);
            assertTrue(manager.isUpdating());
            Thread.sleep(1500);
            assertTrue(manager.isUpdating());
            manager.beginUpdate(null);
            Thread.sleep(1500);
            assertTrue(manager.isUpdating());
        }
    }
}
