package com.bytex.snamp.connector.composite;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class AbsentCompositeConfigurationParameterException extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = 4167212082285527674L;

    AbsentCompositeConfigurationParameterException(final String parameterName) {
        super(parameterName);
    }
}
