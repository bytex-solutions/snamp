package com.bytex.snamp.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

/**
 * Represents tests for {@link LazyValue}.
 */
public final class LazyValueTest extends Assert {
    @Test(expected = IllegalStateException.class)
    public void illegalStateTest(){
        final LazyValue<BigInteger> lazy = LazyContainers.NORMAL.create(() -> BigInteger.TEN);
        assertFalse(lazy.isActivated());
        lazy.getIfActivated();
    }

    @Test
    public void strongReferenceTest(){
        final LazyValue<BigInteger> lazy = LazyContainers.NORMAL.create(() -> BigInteger.TEN);
        assertFalse(lazy.isActivated());
        assertEquals(BigInteger.TEN, lazy.get());
        assertTrue(lazy.isActivated());
        assertEquals(BigInteger.TEN, lazy.get(() -> BigInteger.ONE));
        lazy.reset(value -> assertEquals(BigInteger.TEN, value));
        assertFalse(lazy.isActivated());
        assertEquals(BigInteger.ZERO, lazy.get(() -> BigInteger.ZERO));
        assertEquals(BigInteger.ZERO, lazy.get());
    }

    @Test
    public void softReferenceTest(){
        final LazyValue<BigInteger> lazy = LazyContainers.SOFT_REFERENCED.create(() -> BigInteger.TEN);
        assertFalse(lazy.isActivated());
        assertEquals(BigInteger.TEN, lazy.get());
        assertTrue(lazy.isActivated());
        assertEquals(BigInteger.TEN, lazy.get(() -> BigInteger.ONE));
        lazy.reset(value -> assertEquals(BigInteger.TEN, value));
        assertFalse(lazy.isActivated());
        assertEquals(BigInteger.ZERO, lazy.get(() -> BigInteger.ZERO));
        assertEquals(BigInteger.ZERO, lazy.get());
    }
}
