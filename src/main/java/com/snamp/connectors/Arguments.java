package com.snamp.connectors;

import java.io.Serializable;
import java.util.*;

/**
 * Represents action invocation arguments,
 * @author roman
 */
public final class Arguments extends HashMap<String, Object> implements Serializable {
    static final long serialVersionUID = -1242599979055084673L;

    /**
     * Returns the argument by its index.
     * @param index The index of the argument.
     * @return The value of the argument.
     */
    public final Object get(final int index){
        return get(Integer.toString(index));
    }

    /**
     * Puts the argument to this collection.
     * @param index The index of the argument.
     * @param value The argument value.
     * @return The previous argument value.
     */
    public final Object put(final int index, final Object value){
        return put(Integer.toString(index), value);
    }

    /**
     * Puts the named argument to this collection.
     * @param index The index of the argument.
     * @param name The name of the argument.
     * @param value The value of the argument.
     */
    public final void put(final int index, final String name, final Object value){
        put(index, value);
        put(name, value);
    }
}
