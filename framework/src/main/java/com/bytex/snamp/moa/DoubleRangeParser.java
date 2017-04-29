package com.bytex.snamp.moa;

import com.bytex.snamp.parser.*;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Parses interval notation into {@link Range}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class DoubleRangeParser extends Tokenizer {
    private static final String POSITIVE_INFINITY = "+∞";
    private static final String NEGATIVE_INFINITY = "-∞";

    private static final class DoubleCut{
        final double value;
        final BoundType type;

        private DoubleCut(final double value, final BoundType boundType){
            this.value = value;
            this.type = boundType;
        }

        static DoubleCut negativeInfinity(){
            return new DoubleCut(Double.NEGATIVE_INFINITY, BoundType.OPEN);
        }

        static DoubleCut positiveInfinity(){
            return new DoubleCut(Double.POSITIVE_INFINITY, BoundType.OPEN);
        }

        static DoubleCut inclusive(final double value){
            return new DoubleCut(value, BoundType.CLOSED);
        }

        static DoubleCut exclusive(final double value){
            return new DoubleCut(value, BoundType.OPEN);
        }
    }

    private static final class InvalidStartOfRange extends ParseException{
        private static final long serialVersionUID = 4633860104063688740L;

        private InvalidStartOfRange(final Token invalidToken){
            super(String.format("Expected '[' or '(' but found '%s'", invalidToken));
        }
    }

    private static final class InvalidEndOfRange extends ParseException{
        private static final long serialVersionUID = 8938289629385299761L;

        private InvalidEndOfRange(final Token invalidToken){
            super(String.format("Expected ']' or ')' but found '%s'", invalidToken));
        }
    }

    private DoubleRangeParser(final CharSequence sequence) {
        super(sequence);
    }

    private DoubleCut parseLeftCut() throws ParseException {
        final Token token = nextToken();
        final String leftCut = readTo(TwoDotLeaderToken.VALUE).toString();
        if (LeftBracketToken.INSTANCE.equals(token))//expecting negative infinity or double value
            switch (leftCut) {
                case NEGATIVE_INFINITY:
                    return DoubleCut.negativeInfinity();
                default:
                    return DoubleCut.exclusive(Double.parseDouble(leftCut));
            }
        else if (LeftSquareBracketToken.INSTANCE.equals(token))  //expecting double value only
            return DoubleCut.inclusive(Double.parseDouble(leftCut));
        else
            throw new InvalidStartOfRange(token);
    }

    private DoubleCut parseRightCut() throws ParseException {
        final String rightCut = readTo(RightSquareBracketToken.VALUE, RightBracketToken.VALUE).toString();
        final Token token = nextToken();
        if(RightBracketToken.INSTANCE.equals(token))
            switch (rightCut){
                case POSITIVE_INFINITY:
                    return DoubleCut.positiveInfinity();
                default:
                    return DoubleCut.exclusive(Double.parseDouble(rightCut));
            }
        else if(RightSquareBracketToken.INSTANCE.equals(token))
            return DoubleCut.inclusive(Double.parseDouble(rightCut));
        else
            throw new InvalidEndOfRange(token);
    }

    private Range<Double> parse() throws ParseException {
        final DoubleCut leftCut = parseLeftCut();
        nextToken(TwoDotLeaderToken.INSTANCE::equals);   //pass through comma
        final DoubleCut rightCut = parseRightCut();
        if (Double.isInfinite(leftCut.value))
            return Double.isInfinite(rightCut.value) ? Range.all() : Range.upTo(rightCut.value, rightCut.type);
        else if (Double.isInfinite(rightCut.value))
            return Range.downTo(leftCut.value, leftCut.type);
        else
            return Range.range(leftCut.value, leftCut.type, rightCut.value, rightCut.type);
    }

    @Override
    protected PunctuationToken getPunctuationToken(final char ch) {
        switch (ch) {
            case TwoDotLeaderToken.VALUE:
                return TwoDotLeaderToken.INSTANCE;
            default:
                return super.getPunctuationToken(ch);
        }
    }

    public static Range<Double> parse(final String text) throws ParseException {
        if (isNullOrEmpty(text))
            return null;
        switch (text.charAt(0)) {
            case LeftBracketToken.VALUE:
            case LeftSquareBracketToken.VALUE:
                try (final DoubleRangeParser parser = new DoubleRangeParser(text)) {
                    return parser.parse();
                }
            default:
                return Range.singleton(Double.parseDouble(text));
        }
    }
}
