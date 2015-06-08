package com.itworks.snamp.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a set of framework properties.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FrameworkProperties {
    /**
     * An array of OSGi framework properties.
     * @return An array of OSGi framework properties.
     */
    FrameworkProperty[] value();
}
