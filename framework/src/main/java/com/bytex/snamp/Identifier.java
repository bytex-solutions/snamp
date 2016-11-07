package com.bytex.snamp;

import com.bytex.snamp.concurrent.LazySoftReference;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import static com.bytex.snamp.ArrayUtils.toByteArray;

/**
 * Represents unique identifier.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class Identifier implements Serializable {
    private static final long serialVersionUID = -767298982099087481L;
    private static final LazySoftReference<SecureRandom> SECURE_RANDOM = new LazySoftReference<>();
    private final byte[] content;

    private Identifier(byte[] content){
        this.content = Objects.requireNonNull(content);
    }

    private static SecureRandom getSecureRandom(){
        return SECURE_RANDOM.lazyGet((Supplier<SecureRandom>) SecureRandom::new);
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
    public static Identifier ofString(final String value){
        return ofBytes(value.getBytes());
    }

    /**
     * Constructs identifier using BASE-64 (RFC-4648) encoded string.
     * @param base64 BASE-64 encoded string.
     * @return A new identifier.
     */
    public static Identifier ofBase64(final String base64){
        return ofBytes(Base64.getDecoder().decode(base64));
    }

    /**
     * Wraps {@link UUID}  into {@link Identifier}.
     * @param value Universally Unique Identifier to wrap.
     * @return A new identifier.
     */
    public static Identifier ofUUID(final UUID value){
        return ofString(value.toString());
    }


    public static Identifier ofBytes(final byte... values){
        return new Identifier(values);
    }

    public static Identifier ofChars(final char... values){
        return ofBytes(toByteArray(values));
    }

    public static Identifier ofShorts(final short... values){
        return ofBytes(toByteArray(values));
    }

    public static Identifier ofInts(final int... values){
        return ofBytes(toByteArray(values));
    }

    public static Identifier ofLongs(final long... values){
        return ofBytes(toByteArray(values));
    }

    public static Identifier ofFloats(final float... values){
        return ofBytes(toByteArray(values));
    }

    public static Identifier ofDoubles(final double... values){
        return ofBytes(toByteArray(values));
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

    @Override
    public String toString() {
        return Base64.getEncoder().encodeToString(content);
    }
}
