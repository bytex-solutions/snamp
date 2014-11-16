package com.itworks.snamp.connectors.operations;

import com.google.common.annotations.Beta;
import com.itworks.snamp.connectors.ResourceConnectorException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Beta
public class OperationSupportException extends ResourceConnectorException {
    /**
     * Wraps internal connector exception into well-known exception type.
     *
     * @param cause The underlying exception occurred inside of the connector. Cannot be {@literal null}.
     */
    OperationSupportException(final Throwable cause) {
        super(cause);
    }
}
