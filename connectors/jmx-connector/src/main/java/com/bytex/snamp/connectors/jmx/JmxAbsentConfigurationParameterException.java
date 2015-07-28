package com.bytex.snamp.connectors.jmx;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;

/**
 * Represents exception indicating missing configuration parameter.
 */
final class JmxAbsentConfigurationParameterException extends AbsentConfigurationParameterException {
    JmxAbsentConfigurationParameterException(final String parameterName) {
        super(parameterName);
    }
}
