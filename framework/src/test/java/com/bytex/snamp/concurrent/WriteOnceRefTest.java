package com.bytex.snamp.concurrent;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class WriteOnceRefTest extends Assert {
    @Test
    public void basicTest(){
        final WriteOnceRef<String> str = new WriteOnceRef<>("");
        assertEquals("", str.get());
        assertFalse(str.isLocked());
        assertTrue(str.set("Frank Underwood"));
        assertEquals("Frank Underwood", str.get());
        assertFalse(str.set(""));
    }
}
