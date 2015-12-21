package com.bytex.snamp.metrics;

import com.bytex.snamp.connectors.metrics.AttributeMetricsWriter;
import com.bytex.snamp.connectors.metrics.MetricsInterval;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link com.bytex.snamp.connectors.metrics.AttributeMetricsWriter}.
 */
public final class AttributeMetricsWriterTest extends Assert {
    @Test
    public void numberOfReadsTest() throws InterruptedException {
        final AttributeMetricsWriter writer = new AttributeMetricsWriter();
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
        final AttributeMetricsWriter writer = new AttributeMetricsWriter();
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
}
