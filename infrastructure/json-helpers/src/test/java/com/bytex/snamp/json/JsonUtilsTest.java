package com.bytex.snamp.json;

import com.google.common.collect.ImmutableMap;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class JsonUtilsTest extends Assert {
    @Test
    public void toPlainMap(){
        final Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", ImmutableMap.of("key3", "value3", "key4", "value4"));
        final Map<String, String> result = JsonUtils.toPlainMap(map, '.');
        assertNotNull(result);
        assertTrue(result.containsKey("key1"));
        assertTrue(result.containsKey("key2.key3"));
        assertTrue(result.containsKey("key2.key4"));
        assertEquals(3, result.size());
    }

    @Test
    public void serTest() throws IOException {
        final Object value = new ObjectMapper().readTree("1E+2");
        assertNotNull(value);
    }
}
