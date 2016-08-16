package com.bytex.snamp.connector.aggregator;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;

import javax.management.openmbean.SimpleType;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represents regular expression computation.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class PatternMatcher extends UnaryAttributeAggregation<Boolean> {
    static final String NAME = "matcher";
    private static final String DESCRIPTION = "Checks whether the value of the foreign attribute match to the user-defined regular expression";
    private static final long serialVersionUID = -1026150386772035267L;
    private final Pattern pattern;

    PatternMatcher(final String attributeID,
                   final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        super(attributeID,
                DESCRIPTION,
                SimpleType.BOOLEAN,
                descriptor);
        pattern = Pattern.compile(AggregatorConnectorConfiguration.getUserDefinedValue(descriptor));
    }

    @Override
    protected Boolean compute(final Object foreignAttributeValue) throws Exception {
        return pattern.matcher(Objects.toString(foreignAttributeValue, "")).matches();
    }

    static AttributeConfiguration getConfiguration() {
        final AttributeConfiguration result = createAttributeConfiguration(PatternMatcher.class.getClassLoader());
        result.setAlternativeName(NAME);
        fillParameters(result.getParameters());
        result.getParameters().put(AggregatorConnectorConfiguration.VALUE_PARAM, "");
        return result;
    }
}
