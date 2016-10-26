package com.bytex.snamp.management.jmx;

import com.bytex.snamp.connector.metrics.*;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.CompositeTypeBuilder;
import com.bytex.snamp.management.SummaryMetrics;
import com.google.common.collect.Maps;

import javax.management.openmbean.*;
import java.util.Map;

import static com.bytex.snamp.jmx.OpenMBean.OpenAttribute;
import static com.bytex.snamp.jmx.MetricsConverter.*;

/**
 * Provides global metrics.
 */
final class SummaryMetricsAttribute extends OpenAttribute<CompositeData, CompositeType> {
    private static final String ATTRIBUTES_WRITES_FIELD = "attributesWrites";
    private static final String ATTRIBUTE_READS_FIELD = "attributeReads";
    private static final String NOTIFICATIONS_FIELD = "notifications";
    private static final String INVOCATIONS_FIELD = "invocations";


    static final CompositeType TYPE = Utils.interfaceStaticInitialize(() -> new CompositeTypeBuilder("Metrics", "Consolidated set of metrics")
            .addItem(ATTRIBUTE_READS_FIELD, "Rate of attribute reads", RATE_TYPE)
            .addItem(ATTRIBUTES_WRITES_FIELD, "Rate of attribute writes", RATE_TYPE)
            .addItem(NOTIFICATIONS_FIELD, "Rate of received notifications", RATE_TYPE)
            .addItem(INVOCATIONS_FIELD, "Rate of invoked operations", RATE_TYPE)
            .build());

    private static void collectMetrics(final AttributeMetric metrics, final Map<String, CompositeData> output) {
        final Rate attributeReads;
        final Rate attributeWrites;
        if(metrics == null)
            attributeReads = attributeWrites = Rate.EMPTY;
        else {
            attributeReads = metrics.reads();
            attributeWrites = metrics.writes();
        }
        output.put(ATTRIBUTE_READS_FIELD, fromRate(attributeReads));
        output.put(ATTRIBUTES_WRITES_FIELD, fromRate(attributeWrites));
    }

    private static void collectMetrics(final NotificationMetric metrics, final Map<String, CompositeData> output) throws OpenDataException {
        output.put(NOTIFICATIONS_FIELD, fromRate(metrics == null ? Rate.EMPTY : metrics.notifications()));
    }

    private static void collectMetrics(final OperationMetric metrics, final Map<String, CompositeData> output) throws OpenDataException {
        output.put(INVOCATIONS_FIELD, fromRate(metrics == null ? Rate.EMPTY : metrics.invocations()));
    }

    static CompositeData collectMetrics(final MetricsSupport metrics) throws OpenDataException {
        final Map<String, CompositeData> entries = Maps.newHashMapWithExpectedSize(TYPE.keySet().size());
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
