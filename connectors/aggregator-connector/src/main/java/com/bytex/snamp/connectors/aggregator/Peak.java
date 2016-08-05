package com.bytex.snamp.connectors.aggregator;

import com.bytex.snamp.concurrent.LongAccumulator;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.openmbean.SimpleType;

import static com.bytex.snamp.configuration.ManagedResourceConfiguration.AttributeConfiguration;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class Peak extends UnaryAttributeAggregation<Long> {
    static final String NAME = "peak";
    private static final long serialVersionUID = 9156690032615261535L;
    private static final String DESCRIPTION = "Detects value at the specified time interval";

    private final LongAccumulator accumulator;

    Peak(final String attributeID, final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        super(attributeID, DESCRIPTION, SimpleType.LONG, descriptor);
        accumulator = LongAccumulator.peak(0L, AggregatorConnectorConfiguration.getTimeIntervalInMillis(descriptor));
    }

    @Override
    protected Long compute(final Object value) throws NumberFormatException {
        return accumulator.update(NumberUtils.toLong(value));
    }

    static AttributeConfiguration getConfiguration() {
        final AttributeConfiguration result = createAttributeConfiguration(Peak.class.getClassLoader());
        result.setAlternativeName(NAME);
        fillParameters(result.getParameters());
        return result;
    }
}
