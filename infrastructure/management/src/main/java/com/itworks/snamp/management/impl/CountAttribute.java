package com.itworks.snamp.management.impl;

import com.itworks.snamp.management.jmx.OpenMBean;
import org.osgi.service.log.LogService;

import javax.management.openmbean.SimpleType;

/**
* @author Roman Sakno
* @version 1.0
* @since 1.0
*/
final class CountAttribute extends OpenMBean.OpenAttribute<Long, SimpleType<Long>> {
    private final StatisticCounters counter;
    private final int logLevel;

    CountAttribute(final String attributeName,
                   final StatisticCounters counter,
                   final int logLevel){
        super(attributeName, SimpleType.LONG);
        this.counter = counter;
        this.logLevel = logLevel;
    }

    /**
     * Gets description of this attribute.
     *
     * @return The description of this attribute.
     */
    @Override
    protected String getDescription() {
        switch (logLevel){
            case LogService.LOG_ERROR: return "A number of faults occurred in SNAMP. Increasing of this value may be interpreted as SNAMP malfunction.";
            case LogService.LOG_WARNING: return "A number of alert messages received by OSGI logger for the last time.";
            case LogService.LOG_DEBUG: return "A number of debug messages received by OSGI logger for the last time. You may ignore this attribute.";
            default: return "A number of information messages received by OSGI logger for the last time.";
        }
    }

    @Override
    public Long getValue() {
        return counter.getValue(logLevel);
    }
}