package com.bytex.snamp.parser;

import com.bytex.snamp.SafeCloseable;

import javax.annotation.concurrent.NotThreadSafe;
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
 * @see ColonToken
 * @see SemicolonToken
 * @see DotToken
 */
@NotThreadSafe
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
        switch (ch) {
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
            case SemicolonToken.VALUE:
                return SemicolonToken.INSTANCE;
            case DotToken.VALUE:
                return DotToken.INSTANCE;
            case GTToken.VALUE:
                return GTToken.INSTANCE;
            case LTToken.VALUE:
                return LTToken.INSTANCE;
            case ExclamationToken.VALUE:
                return ExclamationToken.INSTANCE;
            case EQToken.VALUE:
                return EQToken.INSTANCE;
            case PlusToken.VALUE:
                return PlusToken.INSTANCE;
            case MinusToken.VALUE:
                return MinusToken.INSTANCE;
            default:
                return null;
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
            final char ch = skipIgnoredCharsImpl();
            if (reader.getRemaining() > 0) {
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

    private static Predicate<Token> asPredicate(final Class<? extends Token> tokenType){
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

    private char skipIgnoredCharsImpl() throws IOException {
        char ch = '\0';
        while (reader.getRemaining() > 0) {
            ch = reader.get();
            //skip ignored chars
            if (ignore(ch))
                reader.skip();
            else
                break;
        }
        return ch;
    }

    protected final void skipIgnoredChars() throws ParseException {
        try {
            skipIgnoredCharsImpl();
        } catch (final IOException e) {
            throw new ParseException(e);
        }
    }

    public final Token skip(final Predicate<? super Token> skipRule) throws ParseException {
        Token token;
        do {
            token = nextToken();
        } while (skipRule.test(token));
        return token;
    }

    public final Token skip(final Class<? extends Token> tokenToSkip) throws ParseException{
        return skip(asPredicate(tokenToSkip));
    }

    public final Token nextToken(final Predicate<? super Token> expectedToken) throws ParseException {
        return expectToken(nextToken(), expectedToken);
    }

    public final <T extends Token> T nextToken(final Class<T> expectedToken) throws ParseException {
        return expectedToken.cast(nextToken(asPredicate(expectedToken)));
    }

    /**
     * Read all characters from the current position to the end of underlying stream.
     * @return All characters from the current position to the end of underlying stream.
     * @throws ParseException Stream is closed.
     */
    protected final CharSequence readToEnd() throws ParseException {
        try {
            return reader.readToEnd();
        } catch (final IOException e) {
            throw new ParseException(e);
        }
    }

    protected final CharSequence readTo(final char stopChar) throws ParseException {
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
