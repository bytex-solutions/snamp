package com.itworks.snamp.testing;

import com.itworks.snamp.core.RichLogicalOperation;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.lang.reflect.Array;

/**
 * Represents a base class for all SNAMP-specific tests.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractTest extends Assert {

    protected static class TestLogicalOperation extends RichLogicalOperation {
        private static final String TEST_NAME_PROPERTY = "testName";

        protected TestLogicalOperation(final String operationName,
                                     final TestName testName,
                                     final String propertyName,
                                     final Object propertyValue){
            super(operationName,
                    TEST_NAME_PROPERTY, testName.getMethodName(),
                    propertyName, propertyValue);
        }
    }

    @SafeVarargs
    protected static <T> T[] concat(final T[] array1, final T... array2){
        @SuppressWarnings("unchecked")
        final T[] joinedArray = (T[]) Array.newInstance(array1.getClass().getComponentType(), array1.length + array2.length);
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    protected static void assertArrayEquals(final Object expected, final Object actual){
        assertEquals(Array.getLength(expected), Array.getLength(actual));
        for(int i = 0; i < Array.getLength(expected); i++)
            assertEquals(Array.get(expected, i), Array.get(actual, i));
    }

    @Rule
    public final TestName testName = new TestName();
}
