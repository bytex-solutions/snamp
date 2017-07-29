package com.bytex.snamp.gateway.nsca;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;

/**
 * NSCA required parameter is absent.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class AbsentNSCAConfigurationParameterException extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = -6214629994928685764L;

    AbsentNSCAConfigurationParameterException(final String parameterName) {
        super(parameterName);
    }
}
