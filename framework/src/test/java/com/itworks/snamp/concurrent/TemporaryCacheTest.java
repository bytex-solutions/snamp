package com.itworks.snamp.concurrent;

import com.itworks.snamp.ExceptionPlaceholder;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents simple unit tests for {@link TemporaryCache}.
 */
public final class TemporaryCacheTest extends Assert {
    @Test
    public void expirationTest() throws InterruptedException {
        final TemporaryCache<AtomicLong, Long, ExceptionPlaceholder> cache = new TemporaryCache<AtomicLong, Long, ExceptionPlaceholder>(100, TimeUnit.MILLISECONDS) {
            @Override
            protected Long init(final AtomicLong initCounter) {
                return initCounter.incrementAndGet();
            }

            @Override
            protected void expire(final AtomicLong releaseCounter, final Long value) {
                releaseCounter.decrementAndGet();
            }
        };
        final AtomicLong initCounter = new AtomicLong(0L);
        cache.get(initCounter);
        cache.get(initCounter);
        cache.get(initCounter);
        assertEquals(1, initCounter.get());
        Thread.sleep(110);
        initCounter.set(0);
        cache.get(initCounter);
        assertEquals(0, initCounter.get());
        Thread.sleep(110);
        initCounter.set(4);
        cache.get(initCounter);
        assertEquals(4, initCounter.get());
    }
}
