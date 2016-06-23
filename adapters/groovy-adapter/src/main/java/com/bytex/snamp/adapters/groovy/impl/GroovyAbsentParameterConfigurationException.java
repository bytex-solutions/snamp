package com.bytex.snamp.adapters.groovy.impl;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class GroovyAbsentParameterConfigurationException extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = -1123501822629989298L;

    GroovyAbsentParameterConfigurationException(final String parameterName) {
        super(parameterName);
    }
}
