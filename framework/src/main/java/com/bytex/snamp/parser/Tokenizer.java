package com.bytex.snamp.parser;

import com.bytex.snamp.SafeCloseable;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Represents token parser that supports basic set of tokens.
 * @since 2.0
 * @version 2.0
 * @see NameToken
 * @see IntegerToken
 * @see CommaToken
 * @see DollarToken
 * @see SlashToken
 * @see RightBracketToken
 * @see LeftBracketToken
 */
public class Tokenizer implements SafeCloseable {
    /**
     * Represents parser for a token of the specified type.
     */
    @FunctionalInterface
    protected interface TokenParser{
        Token parseToken(final CharReader reader) throws IOException, ParseException;
    }

    private final CharReader reader;
    private final CharSequence source;

    public Tokenizer(final CharSequence sequence){
        reader = new CharReader(this.source = sequence);
    }

    protected final CharSequence getSource(){
        return source;
    }

    protected PunctuationToken getPunctuationToken(final char ch){
        switch (ch){
            case CommaToken.VALUE:
                return CommaToken.INSTANCE;
            case SlashToken.VALUE:
                return SlashToken.INSTANCE;
            case DollarToken.VALUE:
                return DollarToken.INSTANCE;
            case RightBracketToken.VALUE:
                return RightBracketToken.INSTANCE;
            case LeftBracketToken.VALUE:
                return LeftBracketToken.INSTANCE;
            case ColonToken.VALUE:
                return ColonToken.INSTANCE;
            default: return null;
        }
    }

    protected TokenParser getTokenParser(final char ch) {
        if (NameToken.isValidCharacter(ch))
            return NameToken::new;
        else if (IntegerToken.isValidCharacter(ch))
            return IntegerToken::new;
        final PunctuationToken punct = getPunctuationToken(ch);
        if (punct != null)
            return reader -> {
                final boolean skipped = reader.skip();
                assert skipped;
                return punct;
            };
        return null;
    }

    protected boolean ignore(final char ch){
        return Character.isWhitespace(ch);
    }

    public final Token nextToken() throws NoSuchElementException, ParseException {
        try {
            while (reader.getRemaining() > 0) {
                final char ch = reader.get();
                //skip ignored chars
                if (ignore(ch)) {
                    reader.skip();
                    continue;
                }
                //parse regular token
                final TokenParser parser = getTokenParser(ch);
                if (parser == null)
                    throw new UnexpectedCharException(ch);
                else
                    return parser.parseToken(reader);
            }
            throw new ParseException(); //EOS reached
        } catch (final IOException e) {
            throw new ParseException(e);
        }
    }

    private static Predicate<Token> predicateFor(final Class<? extends Token> tokenType){
        return tokenType::isInstance;
    }

    private static Token expectToken(final Token token, final Predicate<? super Token> expectedToken) throws ParseException {
        if (token == null)
            throw new ParseException();
        else if (!expectedToken.test(token))
            throw new UnexpectedTokenException(token);
        else
            return token;
    }

    public final Token skip(final Predicate<? super Token> skipRule) throws ParseException {
        Token token;
        do {
            token = nextToken();
        } while (skipRule.test(token));
        return token;
    }

    public final Token skip(final Class<? extends Token> tokenToSkip) throws ParseException{
        return skip(predicateFor(tokenToSkip));
    }

    public final Token nextToken(final Predicate<? super Token> expectedToken) throws ParseException {
        return expectToken(nextToken(), expectedToken);
    }

    public final <T extends Token> T nextToken(final Class<T> expectedToken) throws ParseException {
        return expectedToken.cast(nextToken(predicateFor(expectedToken)));
    }

    /**
     * Read all characters from the current position to the end of underlying stream.
     * @return All characters from the current position to the end of underlying stream.
     * @throws ParseException Stream is closed.
     */
    public CharSequence readToEnd() throws ParseException {
        try {
            return reader.readToEnd();
        } catch (final IOException e) {
            throw new ParseException(e);
        }
    }

    public CharSequence readTo(final char stopChar) throws ParseException {
        try {
            return reader.readTo(stopChar);
        } catch (final IOException e) {
            throw new ParseException(e);
        }
    }

    @Override
    public final void close() {
        reader.close();
    }
}
