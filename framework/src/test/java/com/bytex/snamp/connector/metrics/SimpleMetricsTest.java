package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SimpleMetricsTest extends Assert {
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
        final ExecutorService executor = Executors.newFixedThreadPool(numOfThreads);
        Future<?> firstTask = null;
        for(int i = 0; i < numOfThreads; i++) {
            final Future<?> f = executor.submit(() -> {
                writer.updateReads();
                writer.updateWrites();
            });
            if(firstTask == null)
                firstTask = f;
        }
        assertNotNull(firstTask);
        firstTask.get();
        assertTrue(writer.writes().getTotalRate() > 0);
        assertTrue(writer.reads().getTotalRate() > 0);
    }

    @Test
    public void concurrentTest() throws ExecutionException, InterruptedException {
        final AttributeMetricRecorder writer = new AttributeMetricRecorder();
        final int numOfThreads = Runtime.getRuntime().availableProcessors() + 1;
        final ExecutorService executor = Executors.newFixedThreadPool(numOfThreads);
        for (int i = 0; i < numOfThreads; i++)
            executor.submit(() -> {
                writer.updateReads();
                writer.updateWrites();
            });
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
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
        final TimeRecorder writer = new TimeRecorder("testMetrics");
        writer.accept(Duration.ofMillis(450));
        writer.accept(Duration.ofMillis(500));
        writer.accept(Duration.ofMillis(1500));
        //timing
        assertEquals(Duration.ofMillis(1500), writer.getLastValue());
        assertEquals(500, writer.getQuantile(0.5D).toMillis());
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

    @Test
    public void rateSerializationTest() throws IOException {
        RateRecorder writer = new RateRecorder("testMetrics");
        writer.mark();
        writer.mark();
        assertEquals(2, writer.getTotalRate());
        final byte[] serializationData = IOUtils.serialize(writer);
        writer = IOUtils.deserialize(serializationData, RateRecorder.class);
        assertEquals(2, writer.getTotalRate());
    }

    @Test
    public void timingSerializationTest() throws IOException {
        TimeRecorder writer = new TimeRecorder("testMetrics");
        writer.accept(Duration.ofMillis(450));
        writer.accept(Duration.ofMillis(500));
        writer.accept(Duration.ofMillis(1500));
        final byte[] serializationData = IOUtils.serialize(writer);
        writer = IOUtils.deserialize(serializationData, TimeRecorder.class);
        assertEquals(Duration.ofMillis(1500), writer.getLastValue());
        assertEquals(500, writer.getQuantile(0.5D).toMillis());
        assertEquals(Duration.ofMillis(450), writer.getMinValue());
        assertEquals(Duration.ofMillis(1500), writer.getMaxValue());
    }

    @Test
    public void ratedFlagSerializationTest() throws IOException {
        RatedFlagRecorder recorder = new RatedFlagRecorder("testMetrics");
        recorder.writeValue(true);
        recorder.writeValue(true);
        recorder.writeValue(false);
        final byte[] serializationData = IOUtils.serialize(recorder);
        recorder = IOUtils.deserialize(serializationData, RatedFlagRecorder.class);
        assertEquals(2, recorder.getTotalCount(true));
        assertEquals(1, recorder.getTotalCount(false));
        assertEquals(3, recorder.getTotalRate());
    }

    @Test
    public void gaugeFpTest(){
        final GaugeFPRecorder writer = new GaugeFPRecorder("testGauge");
        writer.accept(10D);
        writer.accept(20D);
        writer.accept(30D);
        writer.accept(5D);
        writer.accept(15D);
        writer.accept(16D);
        assertEquals(30D, writer.getMaxValue(), 0.1D);
        assertEquals(5D, writer.getMinValue(), 0.1D);
        assertEquals(16D, writer.getLastValue(), 0.1D);
        assertEquals(19.6D, writer.getQuantile(0.7), 0.1D);
    }

    @Test
    public void gaugeFpLoadTest(){
        final GaugeFPRecorder writer = new GaugeFPRecorder("testGauge");
        final Random rnd = new Random(42L);
        final long nanos = System.nanoTime();
        for(int i = 0; i < 100000; i++)
            writer.accept(rnd.nextDouble());
        System.out.println(Duration.ofNanos(System.nanoTime() - nanos));
        System.out.println(writer.getMaxValue());
        System.out.println(writer.getMinValue());
    }

    @Test
    public void gauge64Test(){
        final Gauge64Recorder writer = new Gauge64Recorder("testGauge");
        writer.accept(10L);
        writer.accept(20L);
        writer.accept(30L);
        writer.accept(5L);
        writer.accept(15L);
        writer.accept(16L);
        assertEquals(30L, writer.getMaxValue());
        assertEquals(5L, writer.getMinValue());
        assertEquals(16L, writer.getLastValue());
        assertEquals(19.6D, writer.getQuantile(0.7), 0.1D);
    }

    @Test
    public void stringGaugeTest(){
        final StringGaugeRecorder writer = new StringGaugeRecorder("testGauge");
        writer.accept("a");
        writer.accept("b");
        writer.accept("ab");
        assertEquals("ab", writer.getLastValue());
        assertEquals("b", writer.getMaxValue());
        assertEquals("a", writer.getMinValue());
    }

    @Test
    public void stringGaugeSerializationTest() throws IOException {
        RatedStringGaugeRecorder recorder = new RatedStringGaugeRecorder("testGauge");
        recorder.accept("a");
        recorder.accept("b");
        recorder.accept("ab");
        final byte[] serializationData = IOUtils.serialize(recorder);
        recorder = IOUtils.deserialize(serializationData, RatedStringGaugeRecorder.class);
        assertEquals("ab", recorder.getLastValue());
        assertEquals("b", recorder.getMaxValue());
        assertEquals("a", recorder.getMinValue());
        assertEquals(3, recorder.getTotalRate());
    }

    @Test
    public void gaugeFpSerializationTest() throws IOException {
        RatedGauge64Recorder recorder = new RatedGauge64Recorder("testGauge");
        recorder.accept(10L);
        recorder.accept(20L);
        recorder.accept(30L);
        recorder.accept(5L);
        recorder.accept(15L);
        recorder.accept(16L);
        final byte[] serializationData = IOUtils.serialize(recorder);
        recorder = IOUtils.deserialize(serializationData, RatedGauge64Recorder.class);
        assertEquals(30L, recorder.getMaxValue());
        assertEquals(5L, recorder.getMinValue());
        assertEquals(16L, recorder.getLastValue());
        assertEquals(19.6D, recorder.getQuantile(0.7), 0.1D);
        assertEquals(6, recorder.getTotalRate());
    }
}
