package com.itworks.snamp.connectors.openstack;

import com.itworks.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class OpenStackAbsentConfigurationParameterException extends AbsentConfigurationParameterException {
    OpenStackAbsentConfigurationParameterException(final String parameterName) {
        super(parameterName);
    }
}
