package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.concurrent.LazyValue;
import com.bytex.snamp.concurrent.LazyValueFactory;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class EmptyStringEnumerator implements Enumeration<String> {
    private static final LazyValue<? extends Enumeration<String>> INSTANCE = LazyValueFactory.THREAD_SAFE_SOFT_REFERENCED.of(EmptyStringEnumerator::new);

    private EmptyStringEnumerator(){

    }

    public static Enumeration<String> getInstance(){
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
