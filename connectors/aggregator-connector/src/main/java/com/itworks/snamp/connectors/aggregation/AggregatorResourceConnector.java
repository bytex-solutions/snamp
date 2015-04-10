package com.itworks.snamp.connectors.aggregation;

import com.itworks.snamp.configuration.AbsentConfigurationParameterException;
import com.itworks.snamp.connectors.ManagedResourceConnectorBean;
import com.itworks.snamp.connectors.attributes.AttributeDescriptorRead;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.internal.annotations.SpecialUse;

import javax.management.JMException;
import java.beans.IntrospectionException;
import java.util.logging.Logger;

/**
 * Represents an aggregator of other managed resources.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class AggregatorResourceConnector extends ManagedResourceConnectorBean {
    static final String NAME = "aggregator";

    private interface AggregationProvider<T extends AttributeAggregation<?>> extends AttributeUserDataFactory<T, AbsentConfigurationParameterException>{
    }

    private static final AggregationProvider<PatternMatcher> PATTERN_MATCHER_FACTORY = new AggregationProvider<PatternMatcher>() {
        @Override
        public PatternMatcher createUserData(final AttributeDescriptorRead metadata) throws AbsentConfigurationParameterException {
            return new PatternMatcher(metadata.getDescriptor());
        }
    };
    private static final AggregationProvider<UnaryComparison> UNARY_COMPARISON_FACTORY = new AggregationProvider<UnaryComparison>() {
        @Override
        public UnaryComparison createUserData(final AttributeDescriptorRead metadata) throws AbsentConfigurationParameterException {
            return new UnaryComparison(metadata.getDescriptor());
        }
    };
    private static final AggregationProvider<BinaryComparison> BINARY_COMPARISON_FACTORY = new AggregationProvider<BinaryComparison>() {
        @Override
        public BinaryComparison createUserData(final AttributeDescriptorRead metadata) throws AbsentConfigurationParameterException {
            return new BinaryComparison(metadata.getDescriptor());
        }
    };

    AggregatorResourceConnector(final String resourceName) throws IntrospectionException {
        super(resourceName);
    }

    private <R, T extends AttributeAggregation<R>> R compute(final AggregationProvider<T> factory,
                                                                        final Class<T> aggregationType) throws AbsentConfigurationParameterException, JMException {
        final T aggregation = JavaBeanAttributeInfo.current().getUserData(factory, aggregationType);
        return aggregation.compute(Utils.getBundleContextByObject(this));
    }

    @ManagementAttribute(description = "Checks whether the value of the foreign attribute matched to the user-defined regula expression")
    public boolean isMatch() throws AbsentConfigurationParameterException, JMException {
        return compute(PATTERN_MATCHER_FACTORY, PatternMatcher.class);
    }

    @ManagementAttribute(description = "Compares the value of the foreign attribute with the user-defined value")
    public boolean getCompareWithValue() throws AbsentConfigurationParameterException, JMException {
        return compute(UNARY_COMPARISON_FACTORY, UnaryComparison.class);
    }

    @ManagementAttribute(description = "Compares two foreign attributes")
    public boolean getCompareTwoAttributes() throws AbsentConfigurationParameterException, JMException {
        return compute(BINARY_COMPARISON_FACTORY, BinaryComparison.class);
    }

    @Override
    public Logger getLogger() {
        return getLogger(NAME);
    }
}
