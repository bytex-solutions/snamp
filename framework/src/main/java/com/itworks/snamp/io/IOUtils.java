package com.itworks.snamp.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
}
