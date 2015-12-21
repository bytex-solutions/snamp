package com.bytex.snamp.management.jmx;

import com.bytex.snamp.jmx.OpenMBean;

import javax.management.openmbean.SimpleType;

/**
 * The type Statistic renewal time attribute.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class StatisticRenewalTimeAttribute extends OpenMBean.OpenAttribute<Long, SimpleType<Long>> {
    private final StatisticCounters counter;
    private static final String NAME = "StatisticRenewalTime";

    /**
     * Instantiates a new Statistic renewal time attribute.
     *
     * @param counter the counter
     */
    StatisticRenewalTimeAttribute(final StatisticCounters counter){
        super(NAME, SimpleType.LONG);
        this.counter = counter;
    }

    /**
     * Gets description of this attribute.
     *
     * @return The description of this attribute.
     */
    @Override
    protected String getDescription() {
        return "Renewal time for SNAMP statistics, in milliseconds. When renewal time comes then SNAMP resets all counters.";
    }

    @Override
    public Long getValue() {
        return counter.getRenewalTime();
    }

    @Override
    public void setValue(final Long value) {
        counter.setRenewalTime(value);
    }
}
