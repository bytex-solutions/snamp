package com.bytex.snamp.io;

import com.bytex.snamp.SnapshotSupport;
import com.bytex.snamp.SpecialUse;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Represents serializable object which state can be captured using serialization.
 * @since 2.0
 * @version 2.0
 */
public interface SerializableSnapshotSupport<T extends SerializableSnapshotSupport<T>> extends SnapshotSupport<T>, Serializable {
    @Override
    SerializedState<? extends T> takeSnapshot();

    /**
     * Special method required by {@link Serializable} specification.
     * @return Serializable state of this object.
     * @throws ObjectStreamException Serializable state cannot be created.
     * @implSpec This method should always call method {@link #takeSnapshot()}.
     */
    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    Object writeReplace() throws ObjectStreamException;
}
