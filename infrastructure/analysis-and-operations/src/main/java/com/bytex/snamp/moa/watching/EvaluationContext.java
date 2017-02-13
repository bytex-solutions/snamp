package com.bytex.snamp.moa.watching;

import java.util.Optional;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface EvaluationContext {
    <V> Optional<V> getAttribute(final String namespace, final String attribute);
}
