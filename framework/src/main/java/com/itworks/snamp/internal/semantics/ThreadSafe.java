package com.itworks.snamp.internal.semantics;

import com.itworks.snamp.internal.semantics.Internal;

import java.lang.annotation.*;

/**
 * Represents informative annotation that describes method usage in the multi-thread context.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Target(ElementType.METHOD)
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
