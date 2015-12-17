package com.bytex.snamp.internal;

import com.google.common.base.Supplier;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Callable;

import static com.bytex.snamp.internal.Utils.changeFunctionalInterfaceType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class ChangeFunctionalInterfaceTypeTest extends Assert {
    private static final class TestSupplier implements Supplier<Integer>{
        @Override
        public Integer get() {
            return 42;
        }
    }

    @Test
    public void changeInterfaceTypeTest() throws Exception {
        final Callable newInterface =
                changeFunctionalInterfaceType(new TestSupplier(), Supplier.class, Callable.class);
        assertEquals(42, newInterface.call());
    }
}
