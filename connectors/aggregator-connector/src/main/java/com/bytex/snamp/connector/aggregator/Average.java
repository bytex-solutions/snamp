package com.bytex.snamp.connector.aggregator;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;

import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class Average extends UnaryAttributeAggregation<Double> {
    static final String NAME = "average";
    private static final long serialVersionUID = -3823081282353067204L;
    private static final String DESCRIPTION = "Computes average value of the attribute during update interval";

    private final long updateInterval;
    private long timer;
    private double sum;
    private long count;

    Average(final String attributeID,
                      final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        super(attributeID, DESCRIPTION, SimpleType.DOUBLE, descriptor);
        updateInterval = AggregatorConnectorConfiguration.getTimeIntervalInMillis(descriptor);
        timer = System.currentTimeMillis();
    }

    @Override
    protected synchronized Double compute(final Object value) throws NumberFormatException {
        final long currentTime = System.currentTimeMillis();
        if(currentTime - timer > updateInterval){
            timer = currentTime;
            sum = 0;
            count = 0;
        }
        sum += NumberUtils.toDouble(value);
        count += 1;
        return sum / count;
    }

    static AttributeConfiguration getConfiguration() {
        final AttributeConfiguration result = createAttributeConfiguration(Average.class.getClassLoader());
        result.setAlternativeName(NAME);
        fillParameters(result.getParameters());
        return result;
    }
}
