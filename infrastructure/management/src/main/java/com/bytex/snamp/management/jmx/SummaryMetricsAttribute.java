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
    private static final String MEAN_NUM_OF_WRITES = "meanNumberOfAttributeWrites";
    private static final String MEAN_NUM_OF_WRITES_MINUTE = MEAN_NUM_OF_WRITES.concat("LastMinute");
    private static final String MEAN_NUM_OF_WRITES_HOUR = MEAN_NUM_OF_WRITES.concat("LastHour");


    private static final String NUM_OF_READS = "numberOfAttributeReads";
    private static final String NUM_OF_READS_MINUTE = NUM_OF_READS.concat("LastMinute");
    private static final String NUM_OF_READS_HOUR = NUM_OF_READS.concat("LastHour");
    private static final String NUM_OF_READS_DAY = NUM_OF_READS.concat("Last24Hours");
    private static final String MEAN_NUM_OF_READS = "meanNumberOfAttributeReads";
    private static final String MEAN_NUM_OF_READS_MINUTE = MEAN_NUM_OF_READS.concat("LastMinute");
    private static final String MEAN_NUM_OF_READS_HOUR = MEAN_NUM_OF_READS.concat("LastHour");


    private static final String NUM_OF_EMITTED = "numberOfEmittedNotifications";
    private static final String NUM_OF_EMITTED_MINUTE = NUM_OF_READS.concat("LastMinute");
    private static final String NUM_OF_EMITTED_HOUR = NUM_OF_READS.concat("LastHour");
    private static final String NUM_OF_EMITTED_DAY = NUM_OF_READS.concat("Last24Hours");
    private static final String MEAN_NUM_OF_EMITTED = "meanNumberOfEmittedNotifications";
    private static final String MEAN_NUM_OF_EMITTED_MINUTE = MEAN_NUM_OF_EMITTED.concat("LastMinute");
    private static final String MEAN_NUM_OF_EMITTED_HOUR = MEAN_NUM_OF_EMITTED.concat("LastHour");


    private static final String NUM_OF_INVOKED = "numberOfOperationInvocations";
    private static final String NUM_OF_INVOKED_MINUTE = NUM_OF_READS.concat("LastMinute");
    private static final String NUM_OF_INVOKED_HOUR = NUM_OF_READS.concat("LastHour");
    private static final String NUM_OF_INVOKED_DAY = NUM_OF_READS.concat("Last24Hours");
    private static final String MEAN_NUM_OF_INVOKED = "meanNumberOfOperationInvocations";
    private static final String MEAN_NUM_OF_INVOKED_MINUTE = MEAN_NUM_OF_INVOKED.concat("LastMinute");
    private static final String MEAN_NUM_OF_INVOKED_HOUR = MEAN_NUM_OF_INVOKED.concat("LastHour");


    static final CompositeType TYPE = Utils.interfaceStaticInitialize(() -> new CompositeTypeBuilder("Metrics", "Consolidated set of metrics")
            .addItem(NUM_OF_WRITES, "Total number of attribute writes", SimpleType.LONG)
            .addItem(NUM_OF_WRITES_MINUTE, "Number of attribute writes for the last minute", SimpleType.LONG)
            .addItem(NUM_OF_WRITES_HOUR, "Number of attribute writes for the last hour", SimpleType.LONG)
            .addItem(NUM_OF_WRITES_DAY, "Number of attribute writes for the last 24 hours", SimpleType.LONG)
            .addItem(MEAN_NUM_OF_WRITES_MINUTE, "Mean number of attribute writes per minute", SimpleType.DOUBLE)
            .addItem(MEAN_NUM_OF_WRITES_HOUR, "Mean number of attribute writes per hour", SimpleType.DOUBLE)

            .addItem(NUM_OF_READS, "Total number of attribute reads", SimpleType.LONG)
            .addItem(NUM_OF_READS_MINUTE, "Number of attribute reads for the last minute", SimpleType.LONG)
            .addItem(NUM_OF_READS_HOUR, "Number of attribute reads for the last hour", SimpleType.LONG)
            .addItem(NUM_OF_READS_DAY, "Number of attribute reads for the last 24 hours", SimpleType.LONG)
            .addItem(MEAN_NUM_OF_READS_MINUTE, "Mean number of attribute reads per minute", SimpleType.DOUBLE)
            .addItem(MEAN_NUM_OF_READS_HOUR, "Mean number of attribute reads per hour", SimpleType.DOUBLE)

            .addItem(NUM_OF_EMITTED, "Total number of emitted notifications", SimpleType.LONG)
            .addItem(NUM_OF_EMITTED_MINUTE, "Number of emitted notifications for the last minute", SimpleType.LONG)
            .addItem(NUM_OF_EMITTED_HOUR, "Number of emitted notifications for the last hour", SimpleType.LONG)
            .addItem(NUM_OF_EMITTED_DAY, "Number of emitted notifications for the last day", SimpleType.LONG)
            .addItem(MEAN_NUM_OF_EMITTED_MINUTE, "Mean number of emitted notifications per minute", SimpleType.DOUBLE)
            .addItem(MEAN_NUM_OF_EMITTED_HOUR, "Mean number of emitted notifications per hour", SimpleType.DOUBLE)

            .addItem(NUM_OF_INVOKED, "Total number of invoked operations", SimpleType.LONG)
            .addItem(NUM_OF_INVOKED_MINUTE, "Number of invoked operations for the last minute", SimpleType.LONG)
            .addItem(NUM_OF_INVOKED_HOUR, "Number of invoked operations for the last hour", SimpleType.LONG)
            .addItem(NUM_OF_INVOKED_DAY, "Number of invoked operations for the last day", SimpleType.LONG)
            .addItem(MEAN_NUM_OF_INVOKED_MINUTE, "Mean number of invoked operations per minute", SimpleType.DOUBLE)
            .addItem(MEAN_NUM_OF_INVOKED_HOUR, "Mean number of invoked operations per hour", SimpleType.DOUBLE)
            .build());

    private static void collectMetrics(final AttributeMetric metrics, final Map<String, Number> output) {
        if (metrics == null) {
            output.put(NUM_OF_WRITES, 0L);
            output.put(NUM_OF_WRITES_MINUTE, 0L);
            output.put(NUM_OF_WRITES_HOUR, 0L);
            output.put(NUM_OF_WRITES_DAY, 0L);
            output.put(MEAN_NUM_OF_WRITES_MINUTE, 0D);
            output.put(MEAN_NUM_OF_WRITES_HOUR, 0D);

            output.put(NUM_OF_READS, 0L);
            output.put(NUM_OF_READS_MINUTE, 0L);
            output.put(NUM_OF_READS_HOUR, 0L);
            output.put(NUM_OF_READS_DAY, 0L);
            output.put(MEAN_NUM_OF_READS_MINUTE, 0D);
            output.put(MEAN_NUM_OF_READS_HOUR, 0D);
        } else {
            output.put(NUM_OF_WRITES, metrics.writes().getTotalRate());
            output.put(NUM_OF_WRITES_MINUTE, metrics.writes().getLastRate(MetricsInterval.MINUTE));
            output.put(NUM_OF_WRITES_HOUR, metrics.writes().getLastRate(MetricsInterval.HOUR));
            output.put(NUM_OF_WRITES_DAY, metrics.writes().getLastRate(MetricsInterval.DAY));
            output.put(MEAN_NUM_OF_WRITES_MINUTE, metrics.writes().getLastMeanRate(MetricsInterval.MINUTE));
            output.put(MEAN_NUM_OF_WRITES_HOUR, metrics.writes().getLastMeanRate(MetricsInterval.HOUR));


            output.put(NUM_OF_READS, metrics.reads().getTotalRate());
            output.put(NUM_OF_READS_MINUTE, metrics.reads().getLastRate(MetricsInterval.MINUTE));
            output.put(NUM_OF_READS_HOUR, metrics.reads().getLastRate(MetricsInterval.HOUR));
            output.put(NUM_OF_READS_DAY, metrics.reads().getLastRate(MetricsInterval.DAY));
            output.put(MEAN_NUM_OF_READS_MINUTE, metrics.reads().getMeanRate(MetricsInterval.MINUTE));
            output.put(MEAN_NUM_OF_READS_HOUR, metrics.reads().getMeanRate(MetricsInterval.HOUR));
        }
    }

    private static void collectMetrics(final NotificationMetric metrics, final Map<String, Number> output) {
        if (metrics == null) {
            output.put(NUM_OF_EMITTED, 0L);
            output.put(NUM_OF_EMITTED_MINUTE, 0L);
            output.put(NUM_OF_EMITTED_HOUR, 0L);
            output.put(NUM_OF_EMITTED_DAY, 0L);
            output.put(MEAN_NUM_OF_EMITTED_MINUTE, 0D);
            output.put(MEAN_NUM_OF_EMITTED_HOUR, 0D);
        } else {
            output.put(NUM_OF_EMITTED, metrics.notifications().getTotalRate());
            output.put(NUM_OF_EMITTED_MINUTE, metrics.notifications().getLastRate(MetricsInterval.MINUTE));
            output.put(NUM_OF_EMITTED_HOUR, metrics.notifications().getLastRate(MetricsInterval.HOUR));
            output.put(NUM_OF_EMITTED_DAY, metrics.notifications().getLastRate(MetricsInterval.DAY));
            output.put(MEAN_NUM_OF_EMITTED_MINUTE, metrics.notifications().getLastRate(MetricsInterval.MINUTE));
            output.put(MEAN_NUM_OF_EMITTED_HOUR, metrics.notifications().getLastRate(MetricsInterval.HOUR));
        }
    }

    private static void collectMetrics(final OperationMetric metrics, final Map<String, Number> output) {
        if (metrics == null) {
            output.put(NUM_OF_INVOKED, 0L);
            output.put(NUM_OF_INVOKED_MINUTE, 0L);
            output.put(NUM_OF_INVOKED_HOUR, 0L);
            output.put(NUM_OF_INVOKED_DAY, 0L);
            output.put(MEAN_NUM_OF_INVOKED_MINUTE, 0D);
            output.put(MEAN_NUM_OF_INVOKED_HOUR, 0D);
        } else {
            output.put(NUM_OF_INVOKED, metrics.invocations().getTotalRate());
            output.put(NUM_OF_INVOKED_MINUTE, metrics.invocations().getLastRate(MetricsInterval.MINUTE));
            output.put(NUM_OF_INVOKED_HOUR, metrics.invocations().getLastRate(MetricsInterval.HOUR));
            output.put(NUM_OF_INVOKED_DAY, metrics.invocations().getLastRate(MetricsInterval.DAY));
        }
    }

    static CompositeData collectMetrics(final MetricsSupport metrics) throws OpenDataException {
        final Map<String, Number> entries = Maps.newHashMapWithExpectedSize(TYPE.keySet().size());
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
