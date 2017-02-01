package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.connector.metrics.Arrivals;
import com.bytex.snamp.connector.metrics.Metric;
import com.bytex.snamp.connector.metrics.MetricsInterval;
import com.bytex.snamp.moa.topology.ComponentVertex;
import com.google.common.collect.Maps;
import org.codehaus.jackson.annotate.JsonProperty;

import java.time.Duration;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents information about component and its statistics about arrivals of input requests.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
public final class ComponentArrivals {
    @JsonProperty("channels")
    public final long channels;
    @JsonProperty("scalability")
    public final double scalability;
    @JsonProperty("efficiency")
    public final double efficiency;

    @JsonProperty("availability")
    public final Map<String, Double> availability;

    @JsonProperty("maxResponseTime")
    public final Map<String, Long> maxResponseTime;
    @JsonProperty("meanResponseTime")
    public final Map<String, Long> meanResponseTime;

    @JsonProperty("deviationRT")
    public final long deviationRT;
    @JsonProperty("responseTime90")
    public final long responseTime90;
    @JsonProperty("responseTime95")
    public final long responseTime95;
    @JsonProperty("responseTime98")
    public final long responseTime98;

    @JsonProperty("maxRatePerSecond")
    public final Map<String, Long> maxRatePerSecond;
    @JsonProperty("meanRate")
    public final Map<String, Double> meanRate;

    ComponentArrivals(final ComponentVertex vertex) {
        final String ALL_TIME = "AllTime";
        final Arrivals metric = vertex.getArrivals();
        channels = metric.getChannels();
        scalability = 1D - metric.getCorrelation();
        efficiency = metric.getEfficiency();
        //response time
        maxResponseTime = fillMap(metric, Arrivals::getLastMaxValue, Duration::toNanos);
        maxResponseTime.put(ALL_TIME, metric.getMaxValue().toNanos());
        meanResponseTime = fillMap(metric, Arrivals::getLastMeanValue, Duration::toNanos);
        meanResponseTime.put(ALL_TIME, metric.getMeanValue().toNanos());
        deviationRT = metric.getDeviation().toNanos();
        responseTime90 = metric.getQuantile(0.9D).toNanos();
        responseTime95 = metric.getQuantile(0.95D).toNanos();
        responseTime98 = metric.getQuantile(0.98D).toNanos();
        //availability
        availability = fillMap(metric, Arrivals::getLastMeanAvailability, Function.identity());
        //rate
        maxRatePerSecond = fillMap(metric, Arrivals::getLastMaxRatePerSecond, Function.identity());
        meanRate = fillMap(metric, Arrivals::getLastMeanRate, Function.identity());
    }

    private static <M extends Metric, I, O> Map<String, O> fillMap(final M metric,
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
