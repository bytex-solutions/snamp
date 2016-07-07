package com.bytex.snamp;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Supplier;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ExceptionalCallableTest extends Assert {
    private static final class IntegerSupplier implements Supplier<Integer>{
        @Override
        public Integer get() {
            return 10;
        }
    }

    @Test
    public void fromSupplierTest(){
        final ExceptionalCallable<String, ExceptionPlaceholder> callable = ExceptionalCallable.fromSupplier(() -> "Hello");
        assertEquals("Hello", callable.call());
    }

    @Test
    public void fromSupplierTest2(){
        final ExceptionalCallable<Integer, ExceptionPlaceholder> callable = ExceptionalCallable.fromSupplier(new IntegerSupplier());
        assertEquals(Integer.valueOf(10), callable.call());
    }
}
