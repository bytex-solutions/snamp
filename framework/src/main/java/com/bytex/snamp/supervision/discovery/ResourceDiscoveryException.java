package com.bytex.snamp.supervision.discovery;

/**
 * Indicates that the resource cannot be registered or removed
 * using discovery service.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class ResourceDiscoveryException extends Exception {
    private static final long serialVersionUID = 1109867715185456334L;

    protected ResourceDiscoveryException(final String message){
        super(message);
    }

    protected ResourceDiscoveryException(final String message, final Throwable cause){
        super(message, cause);
    }
}
