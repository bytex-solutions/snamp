package com.snamp;

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
public @interface ThreadSafety {
    /**
     * Represents method thread safety kind.
     * @return The method thread safety kind.
     */
    MethodThreadSafety value() default MethodThreadSafety.THREAD_UNSAFE;

    /**
     * Represents an advice that can be used for organizing annotated method invocation.
     * @return The advice that can be used for organizing annotated method invocation.
     */
    SynchronizationType advice() default SynchronizationType.NO_LOCK_REQUIRED;
}
