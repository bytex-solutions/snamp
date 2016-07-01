package com.bytex.snamp.testing;

import com.bytex.snamp.ExceptionalCallable;
import org.osgi.framework.BundleException;

/**
 * Represents variation of {@link ExceptionalCallable} with exception {@link BundleException}.
 */
public interface BundleExceptionCallable extends ExceptionalCallable<Void, BundleException> {
}
