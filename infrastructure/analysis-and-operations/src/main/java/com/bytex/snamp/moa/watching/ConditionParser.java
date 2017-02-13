package com.bytex.snamp.moa.watching;

import com.bytex.snamp.parser.*;

/**
 * Represents condition parser.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ConditionParser extends Tokenizer {
    private ConditionParser(final CharSequence sequence) {
        super(sequence);
    }

    private Condition parseFunction(final String functionName){
        return null;
    }

    private Condition parse() throws ParseException {
        final Token token = nextToken();
        if (token instanceof NameToken)
            return parseFunction(token.toString());
        else
            throw new UnexpectedTokenException(token);
    }

    public static Condition parse(final String expression) throws ParseException {
        try (final ConditionParser parser = new ConditionParser(expression)) {
            return parser.parse();
        }
    }
}
