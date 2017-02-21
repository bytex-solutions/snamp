package com.bytex.snamp.moa.services;

import com.bytex.snamp.connector.health.RootCause;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class TypedStatusDetails<R extends RootCause> extends AbstractStatusDetails {
    private final R rootCause;

    TypedStatusDetails(final String resourceName,
                       final R rootCause) {
        super(resourceName);
        this.rootCause = rootCause;
    }

    @Override
    public final R getRootCause() {
        return rootCause;
    }
}
