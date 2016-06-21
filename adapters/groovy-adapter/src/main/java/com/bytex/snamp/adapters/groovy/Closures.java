package com.bytex.snamp.adapters.groovy;

import com.bytex.snamp.adapters.NotificationListener;
import com.google.common.base.Predicate;
import groovy.lang.Closure;

/**
 * Represents additional converters for Groovy {@link groovy.lang.Closure}.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class Closures {
    private Closures(){

    }

    static <T> Predicate<T> toPredicate(final Closure<Boolean> closure){
        return input -> {
            switch (closure.getMaximumNumberOfParameters()){
                case 1:
                    return closure.call(input);
                case 0:
                    return closure.call();
                default:
                    return false;
            }
        };
    }

    static NotificationListener toNotificationHandler(final Closure<?> closure){
        return event -> {
            switch (closure.getMaximumNumberOfParameters()){
                case 1:
                    closure.call(event);
                    return;
                case 2:
                    closure.call(event.getSource(), event.getNotification());
                    return;
                case 0:
                    closure.call();
            }
        };
    }

    static <I, E extends Throwable> AttributeValueHandler<I, E> toAttributeHandler(final Closure<?> closure){
        return (resourceName, metadata, attributeValue) -> {
            switch (closure.getMaximumNumberOfParameters()){
                case 3:
                    closure.call(resourceName, metadata, attributeValue);
                return;
                case 2:
                    closure.call(resourceName, metadata);
                return;
                case 1:
                    closure.call(resourceName);
                return;
                case 0:
                    closure.call();
            }
        };
    }
}
