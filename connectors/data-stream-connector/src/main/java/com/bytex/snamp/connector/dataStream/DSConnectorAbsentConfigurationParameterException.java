package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public class DSConnectorAbsentConfigurationParameterException extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = 1576035556039625703L;

    public DSConnectorAbsentConfigurationParameterException(final String parameterName) {
        super(parameterName);
    }
}
