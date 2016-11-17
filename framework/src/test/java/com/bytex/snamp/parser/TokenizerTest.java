package com.bytex.snamp.parser;

import com.bytex.snamp.connector.metrics.RangedGauge64;
import org.junit.Assert;
import org.junit.Test;

/**
 * Represents tests for {@link Tokenizer}.
 */
public final class TokenizerTest extends Assert {
    @Test
    public void tokenizationTest() throws ParseException {
        try(final Tokenizer tokenizer = new Tokenizer("($$123a_bc")){
            assertNotNull(tokenizer.nextToken(LeftBracketToken.class));
            assertNotNull(tokenizer.nextToken(DollarToken.class));
            assertNotNull(tokenizer.nextToken(DollarToken.class));
            assertEquals(123, tokenizer.nextToken(IntegerToken.class).getAsLong());
            assertEquals("a_bc", tokenizer.nextToken(NameToken.class).toString());
        }
    }

    @Test(expected = ParseException.class)
    public void eosTest() throws ParseException {
        try(final Tokenizer tokenizer = new Tokenizer("($$")){
            assertNotNull(tokenizer.nextToken(LeftBracketToken.class));
            assertNotNull(tokenizer.nextToken(DollarToken.class));
            assertNotNull(tokenizer.nextToken(DollarToken.class));
            assertEquals(123, tokenizer.nextToken(IntegerToken.class).getAsLong());
        }
    }

    @Test
    public void skipTest() throws ParseException{
        try(final Tokenizer tokenizer = new Tokenizer("\t  123 abc de")){
            assertEquals(123, tokenizer.nextToken(IntegerToken.class).getAsLong());
            assertEquals("abc", tokenizer.nextToken(NameToken.class).toString());
            assertEquals("de", tokenizer.nextToken(NameToken.class).toString());
        }
    }

    @Test
    public void readToEndTest() throws ParseException {
        try(final Tokenizer tokenizer = new Tokenizer("js:a+b")){
            assertEquals("js", tokenizer.nextToken(NameToken.class).toString());
            assertEquals(":a+b", tokenizer.readToEnd());
        }
    }

    @Test
    public void readToCharTest() throws ParseException {
        try(final Tokenizer tokenizer = new Tokenizer("js:a,b")){
            assertEquals("js:a", tokenizer.readTo(','));
            assertEquals(CommaToken.INSTANCE, tokenizer.nextToken());
            assertEquals("b", tokenizer.nextToken(NameToken.class).toString());
        }
    }

    @Test
    public void nameTokenTest() throws ParseException{
        try(final Tokenizer tokenizer = new Tokenizer("abc123 123")){
            assertEquals("abc123", tokenizer.nextToken(NameToken.class).toString());
            assertEquals(123, tokenizer.nextToken(IntegerToken.class).getAsLong());
        }
    }
}
