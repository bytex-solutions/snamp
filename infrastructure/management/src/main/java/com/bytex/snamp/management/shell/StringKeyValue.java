package com.bytex.snamp.management.shell;

import com.google.common.base.Strings;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Represents parameter/value pair.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class StringKeyValue implements Map.Entry<String, String>, Serializable {
    private static final long serialVersionUID = 7340706762238782487L;

    private final String key;
    private final String value;

    StringKeyValue(final String key, String value){
        this.key = Objects.requireNonNull(key);
        this.value = Objects.requireNonNull(value);
    }

    static StringKeyValue parse(final String pair){
        if(Strings.isNullOrEmpty(pair)) return null;
        final int index = pair.indexOf('=');
        return index > 0 ?
                new StringKeyValue(pair.substring(0, index), pair.substring(index + 1)):
                null;
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
    public String toString() {
        return key + '=' + value;
    }
}
