package com.itworks.snamp.testing.jmx;

import com.itworks.snamp.jmx.CompositeDataBuilder;
import com.itworks.snamp.testing.AbstractUnitTest;
import org.junit.Test;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;

import static com.itworks.snamp.jmx.CompositeDataUtils.*;


/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class CompositeDataBuilderTest extends AbstractUnitTest<CompositeDataBuilder> {
    @Test
    public void builderTest() throws OpenDataException {
        final CompositeData dict = new CompositeDataBuilder("map", "description")
            .putBool("boolItem", "description", true)
            .putString("stringItem", "description", "Hello, world!")
            .putInt("intItem", "description", 42)
            .build();
        assertTrue(getBoolean(dict, "boolItem", false));
        assertEquals("Hello, world!", getString(dict, "stringItem", ""));
        assertEquals(42, getInteger(dict, "intItem", 0));
    }
}
