package com.itworks.snamp.connectors.jmx;

import com.itworks.snamp.configuration.AbsentConfigurationParameterException;

/**
 * Represents exception indicating missing configuration parameter.
 */
final class JmxAbsentConfigurationParameterException extends AbsentConfigurationParameterException {
    JmxAbsentConfigurationParameterException(final String parameterName) {
        super(parameterName);
    }
}
