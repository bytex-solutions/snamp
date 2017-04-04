package com.bytex.snamp.instrumentation;


import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.UUID;

/**
 * Represents unique identifier.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonSerialize(using = IdentifierSerializer.class)
@JsonDeserialize(using = IdentifierDeserializer.class)
public final class Identifier implements Serializable {
    private static final class SecureRandomHolder{
        private static final SecureRandom INSTANCE = new SecureRandom();

        static long nextLong(){
            return INSTANCE.nextLong();
        }

        static int nextInt(){
            return INSTANCE.nextInt();
        }

        static void nextBytes(final byte[] bytes) {
            INSTANCE.nextBytes(bytes);
        }
    }

    private static final long serialVersionUID = -767298982099087481L;

    /**
     * Represents empty identifier.
     */
    public static final Identifier EMPTY = new Identifier();

    private final char[] content;

    private Identifier(final char[] content, final boolean cloneNeeded){
        this.content = cloneNeeded ? content.clone() : content;
    }

    private Identifier(){
        this(new char[0], false);
    }

    /**
     * Generates a new unique identifier.
     * @return A new unique identifier.
     */
    public static Identifier randomID() {
        return randomID(16);
    }

    /**
     * Generates a new unique identifier.
     * @param bytes Number of random bytes used to create identifier.
     * @return A new unique identifier.
     */
    public static Identifier randomID(final int bytes) {
        switch (bytes){
            case 4:
                return ofLong(SecureRandomHolder.nextInt());
            case 8:
                return ofLong(SecureRandomHolder.nextLong());
            case 1:
                byte[] randomBytes = new byte[1];
                SecureRandomHolder.nextBytes(randomBytes);
                return ofString(Byte.toString(randomBytes[0]));
            default:
                randomBytes = new byte[bytes];
                SecureRandomHolder.nextBytes(randomBytes);
                return ofBytes(randomBytes);
        }
    }

    /**
     * Wraps string identifier into {@link Identifier}.
     * @param value String representation of the identifier.
     * @return A new identifier.
     */
    public static Identifier ofString(final String value) {
        return value.isEmpty() ? EMPTY : new Identifier(value.toCharArray(), false);
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
        return values.length == 0 ? EMPTY : ofString(DatatypeConverter.printBase64Binary(values));
    }

    public static Identifier ofChars(final char... values){
        return values.length == 0 ? EMPTY : new Identifier(values, true);
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

    public static Identifier deserialize(final ObjectInput input) throws IOException {
        final int size = input.readInt();
        if(size == 0) return EMPTY;
        final char[] content = new char[size];
        for(int i = 0; i < size; i++)
            content[i] = input.readChar();
        return new Identifier(content, false);
    }

    public static Identifier deserialize(final JsonParser input) throws IOException {
        return ofString(input.getText());
    }

    public static Identifier deserialize(final Reader input) throws IOException{
        final char[] buffer = new char[128];
        char[] content = new char[0];
        int count;
        while ((count = input.read(buffer)) > 0)
            if(content.length == 0)
                content = Arrays.copyOf(buffer, count);
            else {
                char[] newContent = new char[content.length + count];
                System.arraycopy(content, 0, newContent, 0, content.length);
                System.arraycopy(buffer, 0, newContent, content.length, count);
                content = newContent;
            }
        return content.length == 0 ? EMPTY : new Identifier(content, false);
    }

    public static Identifier deserialize(final StringBuilder input){
        if(input.length() == 0) return EMPTY;
        final char[] content = new char[input.length()];
        input.getChars(0, content.length, content, 0);
        return new Identifier(content, false);
    }

    public void serialize(final ObjectOutput output) throws IOException {
        output.writeInt(content.length);
        for(final char ch: content)
            output.writeChar(ch);
    }

    public void serialize(final JsonGenerator output) throws IOException {
        output.writeString(toString());
    }

    public void serialize(final Writer output) throws IOException {
        output.write(content);
    }

    public void serialize(final StringBuilder output){
        output.append(content);
    }

    /**
     * Determines whether this identifier is empty.
     * @return {@literal true}, if this identifier is empty.
     */
    public boolean isEmpty(){
        return content.length == 0;
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
        return other instanceof Identifier && equals((Identifier) other);
    }

    @Override
    public String toString() {
        return new String(content);
    }
}
