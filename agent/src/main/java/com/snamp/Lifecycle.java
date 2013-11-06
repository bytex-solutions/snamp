package com.snamp;

import java.lang.annotation.*;

/**
 * Describes lifecycle of the object.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Internal
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Lifecycle {
    /**
     * Represents lifecycle of the annotated class.
     * @return The lifecycle of the annotated class.
     */
    InstanceLifecycle value() default InstanceLifecycle.NORMAL;
}
