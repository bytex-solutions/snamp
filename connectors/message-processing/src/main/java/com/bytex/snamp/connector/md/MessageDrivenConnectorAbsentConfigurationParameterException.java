package com.bytex.snamp.connector.md;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class MessageDrivenConnectorAbsentConfigurationParameterException extends AbsentConfigurationParameterException {
    public MessageDrivenConnectorAbsentConfigurationParameterException(final String parameterName) {
        super(parameterName);
    }
}
