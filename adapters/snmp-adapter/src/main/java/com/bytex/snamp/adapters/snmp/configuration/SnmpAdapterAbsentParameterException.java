package com.bytex.snamp.adapters.snmp.configuration;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class SnmpAdapterAbsentParameterException extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = -9140257085258997415L;

    SnmpAdapterAbsentParameterException(final String parameterName) {
        super(parameterName);
    }
}
