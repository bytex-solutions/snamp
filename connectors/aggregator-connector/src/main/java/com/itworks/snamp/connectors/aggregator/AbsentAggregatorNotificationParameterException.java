package com.itworks.snamp.connectors.aggregator;

import com.itworks.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AbsentAggregatorNotificationParameterException extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = -881318390100688031L;

    AbsentAggregatorNotificationParameterException(final String parameterName) {
        super(parameterName);
    }
}
