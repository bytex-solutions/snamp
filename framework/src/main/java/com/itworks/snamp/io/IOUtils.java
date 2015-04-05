package com.itworks.snamp.io;

import com.google.common.reflect.TypeToken;
import com.itworks.snamp.TypeTokens;

import java.io.*;
import java.nio.charset.Charset;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class IOUtils {
    private IOUtils(){

    }

    public static String toString(final InputStream stream, final Charset encoding) throws IOException {
        if(encoding == null) return toString(stream, Charset.defaultCharset());
        final StringBuilder result = new StringBuilder(1024);
        try(final InputStreamReader reader = new InputStreamReader(stream, encoding)){
            final char[] buffer = new char[128];
            int count;
            while ((count = reader.read(buffer)) > 0)
                result.append(buffer, 0, count);
        }
        return result.toString();
    }

    public static void writeString(final String value, final OutputStream output, final Charset encoding) throws IOException {
        if (value == null || value.isEmpty()) return;
        else if (encoding == null) writeString(value, output, Charset.defaultCharset());
        else output.write(value.getBytes(encoding));
    }

    public static byte[] serialize(final Serializable obj) throws IOException {
        try (final ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
             final ObjectOutputStream serializer = new ObjectOutputStream(os)) {
            serializer.writeObject(obj);
            return os.toByteArray();
        }
    }

    public static <T extends Serializable> T deserialize(final byte[] serializedForm,
                                                         final TypeToken<T> expectedType) throws IOException{
        try (final ByteArrayInputStream stream = new ByteArrayInputStream(serializedForm);
             final ObjectInputStream deserializer = new ObjectInputStream(stream)) {
            return TypeTokens.cast(deserializer.readObject(), expectedType);
        }
        catch (final ClassNotFoundException | ClassCastException e){
            throw new IOException(e);
        }
    }

    public static <T extends Serializable> T deserialize(final byte[] serializedForm,
                                                         final Class<T> expectedType) throws IOException{
        return deserialize(serializedForm, TypeToken.of(expectedType));
    }
}
