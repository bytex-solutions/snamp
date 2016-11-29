package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.Stateful;
import com.bytex.snamp.io.SerializableMap;
import com.google.common.collect.ForwardingMap;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.Objects;


abstract class ModifiableMap<K, V> extends ForwardingMap<K, V> implements Externalizable, Modifiable, Stateful, SerializableMap<K, V> {
    private static final long serialVersionUID = -8689048750446731607L;
    private transient boolean modified = false;

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public final V remove(final Object key) {
        modified = containsKey(key);
        return delegate().remove(key);
    }

    final void importFrom(final Map<? extends K, ? extends V> values){
        delegate().clear();
        delegate().putAll(values);
        modified = true;
    }

    @Override
    public final void clear() {
        modified = true;
        delegate().clear();
    }

    @Override
    public final V put(final K key, final V value) {
        modified = true;
        return delegate().put(key, value);
    }

    @Override
    public final void putAll(final Map<? extends K, ? extends V> map) {
        modified = true;
        delegate().putAll(map);
    }

    @Override
    public void reset() {
        modified = false;
    }

    protected abstract void writeKey(final K key, final ObjectOutput out) throws IOException;

    protected abstract void writeValue(final V value, final ObjectOutput out) throws IOException;

    protected abstract K readKey(final ObjectInput out) throws IOException;

    protected abstract V readValue(final ObjectInput out) throws IOException, ClassNotFoundException;

    @Override
    public final void writeExternal(final ObjectOutput out) throws IOException {
        out.writeInt(size());
        for (final Entry<K, V> entry : entrySet()) {
            writeKey(entry.getKey(), out);
            writeValue(entry.getValue(), out);
        }
    }

    @Override
    public final void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final int size = in.readInt();
        for (int i = 0; i < size; i++) {
            final K key = readKey(in);
            final V value = readValue(in);
            if (key != null && value != null)
                put(key, value);
        }
    }

    private boolean equals(final Map<?, ?> other) {
        if (this == other) return true;
        else if (size() == other.size()) {
            for (final Map.Entry<K, ?> entry1 : entrySet())
                if (!Objects.equals(entry1.getValue(), other.get(entry1.getKey()))) return false;
            return true;
        } else return false;
    }

    @Override
    public final boolean equals(final Object other) {
        return other instanceof Map<?, ?> && equals((Map<?, ?>)other);
    }

    @Override
    public final int hashCode() {
        return delegate().hashCode();
    }
}
