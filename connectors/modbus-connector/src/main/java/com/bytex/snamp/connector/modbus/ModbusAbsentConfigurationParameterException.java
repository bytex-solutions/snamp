package com.bytex.snamp.connector.modbus;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;

/**
 * Indicating that the required parameter is not defined in the configuration.
 */
final class ModbusAbsentConfigurationParameterException extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = 1910680450579356905L;

    ModbusAbsentConfigurationParameterException(final String parameterName) {
        super(parameterName);
    }
}
