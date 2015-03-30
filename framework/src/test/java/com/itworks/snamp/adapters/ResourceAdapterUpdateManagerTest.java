package com.itworks.snamp.adapters;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class ResourceAdapterUpdateManagerTest extends Assert {
    private static final class DummyUpdateManager extends ResourceAdapterUpdateManager {

        private DummyUpdateManager() {
            super("dummAdapterInstance", 2000);
        }

        @Override
        public void endUpdateCore() {

        }

        @Override
        protected void beginUpdateCore() {

        }
    }

    @Test
    public void stateTest() throws Exception {
        try(final ResourceAdapterUpdateManager manager = new DummyUpdateManager()){
            //changes the state of the manager
            manager.beginUpdate();
            Thread.sleep(1500);
            assertTrue(manager.isUpdating());
            Thread.sleep(1500);
            assertFalse(manager.isUpdating());
            manager.beginUpdate();
            assertTrue(manager.isUpdating());
            Thread.sleep(1500);
            assertTrue(manager.isUpdating());
            manager.beginUpdate();
            Thread.sleep(1500);
            assertTrue(manager.isUpdating());
        }
    }
}
