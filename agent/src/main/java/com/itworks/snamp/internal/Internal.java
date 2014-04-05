package com.itworks.snamp.internal;

import java.lang.annotation.*;

/**
 * Indicates that the annotated element is for internal purposes only and not intended to use directly from
 * your code,
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.ANNOTATION_TYPE})
public @interface Internal {
}
