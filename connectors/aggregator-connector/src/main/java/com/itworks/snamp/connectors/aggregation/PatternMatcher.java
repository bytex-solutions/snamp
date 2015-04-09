package com.itworks.snamp.connectors.aggregation;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represents regular expression computation.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class PatternMatcher extends UnaryAttributeAggregation<Boolean> {
    private final Pattern pattern;

    PatternMatcher(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameter {
        super(descriptor);
        pattern = Pattern.compile(AggregatorConnectorConfigurationDescriptor.getPattern(descriptor));
    }


    @Override
    protected Boolean compute(final Object foreignAttributeValue) throws Exception {
        return pattern.matcher(Objects.toString(foreignAttributeValue, "")).matches();
    }
}
