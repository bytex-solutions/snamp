package com.itworks.snamp.connectors.aggregation;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSupport;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class BinaryAttributeAggregation<V> extends AbstractAttributeAggregation<V> {
    private final String leftOperand;
    private final String rightOperand;

    protected BinaryAttributeAggregation(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameter {
        super(descriptor);
        leftOperand = AggregatorConnectorConfigurationDescriptor.getFirstForeignAttributeName(descriptor);
        rightOperand = AggregatorConnectorConfigurationDescriptor.getSecondForeignAttributeName(descriptor);
    }

    public final String getFirstOperandAttribute(){
        return leftOperand;
    }

    public final String getSecondOperandAttribute(){
        return rightOperand;
    }

    protected abstract V compute(final Object left, final Object right) throws Exception;

    @Override
    protected final V compute(final AttributeSupport attributeSupport) throws Exception {
        return compute(attributeSupport.getAttribute(getFirstOperandAttribute()),
                attributeSupport.getAttribute(getSecondOperandAttribute()));
    }
}
