package com.bytex.snamp.connector.composite.functions;

import javax.management.openmbean.OpenType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AggregationFunctionWithArity<T> extends AggregationFunction<T> {
    private final Expression[] arguments;

    AggregationFunctionWithArity(final OpenType<T> rt, final int arity) {
        super(rt);
        arguments = new Expression[arity];
    }

    final int getArity(){
        return arguments.length;
    }

    abstract void validateArgument(final Expression expr, final int position) throws FunctionParserException;

    final void setArgument(final Expression expr, final int position) throws FunctionParserException {
        validateArgument(expr, position);
        arguments[position] = expr;
    }
}
