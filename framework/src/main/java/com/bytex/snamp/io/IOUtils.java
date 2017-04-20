package com.bytex.snamp.io;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.Convert;
import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.BitSet;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class IOUtils {
    /**
     * Represents charset used by default in SNAMP for string encoding/decoding.
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * Represents empty {@link Reader}.
     */
    public static final Reader EMPTY_READER = new Reader() {
        @Override
        public int read(@Nonnull final char[] cbuf, final int off, final int len) {
            return 0;
        }

        @Override
        public void close() {

        }
    };

    private IOUtils() {
        throw new InstantiationError();
    }

    public static void copy(final Reader input, final Writer output) throws IOException {
        final char[] buffer = new char[1024];
        int count;
        while ((count = input.read(buffer)) > 0)
            output.write(buffer, 0, count);
    }

    public static String toString(final InputStream stream, final Charset encoding) throws IOException {
        if (encoding == null) return toString(stream, Charset.defaultCharset());
        final StringBuilder result = new StringBuilder(1024);
        final char[] buffer = new char[128];
        try (final InputStreamReader reader = new InputStreamReader(stream, encoding)) {
            int count;
            while ((count = reader.read(buffer)) > 0)
                result.append(buffer, 0, count);
        }
        return result.toString();
    }

    public static String toString(final InputStream stream) throws IOException {
        return toString(stream, DEFAULT_CHARSET);
    }

    public static void writeString(final String value, final OutputStream output, final Charset encoding) throws IOException {
        if (Strings.isNullOrEmpty(value)) return;
        else if (encoding == null) writeString(value, output, Charset.defaultCharset());
        else output.write(value.getBytes(encoding));
    }

    public static void serialize(final Serializable obj, final OutputStream output) throws IOException {
        try (final ObjectOutputStream serializer = new ObjectOutputStream(output)) {
            serializer.writeObject(obj);
        }
    }

    public static byte[] serialize(final Serializable obj) throws IOException {
        try (final ByteArrayOutputStream os = new ByteArrayOutputStream(1024)) {
            serialize(obj, os);
            return os.toByteArray();
        }
    }

    public static <T extends Serializable> T deserialize(final InputStream serializedForm,
                                                         final TypeToken<T> expectedType,
                                                         final ClassResolver resolver) throws IOException {
        try (final ObjectInputStream deserializer = new CustomObjectInputStream(serializedForm, resolver)) {
            return Convert.toType(deserializer.readObject(), expectedType).orElseThrow(ClassCastException::new);
        } catch (final ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    public static <T extends Serializable> T deserialize(final byte[] serializedForm,
                                                         final TypeToken<T> expectedType,
                                                         final ClassResolver resolver) throws IOException {
        if (ArrayUtils.isNullOrEmpty(serializedForm))
            return null;
        else
            try (final ByteArrayInputStream stream = new ByteArrayInputStream(serializedForm)) {
                return deserialize(stream, expectedType, resolver);
            }
    }

    public static <T extends Serializable> T deserialize(final InputStream serializedForm,
                                                         final TypeToken<T> expectedType,
                                                         final ClassLoader customLoader) throws IOException {
        return deserialize(serializedForm, expectedType, ClassResolver.forClassLoader(customLoader));
    }

    public static <T extends Serializable> T deserialize(final byte[] serializedForm,
                                                         final TypeToken<T> expectedType,
                                                         final ClassLoader customLoader) throws IOException {
        return deserialize(serializedForm, expectedType, ClassResolver.forClassLoader(customLoader));
    }

    public static <T extends Serializable> T deserialize(final byte[] serializedForm,
                                                         final TypeToken<T> expectedType) throws IOException {
        return deserialize(serializedForm, expectedType, expectedType.getRawType().getClassLoader());
    }

    public static <T extends Serializable> T deserialize(final byte[] serializedForm,
                                                         final Class<T> expectedType) throws IOException {
        return deserialize(serializedForm, TypeToken.of(expectedType));
    }

    public static <T extends Serializable> T deserialize(final byte[] serializedForm,
                                                         final Class<T> expectedType,
                                                         final ClassLoader customLoader) throws IOException {
        return deserialize(serializedForm, TypeToken.of(expectedType), customLoader);
    }

    public static <T extends Serializable> T deserialize(final byte[] serializedForm,
                                                         final Class<T> expectedType,
                                                         final ClassResolver resolver) throws IOException {
        return deserialize(serializedForm, TypeToken.of(expectedType), resolver);
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
        while (timeout >= 0 && (is.available() == 0)) {
            final long PAUSE = 1L;
            Thread.sleep(PAUSE);
            timeout -= PAUSE;
        }
        return timeout > 0L;
    }

    public static boolean waitForData(final InputStream is,
                                      final Duration timeout) throws IOException, InterruptedException {
        return waitForData(is, timeout.toMillis());
    }

    public static String toString(final Reader reader) throws IOException {
        final StringBuilder result = new StringBuilder(512);
        final char[] buffer = new char[16];
        int count;
        while ((count = reader.read(buffer)) >= 0)
            result.append(buffer, 0, count);
        return result.toString();
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

    public static boolean contentAreEqual(final CharSequence sequence1, final CharSequence sequence2) {
        if (sequence1 == sequence2)
            return true;
        else if (sequence1 == null)
            return false;
        else if (sequence1.length() == sequence2.length()) {
            for (int i = 0; i < sequence1.length(); i++)
                if (sequence1.charAt(i) != sequence2.charAt(i))
                    return false;
            return true;
        } else
            return false;
    }
}
