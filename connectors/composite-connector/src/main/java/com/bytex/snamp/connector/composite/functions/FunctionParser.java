package com.bytex.snamp.connector.composite.functions;

import com.bytex.snamp.parser.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents parser for function expression.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class FunctionParser extends Tokenizer {
    private FunctionParser(final CharSequence input){
        super(input);
    }

    private TimeUnit parseTimeUnit() throws ParseException {
        final String unitName;
        switch (unitName = nextToken(NameToken.class).toString()) {
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

    private AverageFunction parseAvgFunction() throws ParseException{
        nextToken(LeftBracketToken.class);
        final long interval = nextToken(IntegerToken.class).getAsLong();
        final TimeUnit unit = parseTimeUnit();
        nextToken(RightBracketToken.class);
        return new AverageFunction(interval, unit);
    }

    private PercentileFunction parsePercentileFunction() throws ParseException{
        nextToken(LeftBracketToken.class);
        final long percentile = nextToken(IntegerToken.class).getAsLong();
        nextToken(RightBracketToken.class);
        return new PercentileFunction(percentile);
    }

    private SumFunction parseSumFunction() throws ParseException{
        nextToken(LeftBracketToken.class);
        final long interval = nextToken(IntegerToken.class).getAsLong();
        final TimeUnit unit = parseTimeUnit();
        nextToken(RightBracketToken.class);
        return new SumFunction(interval, unit);
    }

    private CorrelationFunction parseCorrelationFunction() throws ParseException{
        nextToken(LeftBracketToken.class);
        //parse reference
        nextToken(DollarToken.class);
        final String operand = nextToken(NameToken.class).toString();
        nextToken(RightBracketToken.class);
        return new CorrelationFunction(operand);
    }

    private AbstractExtractFunction<?> parseExtractFunction(final Function<? super Collection<String>, ? extends AbstractExtractFunction<?>> factory) throws ParseException {
        final List<String> path = new LinkedList<>();
        for (Token lookup = nextToken(LeftBracketToken.class); !(lookup instanceof RightBracketToken); lookup = nextToken(token -> token instanceof SlashToken || token instanceof RightBracketToken)) {
            lookup = nextToken(NameToken.class);
            path.add(lookup.toString());
        }
        return factory.apply(path);
    }

    private <F extends AggregationFunction<?>> F parseTrivialFunction(final Supplier<? extends F> factory) throws ParseException {
        nextToken(LeftBracketToken.class);
        nextToken(RightBracketToken.class);
        return factory.get();
    }

    private Expression parse() throws ParseException {
        final Token token = nextToken();
        if (token == null)
            throw new FunctionParserException();
        else if (token instanceof NameToken)
            switch (token.toString()){
                case "max":
                    return parseTrivialFunction(NumericUnaryFunction::max);
                case "min":
                    return parseTrivialFunction(NumericUnaryFunction::min);
                case "gauge_fp":
                    return parseTrivialFunction(GaugeFPFunction::new);
                case "gauge_int":
                    return parseTrivialFunction(GaugeIntFunction::new);
                case "sum":
                    return parseSumFunction();
                case "avg":
                    return parseAvgFunction();
                case "percentile":
                    return parsePercentileFunction();
                case "correl":
                    return parseCorrelationFunction();
                case "extract":
                    return parseExtractFunction(ExtractAsStringFunction::new);
                case "extract_fp":
                    return parseExtractFunction(ExtractAsDoubleFunction::new);
                case "extract_int":
                    return parseExtractFunction(ExtractAsIntFunction::new);
                case "flag":
                    return parseTrivialFunction(FlagFunction::new);
                default:
                    throw FunctionParserException.unknownFunctionName(token.toString());
            }
        else
            throw new UnexpectedTokenException(token);
    }

    /**
     * Parses formula expression.
     * @param input An expression to parse.
     * @return Parsed function.
     * @throws ParseException Invalid formula expression.
     */
    public static AggregationFunction<?> parse(final CharSequence input) throws ParseException {
        final Expression result;
        try (final FunctionParser parser = new FunctionParser(input)) {
            result = parser.parse();
        }
        if (result instanceof AggregationFunction<?>)
            return (AggregationFunction<?>) result;
        else
            throw new FunctionParserException(result);
    }
}
