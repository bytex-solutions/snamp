package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.connector.metrics.Arrivals;
import com.bytex.snamp.moa.topology.ComponentVertex;
import com.bytex.snamp.moa.topology.ComponentVertexIdentity;
import com.bytex.snamp.web.serviceModel.ObjectMapperSingleton;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.node.ObjectNode;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Adjacency matrix with statistics about arrivals of input requests for each vertex.
 */
public abstract class AdjacencyMatrixWithArrivals extends AdjacencyMatrix {
    /**
     * Represents information about component and its statistics about arrivals of input requests.
     * @author Roman Sakno
     * @since 2.0
     * @version 2.1
     */
    public static final class ComponentArrivals extends AbstractComponentStatistics {
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

        @JsonProperty("responseTimeStdDev")
        public final long responseTimeStdDev;
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

        private ComponentArrivals(final ComponentVertex vertex) {
            final String ALL_TIME = "AllTime";
            final Arrivals metric = vertex.getArrivals();
            channels = metric.getChannels();
            scalability = 1D - metric.getCorrelation();
            efficiency = metric.getEfficiency();
            //response time
            maxResponseTime = fillMap(metric, Arrivals::getLastMaxValue, Duration::toNanos);
            maxResponseTime.put(ALL_TIME, metric.getMaxValue().toNanos());
            meanResponseTime = fillMap(metric, Arrivals::getMeanValue, Duration::toNanos);
            meanResponseTime.put(ALL_TIME, metric.getMeanValue().toNanos());
            responseTimeStdDev = metric.getDeviation().toNanos();
            responseTime90 = metric.getQuantile(0.9F).toNanos();
            responseTime95 = metric.getQuantile(0.95F).toNanos();
            responseTime98 = metric.getQuantile(0.98F).toNanos();
            //availability
            availability = fillMap(metric, Arrivals::getLastMeanAvailability, Function.identity());
            //rate
            maxRatePerSecond = fillMap(metric, Arrivals::getLastMaxRatePerSecond, Function.identity());
            meanRate = fillMap(metric, Arrivals::getMeanRate, Function.identity());
        }
    }

    private final Map<ComponentVertexIdentity, ComponentArrivals> arrivals = new HashMap<>();

    final void computeArrivals(final ComponentVertex vertex){
        if(arrivals.containsKey(vertex.getIdentity())) return; //little optimization because computing arrivals stat is expensive operation
        arrivals.put(vertex.getIdentity(), new ComponentArrivals(vertex));
    }

    @Override
    final void serialize(final ObjectNode node) {
        node.put("arrivals", ObjectMapperSingleton.INSTANCE.valueToTree(arrivals));
    }
}
