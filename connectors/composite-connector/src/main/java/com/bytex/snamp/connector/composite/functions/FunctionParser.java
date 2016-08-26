package com.bytex.snamp.connector.composite.functions;

import com.bytex.snamp.gateway.modeling.AttributeAccessor;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

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

    private static TimeUnit parseTimeUnit(final Tokenizer lexer) throws FunctionParserException{
        final String unitName;
        switch (unitName = lexer.nextToken(NameToken.class).toString()){
            case "s":
            case "sec":
            case "seconds":
                return TimeUnit.SECONDS;
            case "ms":
            case "millis":
                return TimeUnit.MILLISECONDS;
            case "ns":
            case "nanos":
                return TimeUnit.NANOSECONDS;
            case "m":
            case "minutes":
                return TimeUnit.MINUTES;
            case "h":
            case "hours":
                return TimeUnit.HOURS;
            case "d":
            case "days":
                return TimeUnit.DAYS;
            default:
                throw FunctionParserException.unknownTimeUnit(unitName);
        }
    }

    private static AverageFunction parseAvgFunction(final Tokenizer lexer) throws FunctionParserException{
        lexer.nextToken(LeftBracketToken.class);
        final long interval = lexer.nextToken(IntegerToken.class).getAsLong();
        final TimeUnit unit = parseTimeUnit(lexer);
        lexer.nextToken(RightBracketToken.class);
        return new AverageFunction(interval, unit);
    }

    private static PercentileFunction parsePercentileFunction(final Tokenizer lexer) throws FunctionParserException{
        lexer.nextToken(LeftBracketToken.class);
        final long percentile = lexer.nextToken(IntegerToken.class).getAsLong();
        lexer.nextToken(CommaToken.class);
        final long interval = lexer.nextToken(IntegerToken.class).getAsLong();
        final TimeUnit unit = parseTimeUnit(lexer);
        lexer.nextToken(RightBracketToken.class);
        return new PercentileFunction(percentile, interval, unit);
    }

    private static SumFunction parseSumFunction(final Tokenizer lexer) throws FunctionParserException{
        lexer.nextToken(LeftBracketToken.class);
        final long interval = lexer.nextToken(IntegerToken.class).getAsLong();
        final TimeUnit unit = parseTimeUnit(lexer);
        lexer.nextToken(RightBracketToken.class);
        return new SumFunction(interval, unit);
    }

    private static CorrelationFunction parseCorrelationFunction(final Tokenizer lexer, final ReferenceResolver resolver) throws FunctionParserException{
        lexer.nextToken(LeftBracketToken.class);
        //parse reference
        lexer.nextToken(DollarToken.class);
        final String ref = lexer.nextToken(NameToken.class).toString();
        final Callable<?> valueProvider = resolver.resolve(ref);
        if(valueProvider == null)
            throw FunctionParserException.invalidReference(ref);
        lexer.nextToken(RightBracketToken.class);
        return new CorrelationFunction(valueProvider);
    }

    private static AggregationFunction<?> parseFunction(final String functionName, final Tokenizer lexer, final ReferenceResolver resolver) throws FunctionParserException{
        switch (functionName){
            case "max":
                lexer.nextToken(LeftBracketToken.class);
                lexer.nextToken(RightBracketToken.class);
                return ToDoubleUnaryFunction.max();
            case "min":
                lexer.nextToken(LeftBracketToken.class);
                lexer.nextToken(RightBracketToken.class);
                return ToDoubleUnaryFunction.min();
            case "sum":
                return parseSumFunction(lexer);
            case "avg":
                return parseAvgFunction(lexer);
            case "percentile":
                return parsePercentileFunction(lexer);
            case "correl":
                return parseCorrelationFunction(lexer, resolver);
            default:
                throw FunctionParserException.unknownFunctionName(functionName);
        }
    }

    private static Expression parse(final Tokenizer lexer, final ReferenceResolver resolver) throws FunctionParserException {
        final Token token = lexer.nextToken();
        if (token == null)
            throw new FunctionParserException();
        else if (token instanceof NameToken)
            return parseFunction(token.toString(), lexer, resolver);
        else
            throw new FunctionParserException(token, lexer.get());
    }

    /**
     * Parses formula expression.
     * @param input An expression to parse.
     * @param referenceResolver A function used to resolve references.
     * @return Parsed function.
     * @throws FunctionParserException Invalid formula expression.
     */
    public static AggregationFunction<?> parse(final CharSequence input, final ReferenceResolver referenceResolver) throws FunctionParserException{
        final Expression result = parse(new Tokenizer(input), Objects.requireNonNull(referenceResolver));
        if(result instanceof AggregationFunction<?>)
            return (AggregationFunction<?>)result;
        else
            throw new FunctionParserException(result);
    }
}
