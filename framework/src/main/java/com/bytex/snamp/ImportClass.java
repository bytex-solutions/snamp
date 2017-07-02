package com.bytex.snamp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents special annotation to help maven-bundle-plugin construct correct entries
 * in section Import-Package.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ImportClass {
    /**
     * Array of referenced types.
     * @return Array of referenced types.
     */
    Class<?>[] value();
}
