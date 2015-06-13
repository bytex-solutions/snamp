package com.itworks.snamp.adapters.groovy;

import com.google.common.base.Predicate;
import groovy.lang.Closure;

import javax.management.MBeanAttributeInfo;

/**
 * Represents additional converters for Groovy {@link groovy.lang.Closure}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class Closures {
    private Closures(){

    }

    static Predicate toPredicate(final Closure<Boolean> closure){
        return new Predicate() {
            @Override
            public boolean apply(final Object input) {
                return closure.call(input);
            }
        };
    }

    static <I, E extends Throwable> AttributeValueHandler<I, E> toHandler(final Closure<?> closure){
        return new AttributeValueHandler<I, E>() {
            @Override
            public void handle(final String resourceName, final MBeanAttributeInfo metadata, final I attributeValue) {
                closure.call(resourceName, metadata, attributeValue);
            }
        };
    }
}
