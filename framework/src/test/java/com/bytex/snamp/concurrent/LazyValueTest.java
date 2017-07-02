package com.bytex.snamp.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

/**
 * Represents tests for {@link LazyStrongReference} and {@link LazySoftReference}.
 */
public final class LazyValueTest extends Assert {
    @Test
    public void strongReferenceTest() throws Exception{
        final LazyStrongReference<BigInteger> lazy = new LazyStrongReference<>();
        assertNull(lazy.get());
        assertEquals(BigInteger.TEN, lazy.lazyGet(() -> BigInteger.TEN));
        assertNotNull(lazy.get());
        assertEquals(BigInteger.TEN, lazy.lazyGet(() -> BigInteger.ONE));
        lazy.reset();
        assertEquals(BigInteger.ZERO, lazy.lazyGet(() -> BigInteger.ZERO));
        assertEquals(BigInteger.ZERO, lazy.get());
    }

    @Test
    public void softReferenceTest() throws Exception{
        final LazySoftReference<BigInteger> lazy = new LazySoftReference<>();
        assertNull(lazy.get());
        assertEquals(BigInteger.TEN, lazy.lazyGet(() -> BigInteger.TEN));
        assertNotNull(lazy.get());
        assertEquals(BigInteger.TEN, lazy.lazyGet(() -> BigInteger.ONE));
        lazy.reset();
        assertEquals(BigInteger.ZERO, lazy.lazyGet(() -> BigInteger.ZERO));
        assertEquals(BigInteger.ZERO, lazy.get().get());
    }
}
