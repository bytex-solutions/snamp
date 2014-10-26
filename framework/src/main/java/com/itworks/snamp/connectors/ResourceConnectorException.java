package com.itworks.snamp.connectors;

/**
 * Represents an exception associated with internal error occurred in the resource connector
 * and exposed to the connector consumer.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class ResourceConnectorException extends Exception {
    /**
     * Wraps internal connector exception into well-known exception type.
     * @param cause The underlying exception occurred inside of the connector. Cannot be {@literal null}.
     */
    protected ResourceConnectorException(final Throwable cause){
        super(cause);
    }
}
