package com.bytex.snamp.supervision.def;

import com.bytex.snamp.supervision.discovery.ResourceDiscoveryException;

import java.io.IOException;

/**
 * Indicates that underlying configuration system is crashed.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class ResourceDiscoveryIOException extends ResourceDiscoveryException {
    private static final long serialVersionUID = -7598102324050800756L;

    ResourceDiscoveryIOException(final IOException e){
        super("Configuration subsystem crashed", e);
    }
}
