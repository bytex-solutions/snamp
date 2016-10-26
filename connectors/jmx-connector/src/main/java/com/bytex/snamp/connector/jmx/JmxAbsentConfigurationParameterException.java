package com.bytex.snamp.connector.jmx;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;

/**
 * Represents exception indicating missing configuration parameter.
 */
final class JmxAbsentConfigurationParameterException extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = -9055991040585053810L;

    JmxAbsentConfigurationParameterException(final String parameterName) {
        super(parameterName);
    }
}
