package com.bytex.snamp.concurrent;

import com.bytex.snamp.SpecialUse;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Supplier;

/**
 * Represents a container that can be written once per its lifetime.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@ThreadSafe
public class WriteOnceRef<T> implements Supplier<T> {
    private static final AtomicIntegerFieldUpdater<WriteOnceRef> LOCKED_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(WriteOnceRef.class, "locked");
    private T value;
    @SpecialUse(SpecialUse.Case.JVM)
    private volatile int/*boolean*/ locked = 0;

    /**
     * Initializes a new write-once container.
     * @param initValue Initial value placed in the new container.
     */
    public WriteOnceRef(final T initValue){
        value = initValue;
    }

    /**
     * Initializes a new write-once container with default {@literal null} value.
     */
    public WriteOnceRef(){
        this(null);
    }

    /**
     * Changes the value of the container.
     * <p>
     *     This method returns {@literal true} only once per instance lifetime.
     * </p>
     * @param value The value to set.
     * @return {@literal true}, if container is changed successfully; otherwise, {@literal false}.
     */
    public final boolean set(final T value) {
        if (LOCKED_UPDATER.compareAndSet(this, 0, 1)) {
            this.value = value;
            return true;
        } else return false;
    }

    /**
     * Clears this reference.
     */
    public final void clear(){
        LOCKED_UPDATER.set(this, 1);
        this.value = null;
    }

    /**
     * Determines whether the container is locked and value inside of it cannot be changed.
     * @return {@literal true}, if this container is locked; otherwise, {@literal false}.
     */
    public final boolean isLocked(){
        return LOCKED_UPDATER.get(this) > 0;
    }

    /**
     * Gets the value stored in this container.
     * @return The value stored in this container.
     */
    public final T get(){
        return value;
    }

    /**
     * Returns a string representation of the object stored in this container.
     * @return A string representation of the object stored in this container.
     */
    @Override
    public String toString() {
        return Objects.toString(value);
    }

    /**
     * Computes the hash code for the object stored in this container.
     * @return The hash code for the object stored in this container.
     */
    @Override
    public int hashCode() {
        final T v = value;
        return v != null ? v.hashCode() : 0;
    }

    private boolean equals(final WriteOnceRef<?> other){
        return Objects.equals(value, other.get());
    }

    /**
     * Determines whether the stored object equals to the specified object.
     * @param obj An object to compare.
     * @return {@literal true}, if stored object equals to the specified object; otherwise, {@literal false}.
     */
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof WriteOnceRef<?> ?
                equals(((WriteOnceRef<?>) obj)) :
                Objects.equals(value, obj);

    }
}
