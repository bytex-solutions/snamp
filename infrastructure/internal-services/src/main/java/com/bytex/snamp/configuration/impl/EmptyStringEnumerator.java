package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.concurrent.LazyStrongReference;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Represents empty enumerator over set of strings.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class EmptyStringEnumerator implements Enumeration<String> {
    private static final LazyStrongReference<EmptyStringEnumerator> INSTANCE = new LazyStrongReference<>();

    private EmptyStringEnumerator(){

    }

    static Enumeration<String> getInstance(){
        return INSTANCE.lazyGet(EmptyStringEnumerator::new);
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
