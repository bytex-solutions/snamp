package com.bytex.snamp.testing;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.lang.reflect.Array;

/**
 * Represents a base class for all SNAMP-specific tests.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class AbstractTest extends Assert {

    protected static void assertArrayEquals(final Object expected, final Object actual){
        assertEquals(Array.getLength(expected), Array.getLength(actual));
        for(int i = 0; i < Array.getLength(expected); i++)
            assertEquals(Array.get(expected, i), Array.get(actual, i));
    }

    @Rule
    public final TestName testName = new TestName();
}
