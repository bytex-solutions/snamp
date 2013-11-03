package com.snamp;

import java.lang.annotation.*;

/**
 * Represents method usage description in the multi-thread environment.
 * @author roman
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface ThreadSafety {
    /**
     * Represents method thread safe kind.
     * @return
     */
    MethodThreadSafety value();
}
