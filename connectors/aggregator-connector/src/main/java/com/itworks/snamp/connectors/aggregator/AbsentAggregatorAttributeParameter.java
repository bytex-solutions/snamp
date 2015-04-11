package com.itworks.snamp.connectors.aggregator;

import com.itworks.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AbsentAggregatorAttributeParameter extends AbsentConfigurationParameterException {
    AbsentAggregatorAttributeParameter(final String parameterName) {
        super(parameterName);
    }
}
