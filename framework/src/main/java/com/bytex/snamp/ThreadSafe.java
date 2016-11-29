package com.bytex.snamp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents informative annotation that describes method usage in the multi-thread context.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
@Internal
public @interface ThreadSafe {
    /**
     * Gets a value indicating that the marked method can be called in multi-threaded environment
     * without additional locks and synchronization.
     * @return {@literal true}, if the marked method is synchronized; otherwise, {@literal false}.
     */
    boolean value() default true;
}
