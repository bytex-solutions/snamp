package com.itworks.snamp.connectors.notifications;

import com.itworks.snamp.connectors.ResourceConnectorException;

/**
 * Represents exception related to notification support.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class NotificationSupportException extends ResourceConnectorException {
    /**
     * Wraps internal connector exception into well-known exception type.
     *
     * @param cause The underlying exception occurred inside of the connector. Cannot be {@literal null}.
     */
    public NotificationSupportException(final Throwable cause) {
        super(cause);
    }
}
