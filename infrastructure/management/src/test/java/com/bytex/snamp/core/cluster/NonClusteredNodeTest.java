package com.bytex.snamp.core.cluster;

import com.bytex.snamp.core.ClusterNode;
import com.bytex.snamp.core.IDGenerator;
import com.bytex.snamp.core.ObjectStorage;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class NonClusteredNodeTest extends Assert {
    @Test
    public void idGeneratorTest(){
        final ClusterNode node = new NonClusteredNode();
        final IDGenerator generator = node.queryObject(IDGenerator.class);
        assertNotNull(generator);
        assertEquals(0L, generator.generateID("A"));
        assertEquals(1L, generator.generateID("A"));
        assertEquals(0L, generator.generateID("B"));
    }

    @Test
    public void storageTest(){
        final ClusterNode node = new NonClusteredNode();
        final ObjectStorage storage = node.queryObject(ObjectStorage.class);
        assertNotNull(storage);
        storage.getCollection("A").put("k1", 42L);
        assertEquals(42L, storage.getCollection("A").get("k1"));
        storage.getCollection("B").put("k1", 43L);
        assertEquals(43L, storage.getCollection("B").get("k1"));
    }
}
