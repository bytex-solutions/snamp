package com.bytex.snamp.parser;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.function.LongSupplier;

/**
 * Represents integer as token.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@Immutable
public class IntegerToken extends Token implements LongSupplier {
    private static final long serialVersionUID = 5296717972150866303L;
    public static final int TYPE = 529671;

    protected IntegerToken(final int type, final CharSequence value){
        super(type, value);
    }

    IntegerToken(final CharReader reader) throws IOException {
        this(TYPE, parse(reader));
    }

    @Override
    public long getAsLong() {
        return Long.parseLong(toString());
    }

    public byte getAsByte(){
        return Byte.parseByte(toString());
    }

    static boolean isValidCharacter(final char ch){
        return Character.isDigit(ch);
    }

    private static CharSequence parse(final CharReader reader) throws IOException {
        final StringBuilder value = new StringBuilder();
        do {
            final char currentChar = reader.get();
            if (isValidCharacter(currentChar)) {
                value.append(currentChar);
                reader.skip();
            } else
                break;
        } while (reader.getRemaining() > 0);
        return value;
    }
}
