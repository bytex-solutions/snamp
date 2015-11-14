package com.bytex.snamp.core;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class DistributedServicesTest extends Assert {
    @Test
    public void idGeneratorTest(){
        assertEquals(0L, DistributedServices.getProcessLocalSequenceNumberGenerator("gen1").next());
        assertEquals(1L, DistributedServices.getProcessLocalSequenceNumberGenerator("gen1").next());
        assertEquals(0L, DistributedServices.getProcessLocalSequenceNumberGenerator("gen2").next());
    }

    @Test
    public void storageTest(){
        DistributedServices.getProcessLocalStorage("collection1").put("k1", 42L);
        DistributedServices.getProcessLocalStorage("collection2").put("k1", 43L);
        assertEquals(42L, DistributedServices.getProcessLocalStorage("collection1").get("k1"));
        assertEquals(43L, DistributedServices.getProcessLocalStorage("collection2").get("k1"));
    }
}
