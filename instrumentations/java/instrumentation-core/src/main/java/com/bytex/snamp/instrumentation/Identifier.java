package com.bytex.snamp.instrumentation;


import javax.xml.bind.DatatypeConverter;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.UUID;
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
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final long serialVersionUID = -767298982099087481L;
    /**
     * Represents empty identifier.
     */
    public static final Identifier EMPTY = new Identifier(new byte[0]);

    private final byte[] content;

    private Identifier(final byte[] content){
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
        return value.isEmpty() ? EMPTY : ofBytes(value.getBytes(UTF_8));
    }

    /**
     * Constructs identifier using BASE-64 (RFC-4648) encoded string.
     * @param base64 BASE-64 encoded string.
     * @return A new identifier.
     */
    public static Identifier ofBase64(final String base64){
        return ofBytes(DatatypeConverter.parseBase64Binary(base64));
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
        return values.length == 0 ? EMPTY : new Identifier(values);
    }

    public static Identifier ofChars(final char... values){
        return ofString(new String(values));
    }

    public static Identifier ofLong(long value){
        final byte[] result = new byte[8];
        for (int i = result.length - 1; i >= 0; i--) {
            result[i] = (byte)(value & 0xFF);
            value >>= 8;
        }

        return ofBytes(result);
    }

    public static Identifier ofDouble(final double value){
        return ofLong(Double.doubleToRawLongBits(value));
    }

    public static Identifier ofBitInteger(final BigInteger value){
        return new Identifier(value.toByteArray());
    }

    /**
     * Computes hash code for this identifier.
     * @return Hash code of this identifier.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(content);
    }

    private boolean equals(final Identifier other){
        return Arrays.equals(content, other.content);
    }

    @Override
    public boolean equals(final Object other) {
        return other == this || other instanceof Identifier && equals((Identifier) other);
    }

    public String toBase64(){
        return DatatypeConverter.printBase64Binary(content);
    }

    @Override
    public String toString() {
        return content.length == 0 ? "" : new String(content, UTF_8);
    }
}
