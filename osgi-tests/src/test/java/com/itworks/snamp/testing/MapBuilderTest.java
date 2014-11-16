package com.itworks.snamp.testing;

import com.itworks.snamp.MapBuilder;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MapBuilderTest extends AbstractUnitTest<MapBuilder> {

    @Test
    public void iteratorTest(){
        final Iterable<Map.Entry<String, Integer>> builder = MapBuilder
                .<String, Integer>create()
                .add("STR 1", 1)
                .add("STR 2", 2);
        final Map<String, Integer> map = new HashMap<>();
        for(final Map.Entry<String, Integer> pair: builder)
            map.put(pair.getKey(), pair.getValue());
        assertEquals(2, map.size());
        assertEquals(new Integer(1), map.get("STR 1"));
        assertEquals(new Integer(2), map.get("STR 2"));
    }

    @Test
    public void buildingTest(){
        final Map<String, Integer> map = MapBuilder
                .<String, Integer>create()
                .add("STR 1", 1)
                .add("STR 2", 2)
                .buildHashMap();
        assertEquals(2, map.size());
        assertEquals(new Integer(1), map.get("STR 1"));
        assertEquals(new Integer(2), map.get("STR 2"));
    }
}
