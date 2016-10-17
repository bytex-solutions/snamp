package com.bytex.snamp;

import java.io.Serializable;
import java.util.function.BooleanSupplier;

/**
 * Represents mutable container for {@code boolean} data type.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@ThreadSafe(false)
public final class MutableBoolean implements BooleanSupplier, Serializable, Comparable<BooleanSupplier> {
    private static final long serialVersionUID = 5106640286011881150L;
    private boolean value;

    public MutableBoolean(final boolean value){
        this.value = value;
    }

    public MutableBoolean(){
        this(false);
    }

    /**
     * Gets a boolean value stored in this container.
     * @return A boolean value stored in this container.
     */
    @Override
    public boolean getAsBoolean() {
        return get();
    }

    /**
     * Gets a boolean value stored in this container.
     * @return A boolean value stored in this container.
     */
    public boolean get(){
        return value;
    }

    /**
     * Applies NOT operator to the value in this container and return new value.
     * @return A new value in the container.
     */
    public boolean invert() {
        return modify(BooleanUnaryOperator.NEGATE);
    }

    public boolean and(final boolean right) {
        return set(right, (l, r) -> l & r);
    }

    public boolean or(final boolean right){
        return set(right, (l, r) -> l | r);
    }

    public boolean xor(final boolean right){
        return set(right, (l, r) -> l ^ r);
    }

    public boolean set(final boolean right, final BooleanBinaryOperator operator){
        return value = operator.applyAsBoolean(value, right);
    }

    public boolean modify(final BooleanUnaryOperator operator){
        return value = operator.applyAsBoolean(value);
    }

    /**
     * Change value stored in this container.
     * @param value A new value to store.
     */
    public void set(final boolean value){
        this.value = value;
    }

    public void setTrue(){
        set(true);
    }

    public void setFalse(){
        set(false);
    }

    public int compareTo(final boolean other){
        return Boolean.compare(value, other);
    }

    @Override
    public int compareTo(final BooleanSupplier other) {
        return compareTo(other.getAsBoolean());
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }

    private boolean equals(final boolean other){
        return value == other;
    }

    private boolean equals(final BooleanSupplier other){
        return equals(other.getAsBoolean());
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof Boolean)
            return equals((boolean) other);
        else
            return other instanceof BooleanSupplier && equals((BooleanSupplier) other);
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }
}
