package com.itworks.snamp.mapping;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class RecordReaderUtils {
    private RecordReaderUtils() {

    }

    @SuppressWarnings("unchecked")
    static <E extends Exception> Class<E> getExceptionType(final Class<? extends RecordReader> readerType) {
        for (final Type iface : readerType.getGenericInterfaces())
            if (iface instanceof ParameterizedType) {
                final ParameterizedType parameterized = (ParameterizedType) iface;
                if (Objects.equals(parameterized.getRawType(), RecordReader.class)) {
                    final Type[] actualArgs = parameterized.getActualTypeArguments();
                    final Type exception = actualArgs.length == 3 ? actualArgs[2] : null;
                    return exception instanceof Class<?> ? (Class<E>) exception : null;
                }
            }
        return null;
    }

    static <E extends Exception> void checkAndThrow(final RecordReader<?, ?, E> reader,
                                                    final Throwable candidate) throws E {
        if (candidate instanceof RuntimeException)
            throw (RuntimeException) candidate;
        else if (candidate instanceof Error)
            throw (Error) candidate;
        else {
            final Class<E> exceptionType = getExceptionType(reader.getClass());
            if (exceptionType != null && exceptionType.isInstance(candidate))
                throw exceptionType.cast(candidate);
        }
    }
}