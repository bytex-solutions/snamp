package com.bytex.snamp.connector.dsp;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class DSPConnectorAbsentConfigurationParameterException extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = 1576035556039625703L;

    public DSPConnectorAbsentConfigurationParameterException(final String parameterName) {
        super(parameterName);
    }
}
