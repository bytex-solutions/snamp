package com.itworks.snamp.testing;

import com.itworks.snamp.concurrent.FutureThread;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class FutureThreadTest extends AbstractUnitTest<FutureThread> {


    @Test
    public final void multipleTasksTest() throws ExecutionException, InterruptedException {
        final FutureThread<Boolean> bThread = new FutureThread<>(new Callable<Boolean>() {
            @Override
            public final Boolean call() {
                return true;
            }
        });
        final FutureThread<Integer> nThread = new FutureThread<>(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception{
                Thread.sleep(300);
                return 42;
            }
        });
        final FutureThread<Object> eThread = new FutureThread<>(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                throw new Exception("Some exception occured.");
            }
        });
        bThread.start();
        nThread.start();
        eThread.start();
        final Boolean bValue = bThread.get();
        assertTrue(bValue.booleanValue());
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

    @Test
    public final void taskCancellationTest() throws InterruptedException, ExecutionException {
        final FutureThread<String> longRunning = new FutureThread<>(new Callable<String>() {
            @Override
            public String call() {
                while (!Thread.currentThread().isInterrupted())
                    Thread.yield();
                return "interrupted";
            }
        });
        longRunning.start();
        Thread.sleep(100);
        //cancel task
        assertTrue(longRunning.cancel(true));
        assertEquals("interrupted", longRunning.get());
        assertTrue(longRunning.isCancelled());
        assertTrue(longRunning.isDone());
    }
}
