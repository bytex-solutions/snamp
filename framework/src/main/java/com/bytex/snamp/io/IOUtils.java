package com.bytex.snamp.io;

import com.google.common.base.Splitter;
import com.google.common.base.StandardSystemProperty;
import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.TypeTokens;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class IOUtils {
    /**
     * Represents charset used by default in SNAMP for string encoding/decoding.
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final Splitter PATH_SPLITTER;

    static {
        final String pathSeparator = StandardSystemProperty.PATH_SEPARATOR.value();
        PATH_SPLITTER = Splitter.on(Strings.isNullOrEmpty(pathSeparator) ? ":" : pathSeparator);
    }

    private IOUtils() {

    }

    public static String toString(final InputStream stream, final Charset encoding) throws IOException {
        if (encoding == null) return toString(stream, Charset.defaultCharset());
        final StringBuilder result = new StringBuilder(1024);
        try (final InputStreamReader reader = new InputStreamReader(stream, encoding)) {
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
                                                         final TypeToken<T> expectedType) throws IOException {
        try (final ByteArrayInputStream stream = new ByteArrayInputStream(serializedForm);
             final ObjectInputStream deserializer = new ObjectInputStream(stream)) {
            return TypeTokens.cast(deserializer.readObject(), expectedType);
        } catch (final ClassNotFoundException | ClassCastException e) {
            throw new IOException(e);
        }
    }

    public static <T extends Serializable> T deserialize(final byte[] serializedForm,
                                                         final Class<T> expectedType) throws IOException {
        return deserialize(serializedForm, TypeToken.of(expectedType));
    }

    public static byte[] readFully(final InputStream inputStream) throws IOException {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream(1024)) {
            final byte[] buffer = new byte[512];
            int count;
            while ((count = inputStream.read(buffer)) > 0)
                out.write(buffer, 0, count);
            return out.toByteArray();
        }
    }

    public static boolean hasMoreData(final InputStream is) {
        try {
            return is.available() > 0;
        } catch (final IOException ignored) {
            return false;
        }
    }

    public static boolean waitForData(final InputStream is,
                                      long timeout) throws IOException, InterruptedException {
        while ((is.available() == 0) && timeout >= 0) {
            final long PAUSE = 1L;
            Thread.sleep(PAUSE);
            timeout -= PAUSE;
        }
        return timeout > 0L;
    }

    public static boolean waitForData(final InputStream is,
                                      final TimeSpan timeout) throws IOException, InterruptedException {
        return waitForData(is, timeout.toMillis());
    }

    public static String toString(final Reader reader) throws IOException {
        final StringBuilder result = new StringBuilder();
        while (reader.ready()) {
            final char[] buffer = new char[10];
            final int count = reader.read(buffer);
            result.append(buffer, 0, count);
        }
        return result.toString();
    }

    public static String[] splitPath(final String connectionString) {
        return ArrayUtils.toArray(PATH_SPLITTER.trimResults().splitToList(connectionString),
                String.class);
    }

    public static BitSet toBitSet(final boolean[] bits) {
        final BitSet result = new BitSet(bits.length);
        for(int position = 0; position < bits.length; position++)
            result.set(position, bits[position]);
        return result;
    }

    public static boolean[] fromBitSet(final BitSet bits){
        final boolean[] result = new boolean[bits.length()];
        for(int position = 0; position < bits.length(); position++)
            result[position] = bits.get(position);
        return result;
    }
}