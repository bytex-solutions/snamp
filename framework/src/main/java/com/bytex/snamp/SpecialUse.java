package com.bytex.snamp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the method, field, constructor or parameter as an element with special use.
 * This means that the program element is used by Reflection or native code
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.SOURCE)
public @interface SpecialUse {
}
