package com.itworks.snamp.testing;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Represents a base class for all test classes.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractUnitTest<T> extends AbstractTest {

    @SuppressWarnings("unchecked")
    protected final Class<T> getTestingClass(){
        final Type superclass = getClass().getGenericSuperclass();
        return (Class<T>)((ParameterizedType) superclass).getActualTypeArguments()[0];
    }
}
