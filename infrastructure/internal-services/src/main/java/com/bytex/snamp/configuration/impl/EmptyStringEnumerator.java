package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.LazyValue;
import com.bytex.snamp.LazyValueFactory;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Represents empty enumerator over set of strings.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class EmptyStringEnumerator implements Enumeration<String> {
    private static final LazyValue<? extends Enumeration<String>> INSTANCE = LazyValueFactory.THREAD_SAFE_SOFT_REFERENCED.of(EmptyStringEnumerator::new);

    private EmptyStringEnumerator(){

    }

    static Enumeration<String> getInstance(){
        return INSTANCE.get();
    }

    @Override
    public boolean hasMoreElements() {
        return false;
    }

    @Override
    public String nextElement() {
        throw new NoSuchElementException();
    }
}
