package com.bytex.snamp.gateway.snmp;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class SnmpGatewayAbsentParameterException extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = -9140257085258997415L;

    SnmpGatewayAbsentParameterException(final String parameterName) {
        super(parameterName);
    }
}
