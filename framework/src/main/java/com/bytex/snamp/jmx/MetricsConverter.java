package com.bytex.snamp.jmx;

import com.bytex.snamp.connector.metrics.GaugeFP;
import com.bytex.snamp.connector.metrics.MetricsInterval;
import com.bytex.snamp.connector.metrics.Rate;
import com.bytex.snamp.connector.metrics.RatedGaugeFP;

import javax.management.openmbean.*;
import java.util.HashMap;

import static com.bytex.snamp.internal.Utils.interfaceStaticInitialize;

/**
 * Provides conversion between SNAMP metrics declared in {@link com.bytex.snamp.connector.metrics} package
 * and JMX open types.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class MetricsConverter {
    private static final class CompositeDataFields extends HashMap<String, Number>{
        private static final long serialVersionUID = -1438136908623683624L;

        private CompositeDataFields(final CompositeType prototype) {
            super(Math.round(prototype.keySet().size() * 1.5F));
        }

        private CompositeDataFields put(final String name, final long value){
            super.put(name, value);
            return this;
        }

        private CompositeDataFields put(final String name, final double value){
            super.put(name, value);
            return this;
        }
    }
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
    private static final String RATE_FOR_LAST_SECOND_FIELD = "rateLastSecond";
    private static final String RATE_FOR_LAST_MINUTE_FIELD = "rateLastMinute";
    private static final String RATE_FOR_LAST_5_MINUTES_FIELD = "rateLast5Minutes";
    private static final String RATE_FOR_LAST_15_MINUTES_FIELD = "rateLast15Minutes";
    private static final String RATE_FOR_LAST_HOUR_FIELD = "rateLastHour";
    private static final String RATE_FOR_LAST_12_HOURS_FIELD = "rateLast12Hours";
    private static final String RATE_FOR_LAST_DAY_FIELD = "rateLastDay";
    private static final String MAX_RATE_PER_SECOND_FIELD = "maxRatePerSecond";
    private static final String MAX_RATE_PER_MINUTE_FIELD = "maxRatePerMinute";
    private static final String MAX_RATE_PER_5_MINUTES_FIELD = "maxRatePer5Minutes";
    private static final String MAX_RATE_PER_15_MINUTES_FIELD = "maxRatePer15Minutes";
    private static final String MAX_RATE_PER_HOUR_FIELD = "maxRatePerHour";
    private static final String MAX_RATE_PER_12_HOURS_FIELD = "maxRatePer12Hours";
    private static final String MAX_RATE_PER_DAY_FIELD = "maxRatePerDay";
    private static final String MAX_RATE_PER_SECOND_FOR_LAST_MINUTE_FIELD = "maxRatePerSecondLastMinute";
    private static final String MAX_RATE_PER_SECOND_FOR_LAST_5_MINUTES_FIELD = "maxRatePerSecondLast5Minutes";
    private static final String MAX_RATE_PER_SECOND_FOR_LAST_15_MINUTES_FIELD = "maxRatePerSecondLast15Minutes";
    private static final String MAX_RATE_PER_SECOND_FOR_LAST_HOUR_FIELD = "maxRatePerSecondLastHour";
    private static final String MAX_RATE_PER_SECOND_FOR_LAST_12_HOURS_FIELD = "maxRatePerSecondLast12Hours";
    private static final String MAX_RATE_PER_SECOND_FOR_LAST_DAY_FIELD = "maxRatePerSecondLastDay";
    private static final String MAX_RATE_PER_MINUTE_FOR_LAST_5_MINUTES_FIELD = "maxRatePerMinuteLast5Minutes";
    private static final String MAX_RATE_PER_MINUTE_FOR_LAST_15_MINUTES_FIELD = "maxRatePerMinuteLast15Minutes";
    private static final String MAX_RATE_PER_MINUTE_FOR_LAST_HOUR_FIELD = "maxRatePerMinuteLastHour";
    private static final String MAX_RATE_PER_MINUTE_FOR_LAST_12_HOURS_FIELD = "maxRatePerMinuteLast12Hours";
    private static final String MAX_RATE_PER_MINUTE_FOR_LAST_DAY_FIELD = "maxRatePerMinuteLastDay";


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

    public static final CompositeType RATE_TYPE = interfaceStaticInitialize(() -> new CompositeTypeBuilder("com.bytex.snamp.metrics.Rate", "Rate counter")
        .addItem(TOTAL_RATE_FIELD, "Total rate", SimpleType.LONG)
            //mean rate for the last time
        .addItem(MEAN_RATE_FOR_LAST_SECOND_FIELD, "Mean rate per second computed for the last second", SimpleType.DOUBLE)
        .addItem(MEAN_RATE_FOR_LAST_MINUTE_FIELD, "Mean rate per second computed for the last minute", SimpleType.DOUBLE)
            .addItem(MEAN_RATE_FOR_LAST_5_MINUTES_FIELD, "Mean rate per second computed for the last 5 minutes", SimpleType.DOUBLE)
            .addItem(MEAN_RATE_FOR_LAST_15_MINUTES_FIELD, "Mean rate per second computed for the last 15 minutes", SimpleType.DOUBLE)
            .addItem(MEAN_RATE_FOR_LAST_HOUR_FIELD, "Mean rate per second computed for the last hour", SimpleType.DOUBLE)
            .addItem(MEAN_RATE_FOR_LAST_12_HOURS_FIELD, "Mean rate per second computed for the last 12 hours", SimpleType.DOUBLE)
            .addItem(MEAN_RATE_FOR_LAST_DAY_FIELD, "Mean rate per second computed for the last day", SimpleType.DOUBLE)
            //mean rate
            .addItem(MEAN_RATE_PER_SECOND_FIELD, "Mean rate per second", SimpleType.DOUBLE)
            .addItem(MEAN_RATE_PER_MINUTE_FIELD, "Mean rate per minute", SimpleType.DOUBLE)
            .addItem(MEAN_RATE_PER_5_MINUTES_FIELD, "Mean rate in 5-minutes interval", SimpleType.DOUBLE)
            .addItem(MEAN_RATE_PER_15_MINUTES_FIELD, "Mean rate in 15-minutes interval", SimpleType.DOUBLE)
            .addItem(MEAN_RATE_PER_HOUR_FIELD, "Mean rate per hour", SimpleType.DOUBLE)
            .addItem(MEAN_RATE_PER_12_HOURS_FIELD, "Mean rate in 12-hours interval", SimpleType.DOUBLE)
            .addItem(MEAN_RATE_PER_DAY_FIELD, "Mean rate per day", SimpleType.DOUBLE)
            //last rate
            .addItem(RATE_FOR_LAST_SECOND_FIELD, "Rate for the last second", SimpleType.LONG)
            .addItem(RATE_FOR_LAST_MINUTE_FIELD, "Rate for the last minute", SimpleType.LONG)
            .addItem(RATE_FOR_LAST_5_MINUTES_FIELD, "Rate for the last 5 minutes", SimpleType.LONG)
            .addItem(RATE_FOR_LAST_15_MINUTES_FIELD, "Rate last 15 minutes", SimpleType.LONG)
            .addItem(RATE_FOR_LAST_HOUR_FIELD, "Rate for the last hour", SimpleType.LONG)
            .addItem(RATE_FOR_LAST_12_HOURS_FIELD, "Rate for the last 12 hours", SimpleType.LONG)
            .addItem(RATE_FOR_LAST_DAY_FIELD, "Rate for the last day", SimpleType.LONG)
            //max rate
            .addItem(MAX_RATE_PER_SECOND_FIELD, "Max rate per second", SimpleType.LONG)
            .addItem(MAX_RATE_PER_MINUTE_FIELD, "Max rate per minute", SimpleType.LONG)
            .addItem(MAX_RATE_PER_5_MINUTES_FIELD, "Max rate in 5-minutes interval", SimpleType.LONG)
            .addItem(MAX_RATE_PER_15_MINUTES_FIELD, "Max rate in 15-minutes interval", SimpleType.LONG)
            .addItem(MAX_RATE_PER_HOUR_FIELD, "Max rate per hour", SimpleType.LONG)
            .addItem(MAX_RATE_PER_12_HOURS_FIELD, "Max rate in 12-hours interval", SimpleType.LONG)
            .addItem(MAX_RATE_PER_DAY_FIELD, "Max rate per day", SimpleType.LONG)
            //max rate per second
            .addItem(MAX_RATE_PER_SECOND_FOR_LAST_MINUTE_FIELD, "Max rate per second for the last minute", SimpleType.LONG)
            .addItem(MAX_RATE_PER_SECOND_FOR_LAST_5_MINUTES_FIELD, "Max rate per second for the last 5 minutes", SimpleType.LONG)
            .addItem(MAX_RATE_PER_SECOND_FOR_LAST_15_MINUTES_FIELD, "Max rate per second for the last 15 minutes", SimpleType.LONG)
            .addItem(MAX_RATE_PER_SECOND_FOR_LAST_HOUR_FIELD, "Max rate per second for the last hour", SimpleType.LONG)
            .addItem(MAX_RATE_PER_SECOND_FOR_LAST_12_HOURS_FIELD, "Max rate per second for the last 12 hours", SimpleType.LONG)
            .addItem(MAX_RATE_PER_SECOND_FOR_LAST_DAY_FIELD, "Max rate per second for the last day", SimpleType.LONG)
            //max rate per minute
            .addItem(MAX_RATE_PER_MINUTE_FOR_LAST_5_MINUTES_FIELD, "Max rate per minute for the last 5 minutes", SimpleType.LONG)
            .addItem(MAX_RATE_PER_MINUTE_FOR_LAST_15_MINUTES_FIELD, "Max rate per minute for the last 15 minutes", SimpleType.LONG)
            .addItem(MAX_RATE_PER_MINUTE_FOR_LAST_HOUR_FIELD, "Max rate per minute for the last hour", SimpleType.LONG)
            .addItem(MAX_RATE_PER_MINUTE_FOR_LAST_12_HOURS_FIELD, "Max rate per minute for the last 12 hours", SimpleType.LONG)
            .addItem(MAX_RATE_PER_MINUTE_FOR_LAST_DAY_FIELD, "Max rate per minute for the last day", SimpleType.LONG)
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
            .addItem(MIN_VALUE_FOR_LAST_SECOND_FIELD, "Minimum value for the last second", SimpleType.DOUBLE)
            .addItem(MIN_VALUE_FOR_LAST_MINUTE_FIELD, "Minimum value for the last minute", SimpleType.DOUBLE)
            .addItem(MIN_VALUE_FOR_LAST_5_MINUTES_FIELD, "Minimum value for the last five minutes", SimpleType.DOUBLE)
            .addItem(MIN_VALUE_FOR_LAST_15_MINUTES_FIELD, "Minimum value for the last fifteen minutes", SimpleType.DOUBLE)
            .addItem(MIN_VALUE_FOR_LAST_HOUR_FIELD, "Minimum value for the last hour", SimpleType.DOUBLE)
            .addItem(MIN_VALUE_FOR_LAST_12_HOURS_FIELD, "Minimum value for the last twelve hours", SimpleType.DOUBLE)
            .addItem(MIN_VALUE_FOR_LAST_DAY_FIELD, "Minimum value for the last day", SimpleType.DOUBLE)
            .build());

    /**
     * Represents Open Type equivalent of {@link RatedGaugeFP}.
     */
    public static final CompositeType RATED_GAUGE_FP_TYPE = interfaceStaticInitialize(() -> new CompositeTypeBuilder("com.bytex.snamp.metrics.RatedGaugeFP", "Floating-point gauge with rate support")
            .importFrom(GAUGE_FP_TYPE)
            .importFrom(RATE_TYPE)
            .build());

    /**
     * Converts {@link RatedGaugeFP} into {@link CompositeData}.
     * @param gauge A gauge to convert. Cannot be {@literal null}.
     * @return A {@link CompositeData} which contains gauge data.
     */
    public static CompositeData fromRatedGaugeFP(final RatedGaugeFP gauge){
        final CompositeDataFields fields = new CompositeDataFields(RATED_GAUGE_FP_TYPE);
        fillForRate(gauge, fields);
        fillForGaugeFP(gauge, fields);
        try {
            return new CompositeDataSupport(RATED_GAUGE_FP_TYPE, fields);
        } catch (final OpenDataException e) {
            throw new AssertionError(e);    //should never be happened
        }
    }

    private MetricsConverter(){
        throw new InstantiationError();
    }


    private static void fillForRate(final Rate rate, final CompositeDataFields output){
        output
                .put(TOTAL_RATE_FIELD, rate.getTotalRate())
                //mean rate for the last time
                .put(MEAN_RATE_FOR_LAST_SECOND_FIELD, rate.getLastMeanRate(MetricsInterval.SECOND))
                .put(MEAN_RATE_FOR_LAST_MINUTE_FIELD, rate.getLastMeanRate(MetricsInterval.MINUTE))
                .put(MEAN_RATE_FOR_LAST_5_MINUTES_FIELD, rate.getLastMeanRate(MetricsInterval.FIVE_MINUTES))
                .put(MEAN_RATE_FOR_LAST_15_MINUTES_FIELD, rate.getLastMeanRate(MetricsInterval.FIFTEEN_MINUTES))
                .put(MEAN_RATE_FOR_LAST_HOUR_FIELD, rate.getLastMeanRate(MetricsInterval.HOUR))
                .put(MEAN_RATE_FOR_LAST_12_HOURS_FIELD, rate.getLastMeanRate(MetricsInterval.TWELVE_HOURS))
                .put(MEAN_RATE_FOR_LAST_DAY_FIELD, rate.getLastMeanRate(MetricsInterval.DAY))
                //mean rate
                .put(MEAN_RATE_PER_SECOND_FIELD, rate.getMeanRate(MetricsInterval.SECOND))
                .put(MEAN_RATE_PER_MINUTE_FIELD, rate.getMeanRate(MetricsInterval.MINUTE))
                .put(MEAN_RATE_PER_5_MINUTES_FIELD, rate.getMeanRate(MetricsInterval.FIVE_MINUTES))
                .put(MEAN_RATE_PER_15_MINUTES_FIELD, rate.getMeanRate(MetricsInterval.FIFTEEN_MINUTES))
                .put(MEAN_RATE_PER_HOUR_FIELD, rate.getMeanRate(MetricsInterval.HOUR))
                .put(MEAN_RATE_PER_12_HOURS_FIELD, rate.getMeanRate(MetricsInterval.TWELVE_HOURS))
                .put(MEAN_RATE_PER_DAY_FIELD, rate.getMeanRate(MetricsInterval.DAY))
                //last rate
                .put(RATE_FOR_LAST_SECOND_FIELD, rate.getLastRate(MetricsInterval.SECOND))
                .put(RATE_FOR_LAST_MINUTE_FIELD, rate.getLastRate(MetricsInterval.MINUTE))
                .put(RATE_FOR_LAST_5_MINUTES_FIELD, rate.getLastRate(MetricsInterval.FIVE_MINUTES))
                .put(RATE_FOR_LAST_15_MINUTES_FIELD, rate.getLastRate(MetricsInterval.FIFTEEN_MINUTES))
                .put(RATE_FOR_LAST_HOUR_FIELD, rate.getLastRate(MetricsInterval.HOUR))
                .put(RATE_FOR_LAST_12_HOURS_FIELD, rate.getLastRate(MetricsInterval.TWELVE_HOURS))
                .put(RATE_FOR_LAST_DAY_FIELD, rate.getLastRate(MetricsInterval.DAY))
                //max rate
                .put(MAX_RATE_PER_SECOND_FIELD, rate.getMaxRate(MetricsInterval.SECOND))
                .put(MAX_RATE_PER_MINUTE_FIELD, rate.getMaxRate(MetricsInterval.MINUTE))
                .put(MAX_RATE_PER_5_MINUTES_FIELD, rate.getMaxRate(MetricsInterval.FIVE_MINUTES))
                .put(MAX_RATE_PER_15_MINUTES_FIELD, rate.getMaxRate(MetricsInterval.FIFTEEN_MINUTES))
                .put(MAX_RATE_PER_HOUR_FIELD, rate.getMaxRate(MetricsInterval.HOUR))
                .put(MAX_RATE_PER_12_HOURS_FIELD, rate.getMaxRate(MetricsInterval.TWELVE_HOURS))
                .put(MAX_RATE_PER_DAY_FIELD, rate.getMaxRate(MetricsInterval.DAY))
                //max rate per second
                .put(MAX_RATE_PER_SECOND_FOR_LAST_MINUTE_FIELD, rate.getLastMaxRatePerSecond(MetricsInterval.MINUTE))
                .put(MAX_RATE_PER_SECOND_FOR_LAST_5_MINUTES_FIELD, rate.getLastMaxRatePerSecond(MetricsInterval.FIVE_MINUTES))
                .put(MAX_RATE_PER_SECOND_FOR_LAST_15_MINUTES_FIELD, rate.getLastMaxRatePerSecond(MetricsInterval.FIFTEEN_MINUTES))
                .put(MAX_RATE_PER_SECOND_FOR_LAST_HOUR_FIELD, rate.getLastMaxRatePerSecond(MetricsInterval.HOUR))
                .put(MAX_RATE_PER_SECOND_FOR_LAST_12_HOURS_FIELD, rate.getLastMaxRatePerSecond(MetricsInterval.TWELVE_HOURS))
                .put(MAX_RATE_PER_SECOND_FOR_LAST_DAY_FIELD, rate.getLastMaxRatePerSecond(MetricsInterval.DAY))
                //max rate per minute
                .put(MAX_RATE_PER_MINUTE_FOR_LAST_5_MINUTES_FIELD, rate.getLastMaxRatePerSecond(MetricsInterval.FIVE_MINUTES))
                .put(MAX_RATE_PER_MINUTE_FOR_LAST_15_MINUTES_FIELD, rate.getLastMaxRatePerSecond(MetricsInterval.FIFTEEN_MINUTES))
                .put(MAX_RATE_PER_MINUTE_FOR_LAST_HOUR_FIELD, rate.getLastMaxRatePerSecond(MetricsInterval.HOUR))
                .put(MAX_RATE_PER_MINUTE_FOR_LAST_12_HOURS_FIELD, rate.getLastMaxRatePerSecond(MetricsInterval.TWELVE_HOURS))
                .put(MAX_RATE_PER_MINUTE_FOR_LAST_DAY_FIELD, rate.getLastMaxRatePerSecond(MetricsInterval.DAY));
    }

    /**
     * Converts {@link Rate} into {@link CompositeData}.
     * @param rate A counter to convert. Cannot be {@literal null}.
     * @return A {@link CompositeData} which contains counter data.
     */
    public static CompositeData fromRate(final Rate rate) {
        final CompositeDataFields result = new CompositeDataFields(RATE_TYPE);
        fillForRate(rate, result);
        try {
            return new CompositeDataSupport(RATE_TYPE, result);
        } catch (final OpenDataException e) {
            throw new AssertionError(e);    //should never be happened
        }
    }

    private static void fillForGaugeFP(final GaugeFP gauge, final CompositeDataFields output) {
        output
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
                .put(MIN_VALUE_FOR_LAST_DAY_FIELD, gauge.getLastMinValue(MetricsInterval.DAY));
    }

    /**
     * Converts {@link GaugeFP} into {@link CompositeData}.
     * @param gauge A gauge to convert. Cannot be {@literal null}.
     * @return A {@link CompositeData} which contains data from gauge.
     */
    public static CompositeData fromGaugeFP(final GaugeFP gauge) {
        final CompositeDataFields result = new CompositeDataFields(GAUGE_FP_TYPE);
        fillForGaugeFP(gauge, result);
        try {
            return new CompositeDataSupport(GAUGE_FP_TYPE, result);
        } catch (final OpenDataException e) {
            throw new AssertionError(e);    //should never be happened
        }
    }
}
