package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SerializableMap;
import com.google.common.collect.ForwardingMap;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;


abstract class ModifiableMap<K, V> extends ForwardingMap<K, V> implements Externalizable, Modifiable, SerializableMap<K, V> {
    private static final long serialVersionUID = -8689048750446731607L;
    private transient boolean modified = false;

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public final V remove(final Object key) {
        modified = containsKey(key);
        return super.remove(key);
    }

    @Override
    public final void clear() {
        modified = true;
        super.clear();
    }

    @Override
    public final V put(final K key, final V value) {
        modified = true;
        return super.put(key, value);
    }

    @Override
    public final void putAll(final Map<? extends K, ? extends V> map) {
        modified = true;
        super.putAll(map);
    }

    void reset() {
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
}
