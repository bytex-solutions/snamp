package com.bytex.snamp.metrics;

import com.bytex.snamp.concurrent.FutureThread;
import com.bytex.snamp.connector.metrics.AttributeMetricWriter;
import com.bytex.snamp.connector.metrics.MetricsInterval;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MetricTest extends Assert {
    @Test
    public void numberOfReadsTest() throws InterruptedException {
        final AttributeMetricWriter writer = new AttributeMetricWriter();
        writer.updateReads();
        assertEquals(1, writer.getNumberOfReads());
        assertEquals(1, writer.getNumberOfReads(MetricsInterval.DAY));
        assertEquals(1, writer.getNumberOfReads(MetricsInterval.HOUR));
        assertEquals(1, writer.getNumberOfReads(MetricsInterval.MINUTE));
        assertEquals(1, writer.getNumberOfReads(MetricsInterval.SECOND));
        Thread.sleep(1100);
        assertEquals(1, writer.getNumberOfReads());
        assertEquals(1, writer.getNumberOfReads(MetricsInterval.DAY));
        assertEquals(1, writer.getNumberOfReads(MetricsInterval.HOUR));
        assertEquals(1, writer.getNumberOfReads(MetricsInterval.MINUTE));
        assertEquals(0, writer.getNumberOfReads(MetricsInterval.SECOND));
        writer.reset();
        assertEquals(0, writer.getNumberOfReads());
        assertEquals(0, writer.getNumberOfReads(MetricsInterval.DAY));
        assertEquals(0, writer.getNumberOfReads(MetricsInterval.HOUR));
        assertEquals(0, writer.getNumberOfReads(MetricsInterval.MINUTE));
        assertEquals(0, writer.getNumberOfReads(MetricsInterval.SECOND));
    }

    @Test
    public void numberOfWritesTest() throws InterruptedException {
        final AttributeMetricWriter writer = new AttributeMetricWriter();
        writer.updateWrites();
        assertEquals(1, writer.getNumberOfWrites());
        assertEquals(1, writer.getNumberOfWrites(MetricsInterval.DAY));
        assertEquals(1, writer.getNumberOfWrites(MetricsInterval.HOUR));
        assertEquals(1, writer.getNumberOfWrites(MetricsInterval.MINUTE));
        assertEquals(1, writer.getNumberOfWrites(MetricsInterval.SECOND));
        Thread.sleep(1100);
        assertEquals(1, writer.getNumberOfWrites());
        assertEquals(1, writer.getNumberOfWrites(MetricsInterval.DAY));
        assertEquals(1, writer.getNumberOfWrites(MetricsInterval.HOUR));
        assertEquals(1, writer.getNumberOfWrites(MetricsInterval.MINUTE));
        assertEquals(0, writer.getNumberOfWrites(MetricsInterval.SECOND));
        writer.reset();
        assertEquals(0, writer.getNumberOfWrites());
        assertEquals(0, writer.getNumberOfWrites(MetricsInterval.DAY));
        assertEquals(0, writer.getNumberOfWrites(MetricsInterval.HOUR));
        assertEquals(0, writer.getNumberOfWrites(MetricsInterval.MINUTE));
        assertEquals(0, writer.getNumberOfWrites(MetricsInterval.SECOND));
    }

    @Test
    public void concurrentTest2() throws ExecutionException, InterruptedException {
        final AttributeMetricWriter writer = new AttributeMetricWriter();
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
        final AttributeMetricWriter writer = new AttributeMetricWriter();
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
