package com.bytex.snamp.gateway.smtp;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class SmtpGatewayAbsentConfigurationParameterException extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = -492670118787281154L;

    SmtpGatewayAbsentConfigurationParameterException(final String parameterName) {
        super(parameterName);
    }
}
