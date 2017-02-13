package com.bytex.snamp.moa.watching;

/**
 * Represents condition used to evaluate attribute.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Condition {
    boolean eval(final Object attribute, final EvaluationContext context) throws Exception;
}
