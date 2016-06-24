package com.bytex.snamp.connectors.aggregator;

import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import org.osgi.framework.BundleContext;

import javax.management.openmbean.SimpleType;
import java.util.Objects;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;

/**
 * Represents proxy attribute that exposes value of the foreign attribute as a string.
 * This class cannot be inherited.
 */
final class Stringifier extends UnaryAttributeAggregation<String> {
    private static final long serialVersionUID = 3477769347987564924L;
    static final String NAME = "stringifier";
    private static final String DESCRIPTION = "Exposes value of the foreign attribute as a string.";

    Stringifier(final String attributeID,
                final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        super(attributeID, DESCRIPTION, SimpleType.STRING, descriptor);
    }

    static AttributeConfiguration getConfiguration(final BundleContext context) {
        final AttributeConfiguration result = createAttributeConfiguration(context);
        result.setAlternativeName(NAME);
        fillParameters(result.getParameters());
        return result;
    }

    @Override
    protected String compute(final Object foreignAttributeValue) {
        return Objects.toString(foreignAttributeValue, "");
    }
}
