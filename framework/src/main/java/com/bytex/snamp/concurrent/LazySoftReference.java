package com.bytex.snamp.concurrent;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents advanced version of {@link AtomicReference} that can be used for implementation of soft-referenced singletons.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@ThreadSafe
final class LazySoftReference<V> extends AbstractLazyIndirectReference<V> {
    private static final long serialVersionUID = 1898537173263220348L;

    @Override
    SoftReference<V> makeRef(final V value) {
        return new SoftReference<>(value);
    }
}
