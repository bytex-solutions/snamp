package com.bytex.snamp.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SystemProperties {
    /**
     * Use '=' to separate name and value.
     * @return
     */
    String[] value() default {};
}
