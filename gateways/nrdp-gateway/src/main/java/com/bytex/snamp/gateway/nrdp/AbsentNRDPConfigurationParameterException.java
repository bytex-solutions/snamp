package com.bytex.snamp.gateway.nrdp;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;

/**
 * NSCA required parameter is absent.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class AbsentNRDPConfigurationParameterException extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = -6214629994928685764L;

    AbsentNRDPConfigurationParameterException(final String parameterName) {
        super(parameterName);
    }
}
