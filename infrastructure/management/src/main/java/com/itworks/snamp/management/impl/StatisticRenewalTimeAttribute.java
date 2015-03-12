package com.itworks.snamp.management.impl;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.jmx.OpenMBean;

import javax.management.openmbean.SimpleType;
import java.util.concurrent.TimeUnit;

/**
* @author Roman Sakno
* @version 1.0
* @since 1.0
*/
final class StatisticRenewalTimeAttribute extends OpenMBean.OpenAttribute<Long, SimpleType<Long>> {
    private final StatisticCounters counter;
    private static final String NAME = "StatisticRenewalTime";

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
        return counter.getRenewalTime().convert(TimeUnit.MILLISECONDS).duration;
    }

    @Override
    public void setValue(final Long value) {
        counter.setRenewalTime(new TimeSpan(value));
    }
}
