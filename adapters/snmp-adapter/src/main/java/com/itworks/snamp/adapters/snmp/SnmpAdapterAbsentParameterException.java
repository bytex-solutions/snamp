package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpAdapterAbsentParameterException extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = -9140257085258997415L;

    SnmpAdapterAbsentParameterException(final String parameterName) {
        super(parameterName);
    }
}
