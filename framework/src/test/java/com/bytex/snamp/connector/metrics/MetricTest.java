package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.concurrent.FutureThread;
import com.codahale.metrics.Timer;
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
        assertEquals(1, writer.reads().getTotalRate());
        assertEquals(1, writer.reads().getLastRate(MetricsInterval.DAY));
        assertEquals(1, writer.reads().getLastRate(MetricsInterval.HOUR));
        assertEquals(1, writer.reads().getLastRate(MetricsInterval.MINUTE));
        assertEquals(1, writer.reads().getLastMaxRate(MetricsInterval.SECOND));
        Thread.sleep(1100);
        assertEquals(1, writer.reads().getTotalRate());
        assertEquals(1, writer.reads().getLastRate(MetricsInterval.DAY));
        assertEquals(1, writer.reads().getLastRate(MetricsInterval.HOUR));
        assertEquals(1, writer.reads().getLastRate(MetricsInterval.MINUTE));
        assertEquals(0, writer.reads().getLastMaxRate(MetricsInterval.SECOND));
        Timer t;
    }

    @Test
    public void numberOfWritesTest() throws InterruptedException {
        final AttributeMetricRecorder writer = new AttributeMetricRecorder();
        writer.updateWrites();
        assertEquals(1, writer.writes().getTotalRate());
        assertEquals(1, writer.writes().getLastRate(MetricsInterval.DAY));
        assertEquals(1, writer.writes().getLastRate(MetricsInterval.HOUR));
        assertEquals(1, writer.writes().getLastRate(MetricsInterval.MINUTE));
        assertEquals(1, writer.writes().getLastMaxRate(MetricsInterval.SECOND));
        Thread.sleep(1100);
        assertEquals(1, writer.writes().getTotalRate());
        assertEquals(1, writer.writes().getLastRate(MetricsInterval.DAY));
        assertEquals(1, writer.writes().getLastRate(MetricsInterval.HOUR));
        assertEquals(1, writer.writes().getLastRate(MetricsInterval.MINUTE));
        assertEquals(0, writer.writes().getLastMaxRate(MetricsInterval.SECOND));
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
        assertTrue(writer.writes().getTotalRate() > 0);
        assertTrue(writer.reads().getTotalRate() > 0);
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
        assertEquals(numOfThreads, writer.reads().getTotalRate());
        assertEquals(numOfThreads, writer.writes().getTotalRate());
        assertEquals(numOfThreads, writer.reads().getLastRate(MetricsInterval.MINUTE));
        assertEquals(numOfThreads, writer.writes().getLastRate(MetricsInterval.MINUTE));
        writer.reset();
        assertEquals(0, writer.writes().getTotalRate());
        assertEquals(0, writer.reads().getTotalRate());
    }

    @Test
    public void flagTest(){
        final FlagRecorder writer = new FlagRecorder("testMetrics");
        writer.update(true);
        writer.inverse();
        writer.update(false);
        assertFalse(writer.getAsBoolean());
        assertEquals(0.5D, writer.getLastRatio(MetricsInterval.SECOND), 0.01D);
    }

    @Test
    public void timingTest(){
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
