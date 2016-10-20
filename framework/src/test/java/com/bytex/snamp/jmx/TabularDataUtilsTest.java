package com.bytex.snamp.jmx;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;
import java.util.Map;

/**
 * Represents a set of unit tests for {@link TabularDataUtils} class.
 */
public final class TabularDataUtilsTest extends Assert {
    @Test
    public void makeKeyValuePairs() throws OpenDataException {
        final TabularType type = new KeyValueTypeBuilder<String, Integer>()
                .setKeyColumn("key", "descr", SimpleType.STRING)
                .setValueColumn("value", "descr", SimpleType.INTEGER)
                .call();
        assertNotNull(type);
        assertEquals(1, type.getIndexNames().size());
        assertTrue(type.getIndexNames().contains("key"));
        final TabularData data = TabularDataUtils.makeKeyValuePairs(type, ImmutableMap.of("key1", 10, "key2", 20, "key3", 30));
        assertEquals(3, data.size());
        final Map<?, ?> mp = TabularDataUtils.makeKeyValuePairs(data);
        assertEquals(3, mp.size());
        assertEquals(10, mp.get("key1"));
        assertEquals(20, mp.get("key2"));
        assertEquals(30, mp.get("key3"));
    }
}
