package com.bytex.snamp;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class StringAppenderTest extends Assert {
    @Test
    public void concatTest(){
        String value = StringAppender.concat("Hello", ", ", "world!");
        assertEquals("Hello, world!", value);
        value = StringAppender.concat("", "Hello", "");
        assertEquals("Hello", value);
    }
}
