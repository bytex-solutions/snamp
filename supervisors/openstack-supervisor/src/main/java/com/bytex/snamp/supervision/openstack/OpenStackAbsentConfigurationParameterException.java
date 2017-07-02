package com.bytex.snamp.supervision.openstack;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class OpenStackAbsentConfigurationParameterException extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = -3743182923054658077L;

    OpenStackAbsentConfigurationParameterException(final String parameterName) {
        super(parameterName);
    }
}
