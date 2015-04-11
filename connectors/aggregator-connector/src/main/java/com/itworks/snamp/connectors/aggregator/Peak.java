package com.itworks.snamp.connectors.aggregator;

import com.google.common.base.Stopwatch;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.openmbean.SimpleType;
import java.util.concurrent.TimeUnit;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class Peak extends UnaryAttributeAggregation<Long> {
    static final String NAME = "peak";
    private static final long serialVersionUID = 9156690032615261535L;
    private static final String DESCRIPTION = "Detects value at the specified time interval";
    private static final TimeUnit INTERVAL_UNIT = TimeUnit.MILLISECONDS;

    private long peak;
    private final long timeInterval;
    private final Stopwatch timer;

    protected Peak(final String attributeID, final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameter {
        super(attributeID, DESCRIPTION, SimpleType.LONG, descriptor);
        timeInterval = AggregatorConnectorConfigurationDescriptor.getTimeIntervalInMillis(descriptor);
        timer = Stopwatch.createStarted();
        peak = 0L;
    }

    private synchronized long compute(final long value){
        if(timer.elapsed(INTERVAL_UNIT) > timeInterval){
            timer.reset().start();
            return peak = value;
        }
        else return value > peak ? (peak = value) : value;
    }

    @Override
    protected Long compute(final Object value) throws Exception {
        return compute(NumberUtils.toLong(value));
    }
}
