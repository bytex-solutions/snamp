package com.itworks.snamp.internal;

import java.lang.annotation.*;

/**
 * Indicates that the overridden method has partial implementation.
 * <p>
 *     The derived class should override method with partial implementation
 *     and call this implementation using <b>super</b> keyword.
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Partial {
}
