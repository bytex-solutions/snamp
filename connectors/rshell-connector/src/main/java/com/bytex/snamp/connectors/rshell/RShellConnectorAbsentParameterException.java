package com.bytex.snamp.connectors.rshell;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class RShellConnectorAbsentParameterException extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = -5385321092256175376L;

    RShellConnectorAbsentParameterException(final String parameterName) {
        super(parameterName);
    }
}
