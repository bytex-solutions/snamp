package com.bytex.snamp.testing;

import org.osgi.framework.BundleException;

import java.util.concurrent.Callable;

/**
 * Represents variation of {@link Callable} with exception {@link BundleException}.
 */
public interface BundleExceptionCallable extends Callable<Void> {
    @Override
    Void call() throws BundleException;
}
