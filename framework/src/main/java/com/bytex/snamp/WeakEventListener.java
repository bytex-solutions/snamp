package com.bytex.snamp;

import com.google.common.base.Supplier;

import java.lang.ref.WeakReference;
import java.util.EventListener;
import java.util.Objects;

/**
 * Represents a weak reference to the event listener.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class WeakEventListener<L extends EventListener> extends WeakReference<L> implements Supplier<L>, EventListener {

    WeakEventListener(final L listener) {
        super(Objects.requireNonNull(listener));
    }
}
