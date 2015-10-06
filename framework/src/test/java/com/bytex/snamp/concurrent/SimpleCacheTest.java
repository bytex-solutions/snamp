package com.bytex.snamp.concurrent;

import com.bytex.snamp.ExceptionPlaceholder;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents simple unit tests for {@link SimpleCache}.
 */
public final class SimpleCacheTest extends Assert {

    @Test
    public void initTest(){
        final SimpleCache<AtomicLong, String, ExceptionPlaceholder> cache = new SimpleCache<AtomicLong, String, ExceptionPlaceholder>() {
            @Override
            protected String init(final AtomicLong initCounter) {
                initCounter.incrementAndGet();
                return "Hello, world";
            }
        };
        final AtomicLong initCounter = new AtomicLong(0L);
        assertFalse(cache.isInitialized());
        cache.get(initCounter);
        assertTrue(cache.isInitialized());
        cache.get(initCounter);
        cache.get(initCounter);
        assertEquals(1L, initCounter.get());
        assertEquals("Hello, world", cache.getIfPresent());
    }
}
