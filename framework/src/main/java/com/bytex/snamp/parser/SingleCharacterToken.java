package com.bytex.snamp.parser;

import java.io.Serializable;

/**
 * Represents single-character token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface SingleCharacterToken extends CharSequence, Serializable {
    /**
     * Gets character wrapped by this token.
     * @return Wrapped character.
     */
    char getValue();

    /**
     * Returns the length of this character sequence.  The length is the number
     * of 16-bit <code>char</code>s in the sequence.
     *
     * @return the number of <code>char</code>s in this sequence
     */
    @Override
    default int length(){
        return 1;
    }

    /**
     * Returns the <code>char</code> value at the specified index.  An index ranges from zero
     * to <tt>length() - 1</tt>.  The first <code>char</code> value of the sequence is at
     * index zero, the next at index one, and so on, as for array
     * indexing.
     * <p>
     * <p>If the <code>char</code> value specified by the index is a
     * <a href="{@docRoot}/java/lang/Character.html#unicode">surrogate</a>, the surrogate
     * value is returned.
     *
     * @param index the index of the <code>char</code> value to be returned
     * @return the specified <code>char</code> value
     * @throws IndexOutOfBoundsException if the <tt>index</tt> argument is negative or not less than
     *                                   <tt>length()</tt>
     */
    @Override
    default char charAt(final int index) {
        if(index == 0)
            return getValue();
        else
            throw new IndexOutOfBoundsException();
    }
}
