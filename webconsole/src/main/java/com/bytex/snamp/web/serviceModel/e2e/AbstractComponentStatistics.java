package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.connector.metrics.Metric;
import com.bytex.snamp.connector.metrics.MetricsInterval;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents JSON-serializable statistics associated with the component.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class AbstractComponentStatistics {
    static <M extends Metric, I, O> Map<String, O> fillMap(final M metric,
                                                           final BiFunction<M, MetricsInterval, I> extractor,
                                                           final Function<I, O> transformer) {
        final Map<String, O> output = Maps.newHashMapWithExpectedSize(MetricsInterval.ALL_INTERVALS.size() + 1);
        for (final MetricsInterval interval : MetricsInterval.ALL_INTERVALS) {
            final O result = transformer.apply(extractor.apply(metric, interval));
            output.put(interval.toString(), result);
        }
        return output;
    }
}
