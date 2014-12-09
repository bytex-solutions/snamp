package com.itworks.snamp.views;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the method that its behavior is dependent on the specified view types.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface ViewSpecific {
    /**
     * Gets the array of view types that affects the method behavior.
     * @return The array of view types that affects the method behavior.
     */
    Class<? extends View>[] value();
}
