package com.bytex.snamp.connectors.mq;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class MQAbsentConfigurationParameterException extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = -1809722505756348822L;

    MQAbsentConfigurationParameterException(String parameterName) {
        super(parameterName);
    }
}
