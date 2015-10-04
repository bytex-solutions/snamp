package com.bytex.snamp;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SwitchTest extends Assert {

    @Test
    public void simpleTest(){
        @SuppressWarnings("ConstantConditions")
        final boolean result = new Switch<Long, Boolean>()
                .equals(42L, Boolean.FALSE)
                .equals(43L, Boolean.TRUE)
                .apply(43L);
        assertTrue(result);
    }

    @Test
    public void parallelTest() throws ExecutionException, InterruptedException {
        final Executor exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        final boolean result = new Switch<Long, Boolean>()
                .equals(42L, Boolean.FALSE)
                .equals(43L, Boolean.FALSE)
                .equals(44L, Boolean.FALSE)
                .equals(45L, Boolean.FALSE)
                .equals(46L, Boolean.FALSE)
                .equals(47L, Boolean.FALSE)
                .equals(48L, Boolean.FALSE)
                .equals(49L, Boolean.FALSE)
                .equals(50L, Boolean.FALSE)
                .equals(51L, Boolean.FALSE)
                .equals(52L, Boolean.FALSE)
                .equals(53L, Boolean.TRUE)
                .defaultCase(Boolean.FALSE)
                .apply(53L, exec);
        assertTrue(result);
    }
}
