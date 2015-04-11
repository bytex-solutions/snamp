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
final class Average extends UnaryAttributeAggregation<Double> {
    static final String NAME = "average";
    private static final long serialVersionUID = -3823081282353067204L;
    private static final String DESCRIPTION = "Computes average value of the attribute during update interval";
    private static final TimeUnit INTERVAL_UNIT = TimeUnit.MILLISECONDS;

    private final long updateInterval;
    private final Stopwatch timer;
    private double sum;
    private long count;

    Average(final String attributeID,
                      final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameter {
        super(attributeID, DESCRIPTION, SimpleType.DOUBLE, descriptor);
        updateInterval = AggregatorConnectorConfigurationDescriptor.getTimeIntervalInMillis(descriptor);
        timer = Stopwatch.createStarted();
    }

    private synchronized double avg(final Object value){
        if(timer.elapsed(INTERVAL_UNIT) > updateInterval){
            timer.reset().start();
            sum = 0;
            count = 0;
        }
        sum += NumberUtils.toLong(value);
        count += 1;
        return sum / count;
    }

    @Override
    protected Double compute(final Object value) throws Exception {
        return avg(value);
    }
}
