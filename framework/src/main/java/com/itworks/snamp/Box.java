package com.itworks.snamp;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

import java.util.Objects;

/**
 * Represents simple container that holds mutable typed value.
 * @param <T> Type of the value in the container.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class Box<T> implements Wrapper<T>, Supplier<T>, SafeConsumer<T>, Cloneable{
    private T value;

    /**
     * Initializes a new instance of the mutable container.
     * @param initial The initial value of placed to the container.
     */
    public Box(final T initial){
        this.value = initial;
    }

    /**
     * Clones this container.
     * @return A new instance of this container.
     */
    @SuppressWarnings({"CloneDoesntDeclareCloneNotSupportedException", "CloneDoesntCallSuperClone"})
    @Override
    public Box<T> clone() {
        return new Box<>(value);
    }

    /**
     * Initializes a new empty container.
     */
    public Box(){
        this(null);
    }


    /**
     * Performs this operation on the given argument.
     *
     * @param value The value to process.
     */
    @Override
    public final void accept(final T value) {
        set(value);
    }

    /**
     * Retrieves an instance of the appropriate type. The returned object may or
     * may not be a new instance, depending on the implementation.
     *
     * @return an instance of the appropriate type
     */
    @Override
    public final T get() {
        return value;
    }

    /**
     * Places a new value into this container.
     * @param value A new value to store into this container.
     */
    public final void set(final T value){
        this.value = value;
    }

    /**
     * Places a new value into this container.
     * @param value The value to be transformed and placed into this container.
     * @param transformer The transformer applied to input value.
     * @param <I> Type of the value to be transformed.
     */
    public final <I> void set(final I value, final Function<I, T> transformer){
        set(transformer.apply(value));
    }

    /**
     * Handles the wrapped object.
     * <p>
     * It is not recommended to return the original wrapped object from the handler.
     * </p>
     *
     * @param handler The wrapped object handler.
     * @return The wrapped object handling result.
     */
    @Override
    public final <R> R handle(final Function<T, R> handler) {
        return handler.apply(value);
    }

    /**
     * Returns string representation of the aggregated object.
     * @return The string representation of the aggregated object.
     */
    @Override
    public String toString() {
        return Objects.toString(value);
    }
}
