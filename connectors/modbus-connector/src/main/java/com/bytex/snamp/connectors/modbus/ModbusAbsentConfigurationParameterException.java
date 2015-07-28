package com.bytex.snamp.connectors.modbus;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;

/**
 * Indicating that the required parameter is not defined in the configuration.
 */
final class ModbusAbsentConfigurationParameterException extends AbsentConfigurationParameterException {
    ModbusAbsentConfigurationParameterException(final String parameterName) {
        super(parameterName);
    }
}
