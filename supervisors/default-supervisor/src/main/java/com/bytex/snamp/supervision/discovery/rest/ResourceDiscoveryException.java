package com.bytex.snamp.supervision.discovery.rest;

/**
 * Indicates that the resource cannot be registered or removed
 * using discovery service.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class ResourceDiscoveryException extends Exception {
    private static final long serialVersionUID = 1109867715185456334L;

    ResourceDiscoveryException(final String message){
        super(message);
    }

    ResourceDiscoveryException(final String message, final Throwable cause){
        super(message, cause);
    }
}
