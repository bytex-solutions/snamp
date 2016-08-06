package com.bytex.snamp.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class FutureThreadTest extends Assert {


    @Test
    public void multipleTasksTest() throws ExecutionException, InterruptedException {
        final FutureThread<Boolean> bThread = new FutureThread<>(() -> true);
        final FutureThread<Integer> nThread = new FutureThread<>(() -> {
            Thread.sleep(300);
            return 42;
        });
        final FutureThread<Object> eThread = new FutureThread<>(() -> {
            throw new Exception("Some exception occured.");
        });
        bThread.start();
        nThread.start();
        eThread.start();
        final Boolean bValue = bThread.get();
        assertTrue(bValue);
        assertTrue(bThread.isDone());
        assertFalse(bThread.isCancelled());
        final Integer nValue = nThread.get();
        assertEquals(42, nValue.intValue());
        try{
            eThread.get();
        }
        catch (final ExecutionException e){
            assertNotNull(e.getCause());
            assertEquals("Some exception occured.", e.getCause().getMessage());
        }
    }

    @Test(expected = CancellationException.class)
    public void taskCancellationTest() throws InterruptedException, ExecutionException {
        final FutureThread<String> longRunning = new FutureThread<>(() -> {
            while (true)
                Thread.sleep(100);
        });
        longRunning.start();
        Thread.sleep(100);
        //cancel task
        assertTrue(longRunning.cancel(true));
        assertTrue(longRunning.isCancelled());
        assertTrue(longRunning.isDone());
        assertNotNull(longRunning.get());
    }
}
