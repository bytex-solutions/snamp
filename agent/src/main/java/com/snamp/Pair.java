package com.snamp;

import java.io.Serializable;

/**
 * Represents a pair of some objects. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class Pair<T1, T2> implements Serializable {
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
}
