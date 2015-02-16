package com.itworks.snamp.jmx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents name and description of the composite data
 * or tabular data.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JmxType {
    /**
     * Gets the name of the type.
     * @return The name of the type.
     */
    String name();

    /**
     * Gets the type description.
     * @return The type description.
     */
    String description();
}
