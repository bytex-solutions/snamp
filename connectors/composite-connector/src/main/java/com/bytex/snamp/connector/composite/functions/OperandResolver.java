package com.bytex.snamp.connector.composite.functions;

import javax.management.openmbean.SimpleType;

/**
 * A function used to resolve named operands in formula.
 * @since 2.0
 * @version 2.0
 * @author Roman Sakno
 */
public interface OperandResolver {
    <T> T resolveAs(final String operand, final SimpleType<T> expectedType) throws Exception;
}
