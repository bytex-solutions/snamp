package com.bytex.snamp.connector.composite.functions;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class FunctionParserException extends Exception {
    private static final long serialVersionUID = 8017093670029336552L;

    private FunctionParserException(final String message){
        super(message);
    }

    FunctionParserException(){
        this("Unexpected end of expression");
    }

    FunctionParserException(final Token token, final int position){
        this(String.format("Unexpected token '%s' at position '%s'", token, position));
    }

    FunctionParserException(final char ch, final int position){
        this(String.format("Unexpected character '%s' at position '%s'", ch, position));
    }

    FunctionParserException(final Expression expr){
        this(String.format("Expected function definition but found '%s'", expr));
    }

    static FunctionParserException unknownFunctionName(final String functionName){
        return new FunctionParserException(String.format("Unknown function name '%s'", functionName));
    }

    static FunctionParserException unknownTimeUnit(final String unitName) {
        return new FunctionParserException(String.format("Unknown time measurement unit '%s'", unitName));
    }

    static FunctionParserException invalidReference(final String ref){
        return new FunctionParserException(String.format("Reference '%s' could not be resolved", ref));
    }
}
