package com.bytex.snamp.connector.composite.functions;

import java.util.concurrent.Callable;

/**
 * A function used to resolve named references in formula.
 * @since 2.0
 * @version 2.0
 * @author Roman Sakno
 */
public interface ReferenceResolver {
    ReferenceResolver EMPTY = name -> null;
    Callable<?> resolve(final String reference);
}
