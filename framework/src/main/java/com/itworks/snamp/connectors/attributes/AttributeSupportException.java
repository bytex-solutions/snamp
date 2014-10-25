package com.itworks.snamp.connectors.attributes;

import com.itworks.snamp.connectors.ResourceConnectorException;

/**
 * Represents exception associated with attributes support (reading and updating).
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class AttributeSupportException extends ResourceConnectorException {
    /**
     * Initializes a new exception.
     * @param cause The underlying exception occurred inside of the connector. Cannot be {@literal null}.
     */
    public AttributeSupportException(final Throwable cause){
        super(cause);
    }
}
