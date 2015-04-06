package com.itworks.snamp.concurrent;

import com.itworks.snamp.ExceptionPlaceholder;
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
        cache.get(initCounter);
        cache.get(initCounter);
        cache.get(initCounter);
        assertEquals(1L, initCounter.get());
    }
}
