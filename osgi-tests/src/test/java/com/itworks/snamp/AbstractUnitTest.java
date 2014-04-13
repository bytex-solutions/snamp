package com.itworks.snamp;

/**
 * Represents a base class for all test classes.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractUnitTest<T> extends AbstractTest {
    /**
     * Represents a reference to the testing class.
     */
    protected final Class<T> testingClass;

    protected AbstractUnitTest(final Class<T> testingClass){
        this.testingClass = testingClass;
    }
}
