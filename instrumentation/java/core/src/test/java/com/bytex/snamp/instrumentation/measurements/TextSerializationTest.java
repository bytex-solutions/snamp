package com.bytex.snamp.instrumentation.measurements;

import com.bytex.snamp.instrumentation.Identifier;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Provides serialization of {@link Identifier}.
 */
public final class TextSerializationTest extends Assert {
    @Test
    public void readerDeserializationTest() throws IOException {
        final String ID = "kwhflhf3gfh3ugfwiugwuigfwegkekjfjkegfgwjkgfkwjhfjkg2gfugugggjkhgjkjejgejgejgjegh3ug3gu3hg3ugh3ugu3gjkskhwkjg;lwl;kq;ld2elej2l;j;ljf2krjkl2hrlk2hrj2hr";
        try (final Reader reader = new StringReader(ID)) {
            final Identifier id = Identifier.deserialize(reader);
            assertEquals(ID, id.toString());
        }
    }

    @Test
    public void serializationTest(){
        final String ID = "kwhflhf3gfh3ugfwiugwuigfwegkekjfjkegfgwjkgfkwjhfjkg2gfugugggjkhgjkjejgejgejgjegh3ug3gu3hg3ugh3ugu3gjkskhwkjg;lwl;kq;ld2elej2l;j;ljf2krjkl2hrlk2hrj2hr";
        final StringBuilder builder = new StringBuilder();
        builder.append(ID);
        final Identifier id = Identifier.deserialize(builder);
        assertEquals(ID, id.toString());
    }
}
