package com.bytex.snamp.gateway.influx;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;


final class InfluxGatewayAbsentConfigurationParameterException extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = 3648537165588203903L;

    InfluxGatewayAbsentConfigurationParameterException(final String parameterName) {
        super(parameterName);
    }
}
