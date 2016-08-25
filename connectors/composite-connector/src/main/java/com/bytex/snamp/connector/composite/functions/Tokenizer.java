package com.bytex.snamp.connector.composite.functions;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents tokenizer of formula expression.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class Tokenizer implements TokenPosition {
    private final CharSequence input;
    private int position;

    Tokenizer(final CharSequence input){
        this.input = Objects.requireNonNull(input);
        this.position = 0;
    }

    @Override
    public int get() {
        return position;
    }

    @Override
    public int inc() {
        return ++position;
    }

    Token nextToken() throws FunctionParserException {
        if(position >= input.length())
            throw new NoSuchElementException();
        final char ch = input.charAt(position);
        if(NameToken.isValidCharacter(ch))
            return NameToken.parse(input, this);
        else if(IntegerToken.isValidCharacter(ch))
            return IntegerToken.parse(input, this);
        else
            switch (ch){
                case LeftBracketToken.VALUE:
                    inc();
                    return LeftBracketToken.INSTANCE;
                case RightBracketToken.VALUE:
                    inc();
                    return RightBracketToken.INSTANCE;
                case CommaToken.VALUE:
                    inc();
                    return CommaToken.INSTANCE;
                case DollarToken.VALUE:
                    inc();
                    return DollarToken.INSTANCE;
                //omit all whitespace characters
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                    inc();
                    return nextToken();
            }
        //no valid input token
        throw new FunctionParserException(ch, position);
    }

    Token nextToken(final Predicate<? super Token> expectedToken) throws FunctionParserException {
        final Token token = nextToken();
        if(token == null)
            throw new FunctionParserException();
        else if(!expectedToken.test(token))
                throw new FunctionParserException(token, position);
        else
            return token;
    }

    <T extends Token> T nextToken(final Class<T> expectedToken) throws FunctionParserException {
        return expectedToken.cast(nextToken(expectedToken::isInstance));
    }
}
