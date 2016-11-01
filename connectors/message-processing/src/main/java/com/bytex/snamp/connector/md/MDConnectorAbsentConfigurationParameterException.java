package com.bytex.snamp.connector.md;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class MDConnectorAbsentConfigurationParameterException extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = 1576035556039625703L;

    public MDConnectorAbsentConfigurationParameterException(final String parameterName) {
        super(parameterName);
    }
}
