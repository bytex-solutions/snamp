package com.bytex.snamp;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class ConvertTest extends Assert {
    @Test
    public void toLongConversion(){
        assertEquals(10, Convert.toLong("10").orElseThrow(AssertionError::new));
        assertEquals(20, Convert.toLong(20.12D).orElseThrow(AssertionError::new));
    }

    @Test
    public void toDurationConversion(){
        final Duration TWO_SECONDS = Duration.ofSeconds(2);
        assertEquals(TWO_SECONDS, Convert.toDuration(TWO_SECONDS.toString()).orElseThrow(AssertionError::new));
        assertEquals(TWO_SECONDS, Convert.toDuration(TWO_SECONDS.toNanos()).orElseThrow(AssertionError::new));
    }
}
