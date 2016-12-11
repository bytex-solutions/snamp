package com.bytex.snamp.core;

import java.time.Instant;
import java.util.function.Supplier;

/**
 * Represents persistent storage for documents.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface KeyValueStorage {
    interface Key{
        @Override
        boolean equals(final Object otherKey);
        @Override
        int hashCode();
    }

    interface Value{
        String getAsJson();
        void setAsJson(final String value);

        long getAsLong();
        void setAsLong(final long value);

        double getAsDouble();
        void setAsDouble(final double value);

        String getAsString();
        void setAsString(final String value);

        void save();

        void reload();
    }

    interface KeyBuilder extends Supplier<Key>{
        KeyBuilder set(final String name, final boolean value);
        KeyBuilder set(final String name, final long value);
        KeyBuilder set(final String name, final double value);
        KeyBuilder set(final String name, final String value);
    }

    KeyBuilder keyBuilder();

    Object get(final long key);

    Object get(final double key);

    Object get(final String key);

    Object get(final Instant key);
}
