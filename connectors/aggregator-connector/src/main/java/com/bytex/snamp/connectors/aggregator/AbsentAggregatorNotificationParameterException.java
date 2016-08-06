package com.bytex.snamp.connectors.aggregator;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class AbsentAggregatorNotificationParameterException extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = -881318390100688031L;

    AbsentAggregatorNotificationParameterException(final String parameterName) {
        super(parameterName);
    }
}
