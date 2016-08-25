package com.bytex.snamp.connector.composite.functions;

import java.util.concurrent.TimeUnit;

/**
 * Represents parser for function expressiong.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class FunctionParser {
    private FunctionParser(){
        throw new InstantiationError();
    }

    private static AverageFunction parseAvgFunction(final Tokenizer lexer) throws FunctionParserException{
        lexer.nextToken(LeftBracketToken.class);
        final long interval = lexer.nextToken(IntegerToken.class).getAsLong();
        final TimeUnit unit;
        final String unitName;
        switch (unitName = lexer.nextToken(NameToken.class).toString()){
            case "s":
            case "sec":
            case "seconds":
                unit = TimeUnit.SECONDS;
                break;
            case "ms":
            case "millis":
                unit = TimeUnit.MILLISECONDS;
                break;
            case "ns":
            case "nanos":
                unit = TimeUnit.NANOSECONDS;
                break;
            case "m":
            case "minutes":
                unit = TimeUnit.MINUTES;
                break;
            case "h":
            case "hours":
                unit = TimeUnit.HOURS;
                break;
            case "d":
            case "days":
                unit = TimeUnit.DAYS;
                break;
            default:
                throw FunctionParserException.unknownTimeUnit(unitName);
        }
        return new AverageFunction(interval, unit);
    }

    private static AggregationFunction<?> parseFunction(final String functionName, final Tokenizer lexer) throws FunctionParserException{
        switch (functionName){
            case "max":
                lexer.nextToken(LeftBracketToken.class);
                lexer.nextToken(RightBracketToken.class);
                return ToDoubleUnaryFunction.max();
            case "min":
                lexer.nextToken(LeftBracketToken.class);
                lexer.nextToken(RightBracketToken.class);
                return ToDoubleUnaryFunction.min();
            case "avg":
                return parseAvgFunction(lexer);
            default:
                throw FunctionParserException.unknownFunctionName(functionName);
        }
    }

    private static Expression parse(final Tokenizer lexer) throws FunctionParserException {
        final Token token = lexer.nextToken();
        if (token == null)
            throw new FunctionParserException();
        else if (token instanceof NameToken)
            return parseFunction(token.toString(), lexer);
        else
            throw new FunctionParserException(token, lexer.get());
    }

    public static AggregationFunction<?> parse(final CharSequence input) throws FunctionParserException{
        final Expression result = parse(new Tokenizer(input));
        if(result instanceof AggregationFunction<?>)
            return (AggregationFunction<?>)result;
        else
            throw new FunctionParserException(result);
    }
}
