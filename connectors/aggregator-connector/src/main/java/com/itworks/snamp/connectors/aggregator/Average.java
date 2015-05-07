package com.itworks.snamp.connectors.aggregator;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.openmbean.SimpleType;
import static com.itworks.snamp.configuration.SerializableAgentConfiguration.SerializableManagedResourceConfiguration.SerializableAttributeConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
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

    private synchronized double avg(final Object value){
        final long currentTime = System.currentTimeMillis();
        if(currentTime - timer > updateInterval){
            timer = currentTime;
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

    static SerializableAttributeConfiguration getConfiguratoin() {
        final SerializableAttributeConfiguration result = new SerializableAttributeConfiguration(NAME);
        fillParameters(result.getParameters());
        return result;
    }
}
