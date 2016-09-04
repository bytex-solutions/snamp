package com.bytex.snamp.metrics;

import com.bytex.snamp.concurrent.FutureThread;
import com.bytex.snamp.connector.metrics.AttributeMetricRecorder;
import com.bytex.snamp.connector.metrics.MetricsInterval;
import com.bytex.snamp.connector.metrics.TimingRecorder;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
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
        final AttributeMetricRecorder writer = new AttributeMetricRecorder();
        writer.updateReads();
        assertEquals(1, writer.getTotalNumberOfReads());
        assertEquals(1, writer.getLastNumberOfReads(MetricsInterval.DAY));
        assertEquals(1, writer.getLastNumberOfReads(MetricsInterval.HOUR));
        assertEquals(1, writer.getLastNumberOfReads(MetricsInterval.MINUTE));
        assertEquals(1, writer.getLastNumberOfReads(MetricsInterval.SECOND));
        Thread.sleep(1100);
        assertEquals(1, writer.getTotalNumberOfReads());
        assertEquals(1, writer.getLastNumberOfReads(MetricsInterval.DAY));
        assertEquals(1, writer.getLastNumberOfReads(MetricsInterval.HOUR));
        assertEquals(1, writer.getLastNumberOfReads(MetricsInterval.MINUTE));
        assertEquals(0, writer.getLastNumberOfReads(MetricsInterval.SECOND));
        writer.reset();
        assertEquals(0, writer.getTotalNumberOfReads());
        assertEquals(0, writer.getLastNumberOfReads(MetricsInterval.DAY));
        assertEquals(0, writer.getLastNumberOfReads(MetricsInterval.HOUR));
        assertEquals(0, writer.getLastNumberOfReads(MetricsInterval.MINUTE));
        assertEquals(0, writer.getLastNumberOfReads(MetricsInterval.SECOND));
    }

    @Test
    public void numberOfWritesTest() throws InterruptedException {
        final AttributeMetricRecorder writer = new AttributeMetricRecorder();
        writer.updateWrites();
        assertEquals(1, writer.getTotalNumberOfWrites());
        assertEquals(1, writer.getLastNumberOfWrites(MetricsInterval.DAY));
        assertEquals(1, writer.getLastNumberOfWrites(MetricsInterval.HOUR));
        assertEquals(1, writer.getLastNumberOfWrites(MetricsInterval.MINUTE));
        assertEquals(1, writer.getLastNumberOfWrites(MetricsInterval.SECOND));
        Thread.sleep(1100);
        assertEquals(1, writer.getTotalNumberOfWrites());
        assertEquals(1, writer.getLastNumberOfWrites(MetricsInterval.DAY));
        assertEquals(1, writer.getLastNumberOfWrites(MetricsInterval.HOUR));
        assertEquals(1, writer.getLastNumberOfWrites(MetricsInterval.MINUTE));
        assertEquals(0, writer.getLastNumberOfWrites(MetricsInterval.SECOND));
        writer.reset();
        assertEquals(0, writer.getTotalNumberOfWrites());
        assertEquals(0, writer.getLastNumberOfWrites(MetricsInterval.DAY));
        assertEquals(0, writer.getLastNumberOfWrites(MetricsInterval.HOUR));
        assertEquals(0, writer.getLastNumberOfWrites(MetricsInterval.MINUTE));
        assertEquals(0, writer.getLastNumberOfWrites(MetricsInterval.SECOND));
    }

    @Test
    public void concurrentTest2() throws ExecutionException, InterruptedException {
        final AttributeMetricRecorder writer = new AttributeMetricRecorder();
        final int numOfThreads = Runtime.getRuntime().availableProcessors() + 1;
        final Future[] threads = new FutureThread[numOfThreads];
        for(int i = 0; i < numOfThreads; i++)
            threads[i] = FutureThread.start(() -> {
                writer.updateReads();
                writer.updateWrites();
            });
        threads[0].get();
        assertTrue(writer.getTotalNumberOfWrites() > 0);
        assertTrue(writer.getTotalNumberOfReads() > 0);
    }

    @Test
    public void concurrentTest() throws ExecutionException, InterruptedException {
        final AttributeMetricRecorder writer = new AttributeMetricRecorder();
        final int numOfThreads = Runtime.getRuntime().availableProcessors() + 1;
        final Future[] threads = new FutureThread[numOfThreads];
        for(int i = 0; i < numOfThreads; i++)
            threads[i] = FutureThread.start(() -> {
                writer.updateReads();
                writer.updateWrites();
            });
        for(int i = 0; i < numOfThreads; i++)
            threads[i].get();
        assertEquals(numOfThreads, writer.getTotalNumberOfReads());
        assertEquals(numOfThreads, writer.getTotalNumberOfWrites());
        assertEquals(numOfThreads, writer.getLastNumberOfReads(MetricsInterval.HOUR));
        assertEquals(numOfThreads, writer.getLastNumberOfWrites(MetricsInterval.HOUR));
        writer.reset();
        assertEquals(0, writer.getTotalNumberOfWrites());
        assertEquals(0, writer.getTotalNumberOfReads());
    }

    @Test
    public void rateAndTimingTest(){
        final TimingRecorder writer = new TimingRecorder("testMetrics");
        writer.update(Duration.ofMillis(450));
        writer.update(Duration.ofMillis(500));
        writer.update(Duration.ofMillis(1500));
        //timing
        assertEquals(Duration.ofMillis(1500), writer.getLastValue());
        assertEquals(816, writer.getQuantile(0.5D).toMillis());
        assertEquals(Duration.ofMillis(450), writer.getMinValue());
        assertEquals(Duration.ofMillis(1500), writer.getMaxValue());
    }
}
