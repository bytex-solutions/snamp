package com.itworks.snamp.connectors.aggregation;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.DynamicMBean;

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
    protected final V compute(final DynamicMBean attributeSupport) throws Exception {
        return compute(attributeSupport.getAttribute(getOperandAttribute()));
    }
}
