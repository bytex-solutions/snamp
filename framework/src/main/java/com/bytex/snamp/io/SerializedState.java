package com.bytex.snamp.io;

import com.bytex.snamp.SpecialUse;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * Represents serialized state of the object.
 * <p>
 *     Underlying class should have {@code Object writeReplace() throws ObjectStreamException} method.
 * @param <T> Type of underlying object that can be constructed after deserialization of this class.
 * @since 2.0
 * @version 2.1
 */
public abstract class SerializedState<T extends Serializable> implements Supplier<T>, Serializable {
    private static final long serialVersionUID = 1581137873005537765L;

    /**
     * Recreate original object using data stored in this state.
     * @return Recreated object.
     * @implSpec This method always call {@link #get()} method.
     */
    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public final Object readResolve(){
        return get();
    }
}
