package com.snamp;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a mutable (and not thread-safe) box for storing some object
 * of the specified type. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Internal
public class Box<T> implements Serializable, Wrapper<T> {
    private T value;

    /**
     * Initializes a new box with the predefined value.
     * @param val The value to save into box.
     */
    public Box(final T val){
        value = val;
    }

    /**
     * Initializes a new empty box with {@literal null} object inside.
     */
    public Box(){
        this(null);
    }

    /**
     * Clears this box.
     */
    public final void clear(){
        this.value = null;
    }

    /**
     * Overwrites the value inside of this box.
     * @param val A new value for the box.
     */
    public final void setValue(final T val){
        this.value = val;
    }

    /**
     * Returns the value stored inside of this box.
     * @return The value stored inside of this box.
     */
    public final T getValue(){
        return value;
    }

    /**
     * Computes the hash code for the stored object.
     * @return The hash code for the stored object.
     */
    @Override
    public final int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    /**
     * Creates a new copy of this box.
     * @return A new copy of this box.
     */
    @Override
    public final Box<T> clone() {
        return new Box<>(value);
    }

    /**
     * Determines whether the specified object is equal to the stored object.
     * @param obj An object to compare.
     * @return {@literal true}, if the specified object is equal to the stored object; otherwise, {@literal false}.
     */
    @Override
    public final boolean equals(final Object obj) {
        return obj instanceof Box ? equals(((Box<?>)obj).getValue()) : Objects.equals(value, obj);
    }

    /**
     * Returns a string representation of the stored object.
     * @return A string representation of the stored object.
     */
    @Override
    public final String toString() {
        return Objects.toString(value, "NULL");
    }

    /**
     * Determines whether the contained object is of specified type.
     * @param c The type to check.
     * @return {@literal true}, if the stored object is instance of the specified type; otherwise, {@literal false}.
     */
    public final boolean instanceOf(final Class<?> c){
        return c != null && c.isInstance(value);
    }

    /**
     * Converts the box type.
     * @param c A new box type to which the stored object will be casted.
     * @param <G> A new box type.
     * @return A new instance of the box.
     */
    public final <G> Box<G> cast(final Class<G> c){
        return new Box<>(c.cast(value));
    }

    /**
     * Determines whether the stored object is {@literal null}.
     * @return {@literal true}, if stored object is {@literal null}; otherwise, {@literal false}.
     */
    public final boolean isEmpty(){
        return value == null;
    }

    /**
     * Handles the wrapped object.
     *
     * @param handler The wrapped object handler.
     * @return The wrapped object handling result.
     */
    @Override
    public final  <R> R handle(final WrappedObjectHandler<T, R> handler) {
        return handler != null ? handler.invoke(value) : null;
    }
}
