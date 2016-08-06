package com.bytex.snamp.core;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class DistributedServicesTest extends Assert {
    @Test
    public void idGeneratorTest(){
        assertEquals(0L, DistributedServices.getProcessLocalCounterGenerator("gen1").increment());
        assertEquals(1L, DistributedServices.getProcessLocalCounterGenerator("gen1").increment());
        assertEquals(0L, DistributedServices.getProcessLocalCounterGenerator("gen2").increment());
    }

    @Test
    public void storageTest(){
        DistributedServices.getProcessLocalStorage("collection1").put("k1", 42L);
        DistributedServices.getProcessLocalStorage("collection2").put("k1", 43L);
        assertEquals(42L, DistributedServices.getProcessLocalStorage("collection1").get("k1"));
        assertEquals(43L, DistributedServices.getProcessLocalStorage("collection2").get("k1"));
    }
}
