package com.itworks.snamp.adapters.xmpp;

import com.itworks.snamp.configuration.AbsentConfigurationParameterException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AbsentXMPPConfigurationParameterException extends AbsentConfigurationParameterException {
    private static final long serialVersionUID = -256667412021768854L;

    public AbsentXMPPConfigurationParameterException(final String parameterName) {
        super(parameterName);
    }
}
