package com.bytex.snamp.connectors.jmx;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Provides variation of {@link ExceptionalCallable} with exception {@link IOException}.
 */
interface IOExceptionCallable<V> extends Callable<V> {
    @Override
    V call() throws IOException;
}
