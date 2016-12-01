package com.bytex.snamp.instrumentation;


import javax.xml.bind.DatatypeConverter;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Represents unique identifier.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class Identifier implements Serializable {
    private static final class SecureRandomHolder{
        private static final SecureRandom INSTANCE = new SecureRandom();
    }
    private static final WeakHashMap<String, Identifier> POOL = new WeakHashMap<String, Identifier>();

    private static final long serialVersionUID = -767298982099087481L;
    /**
     * Represents empty identifier.
     */
    public static final Identifier EMPTY = new Identifier("");

    private final String content;

    private Identifier(final String content){
        this.content = content;
    }

    private static SecureRandom getSecureRandom(){
        return SecureRandomHolder.INSTANCE;
    }

    /**
     * Generates a new unique identifier.
     * @return A new unique identifier.
     */
    public static Identifier randomID(){
        final byte[] randomBytes = new byte[16];
        getSecureRandom().nextBytes(randomBytes);
        return ofBytes(randomBytes);
    }

    /**
     * Wraps string identifier into {@link Identifier}.
     * @param value String representation of the identifier.
     * @return A new identifier.
     */
    public static Identifier ofString(final String value) {
        return value.isEmpty() ? EMPTY : new Identifier(value);
    }

    /**
     * Wraps {@link UUID}  into {@link Identifier}.
     * @param value Universally Unique Identifier to wrap.
     * @return A new identifier.
     */
    public static Identifier ofUUID(final UUID value){
        return ofString(value.toString());
    }

    public static Identifier ofBytes(final byte... values) {
        return values.length == 0 ? EMPTY : new Identifier(DatatypeConverter.printBase64Binary(values));
    }

    public static Identifier ofChars(final char... values){
        return ofString(new String(values));
    }

    public static Identifier ofLong(final long value){
        return ofString(Long.toString(value));
    }

    public static Identifier ofDouble(final double value){
        return ofString(Double.toString(value));
    }

    public static Identifier ofBitInteger(final BigInteger value){
        return ofString(value.toString());
    }

    /**
     * Computes hash code for this identifier.
     * @return Hash code of this identifier.
     */
    @Override
    public int hashCode() {
        return content.hashCode();
    }

    private boolean equals(final Identifier other){
        return content.equals(other.content);
    }

    @Override
    public boolean equals(final Object other) {
        return other == this || other instanceof Identifier && equals((Identifier) other);
    }

    @Override
    public String toString() {
        return content;
    }
}
