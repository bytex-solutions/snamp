package com.snamp;

import java.io.Serializable;

/**
 * Represents a pair of some objects. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class Pair<T1, T2> implements Serializable, Cloneable {
    /**
     * Represents the first element in the pair
     */
    public final T1 first;

    /**
     * Represents the second element in the pair.
     */
    public final T2 second;

    /**
     * Initializes a new pair of objects.
     * @param f The first object in the pair.
     * @param s The second object in the pair.
     */
    public Pair(final T1 f, final T2 s){
        this.first = f;
        this.second = s;
    }

    /**
     * Returns a new copy of this pair.
     * @return A new copy of this pair.
     */
    @Override
    public final Pair<T1, T2> clone() {
        return new Pair<>(first, second);
    }

    /**
     * Returns a string representation of this pair.
     * @return The string representation of this pair.
     */
    @Override
    public final String toString() {
        return String.format("[%s, %s]", first, second);
    }
}
