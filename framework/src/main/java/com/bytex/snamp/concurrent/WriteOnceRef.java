package com.bytex.snamp.concurrent;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.bytex.snamp.Wrapper;
import com.bytex.snamp.ThreadSafe;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a container that can be written once per its lifetime.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class WriteOnceRef<T> implements Wrapper<T>, Supplier<T> {
    private T value;
    private final AtomicBoolean locked;

    /**
     * Initializes a new write-once container.
     * @param initValue Initial value placed in the new container.
     */
    public WriteOnceRef(final T initValue){
        value = initValue;
        locked = new AtomicBoolean(false);
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
    @ThreadSafe
    public final boolean set(final T value){
        if(locked.compareAndSet(false, true)) {
            this.value = value;
            return true;
        }
        else return false;
    }

    /**
     * Determines whether the container is locked and value inside of it cannot be changed.
     * @return {@literal true}, if this container is locked; otherwise, {@literal false}.
     */
    @SuppressWarnings("UnusedDeclaration")
    @ThreadSafe
    public final boolean isLocked(){
        return locked.get();
    }

    /**
     * Gets the value stored in this container.
     * @return The value stored in this container.
     */
    @ThreadSafe
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

    /**
     * Determines whether the stored object equals to the specified object.
     * @param obj An object to compare.
     * @return {@literal true}, if stored object equals to the specified object; otherwise, {@literal false}.
     */
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof WriteOnceRef<?> ?
                equals(((WriteOnceRef<?>) obj).get()) :
                Objects.equals(value, obj);

    }

    /**
     * Handles the wrapped object.
     *
     * @param handler The wrapped object handler.
     * @return The wrapped object handling result.
     */
    @Override
    public final <R> R handle(final Function<T, R> handler) {
        return handler != null ? handler.apply(value) : null;
    }
}
