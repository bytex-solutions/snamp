package com.bytex.snamp.connectors.aggregator;

import com.bytex.snamp.concurrent.SumLongAccumulator;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.openmbean.SimpleType;
import static com.bytex.snamp.configuration.SerializableAgentConfiguration.SerializableManagedResourceConfiguration.SerializableAttributeConfiguration;

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

    private final SumLongAccumulator accumulator;

    protected Counter(final String attributeID,
                      final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        super(attributeID, DESCRIPTION, SimpleType.LONG, descriptor);
        accumulator = new SumLongAccumulator(0L,
                AggregatorConnectorConfiguration.getTimeIntervalInMillis(descriptor));
    }

    @Override
    protected Long compute(final Object value) throws NumberFormatException {
        return accumulator.update(NumberUtils.toLong(value));
    }

    static SerializableAttributeConfiguration getConfiguration() {
        final SerializableAttributeConfiguration result = new SerializableAttributeConfiguration(NAME);
        fillParameters(result.getParameters());
        return result;
    }
}
