package com.bytex.snamp.connectors.aggregator;

import com.bytex.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.DynamicMBean;
import javax.management.openmbean.OpenType;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
abstract class UnaryAttributeAggregation<T> extends AbstractAttributeAggregation<T> {
    private static final long serialVersionUID = 8188453369203515353L;
    private final String foreignAttribute;

    UnaryAttributeAggregation(final String attributeID,
                              final String description,
                              final OpenType<T> attributeType,
                              final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        super(attributeID, description, attributeType, descriptor);
        foreignAttribute = AggregatorConnectorConfiguration.getForeignAttributeName(descriptor);
    }

    protected static void fillParameters(final Map<String, String> parameters){
        parameters.put(AggregatorConnectorConfiguration.SOURCE_PARAM, "");
        parameters.put(AggregatorConnectorConfiguration.FOREIGN_ATTRIBUTE_PARAM, "");
    }

    private String getOperandAttribute(){
        return foreignAttribute;
    }

    protected abstract T compute(final Object foreignAttributeValue) throws Exception;

    @Override
    protected final T compute(final DynamicMBean attributeSupport) throws Exception {
        return compute(attributeSupport.getAttribute(getOperandAttribute()));
    }
}
