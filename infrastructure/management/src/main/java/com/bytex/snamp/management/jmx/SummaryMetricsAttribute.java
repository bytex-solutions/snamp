package com.bytex.snamp.management.jmx;

import com.bytex.snamp.connector.metrics.*;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.CompositeTypeBuilder;
import com.bytex.snamp.management.SummaryMetrics;
import com.google.common.collect.Maps;

import javax.management.openmbean.*;
import java.util.Map;

import static com.bytex.snamp.jmx.OpenMBean.OpenAttribute;

/**
 * Provides global metrics.
 */
final class SummaryMetricsAttribute extends OpenAttribute<CompositeData, CompositeType> {
    private static final String NUM_OF_WRITES = "numberOfAttributeWrites";
    private static final String NUM_OF_WRITES_MINUTE = NUM_OF_WRITES.concat("LastMinute");
    private static final String NUM_OF_WRITES_HOUR = NUM_OF_WRITES.concat("LastHour");
    private static final String NUM_OF_WRITES_DAY = NUM_OF_WRITES.concat("Last24Hours");

    private static final String NUM_OF_READS = "numberOfAttributeReads";
    private static final String NUM_OF_READS_MINUTE = NUM_OF_READS.concat("LastMinute");
    private static final String NUM_OF_READS_HOUR = NUM_OF_READS.concat("LastHour");
    private static final String NUM_OF_READS_DAY = NUM_OF_READS.concat("Last24Hours");

    private static final String NUM_OF_EMITTED = "numberOfEmittedNotifications";
    private static final String NUM_OF_EMITTED_MINUTE = NUM_OF_READS.concat("LastMinute");
    private static final String NUM_OF_EMITTED_HOUR = NUM_OF_READS.concat("LastHour");
    private static final String NUM_OF_EMITTED_DAY = NUM_OF_READS.concat("Last24Hours");

    private static final String NUM_OF_INVOKED = "numberOfOperationInvocations";
    private static final String NUM_OF_INVOKED_MINUTE = NUM_OF_READS.concat("LastMinute");
    private static final String NUM_OF_INVOKED_HOUR = NUM_OF_READS.concat("LastHour");
    private static final String NUM_OF_INVOKED_DAY = NUM_OF_READS.concat("Last24Hours");

    static final CompositeType TYPE = Utils.interfaceStaticInitialize(() -> new CompositeTypeBuilder("Metrics", "Consolidated set of metrics")
            .addItem(NUM_OF_WRITES, "Total number of attribute writes", SimpleType.LONG)
            .addItem(NUM_OF_WRITES_MINUTE, "Number of attribute writes for the last minute", SimpleType.LONG)
            .addItem(NUM_OF_WRITES_HOUR, "Number of attribute writes for the last hour", SimpleType.LONG)
            .addItem(NUM_OF_WRITES_DAY, "Number of attribute writes for the last 24 hours", SimpleType.LONG)

            .addItem(NUM_OF_READS, "Total number of attribute reads", SimpleType.LONG)
            .addItem(NUM_OF_READS_MINUTE, "Number of attribute reads for the last minute", SimpleType.LONG)
            .addItem(NUM_OF_READS_HOUR, "Number of attribute reads for the last hour", SimpleType.LONG)
            .addItem(NUM_OF_READS_DAY, "Number of attribute reads for the last 24 hours", SimpleType.LONG)

            .addItem(NUM_OF_EMITTED, "Total number of emitted notifications", SimpleType.LONG)
            .addItem(NUM_OF_EMITTED_MINUTE, "Number of emitted notifications for the last minute", SimpleType.LONG)
            .addItem(NUM_OF_EMITTED_HOUR, "Number of emitted notifications for the last hour", SimpleType.LONG)
            .addItem(NUM_OF_EMITTED_DAY, "Number of emitted notifications for the last day", SimpleType.LONG)

            .addItem(NUM_OF_INVOKED, "Total number of emitted notifications", SimpleType.LONG)
            .addItem(NUM_OF_INVOKED_MINUTE, "Number of emitted notifications for the last minute", SimpleType.LONG)
            .addItem(NUM_OF_INVOKED_HOUR, "Number of emitted notifications for the last hour", SimpleType.LONG)
            .addItem(NUM_OF_INVOKED_DAY, "Number of emitted notifications for the last day", SimpleType.LONG)
            .build());

    private static void collectMetrics(final AttributeMetric metrics, final Map<String, Long> output) {
        if (metrics == null) {
            output.put(NUM_OF_WRITES, 0L);
            output.put(NUM_OF_WRITES_MINUTE, 0L);
            output.put(NUM_OF_WRITES_HOUR, 0L);
            output.put(NUM_OF_WRITES_DAY, 0L);

            output.put(NUM_OF_READS, 0L);
            output.put(NUM_OF_READS_MINUTE, 0L);
            output.put(NUM_OF_READS_HOUR, 0L);
            output.put(NUM_OF_READS_DAY, 0L);
        } else {
            output.put(NUM_OF_WRITES, metrics.getTotalNumberOfWrites());
            output.put(NUM_OF_WRITES_MINUTE, metrics.getLastNumberOfWrites(MetricsInterval.MINUTE));
            output.put(NUM_OF_WRITES_HOUR, metrics.getLastNumberOfWrites(MetricsInterval.HOUR));
            output.put(NUM_OF_WRITES_DAY, metrics.getLastNumberOfWrites(MetricsInterval.DAY));

            output.put(NUM_OF_READS, metrics.getTotalNumberOfReads());
            output.put(NUM_OF_READS_MINUTE, metrics.getLastNumberOfWrites(MetricsInterval.MINUTE));
            output.put(NUM_OF_READS_HOUR, metrics.getLastNumberOfWrites(MetricsInterval.HOUR));
            output.put(NUM_OF_READS_DAY, metrics.getLastNumberOfWrites(MetricsInterval.DAY));
        }
    }

    private static void collectMetrics(final NotificationMetric metrics, final Map<String, Long> output) {
        if (metrics == null) {
            output.put(NUM_OF_EMITTED, 0L);
            output.put(NUM_OF_EMITTED_MINUTE, 0L);
            output.put(NUM_OF_EMITTED_HOUR, 0L);
            output.put(NUM_OF_EMITTED_DAY, 0L);
        } else {
            output.put(NUM_OF_EMITTED, metrics.getTotalNumberOfNotifications());
            output.put(NUM_OF_EMITTED_MINUTE, metrics.getLastNumberOfEmitted(MetricsInterval.MINUTE));
            output.put(NUM_OF_EMITTED_HOUR, metrics.getLastNumberOfEmitted(MetricsInterval.HOUR));
            output.put(NUM_OF_EMITTED_DAY, metrics.getLastNumberOfEmitted(MetricsInterval.DAY));
        }
    }

    private static void collectMetrics(final OperationMetric metrics, final Map<String, Long> output) {
        if (metrics == null) {
            output.put(NUM_OF_INVOKED, 0L);
            output.put(NUM_OF_INVOKED_MINUTE, 0L);
            output.put(NUM_OF_INVOKED_HOUR, 0L);
            output.put(NUM_OF_INVOKED_DAY, 0L);
        } else {
            output.put(NUM_OF_INVOKED, metrics.getTotalNumberOfInvocations());
            output.put(NUM_OF_INVOKED_MINUTE, metrics.getLastNumberOfInvocations(MetricsInterval.MINUTE));
            output.put(NUM_OF_INVOKED_HOUR, metrics.getLastNumberOfInvocations(MetricsInterval.HOUR));
            output.put(NUM_OF_INVOKED_DAY, metrics.getLastNumberOfInvocations(MetricsInterval.DAY));
        }
    }

    static CompositeData collectMetrics(final MetricsSupport metrics) throws OpenDataException {
        final Map<String, Long> entries = Maps.newHashMapWithExpectedSize(TYPE.keySet().size());
        for(final AttributeMetric metric: metrics.getMetrics(AttributeMetric.class))
            collectMetrics(metric, entries);
        for(final NotificationMetric metric: metrics.getMetrics(NotificationMetric.class))
            collectMetrics(metric, entries);
        for(final OperationMetric metric: metrics.getMetrics(OperationMetric.class))
            collectMetrics(metric, entries);
        return new CompositeDataSupport(TYPE, entries);
    }

    private final SummaryMetrics metrics;

    SummaryMetricsAttribute() {
        super("SummaryMetrics", TYPE);
        metrics = new SummaryMetrics(Utils.getBundleContextOfObject(this));
    }

    @Override
    public CompositeData getValue() throws OpenDataException {
        return collectMetrics(metrics);
    }
}
