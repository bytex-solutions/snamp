package com.itworks.snamp.adapters.groovy.impl;

import com.itworks.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class GroovyAbsentParameterConfigurationException extends AbsentConfigurationParameterException {
    GroovyAbsentParameterConfigurationException(final String parameterName) {
        super(parameterName);
    }
}
