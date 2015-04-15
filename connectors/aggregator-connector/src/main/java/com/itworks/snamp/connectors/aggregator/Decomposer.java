package com.itworks.snamp.connectors.aggregator;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.InvalidKeyException;
import javax.management.openmbean.SimpleType;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class Decomposer extends UnaryAttributeAggregation<String> {
    static final String NAME = "decomposer";
    private static final long serialVersionUID = -1830507338336882425L;
    private static final String DESCRIPTION = "Extracts field from composite type";
    private final CompositeDataPath path;

    protected Decomposer(final String attributeID,
                         final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        super(attributeID, DESCRIPTION, SimpleType.STRING, descriptor);
        path = AggregatorConnectorConfiguration.getFieldPath(descriptor);
    }

    private static String toString(final Object value){
        return Objects.toString(value, "NULL");
    }

    @Override
    protected String compute(final Object value) throws InvalidKeyException{
        return value instanceof CompositeData ?
                toString(path.getFieldValue((CompositeData)value)):
                toString(value);
    }
}
