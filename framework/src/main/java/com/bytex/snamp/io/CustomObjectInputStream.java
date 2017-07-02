package com.bytex.snamp.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class CustomObjectInputStream extends ObjectInputStream {
    private final ClassResolver resolver;

    CustomObjectInputStream(final InputStream in, final ClassResolver resolver) throws IOException {
        super(in);
        this.resolver = Objects.requireNonNull(resolver);
    }

    @Override
    protected Class<?> resolveClass(final ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        try {
            return resolver.resolveClass(desc);
        } catch (final ClassNotFoundException e) {
            return super.resolveClass(desc);
        }
    }
}
