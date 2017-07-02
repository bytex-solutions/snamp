package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.connector.metrics.MetricsInterval;
import com.bytex.snamp.connector.metrics.Rate;
import com.bytex.snamp.json.InstantSerializer;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.Collectors;

/**
 * Represents chart for visualization of {@link Rate} metrics.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class RateChart extends AbstractChart implements TwoDimensionalChart<ChronoAxis, NumericAxis> {
    public enum RateMetric implements ToDoubleBiFunction<Rate, MetricsInterval>{
        /**
         * Measured rate of actions for the last time.
         */
        LAST_RATE {

            @Override
            public double applyAsDouble(final Rate rate, final MetricsInterval interval) {
                return rate.getLastRate(interval);
            }
        },

        /**
         * Mean rate of actions per unit of time from the historical perspective.
         */
        MEAN_RATE {
            @Override
            public double applyAsDouble(final Rate rate, final MetricsInterval interval) {
                return rate.getMeanRate(interval);
            }
        },

        /**
         * Max rate of actions observed in the specified interval.
         */
        MAX_RATE {
            @Override
            public double applyAsDouble(final Rate rate, final MetricsInterval interval) {
                return rate.getMaxRate(interval);
            }
        },

        /**
         * Max rate of actions received per second for the last time.
         */
        LAST_MAX_RATE_PER_SECOND {
            @Override
            public double applyAsDouble(final Rate rate, final MetricsInterval interval) {
                return rate.getLastMaxRatePerSecond(interval);
            }
        },

        /**
         * Max rate of actions received per minute for the last time.
         */
        LAST_MAX_RATE_PER_MINUTE {
            @Override
            public double applyAsDouble(final Rate rate, final MetricsInterval interval) {
                return rate.getLastMaxRatePerMinute(interval);
            }
        },

        /**
         * Max rate of actions received per 12 hours for the last time.
         */
        LAST_MAX_RATE_PER_12_HOURS{
            @Override
            public double applyAsDouble(final Rate rate, final MetricsInterval interval) {
                return rate.getLastMaxRatePer12Hours(interval);
            }
        }
    }

    public static final class RateMetricSerializer extends JsonSerializer<RateMetric>{

        @Override
        public void serialize(final RateMetric value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
            jgen.writeString(value.name().toLowerCase());
        }
    }

    public static final class RateMetricDeserializer extends JsonDeserializer<RateMetric>{

        @Override
        public RateMetric deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
            return RateMetric.valueOf(jp.getText().toUpperCase());
        }
    }

    private final class ChartData implements com.bytex.snamp.web.serviceModel.charts.ChartData{
        private final Instant timeStamp;
        private final double value;
        private final RateMetric metricType;

        private ChartData(final Rate rate, final MetricsInterval interval, final RateMetric metricType){
            timeStamp = Instant.now();
            value = metricType.applyAsDouble(rate, interval);
            this.metricType = metricType;
        }

        @JsonProperty("timeStamp")
        @JsonSerialize(using = InstantSerializer.class)
        public Instant getTimeStamp(){
            return timeStamp;
        }

        @JsonProperty("type")
        @JsonSerialize(using = RateMetricSerializer.class)
        public RateMetric getType(){
            return metricType;
        }

        @JsonProperty("value")
        public double getValue(){
            return value;
        }

        @Override
        public Object getData(final int dimension) {
            switch (dimension){
                case 0:
                    return getTimeStamp();
                case 1:
                    return getValue();
                default:
                    throw new IndexOutOfBoundsException();
            }
        }
    }
    private MetricsInterval interval;
    private ChronoAxis x;
    private NumericAxis y;
    private final Set<RateMetric> metrics;

    protected RateChart(){
        interval = MetricsInterval.DAY;
        metrics = new HashSet<>();
    }

    @JsonProperty("metrics")
    @JsonSerialize(contentUsing = RateMetricSerializer.class)
    public final Set<RateMetric> getMetrics(){
        return metrics;
    }

    @JsonDeserialize(contentUsing = RateMetricDeserializer.class)
    public final void setMetrics(final RateMetric... metrics){
        this.metrics.clear();
        Collections.addAll(this.metrics, metrics);
    }

    @JsonProperty("interval")
    @JsonSerialize(using = MetricsIntervalSerializer.class)
    public final MetricsInterval getInterval(){
        return interval;
    }

    @JsonDeserialize(using = MetricsIntervalDeserializer.class)
    public final void setInterval(@Nonnull final MetricsInterval value){
        interval = Objects.requireNonNull(value);
    }

    protected ChronoAxis createAxisX(){
        return new ChronoAxis();
    }

    protected abstract NumericAxis createAxisY();

    /**
     * Gets information about X-axis.
     *
     * @return Information about X-axis.
     */
    @Nonnull
    @Override
    @JsonProperty("X")
    public final ChronoAxis getAxisX() {
        if(x == null)
            x = createAxisX();
        return x;
    }

    public final void setAxisX(final ChronoAxis value){
        x = value;
    }

    /**
     * Gets information about Y-axis.
     *
     * @return Information about Y-axis.
     */
    @Nonnull
    @Override
    @JsonProperty("Y")
    public final NumericAxis getAxisY() {
        if(y == null)
            y = createAxisY();
        return y;
    }

    public final void setAxisY(final NumericAxis value){
        y = value;
    }

    /**
     * Gets number of dimensions.
     *
     * @return Number of dimensions.
     */
    @Override
    @JsonIgnore
    public final int getDimensions() {
        return 2;
    }

    /**
     * Gets configuration of the axis associated with the dimension.
     *
     * @param dimensionIndex Zero-based index of the dimension.
     * @return Axis configuration.
     * @throws IndexOutOfBoundsException Invalid dimension index.
     */
    @Override
    public final Axis getAxis(final int dimensionIndex) {
        switch (dimensionIndex){
            case 0:
                return getAxisX();
            case 1:
                return getAxisX();
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    protected abstract Rate extractDataSource(final BundleContext context) throws Exception;

    /**
     * Collects chart data.
     *
     * @param context Bundle context. Cannot be {@literal null}.
     * @return Chart data series.
     * @throws Exception The data cannot be collected.
     */
    @Override
    public final Iterable<? extends ChartData> collectChartData(final BundleContext context) throws Exception {
        final Rate rate = extractDataSource(context);
        return metrics.stream().map(metric -> new ChartData(rate, getInterval(), metric)).collect(Collectors.toList());
    }
}
