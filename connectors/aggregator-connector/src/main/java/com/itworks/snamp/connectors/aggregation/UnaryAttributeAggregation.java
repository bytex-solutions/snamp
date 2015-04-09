package com.itworks.snamp.connectors.aggregation;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSupport;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class UnaryAttributeAggregation<V> extends AbstractAttributeAggregation<V> {
    private final String foreignAttribute;

    protected UnaryAttributeAggregation(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameter {
        super(descriptor);
        foreignAttribute = AggregatorConnectorConfigurationDescriptor.getForeignAttributeName(descriptor);
    }

    public final String getOperandAttribute(){
        return foreignAttribute;
    }

    protected abstract V compute(final Object foreignAttributeValue) throws Exception;

    @Override
    protected V compute(final AttributeSupport attributeSupport) throws Exception {
        return compute(attributeSupport.getAttribute(getOperandAttribute()));
    }
}
