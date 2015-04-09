package com.itworks.snamp.connectors.aggregation;

import com.itworks.snamp.configuration.AbsentConfigurationParameterException;
import com.itworks.snamp.connectors.ManagedResourceConnectorBean;
import com.itworks.snamp.connectors.attributes.AttributeDescriptorRead;
import com.itworks.snamp.internal.Utils;

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

    AggregatorResourceConnector(final String resourceName) throws IntrospectionException {
        super(resourceName);
    }

    private <R, T extends AttributeAggregation<R>> R compute(final AggregationProvider<T> factory,
                                                                        final Class<T> aggregationType) throws AbsentConfigurationParameterException, JMException {
        final T aggregation = JavaBeanAttributeInfo.current().getUserData(factory, aggregationType);
        return aggregation.compute(Utils.getBundleContextByObject(this));
    }

    @ManagementAttribute(description = "")
    public boolean isMatches() throws AbsentConfigurationParameterException, JMException {
        return compute(PATTERN_MATCHER_FACTORY, PatternMatcher.class);
    }

    @Override
    public Logger getLogger() {
        return getLogger(NAME);
    }
}
