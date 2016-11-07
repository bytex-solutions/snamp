package com.bytex.snamp;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ConvertTest extends Assert {
    @Test
    public void toLongConversion(){
        assertEquals(10, Convert.toLong("10"));
        assertEquals(20, Convert.toLong(20.12D));
    }

    @Test
    public void toDurationConversion(){
        final Duration TWO_SECONDS = Duration.ofSeconds(2);
        assertEquals(TWO_SECONDS, Convert.toDuration(TWO_SECONDS.toString()));
        assertEquals(TWO_SECONDS, Convert.toDuration(TWO_SECONDS.toNanos()));
    }
}
