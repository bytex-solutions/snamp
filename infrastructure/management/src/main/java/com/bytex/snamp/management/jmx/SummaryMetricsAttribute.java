package com.bytex.snamp.management.jmx;

import com.bytex.snamp.connectors.metrics.*;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.CompositeTypeBuilder;
import com.google.common.collect.Maps;

import javax.management.openmbean.*;

import java.util.Map;
import java.util.concurrent.Callable;

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

    static final CompositeType TYPE = Utils.interfaceStaticInitialize(new Callable<CompositeType>() {
        @Override
        public CompositeType call() throws OpenDataException {
            return new CompositeTypeBuilder("Metrics", "Consolidated set of metrics")
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
                    .build();
        }
    });

    private static void collectMetrics(final AttributeMetrics metrics, final Map<String, Long> output){
        output.put(NUM_OF_WRITES, metrics.getNumberOfWrites());
        output.put(NUM_OF_WRITES_MINUTE, metrics.getNumberOfWrites(MetricsInterval.MINUTE));
        output.put(NUM_OF_WRITES_HOUR, metrics.getNumberOfWrites(MetricsInterval.HOUR));
        output.put(NUM_OF_WRITES_DAY, metrics.getNumberOfWrites(MetricsInterval.DAY));

        output.put(NUM_OF_READS, metrics.getNumberOfReads());
        output.put(NUM_OF_READS_MINUTE, metrics.getNumberOfWrites(MetricsInterval.MINUTE));
        output.put(NUM_OF_READS_HOUR, metrics.getNumberOfWrites(MetricsInterval.HOUR));
        output.put(NUM_OF_READS_DAY, metrics.getNumberOfWrites(MetricsInterval.DAY));
    }

    private static void collectMetrics(final NotificationMetrics metrics, final Map<String, Long> output) {
        output.put(NUM_OF_EMITTED, metrics.getNumberOfEmitted());
        output.put(NUM_OF_EMITTED_MINUTE, metrics.getNumberOfEmitted(MetricsInterval.MINUTE));
        output.put(NUM_OF_EMITTED_HOUR, metrics.getNumberOfEmitted(MetricsInterval.HOUR));
        output.put(NUM_OF_EMITTED_DAY, metrics.getNumberOfEmitted(MetricsInterval.DAY));
    }

    private static void collectMetrics(final OperationMetrics metrics, final Map<String, Long> output) {
        output.put(NUM_OF_INVOKED, metrics.getNumberOfInvocations());
        output.put(NUM_OF_INVOKED_MINUTE, metrics.getNumberOfInvocations(MetricsInterval.MINUTE));
        output.put(NUM_OF_INVOKED_HOUR, metrics.getNumberOfInvocations(MetricsInterval.HOUR));
        output.put(NUM_OF_INVOKED_DAY, metrics.getNumberOfInvocations(MetricsInterval.DAY));
    }

    static CompositeData collectMetrics(final MetricsReader metrics) throws OpenDataException {
        final Map<String, Long> entries = Maps.newHashMapWithExpectedSize(TYPE.keySet().size());
        collectMetrics(metrics.queryObject(AttributeMetrics.class), entries);
        collectMetrics(metrics.queryObject(NotificationMetrics.class), entries);
        collectMetrics(metrics.queryObject(OperationMetrics.class), entries);
        return new CompositeDataSupport(TYPE, entries);
    }

    private final GlobalMetrics metrics;

    SummaryMetricsAttribute(){
        super("SummaryMetrics", TYPE);
        metrics = new GlobalMetrics(Utils.getBundleContextOfObject(this));
    }

    @Override
    public CompositeData getValue() throws OpenDataException {
        return collectMetrics(metrics);
    }
}
