package com.bytex.snamp.jmx;

import com.bytex.snamp.connector.metrics.*;

import javax.management.openmbean.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Callable;

import static com.bytex.snamp.internal.Utils.*;

/**
 * Provides conversion between SNAMP metrics declared in {@link com.bytex.snamp.connector.metrics} package
 * and JMX open types.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class MetricsConverter {
    private static final class CompositeDataBuilder extends HashMap<String, Comparable<?>> implements Callable<CompositeData>{
        private static final long serialVersionUID = -1438136908623683624L;
        private final CompositeType type;

        private CompositeDataBuilder(final CompositeType prototype) {
            super(Math.round(prototype.keySet().size() * 1.5F));
            this.type = Objects.requireNonNull(prototype);
        }

        @Override
        public CompositeData call() throws OpenDataException {
            return new CompositeDataSupport(type, this);
        }

        private CompositeData build() {
            return callUnchecked(this);
        }

        private CompositeDataBuilder put(final String name, final long value){
            super.put(name, value);
            return this;
        }

        private CompositeDataBuilder put(final String name, final double value){
            super.put(name, value);
            return this;
        }

        private CompositeDataBuilder put(final String name, final String value){
            super.put(name, value);
            return this;
        }

        private CompositeDataBuilder put(final String name, final boolean value){
            super.put(name, value);
            return this;
        }

        private CompositeDataBuilder put(final String name, final Duration value){
            return put(name, value.toNanos() / 1_000_000_000D);     //convert to seconds
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

    //flag fields
    private static final String TRUE_TOTAL_COUNT_FIELD = "totalCountOfTrueValues";
    private static final String FALSE_TOTAL_COUNT_FIELD = "totalCountOfFalseValues";
    private static final String TRUE_COUNT_FOR_LAST_SECOND_FIELD = "totalCountOfTrueValuesLastSecond";
    private static final String TRUE_COUNT_FOR_LAST_MINUTE_FIELD = "totalCountOfTrueValuesLastMinute";
    private static final String TRUE_COUNT_FOR_LAST_5_MINUTES_FIELD = "totalCountOfTrueValuesLast5Minutes";
    private static final String TRUE_COUNT_FOR_LAST_15_MINUTES_FIELD = "totalCountOfTrueValuesLast15Minutes";
    private static final String TRUE_COUNT_FOR_LAST_HOUR_FIELD = "totalCountOfTrueValuesLastHour";
    private static final String TRUE_COUNT_FOR_LAST_12_HOURS_FIELD = "totalCountOfTrueValuesLast12Hours";
    private static final String TRUE_COUNT_FOR_LAST_DAY_FIELD = "totalCountOfTrueValuesLastDay";
    private static final String FALSE_COUNT_FOR_LAST_SECOND_FIELD = "totalCountOfFalseValuesLastSecond";
    private static final String FALSE_COUNT_FOR_LAST_MINUTE_FIELD = "totalCountOfFalseValuesLastMinute";
    private static final String FALSE_COUNT_FOR_LAST_5_MINUTES_FIELD = "totalCountOfFalseValuesLast5Minutes";
    private static final String FALSE_COUNT_FOR_LAST_15_MINUTES_FIELD = "totalCountOfFalseValuesLast15Minutes";
    private static final String FALSE_COUNT_FOR_LAST_HOUR_FIELD = "totalCountOfFalseValuesLastHour";
    private static final String FALSE_COUNT_FOR_LAST_12_HOURS_FIELD = "totalCountOfFalseValuesLast12Hours";
    private static final String FALSE_COUNT_FOR_LAST_DAY_FIELD = "totalCountOfFalseValuesLastDay";
    private static final String RATIO_FIELD = "ratio";
    private static final String RATIO_FOR_LAST_SECOND_FIELD = "ratioLastSecond";
    private static final String RATIO_FOR_LAST_MINUTE_FIELD = "ratioLastMinute";
    private static final String RATIO_FOR_LAST_5_MINUTES_FIELD = "ratioLast5Minutes";
    private static final String RATIO_FOR_LAST_15_MINUTES_FIELD = "ratioLast15Minutes";
    private static final String RATIO_FOR_LAST_HOUR_FIELD = "ratioLastHour";
    private static final String RATIO_FOR_LAST_12_HOURS_FIELD = "ratioLast12Hours";
    private static final String RATIO_FOR_LAST_DAY_FIELD = "ratioLastDay";
    //timer fields
    private static final String SUMMARY_VALUE_FIELD = "summaryValue";
    private static final String MEAN_DURATION_FIELD = "meanValue";
    private static final String MEAN_TASKS_PER_SECOND_FIELD = "meanTasksPerSecond";
    private static final String MEAN_TASKS_PER_MINUTE_FIELD = "meanTasksPerMinute";
    private static final String MEAN_TASKS_FOR_5_MINUTES_FIELD = "meanTasksFor5Minutes";
    private static final String MEAN_TASKS_FOR_15_MINUTES_FIELD = "meanTasksFor15Minutes";
    private static final String MEAN_TASKS_PER_HOUR_FIELD = "meanTasksPerHour";
    private static final String MEAN_TASKS_FOR_12_HOURS_FIELD = "meanTasksFor12Hours";
    private static final String MEAN_TASKS_PER_DAY_FIELD = "meanTasksPerDay";
    private static final String MAX_TASKS_PER_SECOND_FIELD = "maxTasksPerSecond";
    private static final String MAX_TASKS_PER_MINUTE_FIELD = "maxTasksPerMinute";
    private static final String MAX_TASKS_FOR_5_MINUTES_FIELD = "maxTasksFor5Minutes";
    private static final String MAX_TASKS_FOR_15_MINUTES_FIELD = "maxTasksFor15Minutes";
    private static final String MAX_TASKS_PER_HOUR_FIELD = "maxTasksPerHour";
    private static final String MAX_TASKS_FOR_12_HOURS_FIELD = "maxTasksFor12Hours";
    private static final String MAX_TASKS_PER_DAY_FIELD = "maxTasksPerDay";
    private static final String MIN_TASKS_PER_SECOND_FIELD = "minTasksPerSecond";
    private static final String MIN_TASKS_PER_MINUTE_FIELD = "minTasksPerMinute";
    private static final String MIN_TASKS_FOR_5_MINUTES_FIELD = "minTasksFor5Minutes";
    private static final String MIN_TASKS_FOR_15_MINUTES_FIELD = "minTasksFor15Minutes";
    private static final String MIN_TASKS_PER_HOUR_FIELD = "minTasksPerHour";
    private static final String MIN_TASKS_FOR_12_HOURS_FIELD = "minTasksFor12Hours";
    private static final String MIN_TASKS_PER_DAY_FIELD = "minTasksPerDay";
    //ranged fields
    private static final String LESS_THAN_RANGE_FIELD = "lessThanRange";
    private static final String LESS_THAN_RANGE_LAST_SECOND_FIELD = "lessThanRangeLastSecond";
    private static final String LESS_THAN_RANGE_LAST_MINUTE_FIELD = "lessThanRangeLastMinute";
    private static final String LESS_THAN_RANGE_LAST_5_MINUTES_FIELD = "lessThanRangeLast5Minutes";
    private static final String LESS_THAN_RANGE_LAST_15_MINUTES_FIELD = "lessThanRangeLast15Minutes";
    private static final String LESS_THAN_RANGE_LAST_HOUR_FIELD = "lessThanRangeLastHour";
    private static final String LESS_THAN_RANGE_LAST_12_HOURS_FIELD = "lessThanRangeLast12Hours";
    private static final String LESS_THAN_RANGE_LAST_DAY_FIELD = "lessThanRangeLastDay";
    private static final String GREATER_THAN_RANGE_FIELD = "greaterThanRange";
    private static final String GREATER_THAN_RANGE_LAST_SECOND_FIELD = "greaterThanRangeLastSecond";
    private static final String GREATER_THAN_RANGE_LAST_MINUTE_FIELD = "greaterThanRangeLastMinute";
    private static final String GREATER_THAN_RANGE_LAST_5_MINUTES_FIELD = "greaterThanRangeLast5Minutes";
    private static final String GREATER_THAN_RANGE_LAST_15_MINUTES_FIELD = "greaterThanRangeLast15Minutes";
    private static final String GREATER_THAN_RANGE_LAST_HOUR_FIELD = "greaterThanRangeLastHour";
    private static final String GREATER_THAN_RANGE_LAST_12_HOURS_FIELD = "greaterThanRangeLast12Hours";
    private static final String GREATER_THAN_RANGE_LAST_DAY_FIELD = "greaterThanRangeLastDay";
    private static final String IS_IN_RANGE_FIELD = "isInRange";
    private static final String IS_IN_RANGE_LAST_SECOND_FIELD = "isInRangeLastSecond";
    private static final String IS_IN_RANGE_LAST_MINUTE_FIELD = "isInRangeLastMinute";
    private static final String IS_IN_RANGE_LAST_5_MINUTES_FIELD = "isInRangeLast5Minutes";
    private static final String IS_IN_RANGE_LAST_15_MINUTES_FIELD = "isInRangeLast15Minutes";
    private static final String IS_IN_RANGE_LAST_HOUR_FIELD = "isInRangeLastHour";
    private static final String IS_IN_RANGE_LAST_12_HOURS_FIELD = "isInRangeLast12Hours";
    private static final String IS_IN_RANGE_LAST_DAY_FIELD = "isInRangeLastDay";

    /**
     * Represents Open Type equivalent for {@link Ranged}.
     */
    public static final CompositeType RANGED_TYPE = interfaceStaticInitialize(() -> new CompositeTypeBuilder("com.bytex.snamp.metrics.Ranged", "Ranged metric")
            //less than
            .addItem(LESS_THAN_RANGE_FIELD, "Number of recorded values that are less than normative", SimpleType.DOUBLE)
            .addItem(LESS_THAN_RANGE_LAST_SECOND_FIELD, "Number of recorded values that are less than normative for the last second", SimpleType.DOUBLE)
            .addItem(LESS_THAN_RANGE_LAST_MINUTE_FIELD, "Number of recorded values that are less than normative for the last minute", SimpleType.DOUBLE)
            .addItem(LESS_THAN_RANGE_LAST_5_MINUTES_FIELD, "Number of recorded values that are less than normative for the last 5 minutes", SimpleType.DOUBLE)
            .addItem(LESS_THAN_RANGE_LAST_15_MINUTES_FIELD, "Number of recorded values that are less than normative for the last 15 minutes", SimpleType.DOUBLE)
            .addItem(LESS_THAN_RANGE_LAST_HOUR_FIELD, "Number of recorded values that are less than normative for the last hour", SimpleType.DOUBLE)
            .addItem(LESS_THAN_RANGE_LAST_12_HOURS_FIELD, "Number of recorded values that are less than normative for the last 12 hours", SimpleType.DOUBLE)
            .addItem(LESS_THAN_RANGE_LAST_DAY_FIELD, "Number of recorded values that are less than normative for the last day", SimpleType.DOUBLE)
            //greater than
            .addItem(GREATER_THAN_RANGE_FIELD, "Number of recorded values that are less than normative", SimpleType.DOUBLE)
            .addItem(GREATER_THAN_RANGE_LAST_SECOND_FIELD, "Number of recorded values that are less than normative for the last second", SimpleType.DOUBLE)
            .addItem(GREATER_THAN_RANGE_LAST_MINUTE_FIELD, "Number of recorded values that are less than normative for the last minute", SimpleType.DOUBLE)
            .addItem(GREATER_THAN_RANGE_LAST_5_MINUTES_FIELD, "Number of recorded values that are less than normative for the last 5 minutes", SimpleType.DOUBLE)
            .addItem(GREATER_THAN_RANGE_LAST_15_MINUTES_FIELD, "Number of recorded values that are less than normative for the last 15 minutes", SimpleType.DOUBLE)
            .addItem(GREATER_THAN_RANGE_LAST_HOUR_FIELD, "Number of recorded values that are less than normative for the last hour", SimpleType.DOUBLE)
            .addItem(GREATER_THAN_RANGE_LAST_12_HOURS_FIELD, "Number of recorded values that are less than normative for the last 12 hours", SimpleType.DOUBLE)
            .addItem(GREATER_THAN_RANGE_LAST_DAY_FIELD, "Number of recorded values that are less than normative for the last day", SimpleType.DOUBLE)
            //normal values
            .addItem(IS_IN_RANGE_FIELD, "Number of recorded values that are less than normative", SimpleType.DOUBLE)
            .addItem(IS_IN_RANGE_LAST_SECOND_FIELD, "Number of recorded values that are less than normative for the last second", SimpleType.DOUBLE)
            .addItem(IS_IN_RANGE_LAST_MINUTE_FIELD, "Number of recorded values that are less than normative for the last minute", SimpleType.DOUBLE)
            .addItem(IS_IN_RANGE_LAST_5_MINUTES_FIELD, "Number of recorded values that are less than normative for the last 5 minutes", SimpleType.DOUBLE)
            .addItem(IS_IN_RANGE_LAST_15_MINUTES_FIELD, "Number of recorded values that are less than normative for the last 15 minutes", SimpleType.DOUBLE)
            .addItem(IS_IN_RANGE_LAST_HOUR_FIELD, "Number of recorded values that are less than normative for the last hour", SimpleType.DOUBLE)
            .addItem(IS_IN_RANGE_LAST_12_HOURS_FIELD, "Number of recorded values that are less than normative for the last 12 hours", SimpleType.DOUBLE)
            .addItem(IS_IN_RANGE_LAST_DAY_FIELD, "Number of recorded values that are less than normative for the last day", SimpleType.DOUBLE)
            .build());

    /**
     * Represents Open Type equivalent for {@link Timer}.
     */
    public static final CompositeType TIMER_TYPE = interfaceStaticInitialize(() -> new CompositeTypeBuilder("com.bytex.snamp.metrics.Timer", "Timing measurement")
            .addItem(SUMMARY_VALUE_FIELD, "Summary duration of all operations", SimpleType.DOUBLE)
            .addItem(MEAN_DURATION_FIELD, "Mean duration", SimpleType.DOUBLE)
            .addItem(MAX_VALUE_FIELD, "Maximum duration ever presented", SimpleType.DOUBLE)
            .addItem(LAST_VALUE_FIELD, "The last presented duration", SimpleType.DOUBLE)
            .addItem(MIN_VALUE_FIELD, "Minimum duration ever presented", SimpleType.DOUBLE)
            .addItem(STD_DEV_FIELD, "Standard deviation of duration", SimpleType.DOUBLE)
            .addItem(MEDIAN_VALUE_FIELD, "Median duration in historical perspective", SimpleType.DOUBLE)
            .addItem(PERCENTILE_90_FIELD, "Percentile 90", SimpleType.DOUBLE)
            .addItem(PERCENTILE_95_FIELD, "Percentile 95", SimpleType.DOUBLE)
            .addItem(PERCENTILE_97_FIELD, "Percentile 97", SimpleType.DOUBLE)
            //mean values
            .addItem(MEAN_VALUE_FOR_LAST_SECOND_FIELD, "Mean duration for the last second", SimpleType.DOUBLE)
            .addItem(MEAN_VALUE_FOR_LAST_MINUTE_FIELD, "Mean duration for the last minute", SimpleType.DOUBLE)
            .addItem(MEAN_VALUE_FOR_LAST_5_MINUTES_FIELD, "Mean duration for the last five minutes", SimpleType.DOUBLE)
            .addItem(MEAN_VALUE_FOR_LAST_15_MINUTES_FIELD, "Mean duration for the last fifteen minutes", SimpleType.DOUBLE)
            .addItem(MEAN_VALUE_FOR_LAST_HOUR_FIELD, "Mean duration for the last hour", SimpleType.DOUBLE)
            .addItem(MEAN_VALUE_FOR_LAST_12_HOURS_FIELD, "Mean duration for the last twelve hours", SimpleType.DOUBLE)
            .addItem(MEAN_VALUE_FOR_LAST_DAY_FIELD, "Mean duration for the last day", SimpleType.DOUBLE)
            //max values
            .addItem(MAX_VALUE_FOR_LAST_SECOND_FIELD, "Maximum duration for the last second", SimpleType.DOUBLE)
            .addItem(MAX_VALUE_FOR_LAST_MINUTE_FIELD, "Maximum duration for the last minute", SimpleType.DOUBLE)
            .addItem(MAX_VALUE_FOR_LAST_5_MINUTES_FIELD, "Maximum duration for the last five minutes", SimpleType.DOUBLE)
            .addItem(MAX_VALUE_FOR_LAST_15_MINUTES_FIELD, "Maximum duration for the last fifteen minutes", SimpleType.DOUBLE)
            .addItem(MAX_VALUE_FOR_LAST_HOUR_FIELD, "Maximum duration for the last hour", SimpleType.DOUBLE)
            .addItem(MAX_VALUE_FOR_LAST_12_HOURS_FIELD, "Maximum duration for the last twelve hours", SimpleType.DOUBLE)
            .addItem(MAX_VALUE_FOR_LAST_DAY_FIELD, "Maximum duration for the last day", SimpleType.DOUBLE)
            //min values
            .addItem(MIN_VALUE_FOR_LAST_SECOND_FIELD, "Minimum duration for the last second", SimpleType.DOUBLE)
            .addItem(MIN_VALUE_FOR_LAST_MINUTE_FIELD, "Minimum duration for the last minute", SimpleType.DOUBLE)
            .addItem(MIN_VALUE_FOR_LAST_5_MINUTES_FIELD, "Minimum duration for the last five minutes", SimpleType.DOUBLE)
            .addItem(MIN_VALUE_FOR_LAST_15_MINUTES_FIELD, "Minimum duration for the last fifteen minutes", SimpleType.DOUBLE)
            .addItem(MIN_VALUE_FOR_LAST_HOUR_FIELD, "Minimum duration for the last hour", SimpleType.DOUBLE)
            .addItem(MIN_VALUE_FOR_LAST_12_HOURS_FIELD, "Minimum duration for the last twelve hours", SimpleType.DOUBLE)
            .addItem(MIN_VALUE_FOR_LAST_DAY_FIELD, "Minimum duration for the last day", SimpleType.DOUBLE)
            //mean number of completed tasks
            .addItem(MEAN_TASKS_PER_SECOND_FIELD, "Mean number of completed tasks per second", SimpleType.DOUBLE)
            .addItem(MEAN_TASKS_PER_MINUTE_FIELD, "Mean number of completed tasks per minute", SimpleType.DOUBLE)
            .addItem(MEAN_TASKS_FOR_5_MINUTES_FIELD, "Mean number of completed tasks for 5 minutes", SimpleType.DOUBLE)
            .addItem(MEAN_TASKS_FOR_15_MINUTES_FIELD, "Mean number of completed tasks for 15 minutes", SimpleType.DOUBLE)
            .addItem(MEAN_TASKS_PER_HOUR_FIELD, "Mean number of completed tasks per hour", SimpleType.DOUBLE)
            .addItem(MEAN_TASKS_FOR_12_HOURS_FIELD, "Mean number of completed tasks for 12 hours", SimpleType.DOUBLE)
            .addItem(MEAN_TASKS_PER_DAY_FIELD, "Mean number of completed tasks per day", SimpleType.DOUBLE)
            //min number of completed tasks
            .addItem(MIN_TASKS_PER_SECOND_FIELD, "Min number of completed tasks per second", SimpleType.DOUBLE)
            .addItem(MIN_TASKS_PER_MINUTE_FIELD, "Min number of completed tasks per minute", SimpleType.DOUBLE)
            .addItem(MIN_TASKS_FOR_5_MINUTES_FIELD, "Min number of completed tasks for 5 minutes", SimpleType.DOUBLE)
            .addItem(MIN_TASKS_FOR_15_MINUTES_FIELD, "Min number of completed tasks for 15 minutes", SimpleType.DOUBLE)
            .addItem(MIN_TASKS_PER_HOUR_FIELD, "Min number of completed tasks per hour", SimpleType.DOUBLE)
            .addItem(MIN_TASKS_FOR_12_HOURS_FIELD, "Min number of completed tasks for 12 hours", SimpleType.DOUBLE)
            .addItem(MIN_TASKS_PER_DAY_FIELD, "Min number of completed tasks per day", SimpleType.DOUBLE)
            //max number of completed tasks
            .addItem(MAX_TASKS_PER_SECOND_FIELD, "Min number of completed tasks per second", SimpleType.DOUBLE)
            .addItem(MAX_TASKS_PER_MINUTE_FIELD, "Min number of completed tasks per minute", SimpleType.DOUBLE)
            .addItem(MAX_TASKS_FOR_5_MINUTES_FIELD, "Min number of completed tasks for 5 minutes", SimpleType.DOUBLE)
            .addItem(MAX_TASKS_FOR_15_MINUTES_FIELD, "Min number of completed tasks for 15 minutes", SimpleType.DOUBLE)
            .addItem(MAX_TASKS_PER_HOUR_FIELD, "Min number of completed tasks per hour", SimpleType.DOUBLE)
            .addItem(MAX_TASKS_FOR_12_HOURS_FIELD, "Min number of completed tasks for 12 hours", SimpleType.DOUBLE)
            .addItem(MAX_TASKS_PER_DAY_FIELD, "Min number of completed tasks per day", SimpleType.DOUBLE)
            .build());

    /**
     * Represents Open Type equivalent of {@link StringGauge}.
     */
    public static final CompositeType STRING_GAUGE_TYPE = interfaceStaticInitialize(() -> new CompositeTypeBuilder("com.bytex.snamp.metrics.StringGauge", "StringGauge")
            .addItem(MAX_VALUE_FIELD, "Maximum value ever presented", SimpleType.STRING)
            .addItem(LAST_VALUE_FIELD, "The last presented value", SimpleType.STRING)
            .addItem(MIN_VALUE_FIELD, "Minimum value ever presented", SimpleType.STRING)
            //max values
            .addItem(MAX_VALUE_FOR_LAST_SECOND_FIELD, "Maximum value for the last second", SimpleType.STRING)
            .addItem(MAX_VALUE_FOR_LAST_MINUTE_FIELD, "Maximum value for the last minute", SimpleType.STRING)
            .addItem(MAX_VALUE_FOR_LAST_5_MINUTES_FIELD, "Maximum value for the last five minutes", SimpleType.STRING)
            .addItem(MAX_VALUE_FOR_LAST_15_MINUTES_FIELD, "Maximum value for the last fifteen minutes", SimpleType.STRING)
            .addItem(MAX_VALUE_FOR_LAST_HOUR_FIELD, "Maximum value for the last hour", SimpleType.STRING)
            .addItem(MAX_VALUE_FOR_LAST_12_HOURS_FIELD, "Maximum value for the last twelve hours", SimpleType.STRING)
            .addItem(MAX_VALUE_FOR_LAST_DAY_FIELD, "Maximum value for the last day", SimpleType.STRING)
            //min values
            .addItem(MIN_VALUE_FOR_LAST_SECOND_FIELD, "Minimum value for the last second", SimpleType.STRING)
            .addItem(MIN_VALUE_FOR_LAST_MINUTE_FIELD, "Minimum value for the last minute", SimpleType.STRING)
            .addItem(MIN_VALUE_FOR_LAST_5_MINUTES_FIELD, "Minimum value for the last five minutes", SimpleType.STRING)
            .addItem(MIN_VALUE_FOR_LAST_15_MINUTES_FIELD, "Minimum value for the last fifteen minutes", SimpleType.STRING)
            .addItem(MIN_VALUE_FOR_LAST_HOUR_FIELD, "Minimum value for the last hour", SimpleType.STRING)
            .addItem(MIN_VALUE_FOR_LAST_12_HOURS_FIELD, "Minimum value for the last twelve hours", SimpleType.STRING)
            .addItem(MIN_VALUE_FOR_LAST_DAY_FIELD, "Minimum value for the last day", SimpleType.STRING)
            .build());

    /**
     * Represents Open Type equivalent of {@link Flag}.
     */
    public static final CompositeType FLAG_TYPE = interfaceStaticInitialize(() -> new CompositeTypeBuilder("com.bytex.snamp.metrics.Flag", "Flag")
        .addItem(LAST_VALUE_FIELD, "Last value submitted to gauge", SimpleType.BOOLEAN)
        .addItem(TRUE_TOTAL_COUNT_FIELD, "Total count of 'true' values", SimpleType.LONG)
        .addItem(FALSE_TOTAL_COUNT_FIELD, "Total count of 'false' values", SimpleType.LONG)
        //true count
        .addItem(TRUE_COUNT_FOR_LAST_SECOND_FIELD, "Count of 'true' values for the last second", SimpleType.LONG)
            .addItem(TRUE_COUNT_FOR_LAST_MINUTE_FIELD, "Count of 'true' values for the last minute", SimpleType.LONG)
            .addItem(TRUE_COUNT_FOR_LAST_5_MINUTES_FIELD, "Count of 'true' values for the last 5 minutes", SimpleType.LONG)
            .addItem(TRUE_COUNT_FOR_LAST_15_MINUTES_FIELD, "Count of 'true' values for the last 15 minutes", SimpleType.LONG)
            .addItem(TRUE_COUNT_FOR_LAST_HOUR_FIELD, "Count of 'true' values for the last hour", SimpleType.LONG)
            .addItem(TRUE_COUNT_FOR_LAST_12_HOURS_FIELD, "Count of 'true' values for the last 12 hours", SimpleType.LONG)
            .addItem(TRUE_COUNT_FOR_LAST_DAY_FIELD, "Count of 'true' values for the last day", SimpleType.LONG)
        //false count
            .addItem(FALSE_COUNT_FOR_LAST_SECOND_FIELD, "Count of 'false' values for the last second", SimpleType.LONG)
            .addItem(FALSE_COUNT_FOR_LAST_MINUTE_FIELD, "Count of 'false' values for the last minute", SimpleType.LONG)
            .addItem(FALSE_COUNT_FOR_LAST_5_MINUTES_FIELD, "Count of 'false' values for the last 5 minutes", SimpleType.LONG)
            .addItem(FALSE_COUNT_FOR_LAST_15_MINUTES_FIELD, "Count of 'false' values for the last 15 minutes", SimpleType.LONG)
            .addItem(FALSE_COUNT_FOR_LAST_HOUR_FIELD, "Count of 'false' values for the last hour", SimpleType.LONG)
            .addItem(FALSE_COUNT_FOR_LAST_12_HOURS_FIELD, "Count of 'false' values for the last 12 hours", SimpleType.LONG)
            .addItem(FALSE_COUNT_FOR_LAST_DAY_FIELD, "Count of 'false' values for the last day", SimpleType.LONG)
        //ratio
            .addItem(RATIO_FIELD, "Ratio between 'true' and 'false' values", SimpleType.DOUBLE)
            .addItem(RATIO_FOR_LAST_SECOND_FIELD, "Ratio between 'true' and 'false' values for the last second", SimpleType.DOUBLE)
            .addItem(RATIO_FOR_LAST_MINUTE_FIELD, "Ratio between 'true' and 'false' values for the last minute", SimpleType.DOUBLE)
            .addItem(RATIO_FOR_LAST_5_MINUTES_FIELD, "Ratio between 'true' and 'false' values for the last 5 minutes", SimpleType.DOUBLE)
            .addItem(RATIO_FOR_LAST_15_MINUTES_FIELD, "Ratio between 'true' and 'false' values for the last 15 minutes", SimpleType.DOUBLE)
            .addItem(RATIO_FOR_LAST_HOUR_FIELD, "Ratio between 'true' and 'false' values for the last hour", SimpleType.DOUBLE)
            .addItem(RATIO_FOR_LAST_12_HOURS_FIELD, "Ratio between 'true' and 'false' values for the last 12 hours", SimpleType.DOUBLE)
            .addItem(RATIO_FOR_LAST_DAY_FIELD, "Ratio between 'true' and 'false' values for the last day", SimpleType.DOUBLE)
            .build());

    /**
     * Represents Open Type equivalent of {@link Rate}.
     */
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
     * Represents Open Type equivalent of {@link GaugeFP}.
     */
    public static final CompositeType GAUGE_64_TYPE = interfaceStaticInitialize(() -> new CompositeTypeBuilder("com.bytex.snamp.metrics.Gauge64", "64-bit integer gauge")
            .addItem(MAX_VALUE_FIELD, "Maximum value ever presented", SimpleType.LONG)
            .addItem(LAST_VALUE_FIELD, "The last presented value", SimpleType.LONG)
            .addItem(MIN_VALUE_FIELD, "Minimum value ever presented", SimpleType.LONG)
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
            .addItem(MAX_VALUE_FOR_LAST_SECOND_FIELD, "Maximum value for the last second", SimpleType.LONG)
            .addItem(MAX_VALUE_FOR_LAST_MINUTE_FIELD, "Maximum value for the last minute", SimpleType.LONG)
            .addItem(MAX_VALUE_FOR_LAST_5_MINUTES_FIELD, "Maximum value for the last five minutes", SimpleType.LONG)
            .addItem(MAX_VALUE_FOR_LAST_15_MINUTES_FIELD, "Maximum value for the last fifteen minutes", SimpleType.LONG)
            .addItem(MAX_VALUE_FOR_LAST_HOUR_FIELD, "Maximum value for the last hour", SimpleType.LONG)
            .addItem(MAX_VALUE_FOR_LAST_12_HOURS_FIELD, "Maximum value for the last twelve hours", SimpleType.LONG)
            .addItem(MAX_VALUE_FOR_LAST_DAY_FIELD, "Maximum value for the last day", SimpleType.LONG)
            //min values
            .addItem(MIN_VALUE_FOR_LAST_SECOND_FIELD, "Minimum value for the last second", SimpleType.LONG)
            .addItem(MIN_VALUE_FOR_LAST_MINUTE_FIELD, "Minimum value for the last minute", SimpleType.LONG)
            .addItem(MIN_VALUE_FOR_LAST_5_MINUTES_FIELD, "Minimum value for the last five minutes", SimpleType.LONG)
            .addItem(MIN_VALUE_FOR_LAST_15_MINUTES_FIELD, "Minimum value for the last fifteen minutes", SimpleType.LONG)
            .addItem(MIN_VALUE_FOR_LAST_HOUR_FIELD, "Minimum value for the last hour", SimpleType.LONG)
            .addItem(MIN_VALUE_FOR_LAST_12_HOURS_FIELD, "Minimum value for the last twelve hours", SimpleType.LONG)
            .addItem(MIN_VALUE_FOR_LAST_DAY_FIELD, "Minimum value for the last day", SimpleType.LONG)
            .build());

    /**
     * Represents Open Type equivalent of {@link RatedGaugeFP}.
     */
    public static final CompositeType RATED_GAUGE_FP_TYPE = interfaceStaticInitialize(() -> new CompositeTypeBuilder("com.bytex.snamp.metrics.RatedGaugeFP", "Floating-point gauge with rate support")
            .importFrom(GAUGE_FP_TYPE)
            .importFrom(RATE_TYPE)
            .build());

    /**
     * Represents Open Type equivalent of {@link RatedStringGauge}.
     */
    public static final CompositeType RATED_STRING_GAUGE_TYPE = interfaceStaticInitialize(() -> new CompositeTypeBuilder("com.bytex.snamp.metrics.RatedStringGauge", "Rated StringGauge")
            .importFrom(STRING_GAUGE_TYPE)
            .importFrom(RATE_TYPE)
            .build());

    /**
     * Represents Open Type equivalent for {@link RatedGauge64}.
     */
    public static final CompositeType RATED_GAUGE_64_TYPE = interfaceStaticInitialize(() -> new CompositeTypeBuilder("com.bytex.snamp.metrics.RatedGauge64", "64-bit integer gauge with rate support")
            .importFrom(GAUGE_64_TYPE)
            .importFrom(RATE_TYPE)
            .build());

    /**
     * Represents Open Type equivalent for {@link RatedFlag}.
     */
    public static final CompositeType RATED_FLAG_TYPE = interfaceStaticInitialize(() -> new CompositeTypeBuilder("com.bytex.snamp.metrics.RatedFlag", "Flag with rate support")
            .importFrom(FLAG_TYPE)
            .importFrom(RATE_TYPE)
            .build());

    /**
     * Represents Open Type equivalent for {@link RatedTimer}.
     */
    public static final CompositeType RATED_TIMER_TYPE = interfaceStaticInitialize(() -> new CompositeTypeBuilder("com.bytex.snamp.metrics.RatedTimer", "Timing measurements with rate")
            .importFrom(TIMER_TYPE)
            .importFrom(RATE_TYPE)
            .build());

    /**
     * Represents Open Type equivalent for {@link RangedGauge64}.
     */
    public static final CompositeType RANGED_GAUGE_64_TYPE = interfaceStaticInitialize(() -> new CompositeTypeBuilder("com.bytex.snamp.metrics.RangedGauge64", "Gauge64 with normative range")
            .importFrom(RANGED_TYPE)
            .importFrom(RATED_GAUGE_64_TYPE)
            .build());

    /**
     * Represents Open Type equivalent for {@link RangedGaugeFP}.
     */
    public static final CompositeType RANGED_GAUGE_FP_TYPE = interfaceStaticInitialize(() -> new CompositeTypeBuilder("com.bytex.snamp.metrics.RangedGaugeFP", "GaugeFP with normative range")
            .importFrom(RANGED_TYPE)
            .importFrom(RATED_GAUGE_FP_TYPE)
            .build());

    /**
     * Represents Open Type equivalent for {@link RangedTimer}.
     */
    public static final CompositeType RANGED_TIMER_TYPE = interfaceStaticInitialize(() -> new CompositeTypeBuilder("com.bytex.snamp.metrics.RangedTimer", "Timer with normative time limits")
            .importFrom(RANGED_TYPE)
            .importFrom(RATED_TIMER_TYPE)
            .build());

    /**
     * Converts {@link RatedFlag} into {@link CompositeData}.
     * @param flag A gauge to convert. Cannot be {@literal null}.
     * @return A {@link CompositeData} which contains gauge data.
     */
    public static CompositeData fromRatedFlag(final RatedFlag flag){
        final CompositeDataBuilder fields = new CompositeDataBuilder(RATED_FLAG_TYPE);
        fillFlag(flag, fields);
        fillRate(flag, fields);
        return fields.build();
    }

    /**
     * Converts {@link RatedGauge64} into {@link CompositeData}.
     * @param gauge A gauge to convert. Cannot be {@literal null}.
     * @return A {@link CompositeData} which contains gauge data.
     */
    public static CompositeData fromRatedGauge64(final RatedGauge64 gauge){
        final CompositeDataBuilder fields = new CompositeDataBuilder(RATED_GAUGE_64_TYPE);
        fillRate(gauge, fields);
        fillGauge64(gauge, fields);
        return fields.build();
    }

    /**
     * Converts {@link RatedGaugeFP} into {@link CompositeData}.
     * @param gauge A gauge to convert. Cannot be {@literal null}.
     * @return A {@link CompositeData} which contains gauge data.
     */
    public static CompositeData fromRatedGaugeFP(final RatedGaugeFP gauge){
        final CompositeDataBuilder fields = new CompositeDataBuilder(RATED_GAUGE_FP_TYPE);
        fillRate(gauge, fields);
        fillGaugeFP(gauge, fields);
        return fields.build();
    }

    private MetricsConverter(){
        throw new InstantiationError();
    }


    private static void fillRate(final Rate rate, final CompositeDataBuilder output){
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
        final CompositeDataBuilder result = new CompositeDataBuilder(RATE_TYPE);
        fillRate(rate, result);
        return result.build();
    }

    private static void fillGaugeFP(final GaugeFP gauge, final CompositeDataBuilder output) {
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
                .put(MEAN_VALUE_FOR_LAST_SECOND_FIELD, gauge.getLastMeanValue(MetricsInterval.SECOND))
                .put(MEAN_VALUE_FOR_LAST_MINUTE_FIELD, gauge.getLastMeanValue(MetricsInterval.MINUTE))
                .put(MEAN_VALUE_FOR_LAST_5_MINUTES_FIELD, gauge.getLastMeanValue(MetricsInterval.FIVE_MINUTES))
                .put(MEAN_VALUE_FOR_LAST_15_MINUTES_FIELD, gauge.getLastMeanValue(MetricsInterval.FIFTEEN_MINUTES))
                .put(MEAN_VALUE_FOR_LAST_HOUR_FIELD, gauge.getLastMeanValue(MetricsInterval.HOUR))
                .put(MEAN_VALUE_FOR_LAST_12_HOURS_FIELD, gauge.getLastMeanValue(MetricsInterval.TWELVE_HOURS))
                .put(MEAN_VALUE_FOR_LAST_DAY_FIELD, gauge.getLastMeanValue(MetricsInterval.DAY))
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
        final CompositeDataBuilder result = new CompositeDataBuilder(GAUGE_FP_TYPE);
        fillGaugeFP(gauge, result);
        return result.build();
    }

    private static void fillStringGauge(final StringGauge gauge, final CompositeDataBuilder output){
        output
                .put(MAX_VALUE_FIELD, gauge.getMaxValue())
                .put(LAST_VALUE_FIELD, gauge.getLastValue())
                .put(MIN_VALUE_FIELD, gauge.getMinValue())
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
     * Converts {@link StringGauge} into {@link CompositeData}.
     * @param gauge A gauge to convert. Cannot be {@literal null}.
     * @return A {@link CompositeData} which contains data from gauge.
     */
    public static CompositeData fromStringGauge(final StringGauge gauge){
        final CompositeDataBuilder fields = new CompositeDataBuilder(STRING_GAUGE_TYPE);
        fillStringGauge(gauge, fields);
        return fields.build();
    }

    /**
     * Converts {@link RatedStringGauge} into {@link CompositeData}.
     * @param gauge A gauge to convert. Cannot be {@literal null}.
     * @return A {@link CompositeData} which contains data from gauge.
     */
    public static CompositeData fromRatedStringGauge(final RatedStringGauge gauge){
        final CompositeDataBuilder fields = new CompositeDataBuilder(RATED_STRING_GAUGE_TYPE);
        fillStringGauge(gauge, fields);
        fillRate(gauge, fields);
        return fields.build();
    }

    private static void fillGauge64(final Gauge64 gauge, final CompositeDataBuilder output) {
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
                .put(MEAN_VALUE_FOR_LAST_SECOND_FIELD, gauge.getLastMeanValue(MetricsInterval.SECOND))
                .put(MEAN_VALUE_FOR_LAST_MINUTE_FIELD, gauge.getLastMeanValue(MetricsInterval.MINUTE))
                .put(MEAN_VALUE_FOR_LAST_5_MINUTES_FIELD, gauge.getLastMeanValue(MetricsInterval.FIVE_MINUTES))
                .put(MEAN_VALUE_FOR_LAST_15_MINUTES_FIELD, gauge.getLastMeanValue(MetricsInterval.FIFTEEN_MINUTES))
                .put(MEAN_VALUE_FOR_LAST_HOUR_FIELD, gauge.getLastMeanValue(MetricsInterval.HOUR))
                .put(MEAN_VALUE_FOR_LAST_12_HOURS_FIELD, gauge.getLastMeanValue(MetricsInterval.TWELVE_HOURS))
                .put(MEAN_VALUE_FOR_LAST_DAY_FIELD, gauge.getLastMeanValue(MetricsInterval.DAY))
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
     * Converts {@link Gauge64} into {@link CompositeData}.
     * @param gauge A gauge to convert. Cannot be {@literal null}.
     * @return A {@link CompositeData} which contains data from gauge.
     */
    public static CompositeData fromGauge64(final Gauge64 gauge){
        final CompositeDataBuilder result = new CompositeDataBuilder(GAUGE_64_TYPE);
        fillGauge64(gauge, result);
        return result.build();
    }

    private static void fillFlag(final Flag flag, final CompositeDataBuilder fields){
        fields
                .put(LAST_VALUE_FIELD, flag.getAsBoolean())
                .put(TRUE_TOTAL_COUNT_FIELD, flag.getTotalCount(true))
                .put(FALSE_TOTAL_COUNT_FIELD, flag.getTotalCount(false))
                //true count
                .put(TRUE_COUNT_FOR_LAST_SECOND_FIELD, flag.getLastCount(MetricsInterval.SECOND, true))
                .put(TRUE_COUNT_FOR_LAST_MINUTE_FIELD, flag.getLastCount(MetricsInterval.MINUTE, true))
                .put(TRUE_COUNT_FOR_LAST_5_MINUTES_FIELD, flag.getLastCount(MetricsInterval.FIVE_MINUTES, true))
                .put(TRUE_COUNT_FOR_LAST_15_MINUTES_FIELD, flag.getLastCount(MetricsInterval.FIFTEEN_MINUTES, true))
                .put(TRUE_COUNT_FOR_LAST_HOUR_FIELD, flag.getLastCount(MetricsInterval.HOUR, true))
                .put(TRUE_COUNT_FOR_LAST_12_HOURS_FIELD, flag.getLastCount(MetricsInterval.TWELVE_HOURS, true))
                .put(TRUE_COUNT_FOR_LAST_DAY_FIELD, flag.getLastCount(MetricsInterval.DAY, true))
                //false count
                .put(FALSE_COUNT_FOR_LAST_SECOND_FIELD, flag.getLastCount(MetricsInterval.SECOND, false))
                .put(FALSE_COUNT_FOR_LAST_MINUTE_FIELD, flag.getLastCount(MetricsInterval.MINUTE, false))
                .put(FALSE_COUNT_FOR_LAST_5_MINUTES_FIELD, flag.getLastCount(MetricsInterval.FIVE_MINUTES, false))
                .put(FALSE_COUNT_FOR_LAST_15_MINUTES_FIELD, flag.getLastCount(MetricsInterval.FIFTEEN_MINUTES, false))
                .put(FALSE_COUNT_FOR_LAST_HOUR_FIELD, flag.getLastCount(MetricsInterval.HOUR, false))
                .put(FALSE_COUNT_FOR_LAST_12_HOURS_FIELD, flag.getLastCount(MetricsInterval.TWELVE_HOURS, false))
                .put(FALSE_COUNT_FOR_LAST_DAY_FIELD, flag.getLastCount(MetricsInterval.DAY, false))
                //ratio
                .put(RATIO_FIELD, flag.getTotalRatio())
                .put(RATIO_FOR_LAST_SECOND_FIELD, flag.getLastRatio(MetricsInterval.SECOND))
                .put(RATIO_FOR_LAST_MINUTE_FIELD, flag.getLastRatio(MetricsInterval.MINUTE))
                .put(RATIO_FOR_LAST_5_MINUTES_FIELD, flag.getLastRatio(MetricsInterval.FIVE_MINUTES))
                .put(RATIO_FOR_LAST_15_MINUTES_FIELD, flag.getLastRatio(MetricsInterval.FIFTEEN_MINUTES))
                .put(RATIO_FOR_LAST_HOUR_FIELD, flag.getLastRatio(MetricsInterval.HOUR))
                .put(RATIO_FOR_LAST_12_HOURS_FIELD, flag.getLastRatio(MetricsInterval.TWELVE_HOURS))
                .put(RATIO_FOR_LAST_DAY_FIELD, flag.getLastRatio(MetricsInterval.DAY));
    }

    /**
     * Converts {@link Flag} into {@link CompositeData}.
     * @param flag A gauge to convert. Cannot be {@literal null}.
     * @return A {@link CompositeData} which contains data from gauge.
     */
    public static CompositeData fromFlag(final Flag flag){
        final CompositeDataBuilder fields = new CompositeDataBuilder(FLAG_TYPE);
        fillFlag(flag, fields);
        return fields.build();
    }

    private static void fillTimer(final Timer timer, final CompositeDataBuilder output) {
        output
                .put(SUMMARY_VALUE_FIELD, timer.getSummaryValue())
                .put(MEAN_DURATION_FIELD, timer.getMeanValue())
                .put(MAX_VALUE_FIELD, timer.getMaxValue())
                .put(LAST_VALUE_FIELD, timer.getLastValue())
                .put(MIN_VALUE_FIELD, timer.getMinValue())
                .put(STD_DEV_FIELD, timer.getDeviation())
                .put(MEDIAN_VALUE_FIELD, timer.getQuantile(0.5D))
                .put(PERCENTILE_90_FIELD, timer.getQuantile(0.9D))
                .put(PERCENTILE_95_FIELD, timer.getQuantile(0.95D))
                .put(PERCENTILE_97_FIELD, timer.getQuantile(0.97D))
                //mean values
                .put(MEAN_VALUE_FOR_LAST_SECOND_FIELD, timer.getLastMeanValue(MetricsInterval.SECOND))
                .put(MEAN_VALUE_FOR_LAST_MINUTE_FIELD, timer.getLastMeanValue(MetricsInterval.MINUTE))
                .put(MEAN_VALUE_FOR_LAST_5_MINUTES_FIELD, timer.getLastMeanValue(MetricsInterval.FIVE_MINUTES))
                .put(MEAN_VALUE_FOR_LAST_15_MINUTES_FIELD, timer.getLastMeanValue(MetricsInterval.FIFTEEN_MINUTES))
                .put(MEAN_VALUE_FOR_LAST_HOUR_FIELD, timer.getLastMeanValue(MetricsInterval.HOUR))
                .put(MEAN_VALUE_FOR_LAST_12_HOURS_FIELD, timer.getLastMeanValue(MetricsInterval.TWELVE_HOURS))
                .put(MEAN_VALUE_FOR_LAST_DAY_FIELD, timer.getLastMeanValue(MetricsInterval.DAY))
                //max values
                .put(MAX_VALUE_FOR_LAST_SECOND_FIELD, timer.getLastMaxValue(MetricsInterval.SECOND))
                .put(MAX_VALUE_FOR_LAST_MINUTE_FIELD, timer.getLastMaxValue(MetricsInterval.MINUTE))
                .put(MAX_VALUE_FOR_LAST_5_MINUTES_FIELD, timer.getLastMaxValue(MetricsInterval.FIVE_MINUTES))
                .put(MAX_VALUE_FOR_LAST_15_MINUTES_FIELD, timer.getLastMaxValue(MetricsInterval.FIFTEEN_MINUTES))
                .put(MAX_VALUE_FOR_LAST_HOUR_FIELD, timer.getLastMaxValue(MetricsInterval.HOUR))
                .put(MAX_VALUE_FOR_LAST_12_HOURS_FIELD, timer.getLastMaxValue(MetricsInterval.TWELVE_HOURS))
                .put(MAX_VALUE_FOR_LAST_DAY_FIELD, timer.getLastMaxValue(MetricsInterval.DAY))
                //min values
                .put(MIN_VALUE_FOR_LAST_SECOND_FIELD, timer.getLastMinValue(MetricsInterval.SECOND))
                .put(MIN_VALUE_FOR_LAST_MINUTE_FIELD, timer.getLastMinValue(MetricsInterval.MINUTE))
                .put(MIN_VALUE_FOR_LAST_5_MINUTES_FIELD, timer.getLastMinValue(MetricsInterval.FIVE_MINUTES))
                .put(MIN_VALUE_FOR_LAST_15_MINUTES_FIELD, timer.getLastMinValue(MetricsInterval.FIFTEEN_MINUTES))
                .put(MIN_VALUE_FOR_LAST_HOUR_FIELD, timer.getLastMinValue(MetricsInterval.HOUR))
                .put(MIN_VALUE_FOR_LAST_12_HOURS_FIELD, timer.getLastMinValue(MetricsInterval.TWELVE_HOURS))
                .put(MIN_VALUE_FOR_LAST_DAY_FIELD, timer.getLastMinValue(MetricsInterval.DAY))
                //mean number of completed tasks
                .put(MEAN_TASKS_PER_SECOND_FIELD, timer.getMeanNumberOfCompletedTasks(MetricsInterval.SECOND))
                .put(MEAN_TASKS_PER_MINUTE_FIELD, timer.getMeanNumberOfCompletedTasks(MetricsInterval.MINUTE))
                .put(MEAN_TASKS_FOR_5_MINUTES_FIELD, timer.getMeanNumberOfCompletedTasks(MetricsInterval.FIVE_MINUTES))
                .put(MEAN_TASKS_FOR_15_MINUTES_FIELD, timer.getMeanNumberOfCompletedTasks(MetricsInterval.FIFTEEN_MINUTES))
                .put(MEAN_TASKS_PER_HOUR_FIELD, timer.getMeanNumberOfCompletedTasks(MetricsInterval.HOUR))
                .put(MEAN_TASKS_FOR_12_HOURS_FIELD, timer.getMeanNumberOfCompletedTasks(MetricsInterval.TWELVE_HOURS))
                .put(MEAN_TASKS_PER_DAY_FIELD, timer.getMeanNumberOfCompletedTasks(MetricsInterval.DAY))
                //min number of completed tasks
                .put(MIN_TASKS_PER_SECOND_FIELD, timer.getMinNumberOfCompletedTasks(MetricsInterval.SECOND))
                .put(MIN_TASKS_PER_MINUTE_FIELD, timer.getMinNumberOfCompletedTasks(MetricsInterval.MINUTE))
                .put(MIN_TASKS_FOR_5_MINUTES_FIELD, timer.getMinNumberOfCompletedTasks(MetricsInterval.FIVE_MINUTES))
                .put(MIN_TASKS_FOR_15_MINUTES_FIELD, timer.getMinNumberOfCompletedTasks(MetricsInterval.FIFTEEN_MINUTES))
                .put(MIN_TASKS_PER_HOUR_FIELD, timer.getMinNumberOfCompletedTasks(MetricsInterval.HOUR))
                .put(MIN_TASKS_FOR_12_HOURS_FIELD, timer.getMinNumberOfCompletedTasks(MetricsInterval.TWELVE_HOURS))
                .put(MIN_TASKS_PER_DAY_FIELD, timer.getMinNumberOfCompletedTasks(MetricsInterval.DAY))
                //max number of completed tasks
                .put(MAX_TASKS_PER_SECOND_FIELD, timer.getMaxNumberOfCompletedTasks(MetricsInterval.SECOND))
                .put(MAX_TASKS_PER_MINUTE_FIELD, timer.getMaxNumberOfCompletedTasks(MetricsInterval.MINUTE))
                .put(MAX_TASKS_FOR_5_MINUTES_FIELD, timer.getMaxNumberOfCompletedTasks(MetricsInterval.FIVE_MINUTES))
                .put(MAX_TASKS_FOR_15_MINUTES_FIELD, timer.getMaxNumberOfCompletedTasks(MetricsInterval.FIFTEEN_MINUTES))
                .put(MAX_TASKS_PER_HOUR_FIELD, timer.getMaxNumberOfCompletedTasks(MetricsInterval.HOUR))
                .put(MAX_TASKS_FOR_12_HOURS_FIELD, timer.getMaxNumberOfCompletedTasks(MetricsInterval.TWELVE_HOURS))
                .put(MAX_TASKS_PER_DAY_FIELD, timer.getMaxNumberOfCompletedTasks(MetricsInterval.DAY));
    }

    /**
     * Converts {@link Timer} into {@link CompositeData}.
     * @param timer A timer to convert. Cannot be {@literal null}.
     * @return A {@link CompositeData} which contains data from timer.
     */
    public static CompositeData fromTimer(final Timer timer){
        final CompositeDataBuilder fields = new CompositeDataBuilder(TIMER_TYPE);
        fillTimer(timer, fields);
        return fields.build();
    }

    /**
     * Converts {@link RatedTimer} into {@link CompositeData}.
     * @param timer A timer to convert. Cannot be {@literal null}.
     * @return A {@link CompositeData} which contains data from timer.
     */
    public static CompositeData fromRatedTimer(final RatedTimer timer){
        final CompositeDataBuilder fields = new CompositeDataBuilder(RATED_TIMER_TYPE);
        fillTimer(timer, fields);
        fillRate(timer, fields);
        return fields.build();
    }

    private static void fillRanged(final Ranged ranged, final CompositeDataBuilder output){
        output
                //less than
                .put(LESS_THAN_RANGE_FIELD, ranged.getPercentOfLessThanRange())
                .put(LESS_THAN_RANGE_LAST_SECOND_FIELD, ranged.getPercentOfLessThanRange(MetricsInterval.SECOND))
                .put(LESS_THAN_RANGE_LAST_MINUTE_FIELD, ranged.getPercentOfLessThanRange(MetricsInterval.MINUTE))
                .put(LESS_THAN_RANGE_LAST_5_MINUTES_FIELD, ranged.getPercentOfLessThanRange(MetricsInterval.FIVE_MINUTES))
                .put(LESS_THAN_RANGE_LAST_15_MINUTES_FIELD, ranged.getPercentOfLessThanRange(MetricsInterval.FIFTEEN_MINUTES))
                .put(LESS_THAN_RANGE_LAST_HOUR_FIELD, ranged.getPercentOfLessThanRange(MetricsInterval.HOUR))
                .put(LESS_THAN_RANGE_LAST_12_HOURS_FIELD, ranged.getPercentOfLessThanRange(MetricsInterval.TWELVE_HOURS))
                .put(LESS_THAN_RANGE_LAST_DAY_FIELD, ranged.getPercentOfLessThanRange(MetricsInterval.DAY))
                //greater than
                .put(GREATER_THAN_RANGE_FIELD, ranged.getPercentOfGreaterThanRange())
                .put(GREATER_THAN_RANGE_LAST_SECOND_FIELD, ranged.getPercentOfGreaterThanRange(MetricsInterval.SECOND))
                .put(GREATER_THAN_RANGE_LAST_MINUTE_FIELD, ranged.getPercentOfGreaterThanRange(MetricsInterval.MINUTE))
                .put(GREATER_THAN_RANGE_LAST_5_MINUTES_FIELD, ranged.getPercentOfGreaterThanRange(MetricsInterval.FIVE_MINUTES))
                .put(GREATER_THAN_RANGE_LAST_15_MINUTES_FIELD, ranged.getPercentOfGreaterThanRange(MetricsInterval.FIFTEEN_MINUTES))
                .put(GREATER_THAN_RANGE_LAST_HOUR_FIELD, ranged.getPercentOfGreaterThanRange(MetricsInterval.HOUR))
                .put(GREATER_THAN_RANGE_LAST_12_HOURS_FIELD, ranged.getPercentOfGreaterThanRange(MetricsInterval.TWELVE_HOURS))
                .put(GREATER_THAN_RANGE_LAST_DAY_FIELD, ranged.getPercentOfGreaterThanRange(MetricsInterval.DAY))
                //normative
                .put(IS_IN_RANGE_FIELD, ranged.getPercentOfValuesIsInRange())
                .put(IS_IN_RANGE_LAST_SECOND_FIELD, ranged.getPercentOfValuesIsInRange(MetricsInterval.SECOND))
                .put(IS_IN_RANGE_LAST_MINUTE_FIELD, ranged.getPercentOfValuesIsInRange(MetricsInterval.MINUTE))
                .put(IS_IN_RANGE_LAST_5_MINUTES_FIELD, ranged.getPercentOfValuesIsInRange(MetricsInterval.FIVE_MINUTES))
                .put(IS_IN_RANGE_LAST_15_MINUTES_FIELD, ranged.getPercentOfValuesIsInRange(MetricsInterval.FIFTEEN_MINUTES))
                .put(IS_IN_RANGE_LAST_HOUR_FIELD, ranged.getPercentOfValuesIsInRange(MetricsInterval.HOUR))
                .put(IS_IN_RANGE_LAST_12_HOURS_FIELD, ranged.getPercentOfValuesIsInRange(MetricsInterval.TWELVE_HOURS))
                .put(IS_IN_RANGE_LAST_DAY_FIELD, ranged.getPercentOfValuesIsInRange(MetricsInterval.DAY));
    }

    /**
     * Converts {@link Ranged} into {@link CompositeData}.
     * @param ranged A normative to convert. Cannot be {@literal null}.
     * @return A {@link CompositeData} which contains data from normative.
     */
    public static CompositeData fromRanged(final Ranged ranged){
        final CompositeDataBuilder fields = new CompositeDataBuilder(RANGED_TYPE);
        fillRanged(ranged, fields);
        return fields.build();
    }

    /**
     * Converts {@link RangedGauge64} into {@link CompositeData}.
     * @param normative A normative to convert. Cannot be {@literal null}.
     * @return A {@link CompositeData} which contains data from normative.
     */
    public static CompositeData fromRanged64(final RangedGauge64 normative) {
        final CompositeDataBuilder fields = new CompositeDataBuilder(RANGED_GAUGE_64_TYPE);
        fillRanged(normative, fields);
        fillRate(normative, fields);
        fillGauge64(normative, fields);
        return fields.build();
    }

    /**
     * Converts {@link RangedGaugeFP} into {@link CompositeData}.
     * @param normative A normative to convert. Cannot be {@literal null}.
     * @return A {@link CompositeData} which contains data from normative.
     */
    public static CompositeData fromRangedFP(final RangedGaugeFP normative) {
        final CompositeDataBuilder fields = new CompositeDataBuilder(RANGED_GAUGE_FP_TYPE);
        fillRanged(normative, fields);
        fillRate(normative, fields);
        fillGaugeFP(normative, fields);
        return fields.build();
    }

    /**
     * Converts {@link RangedTimer} into {@link CompositeData}.
     * @param normative A normative to convert. Cannot be {@literal null}.
     * @return A {@link CompositeData} which contains data from normative.
     */
    public static CompositeData fromRangedTimer(final RangedTimer normative){
        final CompositeDataBuilder fields = new CompositeDataBuilder(RANGED_TIMER_TYPE);
        fillRanged(normative, fields);
        fillTimer(normative, fields);
        fillRate(normative, fields);
        return fields.build();
    }
}
