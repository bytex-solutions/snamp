package com.itworks.snamp.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a set of required SNAMP dependencies.
 * @author Evgeny Kirichenko
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SnampDependencies {
    /**
     * Returns an array of SNAMP-related dependencies.
     * @return An array of dependencies.
     */
    SnampFeature[] value() default {};
}
