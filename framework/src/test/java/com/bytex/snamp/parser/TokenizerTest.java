package com.bytex.snamp.parser;

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
}
