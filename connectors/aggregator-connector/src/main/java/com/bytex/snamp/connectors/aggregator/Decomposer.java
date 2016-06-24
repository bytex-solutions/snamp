package com.bytex.snamp.connectors.aggregator;

import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import org.osgi.framework.BundleContext;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.InvalidKeyException;
import javax.management.openmbean.SimpleType;
import java.util.Objects;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class Decomposer extends UnaryAttributeAggregation<String> {
    static final String NAME = "decomposer";
    private static final long serialVersionUID = -1830507338336882425L;
    private static final String DESCRIPTION = "Extracts field from composite type";
    private final CompositeDataPath path;

    Decomposer(final String attributeID,
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

    static AttributeConfiguration getConfiguration(final BundleContext context) {
        final AttributeConfiguration result = createAttributeConfiguration(context);
        result.setAlternativeName(NAME);
        fillParameters(result.getParameters());
        result.getParameters().put(AggregatorConnectorConfiguration.FIELD_PATH_PARAM, "");
        return result;
    }
}
