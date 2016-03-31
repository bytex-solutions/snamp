package com.bytex.snamp.jmx;

import org.junit.Assert;
import org.junit.Test;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;

import static com.bytex.snamp.jmx.CompositeDataUtils.*;


/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class CompositeDataBuilderTest extends Assert {
    @Test
    public void builderTest() throws OpenDataException {
        final CompositeData dict = new CompositeDataBuilder("map", "description")
            .put("boolItem", "description", true)
            .put("stringItem", "description", "Hello, world!")
            .put("intItem", "description", 42)
            .build();
        assertTrue(getBoolean(dict, "boolItem", false));
        assertEquals("Hello, world!", getString(dict, "stringItem", ""));
        assertEquals(42, getInteger(dict, "intItem", 0));
    }
}
