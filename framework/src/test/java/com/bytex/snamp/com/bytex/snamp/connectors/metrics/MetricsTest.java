package com.bytex.snamp.com.bytex.snamp.connectors.metrics;

import com.bytex.snamp.concurrent.FutureThread;
import com.bytex.snamp.connectors.metrics.AttributeMetricsWriter;
import com.bytex.snamp.connectors.metrics.MetricsInterval;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MetricsTest extends Assert {
    @Test
    public void concurrentTest2() throws ExecutionException, InterruptedException {
        final AttributeMetricsWriter writer = new AttributeMetricsWriter();
        final int numOfThreads = Runtime.getRuntime().availableProcessors() + 1;
        final Future[] threads = new FutureThread[numOfThreads];
        for(int i = 0; i < numOfThreads; i++)
            threads[i] = FutureThread.start(() -> {
                writer.updateReads();
                writer.updateWrites();
            });
        threads[0].get();
        assertTrue(writer.getNumberOfWrites() > 0);
        assertTrue(writer.getNumberOfReads() > 0);
    }

    @Test
    public void concurrentTest() throws ExecutionException, InterruptedException {
        final AttributeMetricsWriter writer = new AttributeMetricsWriter();
        final int numOfThreads = Runtime.getRuntime().availableProcessors() + 1;
        final Future[] threads = new FutureThread[numOfThreads];
        for(int i = 0; i < numOfThreads; i++)
            threads[i] = FutureThread.start(() -> {
                writer.updateReads();
                writer.updateWrites();
            });
        for(int i = 0; i < numOfThreads; i++)
            threads[i].get();
        assertEquals(numOfThreads, writer.getNumberOfReads());
        assertEquals(numOfThreads, writer.getNumberOfWrites());
        assertEquals(numOfThreads, writer.getNumberOfReads(MetricsInterval.HOUR));
        assertEquals(numOfThreads, writer.getNumberOfWrites(MetricsInterval.HOUR));
        writer.reset();
        assertEquals(0, writer.getNumberOfWrites());
        assertEquals(0, writer.getNumberOfReads());
    }
}
