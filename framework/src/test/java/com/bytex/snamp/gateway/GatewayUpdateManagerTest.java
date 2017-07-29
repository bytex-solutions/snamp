package com.bytex.snamp.gateway;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public class GatewayUpdateManagerTest extends Assert {
    @Test
    public void stateTest() throws Exception {
        try(final GatewayUpdateManager manager = new GatewayUpdateManager("dummyGatewayInstance", 2000)){
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
