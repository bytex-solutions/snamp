package com.itworks.snamp.internal;

import java.lang.annotation.*;

/**
 * Identifies the method without any implementation code.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface MethodStub {
}