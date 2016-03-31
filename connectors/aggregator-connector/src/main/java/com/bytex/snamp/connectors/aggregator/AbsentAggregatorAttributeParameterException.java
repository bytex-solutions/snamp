package com.bytex.snamp.connectors.aggregator;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class AbsentAggregatorAttributeParameterException extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = 7497859739400238690L;

    AbsentAggregatorAttributeParameterException(final String parameterName) {
        super(parameterName);
    }
}
