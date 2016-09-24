package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.concurrent.FutureThread;
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
        assertEquals(1, writer.reads().getMaxRate(MetricsInterval.SECOND));
        Thread.sleep(1100);
        assertEquals(1, writer.reads().getTotalRate());
        assertEquals(1, writer.reads().getLastRate(MetricsInterval.DAY));
        assertEquals(1, writer.reads().getLastRate(MetricsInterval.HOUR));
        assertEquals(1, writer.reads().getLastRate(MetricsInterval.MINUTE));
        assertEquals(0, writer.reads().getMaxRate(MetricsInterval.SECOND));
    }

    @Test
    public void numberOfWritesTest() throws InterruptedException {
        final AttributeMetricRecorder writer = new AttributeMetricRecorder();
        writer.updateWrites();
        assertEquals(1, writer.writes().getTotalRate());
        assertEquals(1, writer.writes().getLastRate(MetricsInterval.DAY));
        assertEquals(1, writer.writes().getLastRate(MetricsInterval.HOUR));
        assertEquals(1, writer.writes().getLastRate(MetricsInterval.MINUTE));
        assertEquals(1, writer.writes().getMaxRate(MetricsInterval.SECOND));
        Thread.sleep(1100);
        assertEquals(1, writer.writes().getTotalRate());
        assertEquals(1, writer.writes().getLastRate(MetricsInterval.DAY));
        assertEquals(1, writer.writes().getLastRate(MetricsInterval.HOUR));
        assertEquals(1, writer.writes().getLastRate(MetricsInterval.MINUTE));
        assertEquals(0, writer.writes().getMaxRate(MetricsInterval.SECOND));
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
        writer.accept(true);
        writer.inverse();
        writer.accept(false);
        assertFalse(writer.getAsBoolean());
        assertEquals(0.5D, writer.getLastRatio(MetricsInterval.SECOND), 0.01D);
    }

    @Test
    public void timingTest(){
        final TimingRecorder writer = new TimingRecorder("testMetrics");
        writer.accept(Duration.ofMillis(450));
        writer.accept(Duration.ofMillis(500));
        writer.accept(Duration.ofMillis(1500));
        //timing
        assertEquals(Duration.ofMillis(1500), writer.getLastValue());
        assertEquals(816, writer.getQuantile(0.5D).toMillis());
        assertEquals(Duration.ofMillis(450), writer.getMinValue());
        assertEquals(Duration.ofMillis(1500), writer.getMaxValue());
    }

    @Test
    public void rateTest() throws InterruptedException {
        final RateRecorder writer = new RateRecorder("testMetrics");
        writer.mark();
        writer.mark();
        Thread.sleep(1001);
        assertEquals(0L, writer.getLastMaxRatePerSecond(MetricsInterval.SECOND));
        assertEquals(2L, writer.getLastMaxRatePerSecond(MetricsInterval.MINUTE));
        assertEquals(2, writer.getMaxRate(MetricsInterval.SECOND));
        assertEquals(0L, writer.getLastRate(MetricsInterval.SECOND));
        writer.mark();
        writer.mark();
        writer.mark();
        assertEquals(3L, writer.getLastMaxRatePerSecond(MetricsInterval.SECOND));
        assertEquals(3L, writer.getLastMaxRatePerSecond(MetricsInterval.MINUTE));
        assertEquals(3L, writer.getMaxRate(MetricsInterval.SECOND));

    }
}
