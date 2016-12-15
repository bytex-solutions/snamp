package com.bytex.snamp.concurrent;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents advanced version of {@link AtomicReference} that can be used for implementation of weak-referenced singletons.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@ThreadSafe
public final class LazyWeakReference<V> extends AbstractLazyReference<V> {
    private static final long serialVersionUID = 1898537173263220348L;

    public LazyWeakReference() {
        super(WeakReference::new);
    }
}
