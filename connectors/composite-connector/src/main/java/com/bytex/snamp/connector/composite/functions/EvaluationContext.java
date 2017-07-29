package com.bytex.snamp.connector.composite.functions;

import javax.management.openmbean.SimpleType;

/**
 * A function used to resolve named references in formula.
 * @since 2.0
 * @version 2.1
 * @author Roman Sakno
 */
public interface EvaluationContext {
    <T> T resolveName(final String name, final SimpleType<T> expectedType) throws Exception;
}
