package com.bytex.snamp.io;

import com.bytex.snamp.SnapshotSupport;
import com.bytex.snamp.SpecialUse;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Represents serializable object which state can be captured using serialization.
 * @since 2.0
 * @version 2.1
 */
public interface SerializableSnapshotSupport extends SnapshotSupport, Serializable {
    /**
     * Captures state of this object in serialized form that can be used to recreate instance of this object in the future.
     * @return Serialized state of this object.
     */
    @Override
    SerializedState<? extends SerializableSnapshotSupport> takeSnapshot();

    /**
     * Special method required by {@link Serializable} specification.
     * @return Serializable state of this object.
     * @throws ObjectStreamException Serializable state cannot be created.
     * @implSpec This method should always call method {@link #takeSnapshot()}.
     */
    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    default Object writeReplace() throws ObjectStreamException{
        return takeSnapshot();
    }
}
