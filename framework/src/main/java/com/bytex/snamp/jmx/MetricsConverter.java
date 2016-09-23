package com.bytex.snamp.jmx;

import com.bytex.snamp.connector.metrics.GaugeFP;
import com.bytex.snamp.connector.metrics.MetricsInterval;
import com.bytex.snamp.connector.metrics.Rate;
import com.google.common.collect.ImmutableMap;

import javax.management.openmbean.*;

import static com.bytex.snamp.internal.Utils.interfaceStaticInitialize;

/**
 * Provides conversion between SNAMP metrics declared in {@link com.bytex.snamp.connector.metrics} package
 * and JMX open types.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class MetricsConverter {
    //rate fields
    private static final String TOTAL_RATE_FIELD = "totalRate";
    private static final String MEAN_RATE_PER_SECOND_FIELD = "meanRatePerSecond";
    private static final String MEAN_RATE_PER_MINUTE_FIELD = "meanRatePerMinute";
    private static final String MEAN_RATE_PER_5_MINUTES_FIELD = "meanRatePer5Minutes";
    private static final String MEAN_RATE_PER_15_MINUTES_FIELD = "meanRatePer15Minutes";
    private static final String MEAN_RATE_PER_HOUR_FIELD = "meanRatePerHour";
    private static final String MEAN_RATE_PER_12_HOURS_FIELD = "meanRatePer12Hours";
    private static final String MEAN_RATE_PER_DAY_FIELD = "meanRatePerDay";
    private static final String MEAN_RATE_FOR_LAST_SECOND_FIELD = "meanRateLastSecond";
    private static final String MEAN_RATE_FOR_LAST_MINUTE_FIELD = "meanRateLastMinute";
    private static final String MEAN_RATE_FOR_LAST_5_MINUTES_FIELD = "meanRateLast5Minutes";
    private static final String MEAN_RATE_FOR_LAST_15_MINUTES_FIELD = "meanRateLast15Minutes";
    private static final String MEAN_RATE_FOR_LAST_HOUR_FIELD = "meanRateLastHour";
    private static final String MEAN_RATE_FOR_LAST_12_HOURS_FIELD = "meanRateLast12Hours";
    private static final String MEAN_RATE_FOR_LAST_DAY_FIELD = "meanRateLastDay";

    //gauge fields
    private static final String MAX_VALUE_FIELD = "maxValue";
    private static final String LAST_VALUE_FIELD = "lastValue";
    private static final String MIN_VALUE_FIELD = "minValue";
    private static final String STD_DEV_FIELD = "deviation";
    private static final String MEDIAN_VALUE_FIELD = "median";
    private static final String PERCENTILE_90_FIELD = "percentile90";
    private static final String PERCENTILE_95_FIELD = "percentile95";
    private static final String PERCENTILE_97_FIELD = "percentile97";
    private static final String MEAN_VALUE_FOR_LAST_SECOND_FIELD = "meanValueLastSecond";
    private static final String MEAN_VALUE_FOR_LAST_MINUTE_FIELD = "meanValueLastMinute";
    private static final String MEAN_VALUE_FOR_LAST_5_MINUTES_FIELD = "meanValueLast5Minutes";
    private static final String MEAN_VALUE_FOR_LAST_15_MINUTES_FIELD = "meanValueLast15Minutes";
    private static final String MEAN_VALUE_FOR_LAST_HOUR_FIELD = "meanValueLastHour";
    private static final String MEAN_VALUE_FOR_LAST_12_HOURS_FIELD = "meanValueLast12Hours";
    private static final String MEAN_VALUE_FOR_LAST_DAY_FIELD = "meanValueLastDay";
    private static final String MAX_VALUE_FOR_LAST_SECOND_FIELD = "maxValueLastSecond";
    private static final String MAX_VALUE_FOR_LAST_MINUTE_FIELD = "maxValueLastMinute";
    private static final String MAX_VALUE_FOR_LAST_5_MINUTES_FIELD = "maxValueLast5Minutes";
    private static final String MAX_VALUE_FOR_LAST_15_MINUTES_FIELD = "maxValueLast15Minutes";
    private static final String MAX_VALUE_FOR_LAST_HOUR_FIELD = "maxValueLastHour";
    private static final String MAX_VALUE_FOR_LAST_12_HOURS_FIELD = "maxValueLast12Hours";
    private static final String MAX_VALUE_FOR_LAST_DAY_FIELD = "maxValueLastDay";
    private static final String MIN_VALUE_FOR_LAST_SECOND_FIELD = "minValueLastSecond";
    private static final String MIN_VALUE_FOR_LAST_MINUTE_FIELD = "minValueLastMinute";
    private static final String MIN_VALUE_FOR_LAST_5_MINUTES_FIELD = "minValueLast5Minutes";
    private static final String MIN_VALUE_FOR_LAST_15_MINUTES_FIELD = "minValueLast15Minutes";
    private static final String MIN_VALUE_FOR_LAST_HOUR_FIELD = "minValueLastHour";
    private static final String MIN_VALUE_FOR_LAST_12_HOURS_FIELD = "minValueLast12Hours";
    private static final String MIN_VALUE_FOR_LAST_DAY_FIELD = "minValueLastDay";


    public static final CompositeType RATE_TYPE = interfaceStaticInitialize(() -> new CompositeTypeBuilder("com.btex.snamp.metrics.Rate", "Rate counter")
        .addItem(TOTAL_RATE_FIELD, "Total rate", SimpleType.LONG)
        .addItem(MEAN_RATE_FOR_LAST_SECOND_FIELD, "Mean rate computed for the last second", SimpleType.DOUBLE)
        .addItem(MEAN_RATE_FOR_LAST_MINUTE_FIELD, "Mean rate computed for the last second", SimpleType.DOUBLE)
        .build());

    /**
     * Represents Open Type equivalent of {@link GaugeFP}.
     */
    public static final CompositeType GAUGE_FP_TYPE = interfaceStaticInitialize(() -> new CompositeTypeBuilder("com.bytex.snamp.metrics.GaugeFP", "Floating-point gauge")
            .addItem(MAX_VALUE_FIELD, "Maximum value ever presented", SimpleType.DOUBLE)
            .addItem(LAST_VALUE_FIELD, "The last presented value", SimpleType.DOUBLE)
            .addItem(MIN_VALUE_FIELD, "Minimum value ever presented", SimpleType.DOUBLE)
            .addItem(STD_DEV_FIELD, "Standard deviation", SimpleType.DOUBLE)
            .addItem(MEDIAN_VALUE_FIELD, "Median value in historical perspective", SimpleType.DOUBLE)
            .addItem(PERCENTILE_90_FIELD, "Percentile 90", SimpleType.DOUBLE)
            .addItem(PERCENTILE_95_FIELD, "Percentile 95", SimpleType.DOUBLE)
            .addItem(PERCENTILE_97_FIELD, "Percentile 97", SimpleType.DOUBLE)
            //mean values
            .addItem(MEAN_VALUE_FOR_LAST_SECOND_FIELD, "Mean value for the last second", SimpleType.DOUBLE)
            .addItem(MEAN_VALUE_FOR_LAST_MINUTE_FIELD, "Mean value for the last minute", SimpleType.DOUBLE)
            .addItem(MEAN_VALUE_FOR_LAST_5_MINUTES_FIELD, "Mean value for the last five minutes", SimpleType.DOUBLE)
            .addItem(MEAN_VALUE_FOR_LAST_15_MINUTES_FIELD, "Mean value for the last fifteen minutes", SimpleType.DOUBLE)
            .addItem(MEAN_VALUE_FOR_LAST_HOUR_FIELD, "Mean value for the last hour", SimpleType.DOUBLE)
            .addItem(MEAN_VALUE_FOR_LAST_12_HOURS_FIELD, "Mean value for the last twelve hours", SimpleType.DOUBLE)
            .addItem(MEAN_VALUE_FOR_LAST_DAY_FIELD, "Mean value for the last day", SimpleType.DOUBLE)
            //max values
            .addItem(MAX_VALUE_FOR_LAST_SECOND_FIELD, "Maximum value for the last second", SimpleType.DOUBLE)
            .addItem(MAX_VALUE_FOR_LAST_MINUTE_FIELD, "Maximum value for the last minute", SimpleType.DOUBLE)
            .addItem(MAX_VALUE_FOR_LAST_5_MINUTES_FIELD, "Maximum value for the last five minutes", SimpleType.DOUBLE)
            .addItem(MAX_VALUE_FOR_LAST_15_MINUTES_FIELD, "Maximum value for the last fifteen minutes", SimpleType.DOUBLE)
            .addItem(MAX_VALUE_FOR_LAST_HOUR_FIELD, "Maximum value for the last hour", SimpleType.DOUBLE)
            .addItem(MAX_VALUE_FOR_LAST_12_HOURS_FIELD, "Maximum value for the last twelve hours", SimpleType.DOUBLE)
            .addItem(MAX_VALUE_FOR_LAST_DAY_FIELD, "Maximum value for the last day", SimpleType.DOUBLE)
            //min values
            //max values
            .addItem(MIN_VALUE_FOR_LAST_SECOND_FIELD, "Minimum value for the last second", SimpleType.DOUBLE)
            .addItem(MIN_VALUE_FOR_LAST_MINUTE_FIELD, "Minimum value for the last minute", SimpleType.DOUBLE)
            .addItem(MIN_VALUE_FOR_LAST_5_MINUTES_FIELD, "Minimum value for the last five minutes", SimpleType.DOUBLE)
            .addItem(MIN_VALUE_FOR_LAST_15_MINUTES_FIELD, "Minimum value for the last fifteen minutes", SimpleType.DOUBLE)
            .addItem(MIN_VALUE_FOR_LAST_HOUR_FIELD, "Minimum value for the last hour", SimpleType.DOUBLE)
            .addItem(MIN_VALUE_FOR_LAST_12_HOURS_FIELD, "Minimum value for the last twelve hours", SimpleType.DOUBLE)
            .addItem(MIN_VALUE_FOR_LAST_DAY_FIELD, "Minimum value for the last day", SimpleType.DOUBLE)
            .build());


    private MetricsConverter(){
        throw new InstantiationError();
    }

    /**
     * Converts {@link Rate} into {@link CompositeData}.
     * @param rate A counter to convert. Cannot be {@literal null}.
     * @return A {@link CompositeData} which contains counter data.
     * @throws OpenDataException Unable to convert counter to {@link CompositeData}.
     */
    public static CompositeData fromRate(final Rate rate) throws OpenDataException {
        final ImmutableMap<String, Number> result = ImmutableMap.<String, Number>builder()
                .put(TOTAL_RATE_FIELD, rate.getTotalRate())
                .put(MEAN_RATE_FOR_LAST_SECOND_FIELD, rate.getLastMeanRate(MetricsInterval.SECOND))
                .put(MEAN_RATE_FOR_LAST_MINUTE_FIELD, rate.getLastMeanRate(MetricsInterval.MINUTE))
                .build();
        return new CompositeDataSupport(RATE_TYPE, result);
    }

    /**
     * Converts {@link GaugeFP} into {@link CompositeData}.
     * @param gauge A gauge to convert. Cannot be {@literal null}.
     * @return A {@link CompositeData} which contains data from gauge.
     * @throws OpenDataException Unable to convert gauge to {@link CompositeData}.
     */
    public static CompositeData fromGaugeFP(final GaugeFP gauge) throws OpenDataException {
        final ImmutableMap<String, ? extends Number> result = ImmutableMap.<String, Double>builder()
                .put(MAX_VALUE_FIELD, gauge.getMaxValue())
                .put(LAST_VALUE_FIELD, gauge.getLastValue())
                .put(MIN_VALUE_FIELD, gauge.getMinValue())
                .put(STD_DEV_FIELD, gauge.getDeviation())
                .put(MEDIAN_VALUE_FIELD, gauge.getQuantile(0.5D))
                .put(PERCENTILE_90_FIELD, gauge.getQuantile(0.9D))
                .put(PERCENTILE_95_FIELD, gauge.getQuantile(0.95D))
                .put(PERCENTILE_97_FIELD, gauge.getQuantile(0.97D))
                //mean values
                .put(MEAN_VALUE_FOR_LAST_SECOND_FIELD, gauge.getMeanValue(MetricsInterval.SECOND))
                .put(MEAN_VALUE_FOR_LAST_MINUTE_FIELD, gauge.getMeanValue(MetricsInterval.MINUTE))
                .put(MEAN_VALUE_FOR_LAST_5_MINUTES_FIELD, gauge.getMeanValue(MetricsInterval.FIVE_MINUTES))
                .put(MEAN_VALUE_FOR_LAST_15_MINUTES_FIELD, gauge.getMeanValue(MetricsInterval.FIFTEEN_MINUTES))
                .put(MEAN_VALUE_FOR_LAST_HOUR_FIELD, gauge.getMeanValue(MetricsInterval.HOUR))
                .put(MEAN_VALUE_FOR_LAST_12_HOURS_FIELD, gauge.getMeanValue(MetricsInterval.TWELVE_HOURS))
                .put(MEAN_VALUE_FOR_LAST_DAY_FIELD, gauge.getMeanValue(MetricsInterval.DAY))
                //max values
                .put(MAX_VALUE_FOR_LAST_SECOND_FIELD, gauge.getLastMaxValue(MetricsInterval.SECOND))
                .put(MAX_VALUE_FOR_LAST_MINUTE_FIELD, gauge.getLastMaxValue(MetricsInterval.MINUTE))
                .put(MAX_VALUE_FOR_LAST_5_MINUTES_FIELD, gauge.getLastMaxValue(MetricsInterval.FIVE_MINUTES))
                .put(MAX_VALUE_FOR_LAST_15_MINUTES_FIELD, gauge.getLastMaxValue(MetricsInterval.FIFTEEN_MINUTES))
                .put(MAX_VALUE_FOR_LAST_HOUR_FIELD, gauge.getLastMaxValue(MetricsInterval.HOUR))
                .put(MAX_VALUE_FOR_LAST_12_HOURS_FIELD, gauge.getLastMaxValue(MetricsInterval.TWELVE_HOURS))
                .put(MAX_VALUE_FOR_LAST_DAY_FIELD, gauge.getLastMaxValue(MetricsInterval.DAY))
                //min values
                .put(MIN_VALUE_FOR_LAST_SECOND_FIELD, gauge.getLastMinValue(MetricsInterval.SECOND))
                .put(MIN_VALUE_FOR_LAST_MINUTE_FIELD, gauge.getLastMinValue(MetricsInterval.MINUTE))
                .put(MIN_VALUE_FOR_LAST_5_MINUTES_FIELD, gauge.getLastMinValue(MetricsInterval.FIVE_MINUTES))
                .put(MIN_VALUE_FOR_LAST_15_MINUTES_FIELD, gauge.getLastMinValue(MetricsInterval.FIFTEEN_MINUTES))
                .put(MIN_VALUE_FOR_LAST_HOUR_FIELD, gauge.getLastMinValue(MetricsInterval.HOUR))
                .put(MIN_VALUE_FOR_LAST_12_HOURS_FIELD, gauge.getLastMinValue(MetricsInterval.TWELVE_HOURS))
                .put(MIN_VALUE_FOR_LAST_DAY_FIELD, gauge.getLastMinValue(MetricsInterval.DAY))
                .build();
        return new CompositeDataSupport(GAUGE_FP_TYPE, result);
    }
}
