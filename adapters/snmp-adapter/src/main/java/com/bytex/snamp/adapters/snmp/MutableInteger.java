package com.bytex.snamp.adapters.snmp;

/**
 * Represents mutable wrapper for {@literal int} data type.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class MutableInteger extends Number implements Comparable<MutableInteger> {
    private static final long serialVersionUID = -7135764717407730727L;
    private int value;

    MutableInteger(final int value){
        this.value = value;
    }

    int get(){
        return value;
    }

    void set(final int value){
        this.value = value;
    }

    int getAndIncrement(){
        return value++;
    }

    /**
     * Returns the value of the specified number as an <code>int</code>.
     * This may involve rounding or truncation.
     *
     * @return the numeric value represented by this object after conversion
     * to type <code>int</code>.
     */
    @Override
    public int intValue() {
        return value;
    }

    /**
     * Returns the value of the specified number as a <code>long</code>.
     * This may involve rounding or truncation.
     *
     * @return the numeric value represented by this object after conversion
     * to type <code>long</code>.
     */
    @Override
    public long longValue() {
        return value;
    }

    /**
     * Returns the value of the specified number as a <code>float</code>.
     * This may involve rounding.
     *
     * @return the numeric value represented by this object after conversion
     * to type <code>float</code>.
     */
    @Override
    public float floatValue() {
        return value;
    }

    /**
     * Returns the value of the specified number as a <code>double</code>.
     * This may involve rounding.
     *
     * @return the numeric value represented by this object after conversion
     * to type <code>double</code>.
     */
    @Override
    public double doubleValue() {
        return value;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(final MutableInteger o) {
        return Integer.compare(value, o.value);
    }

    public boolean equals(final Number other){
        return other != null && value == other.intValue();
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof Number && equals((Number)other);
    }

    @Override
    public int hashCode() {
        return value;
    }
}
