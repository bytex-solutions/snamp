package com.itworks.snamp.connectors.aggregator;

import com.itworks.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AbsentAggregatorAttributeParameter extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = 7497859739400238690L;

    AbsentAggregatorAttributeParameter(final String parameterName) {
        super(parameterName);
    }
}
