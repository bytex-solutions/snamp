package com.bytex.snamp.connectors.mq;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class MQAbsentConfigurationParameterException extends AbsentConfigurationParameterException {
    MQAbsentConfigurationParameterException(String parameterName) {
        super(parameterName);
    }
}
