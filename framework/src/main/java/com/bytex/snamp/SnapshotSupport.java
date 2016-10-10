package com.bytex.snamp;

import java.util.function.Supplier;

/**
 * Represents object which state can be captured at any time of its lifecycle and recreated again.
 * @since 2.0
 * @version 2.0
 */
public interface SnapshotSupport<T extends SnapshotSupport<T>> extends Stateful {
    /**
     * Captures the state of this object.
     * @return A function that can be used to recreate object in its captured state.
     */
    Supplier<? extends T> captureState();
}
