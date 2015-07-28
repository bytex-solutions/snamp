package com.bytex.snamp.adapters.syslog;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AbsentSysLogConfigurationParameterException extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = -8194228078981711139L;

    AbsentSysLogConfigurationParameterException(final String parameterName) {
        super(parameterName);
    }
}