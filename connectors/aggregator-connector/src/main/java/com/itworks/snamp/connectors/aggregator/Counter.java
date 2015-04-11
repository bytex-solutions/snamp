package com.itworks.snamp.connectors.aggregator;

import com.google.common.base.Stopwatch;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.openmbean.SimpleType;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents counter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class Counter extends UnaryAttributeAggregation<Long> {
    static final String NAME = "counter";
    private static final long serialVersionUID = 4529159977546061535L;
    private static final String DESCRIPTION = "Summarizes value of the attribute during update interval";
    private static final TimeUnit INTERVAL_UNIT = TimeUnit.MILLISECONDS;

    private final long updateInterval;
    private final Stopwatch timer;
    private final AtomicLong counter;

    protected Counter(final String attributeID,
                      final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameter {
        super(attributeID, DESCRIPTION, SimpleType.LONG, descriptor);
        updateInterval = AggregatorConnectorConfigurationDescriptor.getTimeIntervalInMillis(descriptor);
        counter = new AtomicLong(0L);
        timer = Stopwatch.createStarted();
    }

    private synchronized void reset(){
        if(timer.elapsed(INTERVAL_UNIT) > updateInterval){
            timer.reset().start();
            counter.set(0L);
        }
    }

    @Override
    protected Long compute(final Object value) throws Exception {
        if(timer.elapsed(INTERVAL_UNIT) > updateInterval)
            reset();
        return counter.addAndGet(NumberUtils.toLong(value));
    }
}
