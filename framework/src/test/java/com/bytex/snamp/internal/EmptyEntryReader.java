package com.bytex.snamp.internal;

import com.bytex.snamp.EntryReader;
import com.bytex.snamp.ExceptionPlaceholder;

/**
 * Represents Entry Reader that does nothing.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class EmptyEntryReader<I, R> implements EntryReader<I, R, ExceptionPlaceholder> {
    private static final EntryReader INSTANCE = new EmptyEntryReader();

    private EmptyEntryReader(){

    }

    @SuppressWarnings("unchecked")
    public static <I, R> EntryReader<I, R, ExceptionPlaceholder> getInstance(){
        return INSTANCE;
    }

    @Override
    public boolean read(final I index, final R value) {
        return true;
    }
}
