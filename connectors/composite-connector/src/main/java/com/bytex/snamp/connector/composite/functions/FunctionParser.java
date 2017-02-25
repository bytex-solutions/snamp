package com.bytex.snamp.connector.composite.functions;

import com.bytex.snamp.jmx.WellKnownType;
import com.bytex.snamp.parser.*;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
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

    private ExtractFunction parseExtractFunction() throws ParseException {
        nextToken(LeftBracketToken.class);
        //parse type name
        skipIgnoredChars();
        final CharSequence targetTypeName = readTo(CommaToken.VALUE);
        final WellKnownType targetType = WellKnownType.parse(targetTypeName.toString());
        if(targetType == null)
            throw FunctionParserException.unknownTypeDef(targetTypeName);
        final List<String> path = new LinkedList<>();
        //now we at comma token
        for (Token lookup = nextToken(CommaToken.class); !(lookup instanceof RightBracketToken); lookup = nextToken(token -> token instanceof SlashToken || token instanceof RightBracketToken)) {
            lookup = nextToken(NameToken.class);
            path.add(lookup.toString());
        }
        return new ExtractFunction(targetType, path);
    }

    private <F extends AggregationFunction<?>> F parseTrivialFunction(final Supplier<? extends F> factory) throws ParseException {
        nextToken(LeftBracketToken.class);
        nextToken(RightBracketToken.class);
        return factory.get();
    }

    private RangedGaugeFPFunction parseRangedGaugeFP() throws ParseException {
        nextToken(LeftBracketToken.class);
        //parse range start
        final double rangeStart = parseDouble(readTo(CommaToken.VALUE));
        nextToken(CommaToken.class);
        //parse range end
        final double rangeEnd = parseDouble(readTo(RightBracketToken.VALUE));
        nextToken(RightBracketToken.class);
        return new RangedGaugeFPFunction(rangeStart, rangeEnd);
    }

    private RangedGaugeIntFunction parseRangedGauge64() throws ParseException {
        nextToken(LeftBracketToken.class);
        //parse range start
        final long rangeStart = nextToken(IntegerToken.class).getAsLong();
        nextToken(CommaToken.class);
        //parse range end
        final long rangeEnd = nextToken(IntegerToken.class).getAsLong();
        nextToken(RightBracketToken.class);
        return new RangedGaugeIntFunction(rangeStart, rangeEnd);
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
                case "ranged_fp":
                    return parseRangedGaugeFP();
                case "ranged_int":
                    return parseRangedGauge64();
                case "sum":
                    return parseSumFunction();
                case "avg":
                    return parseAvgFunction();
                case "percentile":
                    return parsePercentileFunction();
                case "correl":
                    return parseCorrelationFunction();
                case "extract":
                    return parseExtractFunction();
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
