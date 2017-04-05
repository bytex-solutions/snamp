package com.bytex.snamp.management.shell;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents parameter/value pair.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class StringKeyValue implements Map.Entry<String, String>, Serializable {
    private static final long serialVersionUID = 7340706762238782487L;

    private final String key;
    private final String value;

    private StringKeyValue(final String key, String value){
        this.key = Objects.requireNonNull(key);
        this.value = Objects.requireNonNull(value);
    }

    static StringKeyValue parse(final String pair){
        if(isNullOrEmpty(pair)) return null;
        final int index = pair.indexOf('=');
        return index > 0 ?
                new StringKeyValue(pair.substring(0, index), pair.substring(index + 1)):
                null;
    }

    static Map<String, String> parse(final String[] parameters) {
        final Map<String, String> result = new HashMap<>();
        for (final String param : parameters) {
            final StringKeyValue pair = parse(param);
            if (pair != null)
                result.put(pair.getKey(), pair.getValue());
        }
        return result;
    }

    @Override
    public String getKey() {
        return key;
    }
    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String setValue(final String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    private boolean equals(final StringKeyValue other){
        return Objects.equals(key, other.key) && Objects.equals(value, other.value);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof StringKeyValue && equals((StringKeyValue) other);
    }

    @Override
    public String toString() {
        return key + '=' + value;
    }
}
