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
        assertFalse(lazy.get().isPresent());
        assertEquals(BigInteger.TEN, lazy.get(() -> BigInteger.TEN));
        assertNotNull(lazy.get());
        assertEquals(BigInteger.TEN, lazy.get(() -> BigInteger.ONE));
        lazy.remove();
        assertEquals(BigInteger.ZERO, lazy.get(() -> BigInteger.ZERO));
        assertEquals(BigInteger.ZERO, lazy.get().orElseThrow(AssertionError::new));
    }

    @Test
    public void softReferenceTest() throws Exception{
        final LazySoftReference<BigInteger> lazy = new LazySoftReference<>();
        assertFalse(lazy.get().isPresent());
        assertEquals(BigInteger.TEN, lazy.get(() -> BigInteger.TEN));
        assertNotNull(lazy.get());
        assertEquals(BigInteger.TEN, lazy.get(() -> BigInteger.ONE));
        lazy.remove();
        assertEquals(BigInteger.ZERO, lazy.get(() -> BigInteger.ZERO));
        assertEquals(BigInteger.ZERO, lazy.get().orElseThrow(AssertionError::new));
    }
}
