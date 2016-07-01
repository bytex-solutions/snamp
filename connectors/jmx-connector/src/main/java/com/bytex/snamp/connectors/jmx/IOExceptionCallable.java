package com.bytex.snamp.connectors.jmx;

import com.bytex.snamp.ExceptionalCallable;

import java.io.IOException;

/**
 * Provides variation of {@link ExceptionalCallable} with exception {@link IOException}.
 */
interface IOExceptionCallable<V> extends ExceptionalCallable<V, IOException> {
}
