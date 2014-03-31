package com.snamp.internal;

import com.snamp.internal.Internal;

import java.lang.annotation.*;

/**
 * Marks the local variable as temporary variable that stores
 * intermediate computation results.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Internal
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.LOCAL_VARIABLE)
public @interface Temporary {
}
